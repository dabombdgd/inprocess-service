package gov.va.bsms.cwinr.startup;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.va.bsms.cwinr.model.CaseNoteAggregator;
import gov.va.bsms.cwinr.utils.ConfigurationManager;

public class Inprocess {
	private static Logger LOGGER = LoggerFactory.getLogger(Inprocess.class);

	public static void main(String[] args) {
		LOGGER.info("Startup of Inprocess Standalone JAVA Application Version {}", ConfigurationManager.INSTANCE.getResources().getString("version"));
		
		CaseNoteAggregator caseNoteAggregator = new CaseNoteAggregator();

		LOGGER.info("Shutdown of Inprocess Standalone JAVA Application Version {}", ConfigurationManager.INSTANCE.getResources().getString("version"));
	}

}
