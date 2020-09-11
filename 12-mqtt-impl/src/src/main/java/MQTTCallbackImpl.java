import agrirouter.feed.push.notification.PushNotificationOuterClass;
import agrirouter.response.Response;
import agrirouter.response.payload.account.Endpoints;
import com.dke.data.agrirouter.api.dto.encoding.DecodeMessageResponse;
import com.dke.data.agrirouter.api.dto.messaging.FetchMessageResponse;
import com.dke.data.agrirouter.api.dto.onboard.OnboardingResponse;
import com.dke.data.agrirouter.api.service.parameters.MessageConfirmationParameters;
import com.dke.data.agrirouter.api.service.messaging.MessageConfirmationService;
import com.dke.data.agrirouter.api.service.messaging.encoding.DecodeMessageService;
import com.dke.data.agrirouter.api.service.parameters.MessageConfirmationParameters;
import com.dke.data.agrirouter.impl.messaging.encoding.DecodeMessageServiceImpl;
import com.dke.data.agrirouter.impl.messaging.mqtt.MessageConfirmationServiceImpl;
import com.google.gson.Gson;
import org.apache.commons.codec.binary.Base64;
import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MQTTCallbackImpl implements MqttCallback {
  private DecodeMessageService decodeMessageService;
  private MessageConfirmationService messageConfirmationService;

  private IMqttClient mqttClient;
  private List<OnboardingResponse> endpointList;

  public MQTTCallbackImpl(IMqttClient mqttClient){
    super();
    decodeMessageService = new DecodeMessageServiceImpl();
    this.mqttClient = mqttClient;
    this.messageConfirmationService = new MessageConfirmationServiceImpl(this.mqttClient);
    this.endpointList = new ArrayList<>();
  }

  public void addEndpoint(OnboardingResponse onboardingResponse){
    this.endpointList.add(onboardingResponse);
  }

  public  OnboardingResponse findEndpoint(String topic){
    for(OnboardingResponse entry: this.endpointList){
      if(topic.equals(entry.connectionCriteria.getCommands())){
        return entry;
      }
    }
    return null;
  }

  public List<String> saveReceivedFile(OnboardingResponse onboardingResponse, byte[] data ) throws IOException {
    List<String> messageIds = new ArrayList<>();
    PushNotificationOuterClass.PushNotification pushNotification = PushNotificationOuterClass.PushNotification.parseFrom(data);
    int count = 0;
    for( PushNotificationOuterClass.PushNotification.FeedMessage messageEntry: pushNotification.getMessagesList()){
      System.out.println("Message sent by: "+ messageEntry.getHeader().getSenderId());
      System.out.println("        Type: " + messageEntry.getHeader().getTechnicalMessageType());
      messageIds.add(messageEntry.getHeader().getMessageId());
      byte[] rawBase64Data = messageEntry.getContent().getValue().toByteArray();
      String rawBase64String = new String(rawBase64Data,"utf-8");
      byte[] rawData = Base64.decodeBase64(rawBase64String);
      Files.write(Paths.get("C:\\arapp\\tutorial\\in\\" + onboardingResponse.deviceAlternateId +"_" + count + ".bmp"),rawData);
      count++;
    }

    return messageIds;
  }



  public synchronized void confirmMessages(OnboardingResponse onboardingResponse, List<String> messages){
    MessageConfirmationParameters parameters  = new MessageConfirmationParameters();
    parameters.setMessageIds(messages);
    parameters.setOnboardingResponse(onboardingResponse);

    messageConfirmationService.send(parameters);
  }


  @Override
  public void messageArrived(String topic, MqttMessage message) throws Exception {
    try {
      System.out.println("A new message arrived");
      System.out.println("Receiver: " + topic);
      System.out.println("MQTT Message: " + new String(message.getPayload()));
      Gson gson = new Gson();

      String incomingMessage = new String(message.getPayload());
      FetchMessageResponse fetchMessageResponse = gson.fromJson(incomingMessage, FetchMessageResponse.class);
      DecodeMessageResponse decodeMessageResponse = decodeMessageService.decode(fetchMessageResponse.getCommand().getMessage());

      Response.ResponseEnvelope envelope = decodeMessageResponse.getResponseEnvelope();
      Response.ResponsePayloadWrapper payloadWrapper = decodeMessageResponse.getResponsePayloadWrapper();
      switch (envelope.getType()) {
        case ACK:
          System.out.println("Everything worked");
          break;
        case ACK_WITH_MESSAGES:
          System.out.println("Everything worked, but");
          System.out.println(decodeMessageResponse.getResponsePayloadWrapper().toString());
          break;
        case ACK_WITH_FAILURE:
          System.out.println("D'Oh, something went wrong: ");
          System.out.println("          " + decodeMessageResponse.getResponsePayloadWrapper().toString());
          break;
        case ENDPOINTS_LISTING:
          Endpoints.ListEndpointsResponse listEndpointsResponse = Endpoints.ListEndpointsResponse.parseFrom(payloadWrapper.getDetails().getValue().toByteArray());
          System.out.printf("Number of available Endpoints: "+ listEndpointsResponse.getEndpointsCount());
          break;
        case PUSH_NOTIFICATION:
          OnboardingResponse onboardingResponse = findEndpoint(topic);
          if(onboardingResponse != null) {
            List<String> messages = saveReceivedFile(onboardingResponse, payloadWrapper.getDetails().getValue().toByteArray());
            confirmMessages(onboardingResponse, messages);
          }
          break;
        default:
          System.out.println("Unexpected message received: "+envelope.getType().name());
      }
    } catch ( Exception e) {
      System.out.println("Error found: "+e.getMessage());
    }

  }

  @Override
  public void deliveryComplete(IMqttDeliveryToken token) {
    System.out.println("deliveryComplete");
  }

  public synchronized void reconnectMqtt(){
    Main.connectMQTT();
  }

  @Override
  public void connectionLost(Throwable cause) {
    System.out.println("connection Lost :(");
    reconnectMqtt();
  }


}
