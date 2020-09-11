import agrirouter.request.Request;
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
import com.dke.data.agrirouter.api.service.messaging.SendMessageService;
import com.dke.data.agrirouter.api.service.messaging.encoding.DecodeMessageService;
import com.dke.data.agrirouter.api.service.messaging.encoding.EncodeMessageService;
import com.dke.data.agrirouter.api.service.parameters.ListEndpointsParameters;
import com.dke.data.agrirouter.api.service.parameters.MessageHeaderParameters;
import com.dke.data.agrirouter.api.service.parameters.PayloadParameters;
import com.dke.data.agrirouter.api.service.parameters.SendMessageParameters;
import com.dke.data.agrirouter.impl.common.MessageIdService;
import com.dke.data.agrirouter.impl.messaging.encoding.DecodeMessageServiceImpl;
import com.dke.data.agrirouter.impl.messaging.encoding.EncodeMessageServiceImpl;
import com.dke.data.agrirouter.impl.messaging.rest.FetchMessageServiceImpl;
import com.dke.data.agrirouter.impl.messaging.rest.ListEndpointsServiceImpl;
import com.dke.data.agrirouter.impl.messaging.rest.SendMessageServiceImpl;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.protobuf.Any;
import com.google.protobuf.ByteString;
import com.google.protobuf.util.JsonFormat;
import org.apache.commons.net.util.Base64;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class Main {

  private static final String applicationID = "5177c6ec-ff18-4e34-855a-4826db5c666c";
  private static final String certificationVersionID = "e6bc40e5-515d-4c8c-962c-20a6c4e80d9f";
  private static final String receiverEndpointId = "bc127ddb-0df0-44eb-a87c-b7d0e5da254c";
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

    //TODO: Load File and send File
    String fileName = "src\\main\\resources\\file\\logo.bmp";

    File file = new File(fileName);
    if(!file.exists()){
      System.out.println("File does not exist");
      return;
    }

    byte[] binaryData = Files.readAllBytes(Paths.get(fileName));

    String base64Data = Base64.encodeBase64String(binaryData);
    ByteString dataByteString = ByteString.copyFrom(base64Data.getBytes("utf-8"));


    SendMessageService sendMessageService = new SendMessageServiceImpl();

    SendMessageParameters parameters = new SendMessageParameters();
    parameters.setOnboardingResponse(onboardingResponse);
    EncodeMessageService encodeMessageService = new EncodeMessageServiceImpl();
    MessageHeaderParameters encodeMessageHeaderParameters = new MessageHeaderParameters();
    encodeMessageHeaderParameters.setApplicationMessageSeqNo(1);
    String applicationMessageId = MessageIdService.generateMessageId();
    encodeMessageHeaderParameters.setApplicationMessageId(applicationMessageId);
    encodeMessageHeaderParameters.setMode(Request.RequestEnvelope.Mode.DIRECT);
    encodeMessageHeaderParameters.setTechnicalMessageType(TechnicalMessageType.IMG_BMP);
    encodeMessageHeaderParameters.setRecipients(Collections.singletonList("bc127ddb-0df0-44eb-a87c-b7d0e5da254c"));
    encodeMessageHeaderParameters.setTeamSetContextId("");

    PayloadParameters encodePayloadParameters = new PayloadParameters();
    encodePayloadParameters.setTypeUrl("");
    encodePayloadParameters.setValue(dataByteString);
    String encodedMessage = encodeMessageService.encode(encodeMessageHeaderParameters, encodePayloadParameters);
    parameters.setEncodedMessages(Collections.singletonList(encodedMessage));
    sendMessageService.send(parameters);

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
