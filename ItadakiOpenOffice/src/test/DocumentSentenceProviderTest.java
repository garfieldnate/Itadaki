package test;

import static org.junit.Assert.*;

import static test.util.DocumentUtil.*;

import org.itadaki.openoffice.DocumentSentenceProvider;
import org.itadaki.openoffice.util.As;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.sun.star.text.XText;
import com.sun.star.text.XTextCursor;
import com.sun.star.text.XTextDocument;
import com.sun.star.text.XTextTable;


/**
 * Test of DocumentSentenceProvider
 * 
 * Situations:
 *   - Blank document
 *   - Single text paragraph
 *   - Multiple text paragraphs
 *   - Single numbered paragraph
 *   - Multiple numbered paragraphs
 *   - Text section
 *   - Single cell table
 *   - Multiple cell table
 *   - Complex table
 *   - Table with embedded text section
 *   - Frame (anchor to page)
 *   - Frame (anchor to paragraph)
 *   - Frame (anchor to character)
 *   - Frame (anchor as character)
 *   - Header
 *   - Footer
 *   
 *   
 * Tests that:
 *   - Existing readings can be read correctly
 *   - New readings can be written correctly
 * 
 * For text selections, tests that:
 *   - Readings are correctly masked to the bounds of the selection
 * 
 * For table cell selections, tests that:
 *   - The correct table cells are visited
 * 
 * ** Create document + readings
 * --> list of paragraphs + readings
 * ** Whole document test fixture (paragraphs, existing readings, readings to apply)
 * ** Selection test fixture (paragraphs, existing readings, readings to apply, selection start paragraph/index, selection end paragraph/index)
 *     - 
 */
public class DocumentSentenceProviderTest {

	/**
	 * If <code>true</code>, the test document will be displayed and left open
	 * after each test is run
	 */
	private static final boolean SHOW_DOCUMENTS = false;

	/**
	 * An text document automatically created for each test
	 */
	private XTextDocument textDocument = null;


	/**
	 * Sets up the text document for each test
	 * @throws Exception 
	 */
	@Before
	public void setUp() throws Exception {

		this.textDocument = createBlankDocument (SHOW_DOCUMENTS);

	}


	/**
	 * Tears down the text document after each test
	 */
	@After
	public void tearDown() {

		if (!SHOW_DOCUMENTS) {
			this.textDocument.dispose();
		}

	}


	/**
	 * Tests the output of the sentence provider by moving from start to end,
	 * checking the expected state at each point
	 *
	 * @param sentenceProvider The sentence provider to test
	 * @param expectedParagraphs The expected textual paragraph contents
	 */
	private void testFixture (DocumentSentenceProvider sentenceProvider, String[] expectedParagraphs) {

		for (int i = 0; i < expectedParagraphs.length; i++) {
			assertEquals (expectedParagraphs[i], sentenceProvider.getText());
			assertFalse (sentenceProvider.hasPrevious());
			assertEquals ((i != (expectedParagraphs.length - 1)), sentenceProvider.hasNext());
			if (i != (expectedParagraphs.length - 1)) {
				sentenceProvider.next();
			}
		}
	}


	/**
	 * Creates a sentence provider for the given component
	 *
	 * @param textDocument The text document to create the sentence provider for
	 * @return The constructed sentence provider
	 */
	private DocumentSentenceProvider createSentenceProvider (XTextDocument textDocument) {


		DocumentSentenceProvider sentenceProvider = new DocumentSentenceProvider (this.textDocument);

		return sentenceProvider;

	}


	/* Paragraph permutation tests */

	/**
	 * Paragraph permutation tests:
	 * Empty document
	 *
	 * @throws Exception 
	 */
	@Test
	public void testBlank() throws Exception {


		// Create sentence provider
		DocumentSentenceProvider sentenceProvider = createSentenceProvider (this.textDocument);

		// Test sentence provider
		assertEquals ("", sentenceProvider.getText());
		assertFalse (sentenceProvider.hasPrevious());
		assertFalse (sentenceProvider.hasNext());


	}


	/**
	 * Paragraph permutation tests:
	 * Plain text paragraph
	 *
	 * @throws Exception
	 */
	@Test
	public void testPlain() throws Exception {

		XText text = this.textDocument.getText();
		XTextCursor textCursor = text.createTextCursor();

		// Insert test data
		insertPlainParagraph (text, textCursor, "", false);

		// Create sentence provider
		DocumentSentenceProvider sentenceProvider = createSentenceProvider (this.textDocument);

		// Test sentence provider
		String[] expectedParagraphs = {
				"This is a plain paragraph",
		};

		testFixture (sentenceProvider, expectedParagraphs);


	}


