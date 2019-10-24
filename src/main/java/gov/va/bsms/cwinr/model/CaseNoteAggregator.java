package gov.va.bsms.cwinr.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.cxf.common.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.va.bsms.cwinr.exceptions.CaseNotesDaoException;

public class CaseNoteAggregator {
	private static Logger logger = LoggerFactory.getLogger(CaseNoteAggregator.class);

	private List<CaseNote2> caseNotesForProcessing;

	/**
	 * The constructor calls the {@link CaseNoteDao} object to create the
	 * {@link List}<{@link CaseNote2}>.
	 */
	public CaseNoteAggregator() {
		CaseNoteDao cnDao = new CaseNoteDao();
		try {
			setCaseNotesForProcessing(cnDao.getCaseNotesForProcessing());
		} catch (CaseNotesDaoException e) {
			logger.error(e.getMessage());
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
	 * @return Returns a {@link List}<{@link CaseNote2}> that have the caseId as a
	 *         not null value.
	 */
	public List<CaseNote2> getCaseNotesWithNonDbError() {
		List<CaseNote2> returnVal = new ArrayList<>();

		for (CaseNote2 caseNoteForProcessing : this.caseNotesForProcessing) {
			if (!caseNoteForProcessing.isWithDBError()) {
				returnVal.add(caseNoteForProcessing);
			}
		}

		return returnVal;
	}

	/**
	 * 
	 * @return Returns a {@link List}<{@link CaseNote2}> that have the caseId as a
	 *         null value.
	 */
	public List<CaseNote2> getCaseNotesWithDbError() {
		List<CaseNote2> returnVal = new ArrayList<>();

		for (CaseNote2 caseNoteForProcessing : this.caseNotesForProcessing) {
			if (caseNoteForProcessing.isWithDBError()) {
				returnVal.add(caseNoteForProcessing);
			}
		}

		return returnVal;
	}

	/**
	 * 
	 * @return Returns a {@link List}<{@link CaseNote2}> that have bad BGS SOAP
	 *         service reponse data.
	 */
	public List<CaseNote2> getCaseNoteswithBgsError() {
		List<CaseNote2> returnVal = new ArrayList<>();

		for (CaseNote2 caseNoteForProcessing : this.caseNotesForProcessing) {
			if (caseNoteForProcessing.isWithBGSError()) {
				returnVal.add(caseNoteForProcessing);
			}
		}

		return returnVal;
	}

	/**
	 * 
	 * @return Returns a {@link List}<{@link CaseNote2}> that are INSERTS into the
	 *         BGS SOAP service.
	 */
	public List<CaseNote2> getNewCaseNotes() {
		List<CaseNote2> returnVal = new ArrayList<>();

		for (CaseNote2 caseNoteForProcessing : this.caseNotesForProcessing) {
			// case note is not an update and case not id exists and case note document id exists
			if (!caseNoteForProcessing.isUpdate() && !StringUtils.isEmpty(caseNoteForProcessing.getCaseNoteId())
					&& !StringUtils.isEmpty(caseNoteForProcessing.getCaseDocumentId())) {
				returnVal.add(caseNoteForProcessing);
			}
		}

		return returnVal;
	}

}
