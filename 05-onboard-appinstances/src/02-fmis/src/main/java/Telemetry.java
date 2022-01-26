import com.dke.data.agrirouter.api.dto.onboard.OnboardingResponse;
import com.dke.data.agrirouter.api.dto.registrationrequest.secured.AuthorizationResponse;
import com.dke.data.agrirouter.api.dto.registrationrequest.secured.AuthorizationResponseToken;
import com.dke.data.agrirouter.api.enums.CertificationType;
import com.dke.data.agrirouter.api.enums.Gateway;
import com.dke.data.agrirouter.api.env.Environment;
import com.dke.data.agrirouter.api.service.onboard.secured.AuthorizationRequestService;
import com.dke.data.agrirouter.api.service.onboard.secured.OnboardingService;
import com.dke.data.agrirouter.api.service.parameters.AuthorizationRequestParameters;
import com.dke.data.agrirouter.api.service.parameters.SecuredOnboardingParameters;
import com.dke.data.agrirouter.impl.onboard.secured.AuthorizationRequestServiceImpl;
import com.dke.data.agrirouter.impl.onboard.secured.OnboardingServiceImpl;

import java.util.UUID;

public class Telemetry {
    private static final String applicationID = "63d1cb74-4ac6-410c-a6bf-b008db87aba1";
    private static final String certificationVersionID = "89a9e541-d9c0-452c-8dac-73a170cc507f";
    private static final String privateKey = "-----BEGIN PRIVATE KEY-----MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQDQktn7d15uWo9Pa+d4fivcm+0bLglxd4eu3lX0s2mPNNrAQ0A957DRgP6Gubf+xOuTZbKb4E5CNYVUuJp/gig0OjPhQwVuOsxuuJlkeIcisiIYHTZzKUMx8MDSzi8DwxXIXrQ6EeZGrFFH7Eea27l8eCWdUHVWlWs0QiUIaoXeOvAhxwFKta9RUljIUjJHZ/oOrQUsxXm2FsjhReqduNa1Dur2+pLlqJXfsiGetLag1ys0JUkl4+/laqi9Rc4l9arKAcxfM2MvYn/k/1uw2/WdgcwsMOgAdCuqQ4HcTYd/Cj9k5C2BeT5kY+YUCzzMvAfUJ0rJXZZDO3x7sOzf6oI1AgMBAAECggEACiNz8x73TnfvysAyV+S82b6+miP1E43oNjOFNsgt07yRpueIojm2mkh3QYdE5AkgcQwa0Cw+wZsmaFrc2mEyS8T0gU5SX5b+GaJhhRpoFI4CIu1F7ffN1gn05YpeNgRFSnXICaFAcfkDA8hUJrk9h/OzKGAhLrHurHNL/N++CBz7e2q2O1m5bEEGV8mNjSwAZuhVRRwew+dR/NoA7b15S1pUgj9cZQsOYmmbiijxYKKgGvLjCIKTmNq93etZ8kkAHJsbE1MRr0ODwnnJeNWAYzMTX+bnItBKkaAlv68VB1rH1WD5GrPkYuSUxWMOGcxzVoAjIHFBidoAHLbd8BTWAQKBgQDzu5it8AUF5i4D8rkjVCcjzHBcTLj4l4cwoxlFC0KMGxCt3+T+w/xcDPyVBt9Qt7C4IaJLNBru7nHjIXSsc1vjLJG/nlf63Ln2pl832ifUS3KkNCh6XlNXt9YQGCAl33v3uoWaDZ+gC4l8hTQ1POHXTBv8VE3cGpgD6clp92WIgQKBgQDbEj4ob+HAzhUmIjB0wvdQp3woE4Bd3cHUEgfcDMqIW9MfjTlQYeXPQUIhHfE9Y8CgpNIsvFLW2Shk6MjCGTmA4FVQyJ0TAehJVRvzu8kBhaPNfEGdXPUii7Jc93XjnE2aE0E0rj7I2RVetkHQxKh2a/XagT7R1Fnq8e+n2el/tQKBgQDHr1F2Z+flnmDi0TCHVkG+3ZbMt/rviE21NlrUq1X5LY9rtLyLUYxm6ijh+8BoBCbfpIavsF/Ek0xxNo69XsHQjEpYwGT6XW5qOoJWYwAwuSOSjjz/jFohyraxduKXxNJu8rzUqwa1e95HS2arm1a4Kl3fTD6B7rPOuPJQQ+VGgQKBgQDANZBd8FQ101sQ+zxipdFSbbDshLfDI9d1l2BX/M0SuOZTL5iS42I75vj7j4bjIwuWpyZU4+MW6K+dYfULABDI94tDdtECJVFGTitZPXDOKhAfMZ9sT2wxJDqZgPFpg/E+dxhl/V/O31D1qZmzb2iThoPOdEnG1rcSAbYRIovRZQKBgFwithddwtkEpS/DTD2gHfAJnhUsI/ItQZsMYRVZ2Picc30lsBisMV6oxd9DOhR5XaMagMW7GWMqawBSWKfw1PfcB9AMOLHQ5FOD7BRdgcsgdGGp1JX3wj4VjrQ8uBLn+VDtVdiYKNV6iBoh0htbwjzh4w66H7X107RhREX5X/+F-----END PRIVATE KEY-----";
    private static final String publicKey = "-----BEGIN PUBLIC KEY-----\n" +
            "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA0JLZ+3deblqPT2vneH4r\n" +
            "3JvtGy4JcXeHrt5V9LNpjzTawENAPeew0YD+hrm3/sTrk2Wym+BOQjWFVLiaf4Io\n" +
            "NDoz4UMFbjrMbriZZHiHIrIiGB02cylDMfDA0s4vA8MVyF60OhHmRqxRR+xHmtu5\n" +
            "fHglnVB1VpVrNEIlCGqF3jrwIccBSrWvUVJYyFIyR2f6Dq0FLMV5thbI4UXqnbjW\n" +
            "tQ7q9vqS5aiV37IhnrS2oNcrNCVJJePv5WqovUXOJfWqygHMXzNjL2J/5P9bsNv1\n" +
            "nYHMLDDoAHQrqkOB3E2Hfwo/ZOQtgXk+ZGPmFAs8zLwH1CdKyV2WQzt8e7Ds3+qC\n" +
            "NQIDAQAB\n" +
            "-----END PUBLIC KEY-----";
    private static Environment environment;
    private static AuthorizationRequestService authorizationService;

