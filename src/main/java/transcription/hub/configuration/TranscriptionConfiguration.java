package transcription.hub.configuration;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.google.gson.Gson;
import transcription.hub.model.Payload;
import transcription.hub.service.VeritoneServices;
import transcription.hub.validation.ValidateVeritone;

@SpringBootApplication
public class TranscriptionConfiguration implements CommandLineRunner{


	private static final Logger log = LoggerFactory.getLogger(TranscriptionConfiguration.class);

	@Autowired(required=true)
	VeritoneServices veritoneServices;

	@Autowired(required=true)
	ValidateVeritone validateVeritone;

	public static void main(String args[]) {
		SpringApplication.run(TranscriptionConfiguration.class, args);
	}

	@Override
	public void run(String... strings) throws Exception  {
		log.info("Calling the run method");
		String filePath = "/Users/Viswanath/Desktop/payload.json"; //System.getenv("PAYLOAD_FILE"); //"/Users/Viswanath/Desktop/payload.json";
		log.info("reading the file from the path "+filePath);
		Payload payLoad = null;
		try {
			BufferedReader br = new BufferedReader(new FileReader(filePath));
			payLoad = new Gson().fromJson(br, Payload.class);
			String recordId = payLoad.getRecordingId();
			log.info("Recording Id from the input json "+recordId);
			boolean validRequestId = validateVeritone.validateRecordingId(recordId);
			log.info("Validating the record in the database "+validRequestId);
			if(validRequestId) {
				log.info("Calling the media file method");
				String mediaUrl = veritoneServices.getMediaFile(recordId);
				log.info("Retrieved the media url "+mediaUrl);
				log.info("Saving the details to the database");
				veritoneServices.postHybridTranscription(recordId, mediaUrl);
			} else {
				log.error("The transcription is completed for the recording id ==> "+recordId);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

	}

}