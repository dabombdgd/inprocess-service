package gov.va.bsms.cwinr.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.va.bsms.cwinr.exceptions.CaseNotesDaoException;

public class CaseNoteAggregator {
	private static Logger LOGGER = LoggerFactory.getLogger(CaseNoteAggregator.class);
	
	private List<CaseNote2> caseNotesForProcessing;
	
	/**
	 * The constructor calls the {@link CaseNoteDao} object to create the {@link List}<{@link CaseNote2}>.
	 */
	public CaseNoteAggregator() {
		CaseNoteDao cnDao = new CaseNoteDao();
		try {
			setCaseNotesForProcessing(cnDao.getCaseNotesForProcessing());
		} catch (CaseNotesDaoException e) {
			LOGGER.error(e.getMessage());
		} finally {
			this.caseNotesForProcessing = Collections.<CaseNote2>emptyList();
		}
	}
	
	public List<CaseNote2> getCaseNotesForProcessing() {
		return caseNotesForProcessing;
	}

	public void setCaseNotesForProcessing(List<CaseNote2> caseNotesForProcessing) {
		this.caseNotesForProcessing = caseNotesForProcessing;
	}

	/**
	 * 
	 * @return Returns a {@link List}<{@link CaseNote2}> that have the caseId as a not null value.
	 */
	public List<CaseNote2> getCaseNotesWithNonDbError() {
		List<CaseNote2> returnVal = new ArrayList<CaseNote2>();
		
		for(CaseNote2 caseNoteForProcessing : this.caseNotesForProcessing) {
			if(!caseNoteForProcessing.isWithDBError()) {
				returnVal.add(caseNoteForProcessing);
			}
		}
		
		return returnVal;
	}

	/**
	 * 
	 * @return Returns a {@link List}<{@link CaseNote2}> that have the caseId as a null value.
	 */
	public List<CaseNote2> getCaseNotesWithDbError() {
		List<CaseNote2> returnVal = new ArrayList<CaseNote2>();
		
		for(CaseNote2 caseNoteForProcessing : this.caseNotesForProcessing) {
			if(caseNoteForProcessing.isWithDBError()) {
				returnVal.add(caseNoteForProcessing);
			}
		}
		
		return returnVal;
	}

	/** 
	 * 
	 * @return Returns a {@link List}<{@link CaseNote2}> that have bad BGS SOAP service reponse data.
	 */
	public List<CaseNote2> getCaseNoteswithBgsError() {
		List<CaseNote2> returnVal = new ArrayList<CaseNote2>();
		
		for(CaseNote2 caseNoteForProcessing : this.caseNotesForProcessing) {
			if(caseNoteForProcessing.isWithBGSError()) {
				returnVal.add(caseNoteForProcessing);
			}
		}
		
		return returnVal;
	}

	/**
	 * 
	 * @return Returns a {@link List}<{@link CaseNote2}> that are INSERTS into the BGS SOAP service.
	 */
	public List<CaseNote2> getNewCaseNotes() {
		List<CaseNote2> returnVal = new ArrayList<CaseNote2>();
		
		for(CaseNote2 caseNoteForProcessing : this.caseNotesForProcessing) {
			if(!caseNoteForProcessing.isUpdate()) {
				returnVal.add(caseNoteForProcessing);
			}
		}
		
		return returnVal;
	}

}