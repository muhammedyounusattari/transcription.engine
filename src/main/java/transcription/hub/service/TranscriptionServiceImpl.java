package transcription.hub.service;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import transcription.hub.mapper.TranscriptionMapper;
import transcription.hub.model.PayLoad;

@Service
public class TranscriptionServiceImpl implements TranscriptionService {

	@Override
	public String processTranscription(PayLoad payload) {
		ResponseEntity<String> response=  TranscriptionMapper.getMedia(payload);
		if(response.getStatusCode() == HttpStatus.OK){
			TranscriptionMapper.postHybridTranscription(payload.getRecordingId());
			TranscriptionMapper.createTranscriptionJob(payload);
			
		}
		
		return null;
	}

}
