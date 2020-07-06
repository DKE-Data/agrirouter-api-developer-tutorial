import agrirouter.request.Request;
import agrirouter.request.payload.account.Endpoints;
import agrirouter.request.payload.endpoint.Capabilities;
import com.dke.data.agrirouter.api.dto.onboard.OnboardingResponse;
import com.dke.data.agrirouter.api.dto.onboard.RouterDevice;
import com.dke.data.agrirouter.api.enums.TechnicalMessageType;
import com.dke.data.agrirouter.api.env.Environment;
import com.dke.data.agrirouter.api.env.QA;
import com.dke.data.agrirouter.api.service.messaging.ListEndpointsService;
import com.dke.data.agrirouter.api.service.messaging.SetCapabilityService;
import com.dke.data.agrirouter.api.service.messaging.SetSubscriptionService;
import com.dke.data.agrirouter.api.service.messaging.encoding.EncodeMessageService;
import com.dke.data.agrirouter.api.service.parameters.*;
import com.dke.data.agrirouter.convenience.mqtt.client.MqttClientService;
import com.dke.data.agrirouter.convenience.mqtt.client.MqttOptionService;
import com.dke.data.agrirouter.impl.common.MessageIdService;
import com.dke.data.agrirouter.impl.messaging.encoding.EncodeMessageServiceImpl;
import com.dke.data.agrirouter.impl.messaging.mqtt.ListEndpointsServiceImpl;
import com.dke.data.agrirouter.impl.messaging.mqtt.SendMessageServiceImpl;
import com.dke.data.agrirouter.impl.messaging.mqtt.SetCapabilityServiceImpl;
import com.dke.data.agrirouter.impl.messaging.mqtt.SetSubscriptionServiceImpl;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.protobuf.ByteString;
import org.apache.commons.net.util.Base64;
import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Main {
  private static final String applicationID = "577ea33f-2bd0-430b-a492-333b68727850";
  private static final String certificationVersionID = "0ee08ed9-0df6-4995-a9c6-3c5c9257489e";
  private static Environment environment;
  private static List<OnboardingResponse> onboardingResponses;
  private static RouterDevice routerDevice;
  private static IMqttClient mqttClient;
  private static MqttClientService mqttClientService;
  private static MqttOptionService mqttOptionService;
  private static MQTTCallbackImpl mqttCallbackAdapter;

  private static SetSubscriptionService setSubscriptionService;
  private static SetCapabilityService setCapabilityService;
  private static ListEndpointsService listEndpointsService;
  private static SendMessageServiceImpl sendMessageService;


  public static OnboardingResponse loadOnboardingResponse(String path) throws IOException {
    ClassLoader classLoader = ClassLoader.getSystemClassLoader();
    String fullPath = classLoader.getResource(path).getPath();
    fullPath = new File(fullPath).toString();
    byte[] input = Files.readAllBytes(Paths.get(fullPath));
    String inputString = new String(input);
    Gson gson = new Gson();
    GsonBuilder gsonBuilder = new GsonBuilder();
    OnboardingResponse onboardingResponse = gson.fromJson(inputString, OnboardingResponse.class);

    return onboardingResponse;
  }

  public static RouterDevice loadRouterDevice(String path) throws  IOException {
    ClassLoader classLoader = ClassLoader.getSystemClassLoader();
    String fullPath = classLoader.getResource(path).getPath();
    fullPath = new File(fullPath).toString();
    byte[] input = Files.readAllBytes(Paths.get(fullPath));
    String inputString = new String(input);
    Gson gson = new Gson();
    GsonBuilder gsonBuilder = new GsonBuilder();
    RouterDevice routerDevice = gson.fromJson(inputString, RouterDevice.class);

    return routerDevice;
  }

public static SetCapabilitiesParameters buildCapabilitiesMessage(OnboardingResponse onboardingResponse) {
  List<SetCapabilitiesParameters.CapabilityParameters> parametersList = new ArrayList();

  SetCapabilitiesParameters.CapabilityParameters capabilityParameters;
  capabilityParameters = new SetCapabilitiesParameters.CapabilityParameters();
  capabilityParameters.setTechnicalMessageType(TechnicalMessageType.IMG_BMP);
  capabilityParameters.setDirection(Capabilities.CapabilitySpecification.Direction.SEND_RECEIVE);
  parametersList.add(capabilityParameters);

  SetCapabilitiesParameters parameters = new SetCapabilitiesParameters();
  parameters.setOnboardingResponse(onboardingResponse);
  parameters.setApplicationId(applicationID);
  parameters.setCertificationVersionId(certificationVersionID);
  parameters.setCapabilitiesParameters(parametersList);
  parameters.setEnablePushNotifications(Capabilities.CapabilitySpecification.PushNotification.ENABLED);

  return  parameters;
}

public static SetSubscriptionParameters buildSubscriptionMessage(OnboardingResponse onboardingResponse){
  SetSubscriptionParameters setSubscriptionParameters = new SetSubscriptionParameters();
  setSubscriptionParameters.setOnboardingResponse(onboardingResponse);
  List<SetSubscriptionParameters.Subscription> subscriptionList = new ArrayList<>();
  SetSubscriptionParameters.Subscription subscription_Bitmap = new SetSubscriptionParameters.Subscription();
  subscription_Bitmap.setTechnicalMessageType(TechnicalMessageType.IMG_BMP);
  subscriptionList.add(subscription_Bitmap);
  setSubscriptionParameters.setSubscriptions(subscriptionList);

  return setSubscriptionParameters;
}


public static void  publishImage(OnboardingResponse onboardingResponse, String fileName) throws IOException {
  byte[] binaryData = Files.readAllBytes(Paths.get(fileName));

  String base64Data = Base64.encodeBase64String(binaryData);
  ByteString dataByteString = ByteString.copyFrom(base64Data.getBytes("utf-8"));


  SendMessageParameters parameters = new SendMessageParameters();
  parameters.setOnboardingResponse(onboardingResponse);
  EncodeMessageService encodeMessageService = new EncodeMessageServiceImpl();
  MessageHeaderParameters encodeMessageHeaderParameters = new MessageHeaderParameters();
  encodeMessageHeaderParameters.setApplicationMessageSeqNo(1);
  String applicationMessageId = MessageIdService.generateMessageId();
  encodeMessageHeaderParameters.setApplicationMessageId(applicationMessageId);
  encodeMessageHeaderParameters.setTechnicalMessageType(TechnicalMessageType.IMG_BMP);
  encodeMessageHeaderParameters.setMode(Request.RequestEnvelope.Mode.PUBLISH);
  encodeMessageHeaderParameters.setRecipients(Collections.EMPTY_LIST);
  encodeMessageHeaderParameters.setTeamSetContextId("");

  PayloadParameters encodePayloadParameters = new PayloadParameters();
  encodePayloadParameters.setTypeUrl("");
  encodePayloadParameters.setValue(dataByteString);
  String encodedMessage = encodeMessageService.encode(encodeMessageHeaderParameters, encodePayloadParameters);
  parameters.setEncodedMessages(Collections.singletonList(encodedMessage));
  sendMessageService.send(parameters);

}


  public static void connectMQTT() {
    IMqttToken iMqttToken = null;
    do {
      try {
        iMqttToken = mqttClient.connectWithResult(mqttOptionService.createMqttConnectOptions(routerDevice));
      } catch (MqttException e) {
        e.printStackTrace();
      }
    } while(iMqttToken == null);

    mqttClient.setCallback(mqttCallbackAdapter);
    for(OnboardingResponse entry: onboardingResponses){
      try {
        mqttClient.subscribe(entry.connectionCriteria.getCommands());
      } catch (MqttException e) {
        e.printStackTrace();
      }
    }

  }



  public static void main(String[] arguments) throws IOException {

    //TODO: 1.) Load KeyStore
    System.setProperty("javax.net.ssl.keyStore",Main.class.getClassLoader().getResource("keystore.jks").getPath());
    System.setProperty("javax.net.ssl.keystorePassword","changeit");
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

    routerDevice = loadRouterDevice("./routerdevice.json");

    onboardingResponses = new ArrayList<>();

    onboardingResponses.add(loadOnboardingResponse("./telemetry/onboard-fmis1.json"));
    onboardingResponses.add(loadOnboardingResponse("./telemetry/onboard-fmis2.json"));


    int count = 0;
    for(OnboardingResponse onboardingResponse: onboardingResponses){
      count++;
      System.out.println("FMIS"+count+": "+onboardingResponse.getDeviceAlternateId());
    }

    mqttClientService = new MqttClientService(environment);
    mqttClient = mqttClientService.create(routerDevice);
    mqttOptionService = new MqttOptionService(environment);
    mqttCallbackAdapter = new MQTTCallbackImpl(mqttClient);
    for(OnboardingResponse entry: onboardingResponses){
      mqttCallbackAdapter.addEndpoint(entry);
    }


    setCapabilityService = new SetCapabilityServiceImpl(mqttClient);
    setSubscriptionService = new SetSubscriptionServiceImpl(mqttClient);
    listEndpointsService = new ListEndpointsServiceImpl(mqttClient);
    sendMessageService = new SendMessageServiceImpl(mqttClient);

    connectMQTT();


    for(OnboardingResponse onboardingResponse:onboardingResponses) {
      SetCapabilitiesParameters setCapabilitiesParameters = buildCapabilitiesMessage(onboardingResponse);
      setCapabilityService.send(setCapabilitiesParameters);

      try {
        Thread.sleep(5000);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }

      SetSubscriptionParameters setSubscriptionParameters = buildSubscriptionMessage(onboardingResponse);
      setSubscriptionService.send(setSubscriptionParameters);

      try {
        Thread.sleep(5000);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }

    }


    int timingCounter = 0;
    int CHECK_FOR_SENDING_ENDPOINT_LIST_REQUEST = 60;


    while(true) {

      //Requesting Endpoint List every 60 seconds
      if( timingCounter % CHECK_FOR_SENDING_ENDPOINT_LIST_REQUEST == 0) {
        for (OnboardingResponse onboardingResponse : onboardingResponses) {
          ListEndpointsParameters leparameters = new ListEndpointsParameters();
          leparameters.setDirection(Endpoints.ListEndpointsQuery.Direction.RECEIVE);
          leparameters.setOnboardingResponse(onboardingResponse);
          leparameters.setTechnicalMessageType(TechnicalMessageType.IMG_BMP);
          leparameters.setUnfilteredList(false);
          listEndpointsService.send(leparameters);
          System.out.println("Requested endpoints that can send Bitmaps to endpoint " + onboardingResponse.getDeviceAlternateId());
        }
      }

      for (OnboardingResponse onboardingResponse : onboardingResponses) {
        String fileName = "C:\\arapp\\tutorial\\out\\" + onboardingResponse.deviceAlternateId + ".bmp";
        File imageFile = new File(fileName);
        if (imageFile.exists()) {
          publishImage(onboardingResponse, fileName);
          imageFile.renameTo(new File("C:\\arapp\\tutorial\\out\\sent.bmp"));
        }
      }


      timingCounter ++;

      try {
        Thread.sleep(1000);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
  }

}
