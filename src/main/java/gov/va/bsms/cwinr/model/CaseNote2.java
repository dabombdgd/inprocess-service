package gov.va.bsms.cwinr.model;

import org.apache.cxf.common.util.StringUtils;

public class CaseNote2 {

	private String inFromSaraId;
	private String clientId;
	private String caseNoteDate;
	private String caseNote;
	private String caseNoteId;
	private String processStatus;
	private String caseId;
	private String benefitClaimNoyeTypeCd;
	private String additional3;
	private String caseDocumentId;
	private String nvlCD;

	private boolean update = false;
	private boolean withDBError = false;
	private boolean withBGSError = false;

	public String getInFromSaraId() {
		return inFromSaraId;
	}

	public void setInFromSaraId(String inFromSaraId) {
		this.inFromSaraId = inFromSaraId;
	}

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public String getCaseNoteDate() {
		return caseNoteDate;
	}

	public void setCaseNoteDate(String caseNoteDate) {
		this.caseNoteDate = caseNoteDate;
	}

	public String getCaseNote() {
		return caseNote;
	}

	public void setCaseNote(String caseNote) {
		this.caseNote = caseNote;
	}

	public String getCaseNoteId() {
		return caseNoteId;
	}

	public void setCaseNoteId(String caseNoteId) {
		this.caseNoteId = caseNoteId;
	}

	public String getProcessStatus() {
		return processStatus;
	}

	public void setProcessStatus(String processStatus) {
		this.processStatus = processStatus;
	}

	public String getCaseId() {
		return caseId;
	}

	public void setCaseId(String caseId) {
		this.caseId = caseId;

		// if the case_id is null, then set withdberror to true, so that it will not be
		// processed by the BGS SOAP service call.
		if (StringUtils.isEmpty(caseId)) {
			setWithDBError(true);
		}
	}

	public String getBenefitClaimNoyeTypeCd() {
		return benefitClaimNoyeTypeCd;
	}

	public void setBenefitClaimNoyeTypeCd(String benefitClaimNoyeTypeCd) {
		this.benefitClaimNoyeTypeCd = benefitClaimNoyeTypeCd;
	}

	public String getAdditional3() {
		return additional3;
	}

	public void setAdditional3(String additional3) {
		this.additional3 = additional3;
	}

	public String getCaseDocumentId() {
		return caseDocumentId;
	}

	public void setCaseDocumentId(String caseDocumentId) {
		this.caseDocumentId = caseDocumentId;
	}

	public String getNvlCD() {
		return nvlCD;
	}

	public void setNvlCD(String nvlCD) {
		this.nvlCD = nvlCD;
	}

	public boolean isWithDBError() {
		return withDBError;
	}

	public void setWithDBError(boolean withDBError) {
		this.withDBError = withDBError;
	}

	public boolean isWithBGSError() {
		return withBGSError;
	}

	public void setWithBGSError(boolean withBGSError) {
		this.withBGSError = withBGSError;
	}

	public boolean isUpdate() {
		return update;
	}

	public void setUpdate(boolean update) {
		this.update = update;
	}

}
