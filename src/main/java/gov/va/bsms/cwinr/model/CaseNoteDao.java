package gov.va.bsms.cwinr.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.va.bsms.cwinr.exceptions.CaseNotesDaoException;
import gov.va.bsms.cwinr.exceptions.ConfigurationManagerException;
import gov.va.bsms.cwinr.exceptions.ConnectionManagerException;

public class CaseNoteDao {
	static Logger logger = LoggerFactory.getLogger(CaseNoteDao.class);
	
	// constants
	private static final String LOG_ERROR_TABLE_INSERTION_ERROR_MSG = "log-error table insertion error for the following case note: case_id:{}, case_note_id:{}";
	private static final String SARA_CORPDB_XREF_TABLE_INSERTION_ERROR_MSG = "SARA_CORPDB_XREF table insertion error for the following case note: case_id:{}, case_note_id:{}";
	private static final String TBL_IN_FROM_SARA_TABLE_UPDATE_ERROR_MSG = "TBL_IN_FROM_SARA table update error for the following case note: case_id:{}, case_note_id:{}";
	private static final String INSERT_LOG_ERROR_TBL_SQL = "insert into log_error (ERROR_ID,ERROR_DATE,CLIENT_ID,"
			+ "IN_FROM_SARA_ID,TYPE,ERROR,ERROR_THREAD,SENT_TO_CENTRAL,SENT_DATE)"
			+ "values (LOG_ERROR_SEQ.NEXTVAL,SYSDATE,?,?,'IN',?,'','',SYSDATE)";
	private static final String UPDATE_TBL_IN_FROM_SARA_ERROR_SQL = "update TBL_IN_FROM_SARA set PROCESS_STATUS = '1' "
			+ "where IN_FROM_SARA_ID = ?";
	private static final String UPDATE_TBL_IN_FROM_SARA_SUCCESS_SQL = "update TBL_IN_FROM_SARA set PROCESS_STATUS = '1', "
			+ "ADDITIONAL10 = ? where IN_FROM_SARA_ID = ?";
	private static final String INSERT_SARA_CORPDB_XREF_SQL = "insert into SARA_CORPDB_XREF (XREF_ID, CASE_NOTE_ID, CASE_DCMNT_ID) "
			+ "values (sara_corpdb_xref_seq.nextval, ?, ?)";

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
				+ "where (ifs.PROCESS_STATUS = '0' OR ifs.PROCESS_STATUS is NULL) and scx.CASE_NOTE_ID (+) = to_number(ifs.CASE_NOTE_ID) "
				+ "and et.CD (+) = ifs.ADDITIONAL2 "
				+ "order by ifs.CASE_NOTE_DATE";

