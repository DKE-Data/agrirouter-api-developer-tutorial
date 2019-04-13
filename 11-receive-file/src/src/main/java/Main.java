import agrirouter.feed.response.FeedResponse;
import agrirouter.request.Request;
import agrirouter.request.payload.account.Endpoints;
import agrirouter.response.Response;
import agrirouter.response.payload.account.Endpoints.ListEndpointsResponse;
import com.dke.data.agrirouter.api.dto.encoding.DecodeMessageResponse;
import com.dke.data.agrirouter.api.dto.encoding.EncodeMessageResponse;
import com.dke.data.agrirouter.api.dto.messaging.FetchMessageResponse;
import com.dke.data.agrirouter.api.dto.messaging.inner.Message;
import com.dke.data.agrirouter.api.dto.onboard.OnboardingResponse;
import com.dke.data.agrirouter.api.enums.TechnicalMessageType;
import com.dke.data.agrirouter.api.env.Environment;
import com.dke.data.agrirouter.api.env.QA;
import com.dke.data.agrirouter.api.service.messaging.*;
import com.dke.data.agrirouter.api.service.messaging.encoding.DecodeMessageService;
import com.dke.data.agrirouter.api.service.messaging.encoding.EncodeMessageService;
import com.dke.data.agrirouter.api.service.parameters.*;
import com.dke.data.agrirouter.impl.common.MessageIdService;
import com.dke.data.agrirouter.impl.messaging.encoding.DecodeMessageServiceImpl;
import com.dke.data.agrirouter.impl.messaging.encoding.EncodeMessageServiceImpl;
import com.dke.data.agrirouter.impl.messaging.rest.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.protobuf.Any;
import com.google.protobuf.ByteString;
import com.google.protobuf.util.JsonFormat;
import org.apache.commons.net.util.Base64;

import javax.json.Json;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static agrirouter.feed.response.FeedResponse.*;
import static agrirouter.response.Response.ResponseEnvelope.ResponseBodyType.ACK;
import static agrirouter.response.Response.ResponseEnvelope.ResponseBodyType.ACK_FOR_FEED_FAILED_MESSAGE;

public class Main {

  private static final String applicationID = "5177c6ec-ff18-4e34-855a-4826db5c666c";
  private static final String certificationVersionID = "e6bc40e5-515d-4c8c-962c-20a6c4e80d9f";
  private static final String receiverEndpointId = "bc127ddb-0df0-44eb-a87c-b7d0e5da254c";
  private static Environment environment;
  private static OnboardingResponse onboardingResponse;
  private static List<String> msgIdsForBMP;
  private static List<String> msgIdsToConfirm;

  public static OnboardingResponse loadOnboardingResponse(String path) throws IOException {
    ClassLoader classLoader = ClassLoader.getSystemClassLoader();
    String fullPath = classLoader.getResource(path).getPath();
    fullPath = new File(fullPath).toString();
    byte[] input = Files.readAllBytes(Paths.get(fullPath));
    String inputString = new String(input);
    Gson gson = new Gson();
    GsonBuilder gsonBuilder = new GsonBuilder();
    onboardingResponse = gson.fromJson(inputString, OnboardingResponse.class);

    return onboardingResponse;
  }

