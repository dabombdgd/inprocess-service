package gov.va.bsms.cwinr.eva;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Case note record This design uses all Strings for all Case note fields,
 * because in SOAP (and log files) the CaseNote is a caseDcmntDTO, which is all
 * Strings.
 */

// Case note caseDcmntDTO record
class CaseNote {
	static Logger LOGGER = LoggerFactory.getLogger(CaseNote.class);
	// All data fields are strings, as shown in XML and LOG files:
	String caseDcmntId;
	String bnftClaimNoteTypeCd;
	String caseId;
	String modifdDt;
	String dcmntTxt;
	// These are response fields
	String result;
	String error;

	CaseNote(String caseDcmntId, String bnftClaimNoteTypeCd, String caseId, String modifdDt, String dcmntTxt) {
		this.caseDcmntId = caseDcmntId;
		this.bnftClaimNoteTypeCd = bnftClaimNoteTypeCd;
		this.caseId = caseId;
		this.modifdDt = modifdDt;
		this.dcmntTxt = dcmntTxt;
		this.result = null;
		this.error = null;
	}

	String clean(String s, int length) {
		if (s == null)
			s = "";
		if (s.length() > length)
			s = s.substring(0, length); // trim
		s = s.replaceAll("\n", " ");
		s = String.format("%-" + length + "s", s); // pad
		LOGGER.debug("Clean string: {}", s);
		return s;
	}

	public String toString() {
		String e = (this.error == null) ? "PASS " : "FAIL " + this.error + " ";
		return (e + "CaseNote " + this.caseDcmntId + " " + this.bnftClaimNoteTypeCd + " " + this.caseId + " "
				+ this.modifdDt + " " + clean(this.dcmntTxt, 80) + " " // 80 or log-dcmntTxt-length
		);
	}

	public void setError(String str) {
		LOGGER.error("Error string: {}", str);
		this.error = (this.error == null) ? str : (str + " " + this.error);
	}

	public String getResultTag(String tag) {
		try {
			String s = this.result.split("<" + tag + ">")[1];
			return s.split("</" + tag + ">")[0];
		} catch (Exception e) {
			this.setError("SOAP Fault Exception missing tag " + tag + " in result");
			LOGGER.error("Result Tag Error: {}", tag);
			return "";
		}
	}

	private String xml_escape(String s) {
		s = s.replaceAll("&", "&amp;");
		s = s.replaceAll("<", "&lt;");
		s = s.replaceAll(">", "&gt;");
		s = s.replaceAll("'", "&apos;");
		s = s.replaceAll("\"", "&quot;");
		LOGGER.debug("Escaped XML: {}", s);
		return s;
	}

	private String tag(String tag, String value) {
		LOGGER.debug("SOAP tag: {}", tag);
		return ("\n<" + tag + ">" + xml_escape(value) + "</" + tag + ">");
	}

	String toCaseDcmntDTO() { // Note JAXB Marshaller
		String xml = "<CaseDcmntDTO>";
		if (isValid(this.caseDcmntId))
			xml += tag("caseDcmntId", this.caseDcmntId);
		xml += tag("bnftClaimNoteTypeCd", this.bnftClaimNoteTypeCd);
		xml += tag("caseId", this.caseId);
		xml += tag("modifdDt", this.modifdDt);
		xml += tag("dcmntTxt", this.dcmntTxt);
		xml += "\n</CaseDcmntDTO>";
		LOGGER.debug("Caee Note SOAP Request: {}", xml);
		return xml;
	}

	private boolean isValid(String s) {
		return ((s != null) & (s.length() > 0));
	}

	private void valid(String f, String v) {
		if (!isValid(v))
			this.setError("Exception invalid " + f + " value");
	}

	private String now() {
		DateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss");
		return dateFormat.format(new Date());
	}

	public void validate() {
		valid("bnftClaimNoteTypeCd", this.bnftClaimNoteTypeCd);
		valid("caseId", this.caseId);
		valid("modifdDt", this.modifdDt);
		valid("dcmntTxt", this.dcmntTxt);
		if (!isValid(this.modifdDt))
			this.modifdDt = now();
	}
}