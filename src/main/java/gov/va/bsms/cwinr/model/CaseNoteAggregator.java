package gov.va.bsms.cwinr.model;

import java.util.ArrayList;
import java.util.List;

public class CaseNoteAggregator {
	
	private List<CaseNote2> caseNotesForProcessing;
	
	/**
	 * The constructor calls the {@link CaseNoteDao} object to create the {@link List}<{@link CaseNote2}>.
	 */
	public CaseNoteAggregator() {
		CaseNoteDao cnDao = new CaseNoteDao();
		setCaseNotes(cnDao.getCaseNotesForProcessing());
	}
	
	private void setCaseNotes(List<CaseNote2> caseNotesNew) {
		this.caseNotesForProcessing = caseNotesNew;
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
