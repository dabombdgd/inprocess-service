package gov.va.bsms.cwinr.startup;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.va.bsms.cwinr.exceptions.CaseNotesDaoException;
import gov.va.bsms.cwinr.model.CaseNoteAggregator;

public class Inprocess {
	private static Logger LOGGER = LoggerFactory.getLogger(Inprocess.class);

	public static void main(String[] args) {
		LOGGER.info("Hello AWS UNIX VM !!");
		
		CaseNoteAggregator caseNoteAggregator = new CaseNoteAggregator();
		
	}

}
