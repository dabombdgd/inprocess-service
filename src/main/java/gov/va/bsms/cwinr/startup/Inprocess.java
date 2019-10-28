package gov.va.bsms.cwinr.startup;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.va.bsms.cwinr.exceptions.CaseNotesDaoException;
import gov.va.bsms.cwinr.exceptions.ConfigurationManagerException;
import gov.va.bsms.cwinr.model.CaseNoteAggregator;
import gov.va.bsms.cwinr.model.CaseNoteDao;
import gov.va.bsms.cwinr.soap.SoapDao;
import gov.va.bsms.cwinr.utils.ConfigurationManager;

public class Inprocess {
	private static Logger logger = LoggerFactory.getLogger(Inprocess.class);

	public static void main(String[] args) {		
		// initialize start time
        long lStartTime = System.nanoTime();
        
		String version = "";
		try {
			version = ConfigurationManager.INSTANCE.getResources().getString("version");
		} catch (ConfigurationManagerException e1) {
			logger.error("Startup of application failed due to no configuration file found.");
			System.exit(1);
		}

		logger.info("Startup of application v.{}\n", version);
		
		// create the case note DAO to interact with the AWS Oracle Cloud instance
		CaseNoteDao cnDao = new CaseNoteDao();
		
		// generate the List of CaseNotes for processing
		CaseNoteAggregator caseNoteAggregator = new CaseNoteAggregator();
		logger.info("Retrieved {} case notes for processing.\n", caseNoteAggregator.getCaseNotesForProcessing().size());
		
		// log all of the CaseNotes with DB errors to the log_error table
		try {
			cnDao.insertErrorCaseNotes(caseNoteAggregator.getCaseNotesWithDbError());
		} catch (CaseNotesDaoException e) {
			logger.error(e.getMessage());
		}
		logger.info("Logged {} case notes with a null ID.\n", caseNoteAggregator.getCaseNotesWithDbError().size());
		
		// update CaseNotes with DB errors in the TBL_IN_FROM_SARA table
		try {
			cnDao.updateErroredCaseNote(caseNoteAggregator.getCaseNotesWithDbError());
		} catch (CaseNotesDaoException e) {
			logger.error(e.getMessage());
		}
		logger.info("Updated {} case notes that had a null ID.\n", caseNoteAggregator.getCaseNotesWithDbError().size());
		
		// call the SOAP client DAO?
		// ------------------------------------------------------
		SoapDao soapDao = new SoapDao();
		soapDao.processCaseNotesWithBgsServiceMvp(caseNoteAggregator.getCaseNotesForProcessing());
		// ------------------------------------------------------

		// log all of the CaseNotes with SOAP errors to the log_error table
		try {
			cnDao.insertErrorCaseNotes(caseNoteAggregator.getCaseNoteswithBgsError());
		} catch (CaseNotesDaoException e) {
			logger.error(e.getMessage());
		}
		logger.info("Logged {} case notes with a service processing error.\n", caseNoteAggregator.getCaseNoteswithBgsError().size());
		
		// update CaseNotes with non DB errors in the TBL_IN_FROM_SARA table
		try {
			cnDao.updateNonDbErroredCaseNotes(caseNoteAggregator.getCaseNotesWithNonDbError());
		} catch (CaseNotesDaoException e) {
			logger.error(e.getMessage());
		}
		logger.info("Updated {} case notes that have an ID.\n", caseNoteAggregator.getCaseNotesWithNonDbError().size());
		
		// insert new CaseNotes in the SARA_CORPDB_XREF table
		try {
			cnDao.insertNewCaseNotes(caseNoteAggregator.getNewCaseNotes());
		} catch (CaseNotesDaoException e) {
			logger.error(e.getMessage());
		}
		logger.info("Inserted {} new case notes.\n", caseNoteAggregator.getNewCaseNotes().size());
		
		// end time
        long lEndTime = System.nanoTime();
		//time elapsed
        long output = lEndTime - lStartTime;
		logger.info("Total processing time was {} milliseconds for {} case notes.\n", (output / 1000000), caseNoteAggregator.getCaseNotesForProcessing().size());

		try {
			logger.info("Shutdown of JAVA Application Version {}", ConfigurationManager.INSTANCE.getResources().getString("version"));
		} catch (ConfigurationManagerException e) {
			logger.info("Shutdown of application failed due to no configuration file found.");
			System.exit(1);
		}
	}

}
