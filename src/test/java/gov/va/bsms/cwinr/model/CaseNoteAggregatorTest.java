package gov.va.bsms.cwinr.model;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest(ConnectionManager.class)
public class CaseNoteAggregatorTest {

	@Mock
	private Connection conn;

	@Mock
	private PreparedStatement stmt;

	@Mock
	private ResultSet rs;

	@Mock
	private CaseNoteDao cnDao;

	private List<CaseNote2> caseNotesForProcessing = new ArrayList<>();

	@Before
	public void setUp() throws Exception {
		assertNotNull(cnDao);

		for (int i = 0; i < 30; i++) {
			CaseNote2 caseNote = new CaseNote2();
			// set every other casenote with db error
			if (i % 2 == 0) {
				caseNote.setCaseId(String.valueOf(i));
				caseNote.setCaseDocumentId(String.valueOf(i));
			} else {
				caseNote.setCaseId(null);
				caseNote.setCaseDocumentId(null);
			}
			// set bgs error to true for every third caseNote
			if (i % 3 == 0) {
				caseNote.setWithBGSError(true);
			}

			caseNotesForProcessing.add(caseNote);
		}

		PowerMockito.mockStatic(ConnectionManager.class);
		when(ConnectionManager.getConnection(any(Boolean.class))).thenReturn(conn);
		when(conn.prepareStatement(any(String.class))).thenReturn(stmt);
		when(stmt.executeQuery()).thenReturn(rs);
		when(cnDao.getCaseNotesForProcessing()).thenReturn(caseNotesForProcessing);

	}

	@Test
	public void testGetCaseNotesForProcessing() throws Exception {
		CaseNoteAggregator cnAgg = new CaseNoteAggregator();
		cnAgg.setCaseNotesForProcessing(cnDao.getCaseNotesForProcessing());
		assertTrue(cnAgg.getCaseNotesForProcessing().size() == 30);
	}

	@Test
	public void testSetCaseNotesForProcessing() throws Exception {
		CaseNoteAggregator cnAgg = new CaseNoteAggregator();
		cnAgg.setCaseNotesForProcessing(cnDao.getCaseNotesForProcessing());
		assertTrue(cnAgg.getCaseNotesForProcessing().size() == 30);
		cnAgg.setCaseNotesForProcessing(null);
		assertTrue(cnAgg.getCaseNotesForProcessing() == null);
	}

	@Test
	public void testGetCaseNotesWithNonDbError() throws Exception {
		CaseNoteAggregator cnAgg = new CaseNoteAggregator();
		cnAgg.setCaseNotesForProcessing(cnDao.getCaseNotesForProcessing());
		assertTrue(cnAgg.getCaseNotesWithNonDbError().size() == 15);
	}

	@Test
	public void testGetCaseNotesWithDbError() throws Exception {
		CaseNoteAggregator cnAgg = new CaseNoteAggregator();
		cnAgg.setCaseNotesForProcessing(cnDao.getCaseNotesForProcessing());
		assertTrue(cnAgg.getCaseNotesWithDbError().size() == 15);
	}

	@Test
	public void testGetCaseNoteswithBgsError() throws Exception {
		CaseNoteAggregator cnAgg = new CaseNoteAggregator();
		cnAgg.setCaseNotesForProcessing(cnDao.getCaseNotesForProcessing());
		assertTrue(cnAgg.getCaseNoteswithBgsError().size() == 5);
	}

	@Test
	public void testGetNewCaseNotes() throws Exception {
		CaseNoteAggregator cnAgg = new CaseNoteAggregator();
		cnDao.getCaseNotesForProcessing().get(0).setUpdate(false);
		cnDao.getCaseNotesForProcessing().get(0).setCaseNoteId("TEST DATA");
		cnAgg.setCaseNotesForProcessing(cnDao.getCaseNotesForProcessing());
		assertTrue(cnAgg.getNewCaseNotes().size() == 1);
	}

}
