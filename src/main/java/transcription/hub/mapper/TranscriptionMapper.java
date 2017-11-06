package transcription.hub.mapper;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONObject;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import ch.qos.logback.classic.net.SyslogAppender;
import transcription.hub.model.HybridTransScript;
import transcription.hub.model.PayLoad;

public class TranscriptionMapper implements InitializingBean {

	@Value("$media.assert")
	public String mediaAsrt;
	
	@Value("$post.hybrid.transcription")
	public String postTranscription;
	
	public static String mediaAssert,postAssert;
	@Override
	public void afterPropertiesSet() throws Exception {
		this.mediaAssert = mediaAsrt;
		this.postAssert = postTranscription;
	}
	
	public static ResponseEntity<String> getMedia(PayLoad payload) {

		RestTemplate restTemplate = new RestTemplate();
		HttpHeaders headers = createHttpHeaders();
		
		HttpEntity<String> entity = new HttpEntity<String>("parameters", headers);
	//	String url = mediaAssert.replace("recordingId",  payload.getRecordingId());
		String url="https://api.veritone.com/api/recording/"+payload.getRecordingId()+"/asset";
	//	payload.setRecordingId("40392735");
		ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
		System.out.println(response.getBody());
		if (response.getStatusCode() == HttpStatus.OK) {
			try {
//				byte[] array = Files.readAllBytes(new File("https://inspirent.s3.amazonaws.com/assets/40392735/9126821e-49ca-4f8f-8cd5-0dfc9ee2753b.wav").toPath());
				Files.write(Paths.get(payload.getRecordingId() + ".wav"), downloadUrl(new URL("https://inspirent.s3.amazonaws.com/assets/40392735/df790af9-10a7-4c69-95e4-f7070710b794.wav")));
			} catch (Exception e) {
				e.printStackTrace();
			}
			System.out.println("File got successfully downloaded from media assert.....");
		}
		return response;

	}

	private static HttpHeaders createHttpHeaders() {
		HttpHeaders header = new HttpHeaders();
		header.set("authorization", "Bearer 3386b2:6d88a3f1152d4df7b14d93b22bf26c574ca753e3ac6641559de7fdf36be844c7");
		header.set("cache-control", "no-cache");
		header.set("Content-type", "application/json");
		return header;
	}

	public static String postHybridTranscription(String recordingId) {

		final HttpEntity<Resource> requestEntity = createHttpEntity(recordingId);
		//String url = postAssert;
		String url="https://api.veritone.com/api/recording/"+recordingId+"/asset";
		RestTemplate restTemplate = new RestTemplate();
		ResponseEntity<String> result = restTemplate.exchange(url, HttpMethod.POST, requestEntity,String.class);
		System.out.println("File sent successfully for transcription" + result.getBody());
		
		
		
//		HybridTransScript hybridData = new HybridTransScript();
//		hybridData.setFileByte(response.getBody());
		//hybridData.setFileName("fileName...");
		//hybridData.setPayLoad(payload);

		// System.out.println("params are =>" + hybridData);
//		ObjectMapper mapper = new ObjectMapper();
//		String postData = null;
//		try {
//			postData = mapper.writeValueAsString(hybridData);
//			System.out.println("data sent..." + postData);
//		} catch (JsonProcessingException e) {
//			e.printStackTrace();
//		}

		// System.out.println("request is=>" + request);
	/*	Timer timer = new Timer();
		timer.schedule(new TimerTask() {

			@Override
			public void run() {
				//String url = postAssert;
				String url="https://api.veritone.com/api/recording/40392735/asset";
				RestTemplate restTemplate = new RestTemplate();
				ResponseEntity<String> result = restTemplate.exchange(url, HttpMethod.POST, requestEntity,
						String.class);
				System.out.println("File sent successfully for transcription" + result.getBody());

			}
		}, 10000);*/
		// ResponseEntity<String> result = restTemplate.exchange(url,
		// HttpMethod.POST, requestEntity, String.class);
		return "Some text encrypted....";
	}
	
	public static void createTranscriptionJob(PayLoad payload) {
		RestTemplate restTemplate = new RestTemplate();
		String url="https://api.veritone.com/api/job";
		
		String parameters = "{\"recordingId\": \""+payload.getRecordingId()+"\", \"tasks\": [{ \"engineId\": \"transcode-ffmpeg\", \"taskPayload\": { \"setAsPrimary\": true } }, { \"engineId\": \"transcribe-voicebase\" },{\"engineId\": \"insert-into-index\" } ] }";
		HttpHeaders headers = createHttpHeaders();
		
		HttpEntity<String> entity = new HttpEntity<String>(parameters, headers);
		ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, 
	 			entity, String.class);
	//	String response = restTemplate.postForObject(url, entity, String.class);
		String str[] =response.getBody().split(",")[0].split("jobId");
		String jobId= str[1].substring(3,str[1].length()-1);
		System.out.println("jobId created...."+jobId);
		
		String status = createTranscriptionJobStatus(jobId);
		String s_i[]= status.split("status");
		
		String status_i = s_i[1].substring(3, s_i[1].length()-2);
		System.out.println(status_i);
	//	if(status_i.equalsIgnoreCase("completed")){
			String ttml_script = getTranscriptedScript("40392735");
			System.out.println(ttml_script);
	//	}
		
			//		System.out.println(response.getBody().split("jobId"));
	}

	private static String getTranscriptedScript(String string) {
		String url="https://api.veritone.com/api/recording/"+string+"/transcript";
		HttpHeaders headers = createHttpHeaders();
		HttpEntity<String> entity = new HttpEntity<String>("parameters",headers);
		RestTemplate restTemplate = new RestTemplate();
		ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET,entity,String.class);
		return response.getBody();
	}

	private static String createTranscriptionJobStatus(String jobId) {
		String url="https://api.veritone.com/api/job/"+jobId;
		HttpHeaders headers = createHttpHeaders();
		HttpEntity<String> entity = new HttpEntity<String>("parameters",headers);
		
		RestTemplate restTemplate = new RestTemplate();
		ResponseEntity<String> response = restTemplate.exchange(url,HttpMethod.GET,entity,String.class);
		return response.getBody();
	}

	private static HttpEntity<Resource> createHttpEntity(String recordingId) {
		HttpHeaders requestHeaders = new HttpHeaders();
		requestHeaders.add("Content-Type", "audio/wav");
		requestHeaders.add("authorization", "Bearer 3386b2:6d88a3f1152d4df7b14d93b22bf26c574ca753e3ac6641559de7fdf36be844c7");
		requestHeaders.add("Accept", "application/json");
		requestHeaders.add("x-veritone-asset-type", "media");
		requestHeaders.add("cache-control", "no-cache");		
		return new HttpEntity<Resource>(new FileSystemResource(new File(recordingId+".wav")), requestHeaders);
	}
	
	
	private static byte[] downloadUrl(URL toDownload) {
	    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

	    try {
	        byte[] chunk = new byte[4096];
	        int bytesRead;
	        InputStream stream = toDownload.openStream();

	        while ((bytesRead = stream.read(chunk)) > 0) {
	            outputStream.write(chunk, 0, bytesRead);
	        }

	    } catch (IOException e) {
	        e.printStackTrace();
	        return null;
	    }

	    return outputStream.toByteArray();
	}
	
}