  public static void fetchAnswer() throws IOException {
    FetchMessageService fetchMessageService = new FetchMessageServiceImpl();

    Optional<List<FetchMessageResponse>> optionalResponseList = fetchMessageService.fetch(onboardingResponse, 12, 5000);
    if(optionalResponseList.isPresent()){
      List<FetchMessageResponse> responseList = optionalResponseList.get();
      for(FetchMessageResponse response : responseList) {
        Message message = response.getCommand();
        String messageString = message.getMessage();
        DecodeMessageService decodeMessageService = new DecodeMessageServiceImpl();
        DecodeMessageResponse decodedMessage = decodeMessageService.decode(messageString);

        Response.ResponseEnvelope envelope = decodedMessage.getResponseEnvelope();
        Response.ResponsePayloadWrapper payloadWrapper = decodedMessage.getResponsePayloadWrapper();
        switch (envelope.getType()) {
          //TODO: Add MessageResponseTypes
          case ACK_FOR_FEED_HEADER_LIST:
            HeaderQueryResponse headerQueryResponse = HeaderQueryResponse.parseFrom(payloadWrapper.getDetails().getValue().toByteArray());
            System.out.println("Number of messages found in the HeaderQuery: "+ headerQueryResponse.getFeedList().size());
            for(HeaderQueryResponse.Feed feedMessage: headerQueryResponse.getFeedList()){
              for(HeaderQueryResponse.Header header: feedMessage.getHeadersList()){
                if(header.getTechnicalMessageType().equals(TechnicalMessageType.IMG_BMP.getKey())){
                  System.out.println("Message Found: "+header.getMessageId());
                  msgIdsForBMP.add(header.getMessageId());
                }
              }
            }

            for(String msgId: msgIdsForBMP){
              System.out.println("Part of the List: "+msgId);
            }
            break;

          case ACK_FOR_FEED_MESSAGE:
            MessageQueryResponse messageQueryResponse = MessageQueryResponse.parseFrom(payloadWrapper.getDetails().getValue().toByteArray());
            int count = 0;
            for( MessageQueryResponse.FeedMessage messageEntry: messageQueryResponse.getMessagesList()){
              System.out.println("Message sent by: "+ messageEntry.getHeader().getSenderId());
              System.out.println("        Type: " + messageEntry.getHeader().getTechnicalMessageType());

              byte[] rawBase64Data = messageEntry.getContent().getValue().toByteArray();
              String rawBase64String = new String(rawBase64Data,"utf-8");
              byte[] rawData = Base64.decodeBase64(rawBase64String);
              Files.write(Paths.get("C:\\arapp\\out\\output" + count + ".bmp"),rawData);

              msgIdsToConfirm.add(messageEntry.getHeader().getMessageId());

              count++;


            }
            break;

          case ACK_FOR_FEED_FAILED_MESSAGE:
            System.out.println("Failed to read Feed");
            break;
          case ACK:
            System.out.println("Everything worked");
            System.out.println();
            break;
          case ACK_WITH_MESSAGES:
            System.out.println("Everything worked, but");
            System.out.println(decodedMessage.getResponsePayloadWrapper().toString());
            break;
          case ACK_WITH_FAILURE:
            System.out.println("D'Oh, something went wrong");
            System.out.println(decodedMessage.getResponsePayloadWrapper().toString());
            break;
        }
      }
    }

  }


  public static void main(String[] arguments) throws IOException {

    msgIdsForBMP = new ArrayList<>();
    msgIdsToConfirm = new ArrayList<>();

    environment = new QA() {
      @Override
      public String getAgrirouterLoginUsername() {
        return null;
      }

      @Override
      public String getAgrirouterLoginPassword() {
        return null;
      }
    };

    onboardingResponse = loadOnboardingResponse("./cu/onboard-CU2.json");

    //TODO: Read Header Information
    MessageHeaderQueryService messageHeaderQueryService = new MessageHeaderQueryServiceImpl(environment);

    MessageQueryParameters messageHeaderParameters =new MessageQueryParameters();
    messageHeaderParameters.setMessageIds(new ArrayList<>());
    messageHeaderParameters.setSenderIds(new ArrayList<>());
    messageHeaderParameters.setSentFromInSeconds((long)0);
    messageHeaderParameters.setSentToInSeconds(Instant.now().getEpochSecond());
    messageHeaderParameters.setOnboardingResponse(onboardingResponse);
    messageHeaderQueryService.send(messageHeaderParameters);

    fetchAnswer();

/*
    //TODO: Read Messages that fit our TechnicalMessageType
    MessageQueryService messageQueryService = new MessageQueryServiceImpl(environment);

    MessageQueryParameters messageQueryParameters = new MessageQueryParameters();
    messageQueryParameters.setMessageIds(msgIdsForBMP);
    messageQueryParameters.setSenderIds(new ArrayList<>());
    messageQueryParameters.setSentFromInSeconds((long)0);
    messageQueryParameters.setSentToInSeconds(Instant.now().getEpochSecond());
    messageQueryParameters.setOnboardingResponse(onboardingResponse);
    messageQueryService.send(messageQueryParameters);
    fetchAnswer();

    //TODO: ConfirmMessages
    MessageConfirmationService messageConfirmationService = new MessageConfirmationServiceImpl(environment);

    MessageConfirmationParameters confirmationParameters = new MessageConfirmationParameters();
    confirmationParameters.setOnboardingResponse(onboardingResponse);
    confirmationParameters.setMessageIds(msgIdsToConfirm);
    messageConfirmationService.send(confirmationParameters);
    fetchAnswer();
*/
  }
}