		try {
			CaseNote2 tempCaseNote;
			selectCaseNotesForProcessingStmnt = ConnectionManager.getConnection(true)
					.prepareStatement(caseNoteProcessingSQL);
			rs = selectCaseNotesForProcessingStmnt.executeQuery();
			while (rs.next()) {
				tempCaseNote = new CaseNote2();
				tempCaseNote.setInFromSaraId(rs.getString("IN_FROM_SARA_ID"));
				tempCaseNote.setClientId(rs.getString("CLIENT_ID"));
				tempCaseNote.setCaseNoteDate(rs.getString("CASE_NOTE_DATE"));
				tempCaseNote.setCaseNote(rs.getString("CASE_NOTE"));
				tempCaseNote.setProcessStatus(rs.getString("PROCESS_STATUS"));
				tempCaseNote.setCaseNoteId(rs.getString("CASE_NOTE_ID"));
				tempCaseNote.setCaseId(rs.getString("CASE_ID"));
				tempCaseNote.setBenefitClaimNoteTypeCd(rs.getString("BNFT_CLAIM_NOTE_TYPE_CD"));
				tempCaseNote.setAdditional3(rs.getString("ADDITIONAL3"));
				tempCaseNote.setCaseDocumentId(rs.getString("CASE_DCMNT_ID"));
				tempCaseNote.setNvlCD(rs.getString("NVL_CD"));

				returnVal.add(tempCaseNote);
			}
		} catch (SQLException e) {
			logger.error(e.getMessage());
			throw new CaseNotesDaoException(e.getMessage(), e);
		} catch (ConnectionManagerException|ConfigurationManagerException e) {
			throw new CaseNotesDaoException(e.getMessage(), e);
		} finally {
			if(selectCaseNotesForProcessingStmnt!=null) {
				try {
						selectCaseNotesForProcessingStmnt.close();
				} catch (SQLException e) {
					logger.error(e.getMessage());
				}
			}
			if(rs!=null) {
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
		
		// establish connection to Oracle database
		try {
			conn = ConnectionManager.getConnection(false);
			updateErrorTableStmnt = conn.prepareStatement(INSERT_LOG_ERROR_TBL_SQL);
		} catch (SQLException e) {
			logger.error(e.getMessage());
			ConnectionManager.closeDatabaseObjects(updateErrorTableStmnt, conn);
			throw new CaseNotesDaoException(e.getMessage(), e);
		} catch (ConnectionManagerException|ConfigurationManagerException e) {
			ConnectionManager.closeDatabaseObjects(updateErrorTableStmnt, conn);
			throw new CaseNotesDaoException(e.getMessage(), e);
		}

		// iterate through the case notes for insertion into log_error table
		for(CaseNote2 erroredCaseNote : erroredCaseNotes) {
			try {
				if(updateErrorTableStmnt != null && conn != null) {
					updateErrorTableStmnt.setString(1,erroredCaseNote.getClientId());
					updateErrorTableStmnt.setInt(2,Integer.parseInt(erroredCaseNote.getInFromSaraId()));
					updateErrorTableStmnt.setString(3,erroredCaseNote.getError());
					updateErrorTableStmnt.executeUpdate();
					conn.commit();
				} else {
					// log the case note with the case_id and the case_note_id for the failed transaction
					logger.error(LOG_ERROR_TABLE_INSERTION_ERROR_MSG, erroredCaseNote.getCaseId(), erroredCaseNote.getCaseNoteId());
				}
	
			} catch (SQLException e) {
				logger.error(e.getMessage());
				// log the case note with the case_id and the case_note_id for the failed transaction
				logger.error(LOG_ERROR_TABLE_INSERTION_ERROR_MSG, erroredCaseNote.getCaseId(), erroredCaseNote.getCaseNoteId());
			}
		}

		// close the preparedstatement and the connection objects
		ConnectionManager.closeDatabaseObjects(updateErrorTableStmnt, conn);
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
		
		// establish connection to Oracle database
		try {
			conn = ConnectionManager.getConnection(false);
	        updateErrorCaseNoteTableStmnt = conn.prepareStatement(UPDATE_TBL_IN_FROM_SARA_ERROR_SQL);
		} catch (SQLException e) {
			logger.error(e.getMessage());
			ConnectionManager.closeDatabaseObjects(updateErrorCaseNoteTableStmnt, conn);
			throw new CaseNotesDaoException(e.getMessage(), e);
		} catch (ConnectionManagerException|ConfigurationManagerException e) {
			ConnectionManager.closeDatabaseObjects(updateErrorCaseNoteTableStmnt, conn);
			throw new CaseNotesDaoException(e.getMessage(), e);
		}

		// iterate through the case notes for update of the TBL_IN_FROM_SARA table
		for(CaseNote2 erroredCaseNote : erroredDbCaseNotes) {
			try {
				if(updateErrorCaseNoteTableStmnt != null && conn != null) {
					updateErrorCaseNoteTableStmnt.setInt(1, Integer.parseInt(erroredCaseNote.getInFromSaraId()));
					updateErrorCaseNoteTableStmnt.executeUpdate();
					conn.commit();
				} else {
					// log the case note with the case_id and the case_note_id for the failed transaction
					logger.error(TBL_IN_FROM_SARA_TABLE_UPDATE_ERROR_MSG, erroredCaseNote.getCaseId(), erroredCaseNote.getCaseNoteId());
				}
	
			} catch (SQLException e) {
				logger.error(e.getMessage());
				// log the case note with the case_id and the case_note_id for the failed transaction
				logger.error(TBL_IN_FROM_SARA_TABLE_UPDATE_ERROR_MSG, erroredCaseNote.getCaseId(), erroredCaseNote.getCaseNoteId());
			}
		}

		// close the preparedstatement and the connection objects
		ConnectionManager.closeDatabaseObjects(updateErrorCaseNoteTableStmnt, conn);
	}

	/**
	 * This method is called for all case notes that do NOT have a db error of a
	 * NULL CASE_ID.
	 * 
	 * @param nonErroredCaseNotes
	 * @throws CaseNotesDaoException
	 */
	public void updateNonDbErroredCaseNotes(List<CaseNote2> nonErroredCaseNotes) throws CaseNotesDaoException {
		PreparedStatement updateCaseNoteTableStmnt = null;
		Connection conn = null;
		
		// establish connection to Oracle database
		try {
			conn = ConnectionManager.getConnection(false);
	        updateCaseNoteTableStmnt = conn.prepareStatement(UPDATE_TBL_IN_FROM_SARA_SUCCESS_SQL);
		} catch (SQLException e) {
			logger.error(e.getMessage());
			ConnectionManager.closeDatabaseObjects(updateCaseNoteTableStmnt, conn);
			throw new CaseNotesDaoException(e.getMessage(), e);
		} catch (ConnectionManagerException|ConfigurationManagerException e) {
			ConnectionManager.closeDatabaseObjects(updateCaseNoteTableStmnt, conn);
			throw new CaseNotesDaoException(e.getMessage(), e);
		}

		// iterate through the case notes for update of the TBL_IN_FROM_SARA table
		for(CaseNote2 nonErroredCaseNote : nonErroredCaseNotes) {
			try {
				if(updateCaseNoteTableStmnt != null && conn != null) {
					String additional10Col = nonErroredCaseNote.isUpdate()?"U":"I";
					updateCaseNoteTableStmnt.setString(1, additional10Col);
					updateCaseNoteTableStmnt.setInt(2, Integer.parseInt(nonErroredCaseNote.getInFromSaraId()));
					updateCaseNoteTableStmnt.executeUpdate();
					conn.commit();
				} else {
					// log the case note with the case_id and the case_note_id for the failed transaction
					logger.error(TBL_IN_FROM_SARA_TABLE_UPDATE_ERROR_MSG, nonErroredCaseNote.getCaseId(), nonErroredCaseNote.getCaseNoteId());
				}
	
			} catch (SQLException e) {
				logger.error(e.getMessage());
				// log the case note with the case_id and the case_note_id for the failed transaction
				logger.error(TBL_IN_FROM_SARA_TABLE_UPDATE_ERROR_MSG, nonErroredCaseNote.getCaseId(), nonErroredCaseNote.getCaseNoteId());
			} 
		}

		// close the preparedstatement and the connection objects
		ConnectionManager.closeDatabaseObjects(updateCaseNoteTableStmnt, conn);
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
		
		// establish connection to Oracle database
		try {
			conn = ConnectionManager.getConnection(false);
	        insertSaraCorpDbXrefTableStmnt = conn.prepareStatement(INSERT_SARA_CORPDB_XREF_SQL);
		} catch (SQLException e) {
			ConnectionManager.closeDatabaseObjects(insertSaraCorpDbXrefTableStmnt, conn);
			logger.error(e.getMessage());
			throw new CaseNotesDaoException(e.getMessage(), e);
		} catch (ConnectionManagerException|ConfigurationManagerException e) {
			ConnectionManager.closeDatabaseObjects(insertSaraCorpDbXrefTableStmnt, conn);
			throw new CaseNotesDaoException(e.getMessage(), e);
		}

		// iterate through the case notes for insertion into SARA_CORPDB_XREF table
		for(CaseNote2 newCaseNote : newCaseNotes) {
			try {
				if(insertSaraCorpDbXrefTableStmnt != null && conn != null) {
					insertSaraCorpDbXrefTableStmnt.setInt(1,Integer.parseInt(newCaseNote.getCaseNoteId()));
					insertSaraCorpDbXrefTableStmnt.setInt(2,Integer.parseInt(newCaseNote.getCaseDocumentId()));
					insertSaraCorpDbXrefTableStmnt.executeUpdate();
					conn.commit();
				} else {
					// log the case note with the case_id and the case_note_id for the failed transaction
					logger.error(SARA_CORPDB_XREF_TABLE_INSERTION_ERROR_MSG, newCaseNote.getCaseId(), newCaseNote.getCaseNoteId());
				}
	
			} catch (SQLException e) {
				logger.error(e.getMessage());
				// log the case note with the case_id and the case_note_id for the failed transaction
				logger.error(SARA_CORPDB_XREF_TABLE_INSERTION_ERROR_MSG, newCaseNote.getCaseId(), newCaseNote.getCaseNoteId());
			}
		}

		// close the preparedstatement and the connection objects
		ConnectionManager.closeDatabaseObjects(insertSaraCorpDbXrefTableStmnt, conn);
	}

}
