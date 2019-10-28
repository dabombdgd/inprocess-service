package gov.va.bsms.cwinr.soap;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.ws.Holder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.va.bsms.cwinr.model.CaseNote2;
/*import gov.va.vba.vetsnet.services.cases.CaseDcmntDTO;
import gov.va.vba.vetsnet.services.cases.CaseWebService;
import gov.va.vba.vetsnet.services.cases.CaseWebService_Service;
import gov.va.vba.vetsnet.services.cases.MessageException;*/

public class SoapDao {
	private static Logger logger = LoggerFactory.getLogger(SoapDao.class);
	 
	  private static final QName SERVICE_NAME = 
	    new QName("http://cases.services.vetsnet.vba.va.gov/", 
	    "CaseWebService");
	
	public SoapDao() {
	}	

	public void processCaseNotesWithBgsServiceMvp(List<CaseNote2> caseNotesForBgsprocessing) {
		SoapClient soapClient = new SoapClient();
		
		for(CaseNote2 tempCaseNote : caseNotesForBgsprocessing) {
			// process case notes w/o db error
			if(!tempCaseNote.isWithDBError()) {
				soapClient.sendCaseNote(tempCaseNote);
			}
		}
	}

	/**
	 * Create the JAX-WS generated {@link CaseDcmntDTO} objects for each {@link CaseNote2} object
	 * 
	 * @param caseNotesForBgsprocessing
	 */
	public void processCaseNotesWithBgsService(List<CaseNote2> caseNotesForBgsprocessing) {
		
		/*
		 * BindingProvider provider = (BindingProvider) port;
		 * provider.getRequestContext().put("ws-security.username", "281CEASL");
		 * provider.getRequestContext().put("ws-security.password", "Buda110!");
		 * provider.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY,
		 * "http://cases.services.vetsnet.vba.va.gov/");
		 */
		
		//List<CaseNote2> returnVal = Collections.<CaseNote2>emptyList();
		
		/*
		 * URL wsdlURL = null; try { wsdlURL = new
		 * URL("http://bepwebdevl.vba.va.gov/CaseWebServiceBean/CaseWebService?wsdl"); }
		 * catch (MalformedURLException e) { logger.error("Error with the WSDL URL: {}",
		 * e.getMessage()); }
		 * 
		 * for(CaseNote2 tempCaseNote : caseNotesForBgsprocessing) {
		 * if(!tempCaseNote.isWithDBError()) { CaseDcmntDTO caseDcmntDTO = new
		 * CaseDcmntDTO();
		 * caseDcmntDTO.setCaseId(Long.parseLong(tempCaseNote.getCaseId()));
		 * caseDcmntDTO.setBnftClaimNoteTypeCd(tempCaseNote.getBenefitClaimNoteTypeCd())
		 * ; caseDcmntDTO.setDcmntTxt(tempCaseNote.getCaseNote());
		 * 
		 * if(tempCaseNote.isUpdate()) {
		 * caseDcmntDTO.setCaseDcmntId(Long.parseLong(tempCaseNote.getCaseDocumentId()))
		 * ; }
		 * 
		 * CaseWebService_Service caseWebService = new CaseWebService_Service(wsdlURL,
		 * SERVICE_NAME); CaseWebService port = caseWebService.getCaseWebServicePort();
		 * Holder<CaseDcmntDTO> holder = new Holder<CaseDcmntDTO>(caseDcmntDTO);
		 * 
		 * try { port.updateCaseDcmnt(holder); } catch (MessageException e) {
		 * logger.error("Error with the service: {}", e.getMessage());
		 * tempCaseNote.setError(e.getMessage()); tempCaseNote.setWithBGSError(true); }
		 * tempCaseNote.setCaseDocumentId(Long.toString(holder.value.getCaseDcmntId()));
		 * //returnVal.add(tempCaseNote); } }
		 */
		
		//return returnVal;
	}

}
