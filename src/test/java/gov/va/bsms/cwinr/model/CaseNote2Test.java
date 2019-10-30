package gov.va.bsms.cwinr.model;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class CaseNote2Test {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testSetCaseNoteId() {
		CaseNote2 caseNote = new CaseNote2();
		String validId = "123";

		caseNote.setCaseDocumentId(validId);
		assertEquals(validId, caseNote.getCaseDocumentId());
		assertEquals(true, caseNote.isUpdate());
		caseNote = new CaseNote2();
		
		String nullValue = "NULL";
		
		caseNote.setCaseDocumentId(nullValue);
		assertEquals(nullValue, caseNote.getCaseDocumentId());
		assertEquals(false, caseNote.isUpdate());

		nullValue = "";
		caseNote.setCaseDocumentId(nullValue);
		assertEquals(nullValue, caseNote.getCaseDocumentId());
		assertEquals(false, caseNote.isUpdate());
	}

	@Test
	public void testSetCaseId() {
		CaseNote2 caseNote = new CaseNote2();
		String validId = "123";

		caseNote.setCaseId(validId);
		assertEquals(validId, caseNote.getCaseId());
		assertEquals(false, caseNote.isWithDBError());
		caseNote = new CaseNote2();
		
		String nullValue = "NULL";

		caseNote.setCaseId(nullValue);
		assertEquals(nullValue, caseNote.getCaseId());
		assertEquals(true, caseNote.isWithDBError());

		nullValue = "";
		caseNote.setCaseId(nullValue);
		assertEquals(nullValue, caseNote.getCaseId());
		assertEquals(true, caseNote.isWithDBError());
	}

}
