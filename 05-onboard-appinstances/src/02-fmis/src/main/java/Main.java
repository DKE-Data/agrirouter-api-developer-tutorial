import com.dke.data.agrirouter.api.dto.onboard.OnboardingResponse;
import com.dke.data.agrirouter.api.dto.registrationrequest.secured.AuthorizationResponse;
import com.dke.data.agrirouter.api.dto.registrationrequest.secured.AuthorizationResponseToken;
import com.dke.data.agrirouter.api.env.Environment;
import com.dke.data.agrirouter.api.env.QA;
import com.google.gson.Gson;

import java.net.Socket;

public class Main {
    private static Environment environment;



    public static void main(String[] arguments){

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

        Telemetry.configure(environment);
        String url = Telemetry.getAuthorizationRedirectURL();
        System.out.println("Call the following URL in your browser: " + url );

        AuthorizationResponseToken authorizationResponseToken = Telemetry.getAuthorizationResponseToken(environment);
        System.out.println("AccountID: "+ authorizationResponseToken.getAccount());
        System.out.println("RegCode: " +  authorizationResponseToken.getRegcode());

        String regCode   = authorizationResponseToken.getRegcode();
        OnboardingResponse onboardingResponse =  Telemetry.onboard(regCode);


        Gson gson = new Gson();

        System.out.println(gson.toJson(onboardingResponse).toString());

    }
}
