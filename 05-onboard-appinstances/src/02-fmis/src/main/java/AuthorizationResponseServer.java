import com.dke.data.agrirouter.api.dto.registrationrequest.secured.AuthorizationResponse;
import com.dke.data.agrirouter.api.dto.registrationrequest.secured.AuthorizationResponseToken;
import com.dke.data.agrirouter.api.env.Environment;
import com.dke.data.agrirouter.api.service.onboard.secured.AuthorizationRequestService;
import com.dke.data.agrirouter.impl.onboard.secured.AuthorizationRequestServiceImpl;
import com.google.protobuf.ByteString;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.util.StringTokenizer;

// Thanks to SSaurel's Blog  for the base of this our authorization server:
// https://www.ssaurel.com/blog/create-a-simple-http-web-server-in-java
// Each Client Connection will be managed in a dedicated Thread
public class AuthorizationResponseServer implements Runnable{

    static String resultStringForBrowser= "Welcome to our Java Authorization Result server \n" +
            "AuthorizationResult: {authoResult}";
    // port to listen connection
    static final int PORT = 8080;

    // verbose mode
    static final boolean verbose = true;

    // Client Connection via Socket Class
    private Socket connect;
    private String  responseGetQueue;
    private AuthorizationResponseToken authorizationResponseToken;
    private Environment environment;
    private AuthorizationRequestService authorizationRequestService;

    public AuthorizationResponseServer(Socket c, Environment environment) {
        connect = c;
        this.environment = environment;
        this.authorizationRequestService = new AuthorizationRequestServiceImpl(environment);
    }

    @Override
    public void run() {
        // we manage our particular client connection
        BufferedReader in = null;
        PrintWriter out = null;
        BufferedOutputStream dataOut = null;

        try {
            // we read characters from the client via input stream on the socket
            in = new BufferedReader(new InputStreamReader(connect.getInputStream()));
            // we get character output stream to client (for headers)
            out = new PrintWriter(connect.getOutputStream());
            // get binary output stream to client (for requested data)
            dataOut = new BufferedOutputStream(connect.getOutputStream());

            // get first line of the request from the client
            String input = in.readLine();
            // we parse the request with a string tokenizer
            StringTokenizer parse = new StringTokenizer(input);
            String method = parse.nextToken().toUpperCase(); // we get the HTTP method of the client

            // we support only GET and HEAD methods, we check
            if (method.equals("GET")) {
                String subURL = parse.nextToken("?");
                String getQueue = parse.nextToken("?");
                //this.authorizationCallResult = getQueue;
                this.responseGetQueue = getQueue;
                byte[] outputData = ByteString.copyFrom(
                        resultStringForBrowser.replace("{authoResult}", getQueue),
                        "utf-8")
                        .toByteArray();

                int fileLength = outputData.length;

                // send HTTP Headers
                out.println("HTTP/1.1 200 OK");
                out.println("Server: Java HTTP Server by DKEData : 1.0");
                out.println("Date: " + new Date());
                out.println("Content-type: " + getContentType());
                out.println("Content-length: " + fileLength);
                out.println(); // blank line between headers and content, very important !
                out.flush(); // flush character output stream buffer

                dataOut.write(outputData, 0, fileLength);
                dataOut.flush();
            }

        } catch (Exception e) {
            System.out.println("Oh, something went wrong here!");


        } finally {
            try {
                //Even though we don't really do error handling, we should at least do this, so that
                // the port is freed and we can restart our application
                in.close();
                out.close();
                dataOut.close();
                connect.close(); // we close socket connection

            } catch (Exception e) {
                System.err.println("Error closing stream : " + e.getMessage());
            }

            if (verbose) {
                System.out.println("Connection closed.\n");
            }
        }


    }

    // return supported MIME Type; We currently simply return some text
    private String getContentType() {
            return "text/plain";
    }

    private String getResultGetQueue() {
        return responseGetQueue;
    }

    public AuthorizationResponseToken getAuthorizationResponseToken(){
        return this.authorizationResponseToken;

    }

    public static AuthorizationResponse createAuthorizationServerAndAwaitToken(Environment environment) {
        AuthorizationResponse response = null;
        try {
            ServerSocket serverConnect = new ServerSocket(PORT);
            System.out.println("Server started.\nListening for connections on port : " + PORT + " ...\n");

            // we listen until user halts server execution
            while (response == null) {
                AuthorizationResponseServer myServer = new AuthorizationResponseServer(serverConnect.accept(),environment);

                if (verbose) {
                    System.out.println("Connection opened. (" + new Date() + ")");
                }

                // create dedicated thread to manage the client connection
                myServer.run();
                String getQueue = myServer.getResultGetQueue();
                if(!getQueue.equals("")){
                    response = myServer.authorizationRequestService.extractAuthorizationResponseFromQuery(getQueue);
                    myServer.authorizationResponseToken = myServer.authorizationRequestService.decodeToken(response.getToken());
                }

            }

        } catch (IOException e) {
            System.err.println("Server Connection error : " + e.getMessage());
        }
        return response;
    }


}
