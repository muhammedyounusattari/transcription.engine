package transcription.hub.dao;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;


@Repository
public class VeritoneDao {
	
	@Autowired
	JdbcTemplate jdbcTemplate;
	
	public List<Map<String, Object>> getJobStatus(String requestId, String recordingId) {
		String sql = "select requestid, recordingid, mediaurl, status From dbhybrid_transcription.hybrid_transcritpion_tbl where requestId='"+requestId+"'";
		return jdbcTemplate.queryForList(sql);//, new Object[] { requestId, recordingId });
		// execute(sql, new Object[]{requestId,recordingId));
	}
}
