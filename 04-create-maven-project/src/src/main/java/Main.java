import agrirouter.request.Request;
import com.dke.data.agrirouter.api.env.QA;
import com.dke.data.agrirouter.api.service.messaging.MessageConfirmationService;
import com.dke.data.agrirouter.impl.messaging.rest.MessageConfirmationServiceImpl;
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

  public static void main(String[] arguments) throws IOException {
    System.out.println("Hello world");
    QA qa = new QA() {};
    MessageConfirmationService messageConfirmationService = new MessageConfirmationServiceImpl(qa);

  }
}
