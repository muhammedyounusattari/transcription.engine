package transcription.hub.validation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import transcription.hub.model.HybridTranscriptionTbl;
import transcription.hub.repo.HybridTranscriptionRepo;

@Service
public class ValidateVeritone {
	
	@Autowired
	private HybridTranscriptionRepo hybridTranscriptionRepo;
	
	public boolean validateRecordingId(String recordingId)
	{
		boolean isValid = true;
		HybridTranscriptionTbl hybridTranscriptionTbl = hybridTranscriptionRepo.findByRecordingid(recordingId);
		if(hybridTranscriptionTbl != null && hybridTranscriptionTbl.getStatus().equalsIgnoreCase("completed"))
			isValid = false;
		return isValid;
	}
}
