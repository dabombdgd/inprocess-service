package gov.va.bsms.cwinr.utils;

import org.apache.cxf.common.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.va.bsms.cwinr.model.CaseNoteDao;

public final class DaoUtils {
	private static Logger logger = LoggerFactory.getLogger(CaseNoteDao.class);
	
	private DaoUtils() {
		// do nothing - private constructor
	}

	/**
	 * Logging parameterized log message.
	 * 
	 * @param paramLogMessage
	 * @param caseId
	 * @param caseNoteId
	 */
	public static final void logDatabaseErrorParamMessage(String paramLogMessage, String caseId, String caseNoteId) {
		String param1 = "";
		String param2 = "";
		
		if(!StringUtils.isEmpty(caseId)) {
			param1 = caseId;
		}
		
		if(!StringUtils.isEmpty(caseNoteId)) {
			param2 = caseNoteId;
		}
		
		logger.error(paramLogMessage, param1, param2);
	}

}
