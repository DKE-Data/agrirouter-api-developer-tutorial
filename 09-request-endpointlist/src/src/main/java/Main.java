import agrirouter.request.payload.account.Endpoints;
import agrirouter.response.Response;
import agrirouter.response.payload.account.Endpoints.ListEndpointsResponse;
import com.dke.data.agrirouter.api.dto.encoding.DecodeMessageResponse;
import com.dke.data.agrirouter.api.dto.messaging.FetchMessageResponse;
import com.dke.data.agrirouter.api.dto.messaging.inner.Message;
import com.dke.data.agrirouter.api.dto.onboard.OnboardingResponse;
import com.dke.data.agrirouter.api.enums.TechnicalMessageType;
import com.dke.data.agrirouter.api.env.Environment;
import com.dke.data.agrirouter.api.env.QA;
import com.dke.data.agrirouter.api.service.messaging.FetchMessageService;
import com.dke.data.agrirouter.api.service.messaging.ListEndpointsService;
import com.dke.data.agrirouter.api.service.messaging.encoding.DecodeMessageService;
import com.dke.data.agrirouter.api.service.parameters.ListEndpointsParameters;
import com.dke.data.agrirouter.impl.messaging.encoding.DecodeMessageServiceImpl;
import com.dke.data.agrirouter.impl.messaging.rest.FetchMessageServiceImpl;
import com.dke.data.agrirouter.impl.messaging.rest.ListEndpointsServiceImpl;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.protobuf.util.JsonFormat;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
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

    ListEndpointsService listEndpointsService = new ListEndpointsServiceImpl(environment);

    ListEndpointsParameters parameters = new ListEndpointsParameters();
    parameters.setUnfilteredList(false);
    parameters.setTechnicalMessageType(TechnicalMessageType.EMPTY);
    parameters.setOnboardingResponse(onboardingResponse);
    parameters.setDirection(Endpoints.ListEndpointsQuery.Direction.SEND_RECEIVE);
    listEndpointsService.send(parameters);

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
        case ENDPOINTS_LISTING:
          System.out.println("Received an Endpoint list");
          Response.ResponsePayloadWrapper payloadWrapper = decodedMessage.getResponsePayloadWrapper();
          ListEndpointsResponse listEndpointsResponse = ListEndpointsResponse.parseFrom(payloadWrapper.getDetails().getValue().toByteArray());
          Gson gson = new GsonBuilder().setPrettyPrinting().create();
          String listEndpointsGson = gson.toJson(listEndpointsResponse, ListEndpointsResponse.class);
          System.out.println("This is the raw endpoint List");
          System.out.println(listEndpointsGson);
          String listEndpointsJsonFormat = JsonFormat.printer().print(listEndpointsResponse);
          System.out.println("Cleaned Endpoint Response: "+ listEndpointsJsonFormat);
          break;
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
