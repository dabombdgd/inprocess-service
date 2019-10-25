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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.va.bsms.cwinr.model.CaseNote2;
import gov.va.bsms.cwinr.utils.ConfigurationManager;

/*  BGS SOAP client interface */

public class SOAPClient {
	static Logger LOGGER = LoggerFactory.getLogger(SOAPClient.class);
	
	private static final String update_fn = "template.xml";
	private static final String out_fn = "test/soap.xml";
	private static final String res_fn = "test/response.xml";
	private static final String in_fn = "test/case_notes.txt";

	public SOAPClient() {
		// do nothing
	}

	private void sendToBGS(CaseNote2 note, String request) throws IOException {
		//URL url = new URL(config.getString("bgs-url", "-node"));
		URL url = new URL(ConfigurationManager.INSTANCE.getResources().getString("bgs-url-node"));
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
			LOGGER.info("Wrong BGS status: " + responseStatus); // Delete this line
			setError(note, "Wrong BGS status: " + responseStatus);
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
			String request = template.replace("<CaseDcmntDTO/>", toCaseDcmntDTO(note));

			sendToBGS(note, request);

			// Status for Update and Insert
			String status = getResultTag(note, "jrnStatusTypeCd").toLowerCase();
			if ((status.equals("i") | status.equals("u")) == false)
				note.setSoapError("BGS status wrong: " + status + ".");
		} catch (IOException e) {
			note.setSoapError("Error" + e.toString());
			e.printStackTrace();
		}
		LOGGER.info(note.toString());
	}

	/* This code will be moved to a unit test someday */
	/*
	 * Read notes from file. The case notes are one per line. They can not have \n
	 * new lines in this test.
	 */
	/*
	 * public void testCaseNotes() { try { if (!(new File(in_fn).exists())) return;
	 * Scanner scan = new Scanner(new File(in_fn));
	 * 
	 * while (scan.hasNextLine()) { String[] f = scan.nextLine().split(" ", 5); for
	 * (int i = 0; i < f.length; i++) if ("''".equals(f[i])) f[i] = ""; // two
	 * single quotes is the empty string CaseNote note = new CaseNote(f[0], f[1],
	 * f[2], f[3], f[4]); javaService.receive(note); } } catch (IOException e) {
	 * e.printStackTrace(); } }
	 */
	
/**
 * THESE ARE METHODS FROM CaseNote obj ---------------------------------------------
 */
	
	private String xml_escape(String s) {
		s = s.replaceAll("&", "&amp;");
		s = s.replaceAll("<", "&lt;");
		s = s.replaceAll(">", "&gt;");
		s = s.replaceAll("'", "&apos;");
		s = s.replaceAll("\"", "&quot;");
		LOGGER.debug("Escaped XML: {}", s);
		return s;
	}
	
	public String getResultTag(CaseNote2 casenote, String tag) {
		try {
			String s = casenote.getSoapResult().split("<" + tag + ">")[1];
			return s.split("</" + tag + ">")[0];
		} catch (Exception e) { //TODO: refactor to not capture general exception
			casenote.setSoapError("SOAP Fault Exception missing tag " + tag + " in result");
			LOGGER.error("Result Tag Error: {}", tag);
			return "";
		}
	}

	private String tag(String tag, String value) {
		LOGGER.debug("SOAP tag: {}", tag);
		return ("\n<" + tag + ">" + xml_escape(value) + "</" + tag + ">");
	}

	private String toCaseDcmntDTO(CaseNote2 caseNote) { // Note JAXB Marshaller
		String xml = "<CaseDcmntDTO>";
		//if (isValid(this.caseDcmntId))
		if (isValid(caseNote.getCaseDocumentId()))
			xml += tag("caseDcmntId", caseNote.getCaseDocumentId());
		xml += tag("caseId", caseNote.getCaseId());
		xml += tag("modifdDt", caseNote.getModifiedDate());
		xml += tag("dcmntTxt", caseNote.getCaseNote());
		xml += "\n</CaseDcmntDTO>";
		LOGGER.debug("Caee Note SOAP Request: {}", xml);
		return xml;
	}

	public void validate(CaseNote2 caseNote) {
		valid(caseNote, "bnftClaimNoteTypeCd", caseNote.getBenefitClaimNoyeTypeCd());
		valid(caseNote, "caseId", caseNote.getCaseId());
		valid(caseNote, "modifdDt", caseNote.getModifiedDate());
		valid(caseNote, "dcmntTxt", caseNote.getCaseNote());
		if (!isValid(caseNote.getModifiedDate()))
			caseNote.setModifiedDate(now());
	}

	private boolean isValid(String s) {
		return ((s != null) & (s.length() > 0));
	}

	private void valid(CaseNote2 caseNote, String f, String v) {
		if (!isValid(v))
			caseNote.setSoapError("Exception invalid " + f + " value");
	}

	private String now() {
		DateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss");
		return dateFormat.format(new Date());
	}

	public void setError(CaseNote2 caseNote, String str) {
		LOGGER.error("Error string: {}", str);
		caseNote.setSoapError((caseNote.getSoapError() == null) ? str : (str + " " + caseNote.getSoapError()));
	}
	
}

//https://stackoverflow.com/questions/22068864/how-to-generate-soap-request-and-get-response-in-java-coding
//https://chillyfacts.com/java-send-soap-xml-request-read-response/            }
