package gov.va.bsms.cwinr.startup;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.va.bsms.cwinr.exceptions.CaseNotesDaoException;
import gov.va.bsms.cwinr.model.CaseNoteAggregator;
import gov.va.bsms.cwinr.model.CaseNoteDao;
import gov.va.bsms.cwinr.utils.ConfigurationManager;

public class Inprocess {
	private static Logger logger = LoggerFactory.getLogger(Inprocess.class);

	public static void main(String[] args) {
		logger.info("Startup of application v.{}", ConfigurationManager.INSTANCE.getResources().getString("version"));
		
		// initialize start time
        long lStartTime = System.nanoTime();
		
		// create the case note DAO to interact with the AWS Oracle Cloud instance
		CaseNoteDao cnDao = new CaseNoteDao();
		
		// generate the List of CaseNotes for processing
		CaseNoteAggregator caseNoteAggregator = new CaseNoteAggregator();
		logger.info("Retrieved {} case notes for processing.", caseNoteAggregator.getCaseNotesForProcessing().size());
		
		// log all of the CaseNotes with DB errors to the log_error table
		try {
			cnDao.insertErrorCaseNotes(caseNoteAggregator.getCaseNotesWithDbError());
		} catch (CaseNotesDaoException e) {
			logger.error(e.getMessage());
		}
		logger.info("Logged {} case notes with a null ID.", caseNoteAggregator.getCaseNotesWithDbError().size());
		
		// call the SOAP client DAO?
		// ------------------------------------------------------
		//:TODO create the BGS SOAP Service DAO processing call
		// ------------------------------------------------------

		// log all of the CaseNotes with SOAP errors to the log_error table
		try {
			cnDao.insertErrorCaseNotes(caseNoteAggregator.getCaseNoteswithBgsError());
		} catch (CaseNotesDaoException e) {
			logger.error(e.getMessage());
		}
		logger.info("Logged {} case notes with a service processing error.", caseNoteAggregator.getCaseNoteswithBgsError().size());
		
		// update CaseNotes with DB errors in the TBL_IN_FROM_SARA table
		try {
			cnDao.updateErroredCaseNote(caseNoteAggregator.getCaseNotesWithDbError());
		} catch (CaseNotesDaoException e) {
			logger.error(e.getMessage());
		}
		logger.info("Updated {} case notes that had a null ID.", caseNoteAggregator.getCaseNotesWithDbError().size());
		
		// update CaseNotes with non DB errors in the TBL_IN_FROM_SARA table
		try {
			cnDao.updateNonDbErroredCaseNotes(caseNoteAggregator.getCaseNotesWithNonDbError());
		} catch (CaseNotesDaoException e) {
			logger.error(e.getMessage());
		}
		logger.info("Updated {} case notes that have an ID.", caseNoteAggregator.getCaseNotesWithNonDbError().size());
		
		// insert new CaseNotes in the SARA_CORPDB_XREF table
		try {
			cnDao.insertNewCaseNotes(caseNoteAggregator.getNewCaseNotes());
		} catch (CaseNotesDaoException e) {
			logger.error(e.getMessage());
		}
		logger.info("Inserted {} new case notes.", caseNoteAggregator.getNewCaseNotes().size());
		
		// end time
        long lEndTime = System.nanoTime();
		//time elapsed
        long output = lEndTime - lStartTime;
		logger.info("Total processing time was {} milliseconds for {} case notes.", (output / 1000000), caseNoteAggregator.getCaseNotesForProcessing().size());

		logger.info("Shutdown of JAVA Application Version {}", ConfigurationManager.INSTANCE.getResources().getString("version"));
	}

}
