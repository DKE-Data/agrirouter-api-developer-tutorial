import com.dke.data.agrirouter.api.dto.onboard.OnboardingResponse;
import com.dke.data.agrirouter.api.enums.ApplicationType;
import com.dke.data.agrirouter.api.enums.CertificationType;
import com.dke.data.agrirouter.api.enums.Gateway;
import com.dke.data.agrirouter.api.env.Environment;
import com.dke.data.agrirouter.api.env.QA;
import com.dke.data.agrirouter.api.service.onboard.OnboardingService;
import com.dke.data.agrirouter.api.service.parameters.OnboardingParameters;
import com.dke.data.agrirouter.impl.onboard.OnboardingServiceImpl;
import com.google.gson.Gson;

public class Main {

  public static void main(String[] arguments){

    Environment environment = new QA() {
      @Override
      public String getAgrirouterLoginUsername() {
        return null;
      }

      @Override
      public String getAgrirouterLoginPassword() {
        return null;
      }
    };

    OnboardingService onboardingService = new OnboardingServiceImpl(environment);

    OnboardingParameters onboardingParameters = new OnboardingParameters();
    onboardingParameters.setGatewayId(Gateway.REST.getKey());
    onboardingParameters.setCertificationVersionId("e6bc40e5-515d-4c8c-962c-20a6c4e80d9f");
    onboardingParameters.setCertificationType(CertificationType.PEM);
    onboardingParameters.setApplicationId("5177c6ec-ff18-4e34-855a-4826db5c666c");
    onboardingParameters.setRegistrationCode("0b6b2a816a");
    onboardingParameters.setApplicationType(ApplicationType.APPLICATION);
    onboardingParameters.setUuid("dke:data:examplecu:532ef");

    OnboardingResponse onboardingResponse = onboardingService.onboard(onboardingParameters);
    System.out.println("Result: "+onboardingResponse.toString());
    System.out.println("DeviceAlternateId: " + onboardingResponse.deviceAlternateId);
    System.out.println("CapabilityAlternateId: " + onboardingResponse.capabilityAlternateId);
    System.out.println("SendorAlternateId: " + onboardingResponse.sensorAlternateId);
    System.out.println("Authentication: ");
    System.out.println("    Certificate: " + onboardingResponse.getAuthentication().getCertificate());
    System.out.println("    Password: " + onboardingResponse.getAuthentication().getSecret());
    System.out.println("    Type: " + onboardingResponse.getAuthentication().getType().toString());
    System.out.println("ConnectionCriteria: ");
    System.out.println(" Measures(Inbox): " + onboardingResponse.getConnectionCriteria().getMeasures());
    System.out.println(" Commands(Outbox): " +  onboardingResponse.getConnectionCriteria().getCommands());
    System.out.println(" Host: " + onboardingResponse.getConnectionCriteria().getHost());
    System.out.println(" Port: " + onboardingResponse.getConnectionCriteria().getPort());

    Gson gson = new Gson();
    System.out.println("JSON Result: " + gson.toJson(onboardingResponse));


  }
}
