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

import gov.va.bsms.cwinr.exceptions.CaseNotesDaoException;
import gov.va.bsms.cwinr.exceptions.ConnectionManagerException;

public class CaseNoteDao {
	static Logger logger = LoggerFactory.getLogger(CaseNoteDao.class);

	/**
	 * This SQL query returns all of the case note needed for BGS Soap service
	 * processing
	 * 
	 * @return
	 * @throws CaseNotesDaoException
	 */
	public List<CaseNote2> getCaseNotesForProcessing() throws CaseNotesDaoException {
		List<CaseNote2> returnVal = new ArrayList<>();

		PreparedStatement selectCaseNotesForProcessingStmnt = null;
		ResultSet rs = null;
		String caseNoteProcessingSQL = "select ifs.IN_FROM_SARA_ID,"
				+ "ifs.CLIENT_ID,"
				+ "ifs.CASE_NOTE_DATE,"
				+ "substr(ifs.CASE_NOTE,1,30000) as CASE_NOTE,"
				+ "ifs.PROCESS_STATUS,"
				+ "ifs.CASE_NOTE_ID,"
				+ "ifs.ADDITIONAL1 as CASE_ID,"
				+ "ifs.ADDITIONAL2 as BNFT_CLAIM_NOTE_TYPE_CD,"
				+ "ifs.ADDITIONAL3,"
				+ "scx.CASE_DCMNT_ID,"
				+ "NVL(et.CD,'001') as NVL_CD "
				+ "from TBL_IN_FROM_SARA ifs, SARA_CORPDB_XREF scx, EVA_TYPE et "
				+ "where ifs.PROCESS_STATUS is NULL and scx.CASE_NOTE_ID (+) = to_number(ifs.CASE_NOTE_ID) "
				+ "and et.CD (+) = ifs.ADDITIONAL2 "
				+ "order by ifs.CASE_NOTE_DATE";

		try {
			CaseNote2 tempCaseNote = new CaseNote2();
			selectCaseNotesForProcessingStmnt = ConnectionManager.getConnection()
					.prepareStatement(caseNoteProcessingSQL);
			rs = selectCaseNotesForProcessingStmnt.executeQuery();
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
			logger.error(e.getMessage());
			throw new CaseNotesDaoException(e.getMessage());
		} catch (ConnectionManagerException e) {
			throw new CaseNotesDaoException(e.getMessage());
	} finally {
			if (selectCaseNotesForProcessingStmnt != null) {
				try {
					selectCaseNotesForProcessingStmnt.close();
				} catch (SQLException e) {
					logger.error(e.getMessage());
				}
			}
			if(rs != null) {
				try {
					rs.close();
				} catch (SQLException e) {
					logger.error(e.getMessage());
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
	 * @throws CaseNotesDaoException
	 */
	public void insertErrorCaseNotes(List<CaseNote2> erroredCaseNotes) throws CaseNotesDaoException {
		PreparedStatement updateErrorTableStmnt = null;
		Connection conn = null;
		String insertErrorLoggingTableSQL = "insert into log_error (ERROR_ID,ERROR_DATE,CLIENT_ID,"
				+ "IN_FROM_SARA_ID,TYPE,ERROR,ERROR_THREAD,SENT_TO_CENTRAL,SENT_DATE)"
				+ "values (ERROR_ID_SEQ.NEXTVAL,?,?,?,'IN','CASE_ID NOT FOUND','','','')";

		try {
			conn = ConnectionManager.getConnection();
	        conn.setAutoCommit(false);
			updateErrorTableStmnt = conn.prepareStatement(insertErrorLoggingTableSQL);

			for(CaseNote2 erroredCaseNote : erroredCaseNotes) {
				long millis=System.currentTimeMillis();
				Date date=new Date(millis);
				updateErrorTableStmnt.setDate(1, date);
				updateErrorTableStmnt.setInt(2,Integer.parseInt(erroredCaseNote.getClientId()));
				updateErrorTableStmnt.setInt(3,Integer.parseInt(erroredCaseNote.getInFromSaraId()));
				updateErrorTableStmnt.executeUpdate();
				conn.commit();
			}

		} catch (SQLException e) {
			logger.error(e.getMessage());
			throw new CaseNotesDaoException(e.getMessage());
		} catch (ConnectionManagerException e) {
			throw new CaseNotesDaoException(e.getMessage());
		} finally {
			if (updateErrorTableStmnt != null) {
				try {
					updateErrorTableStmnt.close();
				} catch (SQLException e) {
					logger.error(e.getMessage());
				}
			}
		}
	}

	/**
	 * This method is only called if CASE_ID is NULL within a case note.
	 * 
	 * @param erroredBgsCaseNotes
	 * @throws CaseNotesDaoException
	 */
	public void updateErroredCaseNote(List<CaseNote2> erroredDbCaseNotes) throws CaseNotesDaoException {
		PreparedStatement updateErrorCaseNoteTableStmnt = null;
		Connection conn = null;
		String updateCaseNoteTableSQL = "update TBL_IN_FROM_SARA set PROCESS_STATUS = '1' "
				+ "where IN_FROM_SARA_ID = ?";

		try {
			conn = ConnectionManager.getConnection();
	        conn.setAutoCommit(false);
	        updateErrorCaseNoteTableStmnt = conn.prepareStatement(updateCaseNoteTableSQL);

			for(CaseNote2 erroredCaseNote : erroredDbCaseNotes) {
				updateErrorCaseNoteTableStmnt.setInt(1, Integer.parseInt(erroredCaseNote.getInFromSaraId()));
				updateErrorCaseNoteTableStmnt.executeUpdate();
				conn.commit();
			}

		} catch (SQLException e) {
			logger.error(e.getMessage());
			throw new CaseNotesDaoException(e.getMessage());
		} catch (ConnectionManagerException e) {
			throw new CaseNotesDaoException(e.getMessage());
		} finally {
			if (updateErrorCaseNoteTableStmnt != null) {
				try {
					updateErrorCaseNoteTableStmnt.close();
				} catch (SQLException e) {
					logger.error(e.getMessage());
				}
			}
		}
	}

	/**
	 * This method is called for all case notes that do NOT have a db error of a
	 * NULL CASE_ID.
	 * 
	 * @param nonErroredCaseNotes
	 * @throws CaseNotesDaoException
	 */
	public void updateNonDbErroredCaseNotes(List<CaseNote2> nonErroredCaseNotes) throws CaseNotesDaoException {
		PreparedStatement updateErrorCaseNoteTableStmnt = null;
		Connection conn = null;
		String updateCaseNoteTableSQL = "update TBL_IN_FROM_SARA set PROCESS_STATUS = '1', "
				+ "ADDITIONAL10 = ? where IN_FROM_SARA_ID = ?";

		try {
			conn = ConnectionManager.getConnection();
	        conn.setAutoCommit(false);
	        updateErrorCaseNoteTableStmnt = conn.prepareStatement(updateCaseNoteTableSQL);

			for(CaseNote2 erroredCaseNote : nonErroredCaseNotes) {
				updateErrorCaseNoteTableStmnt.setInt(1, Integer.parseInt(erroredCaseNote.getCaseId()));
				updateErrorCaseNoteTableStmnt.setInt(2, Integer.parseInt(erroredCaseNote.getInFromSaraId()));
				updateErrorCaseNoteTableStmnt.executeUpdate();
				conn.commit();
			}

		} catch (SQLException e) {
			logger.error(e.getMessage());
			throw new CaseNotesDaoException(e.getMessage());
		} catch (ConnectionManagerException e) {
			throw new CaseNotesDaoException(e.getMessage());
		} finally {
			if (updateErrorCaseNoteTableStmnt != null) {
				try {
					updateErrorCaseNoteTableStmnt.close();
				} catch (SQLException e) {
					logger.error(e.getMessage());
				}
			}
		}
	}

	/**
	 * This method is called for all case notes that are true for Insert calls to
	 * the BGS SOAP service.
	 * 
	 * @param newCaseNotes
	 * @throws CaseNotesDaoException
	 */
	public void insertNewCaseNotes(List<CaseNote2> newCaseNotes) throws CaseNotesDaoException {
		PreparedStatement insertSaraCorpDbXrefTableStmnt = null;
		Connection conn = null;
		String updateCaseNoteTableSQL = "insert into SARA_CORPDB_XREF (XREF_ID, CASE_NOTE_ID, CASE_DCMNT_ID) "
				+ "values (sara_corpdb_xref_seq.nextval, ?, ?)";

		try {
			conn = ConnectionManager.getConnection();
	        conn.setAutoCommit(false);
			insertSaraCorpDbXrefTableStmnt = conn.prepareStatement(updateCaseNoteTableSQL);

			for(CaseNote2 newCaseNote : newCaseNotes) {
				insertSaraCorpDbXrefTableStmnt.setInt(1,Integer.parseInt(newCaseNote.getCaseNoteId()));
				insertSaraCorpDbXrefTableStmnt.setInt(2,Integer.parseInt(newCaseNote.getCaseDocumentId()));
				insertSaraCorpDbXrefTableStmnt.executeUpdate();
				conn.commit();
			}

		} catch (SQLException e) {
			logger.error(e.getMessage());
			throw new CaseNotesDaoException(e.getMessage());
		} catch (ConnectionManagerException e) {
			throw new CaseNotesDaoException(e.getMessage());
		} finally {
			if (insertSaraCorpDbXrefTableStmnt != null) {
				try {
					insertSaraCorpDbXrefTableStmnt.close();
				} catch (SQLException e) {
					logger.error(e.getMessage());
				}
			}
		}
	}

}
