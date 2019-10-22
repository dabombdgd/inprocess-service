package gov.va.bsms.cwinr.model;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CaseNoteDao {
	static Logger LOGGER = LoggerFactory.getLogger(CaseNoteDao.class);

	public List<CaseNote2> getCaseNotesForProcessing() {
		List<CaseNote2> returnVal = new ArrayList<CaseNote2>();
		
		PreparedStatement stmt = null;
		//:TODO update select SQL
		String query = "select in.IN_FROM_SARA_ID, " + 
				"in.CLIENT_ID, " + 
				"in.CASE_NOTE_DATE, " + 
				"substr(in.CASE_NOTE,1,30000) as CASE_NOTE, " + 
				"in.PROCESS_STATUS, " + 
				"in.CASE_NOTE_ID, " + 
				"in.ADDITIONAL1 as CASE_ID          --CASE_ID\r\n" + 
				"in.ADDITIONAL2 as BNFT_CLAIM_NOTE_TYPE_CD, " + 
				"in.ADDITIONAL3, n" + 
				"scx.CASE_DCMNT_ID, n" + 
				"NVL(et.CD,'001') as NVL_CD " + 
				"from CWINRS.TBL_IN_FROM_SARA in, CWINRS.SARA_CORPDB_XREF scx, CWINRS.EVA_TYPE " + 
				"where in.PROCESS_STATUS is NULL " + 
				"and scx.CASE_NOTE_ID (+) = to_number(in.CASE_NOTE_ID) " + 
				"and et.CD (+) = in.ADDITIONAL2 " + 
				"order by in.CASE_NOTE_DATE";

		try {
			CaseNote2 tempCaseNote = new CaseNote2();
			stmt = ConnectionManager.getConnection().prepareStatement(query);
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				tempCaseNote.setClientId(rs.getString("CLIENT_ID"));
				tempCaseNote.setCaseNoteDate(rs.getString("CASE_NOTE_DATE"));
				tempCaseNote.setCaseNote(rs.getString("CASE_NOTE"));
				tempCaseNote.setProcessStatus(rs.getString("PROCESS_STATUS"));
				tempCaseNote.setCaseNoteId(rs.getString("CASE_NOTE_ID"));
				tempCaseNote.setCaseId(rs.getString("CASE_ID"));
				tempCaseNote.setBenefitClaimNoyeTypeCd(rs.getString("BNFT_CLAIM_NOTE_TYPE_CD"));
				tempCaseNote.setAdditional3(rs.getString("ADDITIONAL3"));
				tempCaseNote.setCaseDocumentId(rs.getString("CASE_DCMNT_ID"));
				tempCaseNote.setNvlCD(rs.getString("NVL_CD"));
				
				returnVal.add(tempCaseNote);
			}
		} catch (SQLException e) {
			LOGGER.error(e.getMessage());
		} finally {
			if (stmt != null) {
				try {
					stmt.close();
				} catch (SQLException e) {
					LOGGER.error(e.getMessage());
				}
			}
		}

		return returnVal;
	}

	public void insertErrorCaseNotes(List<CaseNote2> erroredCaseNotes) {
		// :TODO error logging for a casenote
	}

	public void updateErroredCaseNote(List<CaseNote2> erroredCaseNotes) {
		// :TODO update errored casenote
	}

	public void updateNonErroredCaseNotes(List<CaseNote2> nonErroredCaseNotes) {
		// :TODO update successful casenote
	}

	public void insertNewCaseNotes(List<CaseNote2> newCaseNotes) {
		// :TODO insert casenote xref
	}

}
