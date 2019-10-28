package gov.va.bsms.cwinr.soap;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

import org.apache.cxf.common.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.va.bsms.cwinr.exceptions.ConfigurationManagerException;
import gov.va.bsms.cwinr.model.CaseNote2;
import gov.va.bsms.cwinr.utils.ConfigurationManager;

/*  BGS SOAP client interface */

public class SoapClient {
	static Logger logger = LoggerFactory.getLogger(SoapClient.class);
	
	private static final String update_fn = "template.xml";
	private static final String out_fn = "test/soap.xml";
	private static final String res_fn = "test/response.xml";
	private static final String in_fn = "test/case_notes.txt";

	public SoapClient() {
		// do nothing
	}

	private void sendToBGS(CaseNote2 note, String request) throws IOException {
		//URL url = new URL(config.getString("bgs-url", "-node"));
		String bgsUrl= "";
		try {
			bgsUrl = ConfigurationManager.INSTANCE.getResources().getString("bgs-url2");
		} catch (ConfigurationManagerException e) {
			throw new IOException(e);
		}		
		
		URL url = new URL(bgsUrl);
		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setRequestMethod("POST");
		con.setRequestProperty("Content-Type", "text/xml; charset=utf-8");
		con.setDoOutput(true);
		DataOutputStream wr = new DataOutputStream(con.getOutputStream());
		wr.writeBytes(request);
		wr.flush();
		wr.close();

		String responseStatus = con.getResponseMessage(); // "OK" or not
		if (!responseStatus.equalsIgnoreCase("OK")) {
			logger.warn("Wrong BGS status: " + responseStatus); // Delete this line
			note.setError("BGS SOAP Service invalid status: " + responseStatus);
		}

		BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
		StringBuilder response = new StringBuilder();
		String inputLine;
		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();
		
		// set the SOAP result from BGS
		note.setSoapResult(response.toString());
	}

	private String readFile(String fn) throws IOException {
		Scanner in = new Scanner(new File(fn));
		in.useDelimiter("\\Z");
		String text = in.next();
		in.close();
		return text;
	}

	private void writeFile(String fn, String data) throws IOException {
		FileWriter out = new FileWriter(new File(fn));
		out.write(data);
		out.close();
	}

	private void sendToFile(CaseNote2 note, String request) throws IOException {
		//:TODO WHY?
		writeFile(out_fn, request); // write to fake output file
		//:TODO WHY?
		note.setSoapResult(readFile(res_fn)); // read from fake input file // if (new File(res_fn).exists())
	}

	public void sendCaseNote(CaseNote2 note) {
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
				note.setError("BGS SOAP Service status error: " + status + ".");
			}
		} catch (IOException e) {
			note.setError("PRE BGS SOAP Service Error" + e.toString());
			logger.error(e.getMessage());
		}

		//:TODO update casenote with SOAP response
	}
	


	/*
	 * public void sendCaseNoteMock(CaseNote2 note) { validate(note);
	 * 
	 * try { String template = readFile(update_fn); String request =
	 * template.replace("<CaseDcmntDTO />", toCaseDcmntDTO(note));
	 * 
	 * logger.debug(request);
	 * 
	 * // sendToBGS(note, request); note.
	 * setSoapResult("<S:Envelope xmlns:S=\"http://schemas.xmlsoap.org/soap/envelope/\">\r\n"
	 * + "<S:Body>\r\n" +
	 * "<ns2:updateCaseDcmntResponse xmlns:ns2=\"http://cases.services.vetsnet.vba.va.gov/\">\r\n"
	 * + "<CaseDcmntDTO>\r\n" +
	 * "<bnftClaimNoteTypeCd>001</bnftClaimNoteTypeCd> \r\n" +
	 * "<caseDcmntId>3612</caseDcmntId> \r\n" + "<caseId>65858</caseId> \r\n" +
	 * "<dcmntTxt>updating a note</dcmntTxt> \r\n" +
	 * "<jrnDt>2019-07-11T15:23:59-05:00</jrnDt> \r\n" +
	 * "<jrnLctnId>281</jrnLctnId> \r\n" + "<jrnObjId>VBMS</jrnObjId> \r\n" +
	 * "<jrnStatusTypeCd>I</jrnStatusTypeCd> \r\n" +
	 * "<jrnUserId>281CEASL</jrnUserId> \r\n" +
	 * "<modifdDt>2019-07-11T15:23:59-05:00</modifdDt > \r\n" +
	 * "</CaseDcmntDTO>\r\n" + "</ns2:updateCaseDcmntResponse>\r\n" +
	 * "</S:Body>\r\n" + "</S:Envelope>\r\n");
	 * 
	 * // Status for Update and Insert String status = getResultTag(note,
	 * "jrnStatusTypeCd").toLowerCase(); if ("i".equalsIgnoreCase(status)) { String
	 * soapCaseDocumentId = getResultTag(note, "caseDcmntId");
	 * if(!StringUtils.isEmpty(soapCaseDocumentId)) {
	 * note.setCaseDocumentId(Integer.toString(getRandonInt())); } } else
	 * if("u".equalsIgnoreCase(status)) { // do nothing } else { // else set SOAP
	 * error note.setError("BGS SOAP Service status error: " + status + "."); } }
	 * catch (IOException e) { note.setError("PRE BGS SOAP Service Error" +
	 * e.toString()); logger.error(e.getMessage()); }
	 * 
	 * //:TODO update casenote with SOAP response }
	 */
	
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
	
	private Integer getRandonInt() {
		double randomDouble = Math.random();
		randomDouble = randomDouble * 1008 + 1;
		int randomInt = (int) randomDouble;
		return randomInt;
	}
	
}