package transcription.hub.configuration;



import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import com.google.gson.Gson;

import transcription.hub.model.PayLoad;
import transcription.hub.service.TranscriptionService;
import transcription.hub.service.TranscriptionServiceImpl;

@SpringBootApplication
//@EnableWebMvc
@ComponentScan("transcription")
public class TranscriptionConfiguration {

	@Autowired
	TranscriptionService service;
	
	public static void main(String[] args) {
		
		ClassLoader classLoader = new TranscriptionConfiguration().getClass().getClassLoader();
		BufferedReader br = new BufferedReader(new InputStreamReader(classLoader.getResourceAsStream("file/payload.json")));

		Gson gson = new Gson();
		PayLoad payLoad = gson.fromJson(br, PayLoad.class);
		String recordId = payLoad.getRecordingId();
		System.out.println("recordId....." + recordId);
		
		TranscriptionService service =new TranscriptionServiceImpl();
		service.processTranscription(payLoad);
	}
	
}