    public static void configure(Environment environment){
        authorizationService  = new AuthorizationRequestServiceImpl(environment);
        Telemetry.environment = environment;
    }


    public static OnboardingResponse onboard(String registrationCode){
        OnboardingService onboardingService = new OnboardingServiceImpl(environment);

        SecuredOnboardingParameters onboardingParameters = new SecuredOnboardingParameters();
        onboardingParameters.setGatewayId(Gateway.MQTT.getKey());
        onboardingParameters.setCertificationVersionId(certificationVersionID);
        onboardingParameters.setCertificationType(CertificationType.PEM);
        onboardingParameters.setApplicationId(applicationID);
        onboardingParameters.setRegistrationCode(registrationCode);
        onboardingParameters.setCertificationType(CertificationType.PEM);
        onboardingParameters.setPrivateKey(privateKey);
        onboardingParameters.setPublicKey(publicKey);
        onboardingParameters.setUuid("DKEData:ExampleTelemetry:"+ UUID.randomUUID());

        OnboardingResponse onboardingResponse = onboardingService.onboard(onboardingParameters);

        return onboardingResponse;
    }

    public static AuthorizationResponseToken getAuthorizationResponseToken(Environment environment) {
        AuthorizationResponse authorizationResponse =  AuthorizationResponseServer.createAuthorizationServerAndAwaitToken(environment);

        AuthorizationResponseToken authorizationResponseToken = authorizationService.decodeToken(authorizationResponse.getToken());
        return authorizationResponseToken;
    }

    public static String getAuthorizationRedirectURL(){
        AuthorizationRequestParameters authorizationRequestParameters = new AuthorizationRequestParameters();
        authorizationRequestParameters.setApplicationId(applicationID);

        return authorizationService.getAuthorizationRequestURL(authorizationRequestParameters);

    }
}
