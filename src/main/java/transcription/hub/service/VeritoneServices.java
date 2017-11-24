package transcription.hub.service;

import java.util.Map;

import org.springframework.http.HttpHeaders;

public interface VeritoneServices {
	
	Map<String, Object> getStatus(String requestId, String recordingId);
	
	String getMediaFile(String recordId);
	
	HttpHeaders createHttpHeaders();
	
	String postHybridTranscription(String recordId, String mediaUrl);
	
	String postMediaAssert(String recordId);
}