	/**
	 * Paragraph permutation tests:
	 * Plain text paragraph
	 * Blank paragraph
	 *
	 * @throws Exception
	 */
	@Test
	public void testPlainBlank() throws Exception {

		XText text = this.textDocument.getText();
		XTextCursor textCursor = text.createTextCursor();

		// Insert test data
		insertPlainParagraph (text, textCursor, "", true);
		insertBlankParagraph (text, textCursor, false);

		// Create sentence provider
		DocumentSentenceProvider sentenceProvider = createSentenceProvider (this.textDocument);

		// Test sentence provider
		String[] expectedParagraphs = {
				"This is a plain paragraph",
				""
		};

		testFixture (sentenceProvider, expectedParagraphs);


	}


	/**
	 * Paragraph permutation tests:
	 * Blank paragraph
	 * Plain text paragraph
	 *
	 * @throws Exception
	 */
	@Test
	public void testBlankPlain() throws Exception {

		XText text = this.textDocument.getText();
		XTextCursor textCursor = text.createTextCursor();

		// Insert test data
		insertBlankParagraph (text, textCursor, true);
		insertPlainParagraph (text, textCursor, "", false);

		// Create sentence provider
		DocumentSentenceProvider sentenceProvider = createSentenceProvider (this.textDocument);

		// Test sentence provider
		String[] expectedParagraphs = {
				"",
				"This is a plain paragraph"
		};

		testFixture (sentenceProvider, expectedParagraphs);


	}

	/**
	 * Paragraph permutation tests:
	 * Blank paragraph
	 * Plain text paragraph
	 * Blank paragraph
	 *
	 * @throws Exception
	 */
	@Test
	public void testBlankPlainBlank() throws Exception {

		XText text = this.textDocument.getText();
		XTextCursor textCursor = text.createTextCursor();

		// Insert test data
		insertBlankParagraph (text, textCursor, true);
		insertPlainParagraph (text, textCursor, "", true);
		insertBlankParagraph (text, textCursor, false);

		// Create sentence provider
		DocumentSentenceProvider sentenceProvider = createSentenceProvider (this.textDocument);

		// Test sentence provider
		String[] expectedParagraphs = {
				"",
				"This is a plain paragraph",
				""
		};

		testFixture (sentenceProvider, expectedParagraphs);


	}


 	/**
	 * Paragraph permutation tests:
	 * Plain text paragraph
	 * Blank paragraph
	 * Plain text paragraph
	 *
	 * @throws Exception
	 */
	@Test
	public void testPlainBlankPlain() throws Exception {

		XText text = this.textDocument.getText();
		XTextCursor textCursor = text.createTextCursor();

		// Insert test data
		insertPlainParagraph (text, textCursor, "", true);
		insertBlankParagraph (text, textCursor, true);
		insertPlainParagraph (text, textCursor, "", false);

		// Create sentence provider
		DocumentSentenceProvider sentenceProvider = createSentenceProvider (this.textDocument);

		// Test sentence provider
		String[] expectedParagraphs = {
				"This is a plain paragraph",
				"",
				"This is a plain paragraph"
		};

		testFixture (sentenceProvider, expectedParagraphs);


	}


	/**
 	 * Paragraph permutation tests:
	 * Plain text paragraph
	 * Plain text paragraph
	 * Plain text paragraph
	 *
	 * @throws Exception
	 */
	@Test
	public void testPlainPlainPlain() throws Exception {

		XText text = this.textDocument.getText();
		XTextCursor textCursor = text.createTextCursor();

		// Insert test data
		insertPlainParagraph (text, textCursor, " 1", true);
		insertPlainParagraph (text, textCursor, " 2", true);
		insertPlainParagraph (text, textCursor, " 3", false);

		// Create sentence provider
		DocumentSentenceProvider sentenceProvider = createSentenceProvider (this.textDocument);

		// Test sentence provider
		String[] expectedParagraphs = {
				"This is a plain paragraph 1",
				"This is a plain paragraph 2",
				"This is a plain paragraph 3"
		};

		testFixture (sentenceProvider, expectedParagraphs);


	}


