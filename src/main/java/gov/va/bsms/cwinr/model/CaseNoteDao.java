package gov.va.bsms.cwinr.model;

import java.util.ArrayList;
import java.util.List;

public class CaseNoteDao {
	
	public List<CaseNote2> getCaseNotesForProcessing() {
		//:TODO query for casenotes
		return new ArrayList<CaseNote2> ();
	}
	
	public void insertErrorCaseNotes(List<CaseNote2> erroredCaseNotes) {
		//:TODO error logging for a casenote 
	}
	
	public void updateErroredCaseNote(List<CaseNote2> erroredCaseNotes) {
		//:TODO update errored casenote
	}
	
	public void updateNonErroredCaseNotes(List<CaseNote2> nonErroredCaseNotes) { 
		//:TODO update successful casenote
	}
	
	public void insertNewCaseNotes(List<CaseNote2> newCaseNotes) {
		//:TODO insert casenote xref
	}

}
