import agrirouter.request.payload.endpoint.Capabilities;
import agrirouter.response.Response;
import com.dke.data.agrirouter.api.dto.encoding.DecodeMessageResponse;
import com.dke.data.agrirouter.api.dto.messaging.FetchMessageResponse;
import com.dke.data.agrirouter.api.dto.messaging.inner.Message;
import com.dke.data.agrirouter.api.dto.onboard.OnboardingResponse;
import com.dke.data.agrirouter.api.enums.TechnicalMessageType;
import com.dke.data.agrirouter.api.env.Environment;
import com.dke.data.agrirouter.api.env.QA;
import com.dke.data.agrirouter.api.service.messaging.FetchMessageService;
import com.dke.data.agrirouter.api.service.messaging.SetCapabilityService;
import com.dke.data.agrirouter.api.service.messaging.encoding.DecodeMessageService;
import com.dke.data.agrirouter.api.service.parameters.SetCapabilitiesParameters;
import com.dke.data.agrirouter.impl.messaging.encoding.DecodeMessageServiceImpl;
import com.dke.data.agrirouter.impl.messaging.rest.FetchMessageServiceImpl;
import com.dke.data.agrirouter.impl.messaging.rest.SetCapabilityServiceImpl;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Main {

  private static final String applicationID = "5177c6ec-ff18-4e34-855a-4826db5c666c";
  private static final String certificationVersionID = "e6bc40e5-515d-4c8c-962c-20a6c4e80d9f";
  private static Environment environment;
  private static OnboardingResponse onboardingResponse;

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


  public static void main(String[] arguments) throws IOException {

    environment = new QA() {
    };

    onboardingResponse = loadOnboardingResponse("./cu/onboard-CU1.json");

    //TODO: Send capabilities for Bitmap (sending) and TaskData(sending and receiving)
    List<SetCapabilitiesParameters.CapabilityParameters> parametersList = new ArrayList();

    SetCapabilitiesParameters.CapabilityParameters capabilityParameters;
    capabilityParameters = new SetCapabilitiesParameters.CapabilityParameters();
    capabilityParameters.setTechnicalMessageType(TechnicalMessageType.IMG_BMP);
    capabilityParameters.setDirection(Capabilities.CapabilitySpecification.Direction.SEND);
    parametersList.add(capabilityParameters);

    capabilityParameters = new SetCapabilitiesParameters.CapabilityParameters();
    capabilityParameters.setTechnicalMessageType(TechnicalMessageType.ISO_11783_TASKDATA_ZIP);
    capabilityParameters.setDirection(Capabilities.CapabilitySpecification.Direction.SEND_RECEIVE);
    parametersList.add(capabilityParameters);


    SetCapabilitiesParameters parameters  = new SetCapabilitiesParameters();
    parameters.setOnboardingResponse(onboardingResponse);
    parameters.setApplicationId(applicationID);
    parameters.setCertificationVersionId(certificationVersionID);
    parameters.setCapabilitiesParameters(parametersList);


    SetCapabilityService setCapabilityService = new SetCapabilityServiceImpl(environment);
    setCapabilityService.send(parameters);

    FetchMessageService fetchMessageService = new FetchMessageServiceImpl();

    Optional<List<FetchMessageResponse>> optionalResponseList = fetchMessageService.fetch(onboardingResponse, 12, 5000);
    if(optionalResponseList.isPresent()){
      List<FetchMessageResponse> responseList = optionalResponseList.get();
      FetchMessageResponse response = responseList.get(0);
      Message message = response.getCommand();
      String messageString = message.getMessage();
      DecodeMessageService decodeMessageService = new DecodeMessageServiceImpl();
      DecodeMessageResponse decodedMessage = decodeMessageService.decode(messageString);
      Response.ResponseEnvelope envelope = decodedMessage.getResponseEnvelope();
      switch (envelope.getType()){
        case ACK:
          System.out.println("Everything worked");
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
