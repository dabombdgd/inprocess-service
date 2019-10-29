package gov.va.bsms.cwinr.soap;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

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

	private static final String BGS_SOAP_SERVICE_INVALID_STATUS = "BGS SOAP Service invalid status: ";

	static Logger logger = LoggerFactory.getLogger(SoapClient.class);
	
	private static final String update_fn = "template.xml";

	public SoapClient() {
		// do nothing for nothing
	}

	private void sendToBGS(CaseNote2 note, String request) throws SoapClientException {
		String bgsUrl= "";
		URL url = null;
		HttpURLConnection con = null;
		DataOutputStream wr = null;
		
		try {
			bgsUrl = ConfigurationManager.INSTANCE.getResources().getString("bgs-url2");
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
		} catch(MalformedURLException e) {
			note.setError(PRE_BGS_SOAP_SERVICE_ERROR + e.toString());
			throw new SoapClientException(ERROR_WITH_SOAP_SERVICE, e);
		} catch (IOException e) {
			note.setError(PRE_BGS_SOAP_SERVICE_ERROR + e.toString());
			throw new SoapClientException(ERROR_WITH_SOAP_SERVICE, e);
		} finally {
			if(wr != null) {
				try {
					wr.flush();
				} catch (IOException e) {
					// do nothing
				}
				try {
					wr.close();
				} catch (IOException e) {
					// do nothing
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
			logger.warn(BGS_SOAP_SERVICE_INVALID_STATUS + responseStatus); // Delete this line
			note.setError(BGS_SOAP_SERVICE_INVALID_STATUS + responseStatus);
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
			if(in != null) {
				try {
					in.close();
				} catch (IOException e) {
					// do nothing
				}
			}
		}
		
		// set the SOAP result from BGS
		if(response != null) {
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
			String template = readFile(update_fn);
			String request = template.replace("<CaseDcmntDTO />", toCaseDcmntDTO(note));
			
			logger.debug(request);

			sendToBGS(note, request);

			// Status for Update and Insert
			String status = getResultTag(note, "jrnStatusTypeCd").toLowerCase();
			if ("i".equalsIgnoreCase(status)) {
				String soapCaseDocumentId = getResultTag(note, "caseDcmntId");
				if(!StringUtils.isEmpty(soapCaseDocumentId)) {
					note.setCaseDocumentId(soapCaseDocumentId);
				}
			} else if("u".equalsIgnoreCase(status)) {
				// do nothing
			} else {
				// else set SOAP error
				note.setError("PRE BGS SOAP Service status error: " + status + ".");
			}
		} catch (IOException e) {
			note.setError(PRE_BGS_SOAP_SERVICE_ERROR + e.toString());
			logger.error(e.getMessage());
		}

		//:TODO update casenote with SOAP response
	}
	
/**
 * THESE ARE METHODS FROM CaseNote obj ---------------------------------------------
 */
	
	private String xml_escape(String s) {
		if(!StringUtils.isEmpty(s)) {
			s = s.replaceAll("&", "&amp;");
			s = s.replaceAll("<", "&lt;");
			s = s.replaceAll(">", "&gt;");
			s = s.replaceAll("'", "&apos;");
			s = s.replaceAll("\"", "&quot;");
		} else {
			s= "No data available.";
		}
		logger.debug("Escaped XML: {}", s);
		return s;
	}
	
	public String getResultTag(CaseNote2 casenote, String tag) {
		String returnVal = "";
		try {
			// if casenote.getSoapResult() is not empty or null
			if(!StringUtils.isEmpty(casenote.getSoapResult())) {
				String s = casenote.getSoapResult().split("<" + tag + ">")[1];
				// if String s is not empty or null
				if(!StringUtils.isEmpty(s)) {
					returnVal =  s.split("</" + tag + ">")[0];
				}
			}
		} catch (Exception e) { //TODO: refactor to not capture general exception
			casenote.setError("PRE BGS SOAP Service Fault Exception missing tag " + tag + " in result");
			logger.error("PRE BGS SOAP Service Result Tag Error: {} does not exist or is empty.", tag);
			returnVal = "";
		}
		
		return returnVal;
	}

	private String tag(String tag, String value) {
		logger.debug("SOAP tag: {}", tag);
		return ("\n<" + tag + ">" + xml_escape(value) + "</" + tag + ">");
	}

	private String toCaseDcmntDTO(CaseNote2 caseNote) { // Note JAXB Marshaller
		String xml = "<CaseDcmntDTO>";
		if (isValid(caseNote.getCaseDocumentId()))
			xml += tag("caseDcmntId", caseNote.getCaseDocumentId());
		xml += tag("caseId", caseNote.getCaseId());
		xml += tag("modifdDt", caseNote.getModifiedDate());
		xml += tag("dcmntTxt", caseNote.getCaseNote());
		xml += "\n</CaseDcmntDTO>";

		return xml;
	}

	public void validate(CaseNote2 caseNote) {
		valid(caseNote, "bnftClaimNoteTypeCd", caseNote.getBenefitClaimNoteTypeCd());
		valid(caseNote, "caseId", caseNote.getCaseId());
		valid(caseNote, "modifdDt", caseNote.getModifiedDate());
		valid(caseNote, "dcmntTxt", caseNote.getCaseNote());
		if (!isValid(caseNote.getModifiedDate()))
			caseNote.setModifiedDate(now());
	}

	private boolean isValid(String s) {
		return (!StringUtils.isEmpty(s));
	}

	private void valid(CaseNote2 caseNote, String f, String v) {
		if (!isValid(v))
			caseNote.setError("PRE BGS SOAP Exception invalid " + f + " value");
	}

	private String now() {
		DateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss");
		return dateFormat.format(new Date());
	}
	
}