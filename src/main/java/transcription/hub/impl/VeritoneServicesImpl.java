package transcription.hub.impl;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import transcription.hub.model.HybridTranscriptionTbl;
import transcription.hub.repo.HybridTranscriptionRepo;
import transcription.hub.service.VeritoneServices;

@Service
public class VeritoneServicesImpl implements VeritoneServices{
	
	private static final Logger log = LoggerFactory.getLogger(VeritoneServicesImpl.class);
	
	@Autowired
	private HybridTranscriptionRepo hybridTranscriptionRepo;

	private RestTemplate getRestTemplate() {
		return new RestTemplate();
	}
	
	@Override
	public String getMediaFile(String recordId) {
		HttpHeaders headers = createHttpHeaders();
		HttpEntity<String> entity = new HttpEntity<String>("parameters", headers);
		String url="https://api.veritone.com/api/recording/"+recordId+"/asset";
		ResponseEntity<String> response = getRestTemplate().exchange(url, HttpMethod.GET, entity, String.class);
		String str[] = response.getBody().split("_uri\":\"")[1].split("\"");
		log.info("File got successfully downloaded from media assert....."+str[0]);
		return str[0];
	}

	@Override
	public HttpHeaders createHttpHeaders() {
		HttpHeaders header = new HttpHeaders();
		header.set("Authorization", "Bearer 3386b2:6d88a3f1152d4df7b14d93b22bf26c574ca753e3ac6641559de7fdf36be844c7");
		header.set("Cache-Control", "no-cache");
		header.set("Content-Type", "application/json");
		return header;
	}

	@Override
	public String postHybridTranscription(String recordId, String mediaUrl) {
		//generating a random number
		long first14 = (long) (Math.random() * 100000000000000L);
		String jobId = ""+first14+"-"+recordId;
		log.info("new jobId..."+jobId);
		final HybridTranscriptionTbl hybridTranscription = new HybridTranscriptionTbl();
		hybridTranscription.setRecordingid(recordId);
		hybridTranscription.setRequestid(jobId);
		hybridTranscription.setTitle("audio_to_text_" + recordId +"_"+mediaUrl);
		hybridTranscription.setMediaurl(mediaUrl);
		hybridTranscription.setStatus("submitted");
		hybridTranscription.setInputmimetype("wav file");
		hybridTranscription.setDuration("24 Hours");
		hybridTranscription.setTimecode("Yes");
		hybridTranscription.setMediatype("General");
		hybridTranscription.setPackageid("VERITONE-24HRS");
		hybridTranscription.setMediadifficultylevel("0");
		hybridTranscription.setCallbackurl("/api/job/"+jobId+"/"+recordId);
		hybridTranscription.setTransformat("text");
		hybridTranscription.setTranslanguage("en-US");
		hybridTranscription.setTestjob("YES");
		hybridTranscription.setDailyvolumecap("");
		hybridTranscription.setAccuracypercentage("100");
		hybridTranscription.setOutput(" ");
		HybridTranscriptionTbl saved = hybridTranscriptionRepo.save(hybridTranscription);
		//int status = veritoneDao.insertHybridDetails(hybridTranscription);
		log.info("inserted the details into database "+ saved.getTranscriptionid());
		if(saved.getTranscriptionid() > 0){
		 String jobStatus="",outputUrl="";
		 while(true){
			 Map<String, Object> statusResult = getStatus(hybridTranscription.getRequestid(),hybridTranscription.getRecordingid());
			 jobStatus = (String) statusResult.get("status");
			 outputUrl = (String) statusResult.get("mediaurl");
			 log.info("Current status is ..."+jobStatus);
			 if (jobStatus.equalsIgnoreCase("completed")&& outputUrl.trim()!="") {
				 String resultTTML = getTTMLFile(jobStatus, outputUrl.trim());
				 log.info("resultTTML....."+resultTTML);
				 log.info("******************Moving file to veritone media assert******************");
				 String result = postMediaAssert(recordId);
				 log.info("File(ttml) uploaded in veritone media assert "+result);
				 break;
			 } else {
				 try {
					 Thread.sleep(300000);
				 } catch (InterruptedException e) {
					 e.printStackTrace();
				 }
			 }
		 }
		}
		return "Some text encrypted....";
	}
	
	private String getTTMLFile(String jobStatus,String outputUrl) {
		String response = getRestTemplate().getForObject(outputUrl, String.class);
		try {
			FileUtils.writeStringToFile(new File("thub.ttml"), response);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return response;
		
	}

	@Override
	public String postMediaAssert(String recordId) {
		final HttpEntity<Resource> requestEntity = createHttpEntity(recordId);
		String url="https://api.veritone.com/api/recording/"+recordId+"/asset";
		ResponseEntity<String> result = getRestTemplate().exchange(url, HttpMethod.POST, requestEntity,String.class);
		log.info("File sent successfully for transcription " + result.getBody()); 
		return result.getBody();
	}
	
	private static HttpEntity<Resource> createHttpEntity(String recordingId) {
		HttpHeaders requestHeaders = new HttpHeaders();
		requestHeaders.add("Content-Type", "application/ttml+xml");
		requestHeaders.add("Authorization", "Bearer 3386b2:6d88a3f1152d4df7b14d93b22bf26c574ca753e3ac6641559de7fdf36be844c7");
		requestHeaders.add("x-veritone-asset-type", "transcript");
		requestHeaders.add("Cache-Control", "no-cache");		
		return new HttpEntity<Resource>(new FileSystemResource(new File("thub.ttml")), requestHeaders);
	}

	@Override
	public Map<String, Object> getStatus(String requestId, String recordingId) {
		Map<String, Object> result = new HashMap<String, Object>();
		HybridTranscriptionTbl hybridTranscriptionTbl = hybridTranscriptionRepo.findByRequestidAndRecordingid(requestId, recordingId);
		result.put("status", hybridTranscriptionTbl.getStatus());
		result.put("mediaurl", hybridTranscriptionTbl.getMediaurl());
		return result;
	}
}