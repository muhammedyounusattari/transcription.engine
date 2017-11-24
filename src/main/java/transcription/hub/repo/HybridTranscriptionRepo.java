package transcription.hub.repo;

import org.springframework.data.jpa.repository.JpaRepository;

import transcription.hub.model.HybridTranscriptionTbl;

public interface HybridTranscriptionRepo extends JpaRepository<HybridTranscriptionTbl, Integer> {

	HybridTranscriptionTbl findByRequestidAndRecordingid(String requestid, String recordingid);
	
	HybridTranscriptionTbl findByRecordingid(String recordingid);
}
