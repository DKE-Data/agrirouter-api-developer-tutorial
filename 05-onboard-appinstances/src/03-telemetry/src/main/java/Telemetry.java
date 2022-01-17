import com.dke.data.agrirouter.api.dto.onboard.OnboardingResponse;
import com.dke.data.agrirouter.api.dto.registrationrequest.secured.AuthorizationResponse;
import com.dke.data.agrirouter.api.dto.registrationrequest.secured.AuthorizationResponseToken;
import com.dke.data.agrirouter.api.enums.CertificationType;
import com.dke.data.agrirouter.api.enums.Gateway;
import com.dke.data.agrirouter.api.enums.SecuredOnboardingResponseType;
import com.dke.data.agrirouter.api.env.Environment;
import com.dke.data.agrirouter.api.service.onboard.secured.AuthorizationRequestService;
import com.dke.data.agrirouter.api.service.onboard.secured.OnboardingService;
import com.dke.data.agrirouter.api.service.parameters.AuthorizationRequestParameters;
import com.dke.data.agrirouter.api.service.parameters.SecuredOnboardingParameters;
import com.dke.data.agrirouter.impl.onboard.secured.AuthorizationRequestServiceImpl;
import com.dke.data.agrirouter.impl.onboard.secured.OnboardingServiceImpl;

import java.util.UUID;


public class Telemetry {
  private static Environment environment;
  private static final String applicationID = "63d1cb74-4ac6-410c-a6bf-b008db87aba1";
  private static final String certificationVersionID = "cc9b00a1-8009-409b-ab06-88cb08109322";
  private static final String privateKey = "-----BEGIN PRIVATE KEY-----MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQDfkxAuXpBFdlbj6QU6oTjROpl0hrD1PSQADMLfBFH/gR5y+CziZLDLC7p6yu1Dc+eszvSqcxisopjkvajvOhUb/IQX00NcbOviGMd1nPbu5lcuxwEL4qzog+BrXa7StazclXmo9t4qpJ1ME0kbXhTmgYjLz37xpDu47P8AqaQFIs3PMyylsExZfwYzkBIvgMIm2y4wrQCP1kzxaeQxdt7lGuceNxbVRbzMkdlbUW27d3KC45x4/2saR8a7ojWEqBqHNHMlPz/8cEIPRLIMkhsIPIeIFAR/7IsHfEk2IrrN82/SDm9AO21OrDeCsUna186Gc4CsVP0uTGWfADvos16lAgMBAAECggEAJo5QDg8UxkXgOjGnSzMef0ahzCymO8Stfqy0to0iLvPvclhGNUy0CHAfWqnAPQ+x9mVCYFq0+KCZyWjDx2220jq79Pfj9/54t2cajrfhBdYHVvPOxGP0+aw0eY+QZJocE3Zbor3gFm6JCeORifyxkgbotm00MSvneL62Q0D+HjxNudFEnLrmajqHWxQelvEKrRUmyUY0k6axJdVS8AVoyuUzeNKEit4VYtMz51z2LRx2sqL9KaHPnMllnRQ7wL/+KQ0Ss4or8QJa2o0A9LnKLftbYsU5+uQxILLwBgCWfgcL6ILIKdg1O3gpY7ZKrwc5Er053ntQdHZ9+Jt8moF+wQKBgQD4vdqDS5AJgZsqW92z2i1qy0DHbygTpEl1VPA+AnamHq9etAGXZllB4jE6qS60DAFWCFzk0hoU2A0AxB/B1rMa5M+lALFOSD03bMjPVqidRJ4zdSVv8vQYivUYs7kfJZX+MLZ8byTmjhxqLJ1k9olqpwCXsHChCRe449UGf9ypLQKBgQDmGTTRcbGnV56aEsg2IGvnR0/WddfJIpAhk7ileT4V4sdxFX64QW04zLycdpbvRpLKo4gJIrqLP4iu5cE4oQ1Ced24nE/7sFeOzfoP4e2vDr09DsuuUx3GgD3IyuGleWXH5Q3R0qpGs++vtjcVpuETJWNQAbWKxA6zul2aHYuGWQKBgFWRcicyilVK2acDvUvOpUsUqq8wxPekz4RmeNLMJCbXbXrFr7p2ggQRHOBSLPfRHXCMJfCtej1raWW3EjmrcyewSOI5T8VfQnfbm2UAKbcYUNfg18UgLep3ewNJaEiXtHaiKVS7I1WQ06OOv4Jo8TLYSnLkRoqPRzLaZYD74VvtAoGBAIpacaXWBQon5xehX30cNYVVHa5IIT2xaJSn7AijRC+Isn9Oe2ly/ad/g5FYxRyOhMkPV0aW+S+tPhRb1bilLgHgoO7WHkmqGJunQkq2gGIYLLU/5jylgWvPxFatlswWmJp/IbYeQ4BLxT0UBT8hnogSnQqIXI5ZoLV0w79dMH3RAoGBANT4lqV5Ns0JoWtzVF6RbLl1tkVVakOeyT6vb5q6g5ESTIAliHtHISjOsaCRv2uBYDN2YLBjDdWQ8QUpAOblQqwN7YoiiDZtpgYiVZ2op4oKUL5WQYH7I3vXGY/osLxABxE/emZ15GuWPjj8RoWCwNOl4ufhU4KwitaEOvHjljaN-----END PRIVATE KEY-----";
  private static final String publicKey = "-----BEGIN PUBLIC KEY-----\n" +
          "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA35MQLl6QRXZW4+kFOqE4\n" +
          "0TqZdIaw9T0kAAzC3wRR/4Eecvgs4mSwywu6esrtQ3PnrM70qnMYrKKY5L2o7zoV\n" +
          "G/yEF9NDXGzr4hjHdZz27uZXLscBC+Ks6IPga12u0rWs3JV5qPbeKqSdTBNJG14U\n" +
          "5oGIy89+8aQ7uOz/AKmkBSLNzzMspbBMWX8GM5ASL4DCJtsuMK0Aj9ZM8WnkMXbe\n" +
          "5RrnHjcW1UW8zJHZW1Ftu3dyguOceP9rGkfGu6I1hKgahzRzJT8//HBCD0SyDJIb\n" +
          "CDyHiBQEf+yLB3xJNiK6zfNv0g5vQDttTqw3grFJ2tfOhnOArFT9LkxlnwA76LNe\n" +
          "pQIDAQAB\n" +
          "-----END PUBLIC KEY-----\n";
  private static AuthorizationRequestService authorizationService;