	/**
	 * Paragraph permutation tests:
	 * Number paragraph
	 *
	 * @throws Exception
	 */
	@Test
	public void testNumber() throws Exception {

		XText text = this.textDocument.getText();
		XTextCursor textCursor = text.createTextCursor();
		
		// Insert test data
		insertNumberedParagraph (this.textDocument, text, textCursor, "", false);

		// Create sentence provider
		DocumentSentenceProvider sentenceProvider = createSentenceProvider (this.textDocument);

		// Test sentence provider
		String[] expectedParagraphs = {
				"This is a numbered paragraph",
		};

		testFixture (sentenceProvider, expectedParagraphs);


	}


	/**
	 * Paragraph permutation tests:
	 * Number paragraph
	 * Blank paragraph
	 *
	 * @throws Exception
	 */
	@Test
	public void testNumberBlank() throws Exception {

		XText text = this.textDocument.getText();
		XTextCursor textCursor = text.createTextCursor();
		
		// Insert test data
		insertNumberedParagraph (this.textDocument, text, textCursor, "", true);
		insertBlankParagraph (text, textCursor, false);

		// Create sentence provider
		DocumentSentenceProvider sentenceProvider = createSentenceProvider (this.textDocument);

		// Test sentence provider
		String[] expectedParagraphs = {
				"This is a numbered paragraph",
				""
		};

		testFixture (sentenceProvider, expectedParagraphs);


	}


	/**
	 * Paragraph permutation tests:
	 * Blank paragraph
	 * Number paragraph
	 *
	 * @throws Exception
	 */
	@Test
	public void testBlankNumber() throws Exception {

		XText text = this.textDocument.getText();
		XTextCursor textCursor = text.createTextCursor();
		
		// Insert test data
		insertBlankParagraph (text, textCursor, true);
		insertNumberedParagraph (this.textDocument, text, textCursor, "", false);

		// Create sentence provider
		DocumentSentenceProvider sentenceProvider = createSentenceProvider (this.textDocument);

		// Test sentence provider
		String[] expectedParagraphs = {
				"",
				"This is a numbered paragraph"
		};

		testFixture (sentenceProvider, expectedParagraphs);


	}


	/**
	 * Paragraph permutation tests:
	 * Blank paragraph
	 * Number paragraph
	 * Blank paragraph
	 *
	 * @throws Exception
	 */
	@Test
	public void testBlankNumberBlank() throws Exception {

		XText text = this.textDocument.getText();
		XTextCursor textCursor = text.createTextCursor();
		
		// Insert test data
		insertBlankParagraph (text, textCursor, true);
		insertNumberedParagraph (this.textDocument, text, textCursor, "", true);
		insertBlankParagraph (text, textCursor, false);

		// Create sentence provider
		DocumentSentenceProvider sentenceProvider = createSentenceProvider (this.textDocument);

		// Test sentence provider
		String[] expectedParagraphs = {
				"",
				"This is a numbered paragraph",
				""
		};

		testFixture (sentenceProvider, expectedParagraphs);


	}


	/**
	 * Paragraph permutation tests:
	 * Number paragraph
	 * Blank paragraph
	 * Number paragraph
	 *
	 * @throws Exception
	 */
	@Test
	public void testNumberBlankNumber() throws Exception {

		XText text = this.textDocument.getText();
		XTextCursor textCursor = text.createTextCursor();
		
		// Insert test data
		insertNumberedParagraph (this.textDocument, text, textCursor, "", true);
		insertBlankParagraph (text, textCursor, true);
		insertNumberedParagraph (this.textDocument, text, textCursor, "", false);

		// Create sentence provider
		DocumentSentenceProvider sentenceProvider = createSentenceProvider (this.textDocument);

		// Test sentence provider
		String[] expectedParagraphs = {
				"This is a numbered paragraph",
				"",
				"This is a numbered paragraph"
		};

		testFixture (sentenceProvider, expectedParagraphs);


	}


