package gov.va.bsms.cwinrs.model;

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import gov.va.bsms.cwinr.model.CaseNote2;

public class CaseNote2Test {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {	
		// do nothing
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {	
		// do nothing
	}

	@Before
	public void setUp() throws Exception {	
		// do nothing
	}

	@After
	public void tearDown() throws Exception {	
		// do nothing
	}

	@Test
	public void setCaseIdWithValidValue() {
		CaseNote2 caseNote = new CaseNote2();
		String validId = "123";

		caseNote.setCaseId(validId);
		assertEquals(validId, caseNote.getCaseId());
		assertEquals(false, caseNote.isWithDBError());
	}

	@Test
	public void setCaseIdWithNullValue() {
		CaseNote2 caseNote = new CaseNote2();
		String nullValue = "NULL";

		caseNote.setCaseId(nullValue);
		assertEquals(nullValue, caseNote.getCaseId());
		assertEquals(true, caseNote.isWithDBError());

		nullValue = "";
		caseNote.setCaseId(nullValue);
		assertEquals(nullValue, caseNote.getCaseId());
		assertEquals(true, caseNote.isWithDBError());
	}

	@Test
	public void setCaseDocumentIdWithValidValue() {
		CaseNote2 caseNote = new CaseNote2();
		String nullValue = "123";

		caseNote.setCaseDocumentId(nullValue);
		assertEquals(nullValue, caseNote.getCaseDocumentId());
		assertEquals(true, caseNote.isUpdate());

	}

	@Test
	public void setCaseDocumentIdWithNullValue() {
		CaseNote2 caseNote = new CaseNote2();
		String nullValue = "NULL";

		caseNote.setCaseDocumentId(nullValue);
		assertEquals(nullValue, caseNote.getCaseDocumentId());
		assertEquals(false, caseNote.isUpdate());

		nullValue = "";
		caseNote.setCaseDocumentId(nullValue);
		assertEquals(nullValue, caseNote.getCaseDocumentId());
		assertEquals(false, caseNote.isUpdate());

	}

}
