package gov.va.bsms.cwinr.soap;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.cxf.common.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.va.bsms.cwinr.exceptions.ConfigurationManagerException;
import gov.va.bsms.cwinr.exceptions.SoapClientException;
import gov.va.bsms.cwinr.model.CaseNote2;
import gov.va.bsms.cwinr.utils.ConfigurationManager;

/*  BGS SOAP client interface */

public class SoapClient {
	private static final String PRE_BGS_SOAP_SERVICE_ERROR = "PRE BGS SOAP Service Error";

	private static final String ERROR_WITH_SOAP_SERVICE = "Error with SOAP service.";

	private static final String ERROR_WITH_CONFIGURATION_MANAGER = "Error with Configuration Manager.";

	private static final String BGS_SOAP_SERVICE_INVALID_STATUS = "BGS SOAP Service invalid status: {}";

	private static Logger logger = LoggerFactory.getLogger(SoapClient.class);

	private static final String TEMPLATE_XML_FILE_NAME = "template.xml";
	
	private boolean bgsMock = false;

	public SoapClient() {
		// determine if the JAVA app will use the live BGS SOAP service or the mock response
		updateBgsMock();
	}

	private void sendToBGS(CaseNote2 note, String request) throws SoapClientException {
		String bgsUrl = "";
		URL url = null;
		HttpURLConnection con = null;
		DataOutputStream wr = null;

		try {
			bgsUrl = ConfigurationManager.INSTANCE.getResources().getString("bgs-url");
		} catch (ConfigurationManagerException e) {
			note.setError(PRE_BGS_SOAP_SERVICE_ERROR + e.toString());
			throw new SoapClientException(ERROR_WITH_CONFIGURATION_MANAGER, e);
		}

		try {
			url = new URL(bgsUrl);
			con = (HttpURLConnection) url.openConnection();
			con.setRequestMethod("POST");
			con.setRequestProperty("Content-Type", "text/xml; charset=utf-8");
			con.setDoOutput(true);
			wr = new DataOutputStream(con.getOutputStream());
			wr.writeBytes(request);
		} catch (MalformedURLException e) {
			note.setError(PRE_BGS_SOAP_SERVICE_ERROR + e.toString());
			throw new SoapClientException(ERROR_WITH_SOAP_SERVICE, e);
		} catch (IOException e) {
			note.setError(PRE_BGS_SOAP_SERVICE_ERROR + e.toString());
			throw new SoapClientException(ERROR_WITH_SOAP_SERVICE, e);
		} finally {
			if (wr != null) {
				try {
					wr.flush();
				} catch (IOException e) {
					logger.warn("DataOutputStream did not close properly.");
				}
				try {
					wr.close();
				} catch (IOException e) {
					logger.warn("DataOutputStream did not close properly.");
				}
			}
		}

		String responseStatus = "";

		try {
			// "OK" or not
			responseStatus = con.getResponseMessage();
		} catch (IOException e) {
			note.setError(PRE_BGS_SOAP_SERVICE_ERROR + e.toString());
			throw new SoapClientException(ERROR_WITH_SOAP_SERVICE, e);
		}

		if (!responseStatus.equalsIgnoreCase("OK")) {
			logger.warn(BGS_SOAP_SERVICE_INVALID_STATUS, responseStatus);
			note.setError(String.format(BGS_SOAP_SERVICE_INVALID_STATUS, responseStatus));
		}

		BufferedReader in = null;
		StringBuilder response = null;

		try {
			in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			response = new StringBuilder();
			String inputLine;
			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
		} catch (IOException e) {
			note.setError(PRE_BGS_SOAP_SERVICE_ERROR + e.toString());
			throw new SoapClientException(ERROR_WITH_SOAP_SERVICE, e);
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					logger.warn("BufferedReader did not close properly.");
				}
			}
		}

		// set the SOAP result from BGS
		if (response != null) {
			note.setSoapResult(response.toString());
		}
	}

	private void sendToBGSMock(CaseNote2 note, String request) throws SoapClientException {
		String response = "<S:Envelope xmlns:S=\"http://schemas.xmlsoap.org/soap/envelope/\">\r\n" + 
				"	<S:Body>\r\n" + 
				"		<ns2:updateCaseDcmntResponse xmlns:ns2=\"http://cases.services.vetsnet.vba.va.gov/\">\r\n" + 
				"			<CaseDcmntDTO>\r\n" + 
				"				<bnftClaimNoteTypeCd>001</bnftClaimNoteTypeCd> \r\n" + 
				"				<caseDcmntId>3612</caseDcmntId> \r\n" + 
				"				<caseId>65858</caseId> \r\n" + 
				"				<dcmntTxt>inserting a note</dcmntTxt> \r\n" + 
				"				<jrnDt>2019-07-11T15:21:12-05:00</jrnDt> \r\n" + 
				"				<jrnLctnId>281</jrnLctnId> \r\n" + 
				"				<jrnObjId>VBMS</jrnObjId> \r\n" + 
				"				<jrnStatusTypeCd>I</jrnStatusTypeCd> \r\n" + 
				"				<jrnUserId>281CEASL</jrnUserId> \r\n" + 
				"				<modifdDt>2019-07-11T15:21:12-05:00</modifdDt> \r\n" + 
				"			</CaseDcmntDTO>\r\n" + 
				"		</ns2:updateCaseDcmntResponse>\r\n" + 
				"	</S:Body>\r\n" + 
				"</S:Envelope>";
		

		// set the SOAP result from BGS
		if (response != null) {
			note.setSoapResult(response.toString());
		}
	}

	private String readFile(String fn) throws IOException {
		Scanner in = new Scanner(new File(fn));
		in.useDelimiter("\\Z");
		String text = in.next();
		in.close();
		return text;
	}

	public void sendCaseNote(CaseNote2 note) throws SoapClientException {
		validate(note);

		try {
			String template = readFile(TEMPLATE_XML_FILE_NAME);
			String request = template.replace("<CaseDcmntDTO />", toCaseDcmntDTO(note));

			if(logger.isDebugEnabled()) {
				logger.debug(request);
			}

			if(isBgsMock()) {
				sendToBGSMock(note, request);
				logger.warn("---------- MOCK BGS SOAP CALL! -----------");
			} else {
				sendToBGS(note, request);
			}

			// Status for Update and Insert
			String status = getResultTag(note, "jrnStatusTypeCd").toLowerCase();
			if ("i".equalsIgnoreCase(status)) {
				String soapCaseDocumentId = getResultTag(note, "caseDcmntId");
				if (!StringUtils.isEmpty(soapCaseDocumentId)) {
					note.setCaseDocumentId(soapCaseDocumentId);
				}
			} else if (!"u".equalsIgnoreCase(status)) {
				// else set SOAP error
				note.setError("PRE BGS SOAP Service status error: " + status + ".");
			}
		} catch (IOException e) {
			note.setError(PRE_BGS_SOAP_SERVICE_ERROR + e.toString());
			logger.error(e.getMessage());
		}
	}

	/**
	 * THESE ARE METHODS FROM CaseNote obj
	 * ---------------------------------------------
	 */

	private String xml_escape(String s) {
		if (!StringUtils.isEmpty(s)) {
			s = StringEscapeUtils.escapeXml(s);
		} else {
			s = "No data available.";
		}
		if(logger.isDebugEnabled()) {
			logger.debug("Escaped XML: {}", s);
		}
		return s;
	}

	public String getResultTag(CaseNote2 casenote, String tag) {
		String returnVal = "";

		// if casenote.getSoapResult() is not empty or null
		if (!StringUtils.isEmpty(casenote.getSoapResult())) {
			String s = casenote.getSoapResult().split("<" + tag + ">")[1];
			// if String s is not empty or null
			if (!StringUtils.isEmpty(s)) {
				returnVal = s.split("</" + tag + ">")[0];
			}
		}

		return returnVal;
	}

	private String tag(String tag, String value) {
		if(logger.isDebugEnabled()) {
			logger.debug("SOAP tag: {}", tag);
		}
		return ("\n<" + tag + ">" + xml_escape(value) + "</" + tag + ">");
	}

	private String toCaseDcmntDTO(CaseNote2 caseNote) { // Note JAXB Marshaller
		String xml = "<CaseDcmntDTO>";
		if (isValid(caseNote.getCaseDocumentId()))
			xml += tag("caseDcmntId", caseNote.getCaseDocumentId());
		xml += tag("caseId", caseNote.getCaseId());
		xml += tag("dcmntTxt", caseNote.getCaseNote());
		xml += "\n</CaseDcmntDTO>";

		return xml;
	}

	public void validate(CaseNote2 caseNote) {
		valid(caseNote, "bnftClaimNoteTypeCd", caseNote.getBenefitClaimNoteTypeCd());
		valid(caseNote, "caseId", caseNote.getCaseId());
		valid(caseNote, "dcmntTxt", caseNote.getCaseNote());
	}

	private boolean isValid(String s) {
		return (!StringUtils.isEmpty(s));
	}

	private void valid(CaseNote2 caseNote, String f, String v) {
		if (!isValid(v))
			caseNote.setError("PRE BGS SOAP Exception invalid " + f + " value");
	}

	private boolean isBgsMock() {
		return this.bgsMock;
	}

	private void setBgsMock(boolean bgsMock) {
		this.bgsMock = bgsMock;
	}
	
	private void updateBgsMock() {
		try {
			setBgsMock(Boolean.parseBoolean(ConfigurationManager.INSTANCE.getResources().getString("bgs-mock")));
		} catch (ConfigurationManagerException e) {
			setBgsMock(false);
		}
	}

}