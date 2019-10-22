package gov.va.bsms.cwinr.startup;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.va.bsms.cwinr.model.CaseNoteAggregator;
import gov.va.bsms.cwinr.model.CaseNoteDao;
import gov.va.bsms.cwinr.utils.ConfigurationManager;

public class Inprocess {
	private static Logger LOGGER = LoggerFactory.getLogger(Inprocess.class);

	public static void main(String[] args) {
		LOGGER.info("Startup of Inprocess Standalone JAVA Application Version {}", ConfigurationManager.INSTANCE.getResources().getString("version"));
		
		// start time
        long lStartTime = System.nanoTime();
		
		// generate the List of CaseNotes for processing
		CaseNoteAggregator caseNoteAggregator = new CaseNoteAggregator();
		LOGGER.info("Retrieved {} case notes for processing.", caseNoteAggregator.getCaseNotesForProcessing().size());
		
		// call the SOAP client DAO?
		// ------------------------------------------------------
		//:TODO create the BGS SOAP Service DAO processing call
		// ------------------------------------------------------
		
		// create the case note DAO to interact with the AWS Oracle Cloud instance
		CaseNoteDao cnDao = new CaseNoteDao();
		
		// log all of the CaseNotes with DB and SOAP errors to the log_error table
		cnDao.insertErrorCaseNotes(caseNoteAggregator.getCaseNotesWithDbError());
		LOGGER.info("Logged {} case notes with a null CASE_ID.", caseNoteAggregator.getCaseNotesWithDbError().size());
		
		cnDao.insertErrorCaseNotes(caseNoteAggregator.getCaseNoteswithBgsError());
		LOGGER.info("Logged {} case notes with a BGS SOAP Service error.", caseNoteAggregator.getCaseNoteswithBgsError().size());
		
		// update CaseNotes with DB errors in the TBL_IN_FROM_SARA table
		cnDao.updateErroredCaseNote(caseNoteAggregator.getCaseNotesWithDbError());
		LOGGER.info("Updated the TBL_IN_FROM_SARA with {} case notes that had a null CASE_ID.", caseNoteAggregator.getCaseNotesWithDbError().size());
		
		// update CaseNotes with non DB errors in the TBL_IN_FROM_SARA table
		cnDao.updateNonDbErroredCaseNotes(caseNoteAggregator.getCaseNotesWithNonDbError());
		LOGGER.info("Updated the TBL_IN_FROM_SARA with {} case notes that had a not null CASE_ID.", caseNoteAggregator.getCaseNotesWithNonDbError().size());
		
		// insert new CaseNotes in the SARA_CORPDB_XREF table
		cnDao.insertNewCaseNotes(caseNoteAggregator.getNewCaseNotes());
		LOGGER.info("Inserted {} new case notes into the SARA_CORPDB_XREF table.", caseNoteAggregator.getNewCaseNotes().size());
		
		// end time
        long lEndTime = System.nanoTime();
		//time elapsed
        long output = lEndTime - lStartTime;
		LOGGER.info("Total processing time was {} milliseconds for {} case notes.", (output / 1000000), caseNoteAggregator.getCaseNotesForProcessing());

		LOGGER.info("Shutdown of Inprocess Standalone JAVA Application Version {}", ConfigurationManager.INSTANCE.getResources().getString("version"));
	}

}
