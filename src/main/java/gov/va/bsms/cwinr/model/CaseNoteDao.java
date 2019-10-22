package gov.va.bsms.cwinr.model;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CaseNoteDao {
	static Logger LOGGER = LoggerFactory.getLogger(CaseNoteDao.class);

	/**
	 * This SQL query returns all of the case note needed for BGS Soap service
	 * processing
	 * 
	 * @return
	 */
	public List<CaseNote2> getCaseNotesForProcessing() {
		List<CaseNote2> returnVal = new ArrayList<CaseNote2>();

		PreparedStatement selectCaseNotesForProcessingStmnt = null;
		String caseNoteProcessingSQL = "select in.IN_FROM_SARA_ID,in.CLIENT_ID,in.CASE_NOTE_DATE, "
				+ "substr(in.CASE_NOTE,1,30000) as CASE_NOTE,in.PROCESS_STATUS,in.CASE_NOTE_ID, "
				+ "in.ADDITIONAL1 as CASE_ID,in.ADDITIONAL2 as BNFT_CLAIM_NOTE_TYPE_CD,in.ADDITIONAL3, "
				+ "scx.CASE_DCMNT_ID,NVL(et.CD,'001') as NVL_CD "
				+ "from CWINRS.TBL_IN_FROM_SARA in, CWINRS.SARA_CORPDB_XREF scx, CWINRS.EVA_TYPE "
				+ "where in.PROCESS_STATUS is NULL and scx.CASE_NOTE_ID (+) = to_number(in.CASE_NOTE_ID) "
				+ "and et.CD (+) = in.ADDITIONAL2 order by in.CASE_NOTE_DATE";

		try {
			CaseNote2 tempCaseNote = new CaseNote2();
			selectCaseNotesForProcessingStmnt = ConnectionManager.getConnection()
					.prepareStatement(caseNoteProcessingSQL);
			ResultSet rs = selectCaseNotesForProcessingStmnt.executeQuery();
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
			if (selectCaseNotesForProcessingStmnt != null) {
				try {
					selectCaseNotesForProcessingStmnt.close();
				} catch (SQLException e) {
					LOGGER.error(e.getMessage());
				}
			}
		}

		return returnVal;
	}

	/**
	 * Always call error logging, if deemed bad data within a case note e.g. CASE_ID
	 * is NULL or BGS returns with bad data
	 * 
	 * @param erroredCaseNotes
	 */
	public void insertErrorCaseNotes(List<CaseNote2> erroredCaseNotes) {
		PreparedStatement updateErrorTableStmnt = null;
		Connection conn = null;
		String insertErrorLoggingTableSQL = "insert into log_error (ERROR_ID,ERROR_DATE,CLIENT_ID,"
				+ "IN_FROM_SARA_ID,TYPE,ERROR,ERROR_THREAD,SENT_TO_CENTRAL,SENT_DATE)"
				+ "values (?,?,?,?,'IN','CASE_ID NOT FOUND','','','')";

		try {
			conn = ConnectionManager.getConnection();
	        conn.setAutoCommit(false);
			updateErrorTableStmnt = conn.prepareStatement(insertErrorLoggingTableSQL);

			for(CaseNote2 erroredCaseNote : erroredCaseNotes) {
				long millis=System.currentTimeMillis();
				Date date=new Date(millis);
				//:TODO find out what ERROR_ID should be set to
				//:TODO get the column types and what should be parameterized.
				updateErrorTableStmnt.setInt(1,-999);
				updateErrorTableStmnt.setDate(2, date);
				updateErrorTableStmnt.setInt(3,Integer.parseInt(erroredCaseNote.getClientId()));
				updateErrorTableStmnt.setInt(4,Integer.parseInt(erroredCaseNote.getInFromSaraId()));
				updateErrorTableStmnt.executeUpdate();
				conn.commit();
			}

		} catch (SQLException e) {
			LOGGER.error(e.getMessage());
		} finally {
			if (updateErrorTableStmnt != null) {
				try {
					updateErrorTableStmnt.close();
				} catch (SQLException e) {
					LOGGER.error(e.getMessage());
				}
			}
		}
	}

	/**
	 * This method is only called if CASE_ID is NULL within a case note.
	 * 
	 * @param erroredBgsCaseNotes
	 */
	public void updateErroredCaseNote(List<CaseNote2> erroredDbCaseNotes) {
		// :TODO update errored casenote

		// :TODO get the column types and what should be parameterized.
		String updateCaseNoteTableSQL = "update TBL_IN_FROM_SARA set PROCESS_STATUS = '1' "
				+ "where IN_FROM_SARA_ID = ?";
	}

	/**
	 * This method is called for all case notes that do NOT have a db error of a
	 * NULL CASE_ID.
	 * 
	 * @param nonErroredCaseNotes
	 */
	public void updateNonDbErroredCaseNotes(List<CaseNote2> nonErroredCaseNotes) {
		// :TODO update successful casenote

		// :TODO get the column types and what should be parameterized.
		String updateCaseNoteTableSQL = "update TBL_IN_FROM_SARA set PROCESS_STATUS = '1', "
				+ "ADDITIONAL10 = ? where IN_FROM_SARA_ID = ?";
	}

	/**
	 * This method is called for all case notes that are true for Insert calls to
	 * the BGS SOAP service.
	 * 
	 * @param newCaseNotes
	 */
	public void insertNewCaseNotes(List<CaseNote2> newCaseNotes) {
		// :TODO insert casenote xref

		// :TODO get the column types and what should be parameterized.
		String updateCaseNoteTableSQL = "insert into SARA_CORPDB_XREF (XREF_ID, CASE_NOTE_ID, CASE_DCMNT_ID) "
				+ "values (?, ?, ?)";
	}

}