	/**
	 * Paragraph permutation tests:
	 * Number paragraph
	 * Number paragraph
	 * Number paragraph
	 *
	 * @throws Exception
	 */
	@Test
	public void testNumberNumberNumber() throws Exception {

		XText text = this.textDocument.getText();
		XTextCursor textCursor = text.createTextCursor();
		
		// Insert test data
		insertNumberedParagraph (this.textDocument, text, textCursor, " 1", true);
		insertNumberedParagraph (this.textDocument, text, textCursor, " 2", true);
		insertNumberedParagraph (this.textDocument, text, textCursor, " 3", false);

		// Create sentence provider
		DocumentSentenceProvider sentenceProvider = createSentenceProvider (this.textDocument);

		// Test sentence provider
		String[] expectedParagraphs = {
				"This is a numbered paragraph 1",
				"This is a numbered paragraph 2",
				"This is a numbered paragraph 3"
		};

		testFixture (sentenceProvider, expectedParagraphs);

	}


	/**
	 * 1x1 table with text
	 * 
	 * @throws Exception
	 */
	@Test
	public void testOneOneTable() throws Exception {


		XText text = this.textDocument.getText();
		XTextCursor textCursor = text.createTextCursor();

		insertTable (this.textDocument, text, textCursor, 1, 1, "");


		// Create sentence provider
		DocumentSentenceProvider sentenceProvider = createSentenceProvider (this.textDocument);


		// Test sentence provider
		String[] expectedParagraphs = {
				"A1",
				""
		};

		testFixture (sentenceProvider, expectedParagraphs);


	}


	/**
	 * 3x3 table with text
	 * 
	 * @throws Exception
	 */
	@Test
	public void testThreeThreeTable() throws Exception {


		XText text = this.textDocument.getText();
		XTextCursor textCursor = text.createTextCursor();

		insertTable (this.textDocument, text, textCursor, 3, 3, "");


		// Create sentence provider
		DocumentSentenceProvider sentenceProvider = createSentenceProvider (this.textDocument);


		// Test sentence provider
		String[] expectedParagraphs = {
				"A1",
				"B1",
				"C1",
				"A2",
				"B2",
				"C2",
				"A3",
				"B3",
				"C3",
				""
		};

		testFixture (sentenceProvider, expectedParagraphs);


	}


	/**
	 * 3x3 table with embedded table in centre cell
	 * 
	 * @throws Exception
	 */
	@Test
	public void testEmbeddedTable() throws Exception {


		XText text = this.textDocument.getText();
		XTextCursor textCursor = text.createTextCursor();

		XTextTable outerTable = insertTable (this.textDocument, text, textCursor, 3, 3, "Outer ");

		XText cellText = As.XText (outerTable.getCellByName ("B2"));
		insertTable (this.textDocument, cellText, cellText.createTextCursor(), 1, 1, "Inner ");


		// Create sentence provider
		DocumentSentenceProvider sentenceProvider = createSentenceProvider (this.textDocument);

		// Test sentence provider
		String[] expectedParagraphs = {
				"Outer A1",
				"Outer B1",
				"Outer C1",
				"Outer A2",
				"Inner A1",
				"Outer B2",
				"Outer C2",
				"Outer A3",
				"Outer B3",
				"Outer C3",
				""
		};

		testFixture (sentenceProvider, expectedParagraphs);


	}


	/**
	 * Single text section
	 * 
	 * @throws Exception
	 */
	@Test
	public void testTextSection() throws Exception {


		XText text = this.textDocument.getText();
		XTextCursor textCursor = text.createTextCursor();

		insertTextSection (this.textDocument, text, textCursor, "Test");


		// Create sentence provider
		DocumentSentenceProvider sentenceProvider = createSentenceProvider (this.textDocument);

		// Test sentence provider
		String[] expectedParagraphs = {
				"Test",
				""
		};

		testFixture (sentenceProvider, expectedParagraphs);


	}


	/**
	 * 1x1 table with embedded text section
	 * 
	 * @throws Exception
	 */
	@Test
	public void testOneOneTableTextSection() throws Exception {


		XText text = this.textDocument.getText();
		XTextCursor textCursor = text.createTextCursor();

		XTextTable textTable = insertTable (this.textDocument, text, textCursor, 1, 1, "");
		XText cellText = As.XText (textTable.getCellByName ("A1"));
		insertTextSection (this.textDocument, cellText, cellText.createTextCursor(), "Test");


		// Create sentence provider
		DocumentSentenceProvider sentenceProvider = createSentenceProvider (this.textDocument);


		// Test sentence provider
		String[] expectedParagraphs = {
				"Test",
				"A1",
				""
		};

		testFixture (sentenceProvider, expectedParagraphs);


	}

}