  public static void configure(Environment environment){
    Telemetry.environment = environment;
    authorizationService = new AuthorizationRequestServiceImpl(environment);
  }

  public static String getAuthorizationRedirectURL(){
    AuthorizationRequestParameters parameters = new AuthorizationRequestParameters();
    parameters.setApplicationId(applicationID);
    parameters.setResponseType(SecuredOnboardingResponseType.ONBOARD);

    return authorizationService.getAuthorizationRequestURL( parameters); //TODO: Generate URL
  }


  public static AuthorizationResponseToken getAuthorizationResponseToken(Environment environment) {
    AuthorizationResponse authorizationResponse = AuthorizationResponseServer.createAuthorizationServerAndAwaitToken(environment);
    AuthorizationResponseToken token = authorizationService.decodeToken(authorizationResponse.getToken()); //TODO Catch Token

    return token;
  }

  public static OnboardingResponse onboard(String registrationCode){
    OnboardingService onboardingService = new OnboardingServiceImpl(environment);
    SecuredOnboardingParameters parameters = new SecuredOnboardingParameters();
    parameters.setPrivateKey(privateKey);
    parameters.setCertificationType(CertificationType.PEM);
    parameters.setApplicationId( applicationID);
    parameters.setGatewayId(Gateway.REST.getKey());
    parameters.setPublicKey(publicKey);
    parameters.setCertificationVersionId(certificationVersionID);
    parameters.setRegistrationCode(registrationCode);
    parameters.setUuid("DKE:Data:Telemetry:Example"+ UUID.randomUUID());
    OnboardingResponse onboardingResponse = onboardingService.onboard(parameters); //TODO Request Onboarding Response

    return onboardingResponse;
  }

}
