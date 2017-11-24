package transcription.hub.configuration;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class JobStatusURL{
	
	@Autowired
	JdbcTemplate jdbcTemplate;
//	http://localhost:8080/api/job/29221100847567-41306431/41306431
	
	@RequestMapping("/api/job/{jobId}/{recordingId}")
	public List<Map<String, Object>> getJobStatus(@PathVariable("jobId") String jobId,@PathVariable("recordingId") String recordingId){
		System.out.println("inside getJobStatus....");
		return getJobStatusResult(jobId, recordingId);
	}
	
	public List<Map<String, Object>> getJobStatusResult(String requestId, String recordingId) {
		System.out.println("inside getJobStatus....");
		String sql = "select requestId, recordingId, mediaUrl, status From dbhybrid_transcription.hybrid_transcritpion_tbl where requestId='"+requestId+"'";
		return jdbcTemplate.queryForList(sql);//, new Object[] { requestId, recordingId });
		// execute(sql, new Object[]{requestId,recordingId));
	}
	
	
	
}