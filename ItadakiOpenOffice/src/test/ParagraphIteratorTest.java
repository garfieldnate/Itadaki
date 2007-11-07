package test;

import static org.junit.Assert.*;

import static test.util.DocumentUtil.*;

import org.itadaki.openoffice.ParagraphIterator;
import org.itadaki.openoffice.TextPortionIterator;
import org.itadaki.openoffice.util.As;
import org.itadaki.openoffice.util.OfficeUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.sun.star.frame.XController;
import com.sun.star.frame.XModel;
import com.sun.star.lang.XComponent;
import com.sun.star.text.XText;
import com.sun.star.text.XTextCursor;
import com.sun.star.text.XTextDocument;
import com.sun.star.text.XTextTable;


/**
 * Test of ParagraphIterator
 */
public class ParagraphIteratorTest {

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
	 * Tests the output of a paragraph iterator
	 *
	 * @param paragraphIterator The paragraph iterator to test
	 * @param expectedParagraphs The expected textual paragraph contents
	 * @throws Exception 
	 */
	private static void testFixture (ParagraphIterator paragraphIterator, String[] expectedParagraphs) throws Exception {

		for (int i = 0; i < expectedParagraphs.length; i++) {
			assertTrue (paragraphIterator.hasNext());
			assertEquals (expectedParagraphs[i], TextPortionIterator.getText (paragraphIterator.next()));
			assertEquals ((i != (expectedParagraphs.length - 1)), paragraphIterator.hasNext());
		}

	}


	/**
	 * Creates a sentence provider for the given component
	 *
	 * @param component The component to create the sentence provider for
	 * @return The constructed sentence provider
	 */
	private static ParagraphIterator createParagraphIterator (XComponent component) {

		XTextDocument textDocument = As.XTextDocument (component);

		ParagraphIterator paragraphIterator = new ParagraphIterator (textDocument);

		return paragraphIterator;

	}


	/**
	 * Creates a sentence provider for the given component's current selection
	 *
	 * @param component The component to create the sentence provider for
	 * @return The constructed sentence provider
	 */
	private static ParagraphIterator createSelectionParagraphIterator (XComponent component) {

		final XModel model = As.XModel (component);
		final XController controller = model.getCurrentController();
		Object selection = OfficeUtil.selectionFor (controller);

		ParagraphIterator paragraphIterator = new ParagraphIterator (selection);

		return paragraphIterator;

	}


	/**
	 * Paragraph permutation tests:
	 * Empty document
	 *
	 * @throws Exception 
	 */
	@Test
	public void testBlank() throws Exception {

		// Create sentence provider
		ParagraphIterator paragraphIterator = createParagraphIterator (this.textDocument);

		// Test sentence provider
		String[] expectedParagraphs = {
				""
		};

		testFixture (paragraphIterator, expectedParagraphs);

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
		ParagraphIterator paragraphIterator = createParagraphIterator (this.textDocument);

		// Test sentence provider
		String[] expectedParagraphs = {
				"This is a plain paragraph",
		};

		testFixture (paragraphIterator, expectedParagraphs);

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
		ParagraphIterator paragraphIterator = createParagraphIterator (this.textDocument);

		// Test sentence provider
		String[] expectedParagraphs = {
				"This is a plain paragraph",
				""
		};

		testFixture (paragraphIterator, expectedParagraphs);

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
		ParagraphIterator paragraphIterator = createParagraphIterator (this.textDocument);

		// Test sentence provider
		String[] expectedParagraphs = {
				"",
				"This is a plain paragraph"
		};

		testFixture (paragraphIterator, expectedParagraphs);

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
		ParagraphIterator paragraphIterator = createParagraphIterator (this.textDocument);

		// Test sentence provider
		String[] expectedParagraphs = {
				"",
				"This is a plain paragraph",
				""
		};

		testFixture (paragraphIterator, expectedParagraphs);

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
		ParagraphIterator paragraphIterator = createParagraphIterator (this.textDocument);

		// Test sentence provider
		String[] expectedParagraphs = {
				"This is a plain paragraph",
				"",
				"This is a plain paragraph"
		};

		testFixture (paragraphIterator, expectedParagraphs);

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
		ParagraphIterator paragraphIterator = createParagraphIterator (this.textDocument);

		// Test sentence provider
		String[] expectedParagraphs = {
				"This is a plain paragraph 1",
				"This is a plain paragraph 2",
				"This is a plain paragraph 3"
		};

		testFixture (paragraphIterator, expectedParagraphs);

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
		ParagraphIterator paragraphIterator = createParagraphIterator (this.textDocument);

		// Test sentence provider
		String[] expectedParagraphs = {
				"This is a numbered paragraph",
		};

		testFixture (paragraphIterator, expectedParagraphs);

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
		ParagraphIterator paragraphIterator = createParagraphIterator (this.textDocument);

		// Test sentence provider
		String[] expectedParagraphs = {
				"This is a numbered paragraph",
				""
		};

		testFixture (paragraphIterator, expectedParagraphs);

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
		ParagraphIterator paragraphIterator = createParagraphIterator (this.textDocument);

		// Test sentence provider
		String[] expectedParagraphs = {
				"",
				"This is a numbered paragraph"
		};

		testFixture (paragraphIterator, expectedParagraphs);

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
		ParagraphIterator paragraphIterator = createParagraphIterator (this.textDocument);

		// Test sentence provider
		String[] expectedParagraphs = {
				"",
				"This is a numbered paragraph",
				""
		};

		testFixture (paragraphIterator, expectedParagraphs);

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
		ParagraphIterator paragraphIterator = createParagraphIterator (this.textDocument);

		// Test sentence provider
		String[] expectedParagraphs = {
				"This is a numbered paragraph",
				"",
				"This is a numbered paragraph"
		};

		testFixture (paragraphIterator, expectedParagraphs);

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
		ParagraphIterator paragraphIterator = createParagraphIterator (this.textDocument);

		// Test sentence provider
		String[] expectedParagraphs = {
				"This is a numbered paragraph 1",
				"This is a numbered paragraph 2",
				"This is a numbered paragraph 3"
		};

		testFixture (paragraphIterator, expectedParagraphs);

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

		// Insert test data
		insertTable (this.textDocument, text, textCursor, 1, 1, "");


		// Create sentence provider
		ParagraphIterator paragraphIterator = createParagraphIterator (this.textDocument);


		// Test sentence provider
		String[] expectedParagraphs = {
				"A1",
				""
		};

		testFixture (paragraphIterator, expectedParagraphs);

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

		// Insert test data
		insertTable (this.textDocument, text, textCursor, 3, 3, "");


		// Create sentence provider
		ParagraphIterator paragraphIterator = createParagraphIterator (this.textDocument);


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

		testFixture (paragraphIterator, expectedParagraphs);

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

		// Insert test data
		XTextTable outerTable = insertTable (this.textDocument, text, textCursor, 3, 3, "Outer ");

		XText cellText = As.XText (outerTable.getCellByName ("B2"));
		insertTable (this.textDocument, cellText, cellText.createTextCursor(), 1, 1, "Inner ");


		// Create sentence provider
		ParagraphIterator paragraphIterator = createParagraphIterator (this.textDocument);

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

		testFixture (paragraphIterator, expectedParagraphs);

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

		// Insert test data
		insertTextSection (this.textDocument, text, textCursor, "Test");


		// Create sentence provider
		ParagraphIterator paragraphIterator = createParagraphIterator (this.textDocument);

		// Test sentence provider
		String[] expectedParagraphs = {
				"Test",
				""
		};

		testFixture (paragraphIterator, expectedParagraphs);

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

		// Insert test data
		XTextTable textTable = insertTable (this.textDocument, text, textCursor, 1, 1, "");
		XText cellText = As.XText (textTable.getCellByName ("A1"));
		insertTextSection (this.textDocument, cellText, cellText.createTextCursor(), "Test");


		// Create sentence provider
		ParagraphIterator paragraphIterator = createParagraphIterator (this.textDocument);


		// Test sentence provider
		String[] expectedParagraphs = {
				"Test",
				"A1",
				""
		};

		testFixture (paragraphIterator, expectedParagraphs);

	}


	/**
	 * Paragraph permutation tests:
	 * Selection of whole plain text paragraph
	 *
	 * @throws Exception
	 */
	@Test
	public void testSelectionPlain() throws Exception {

		XText text = this.textDocument.getText();
		XTextCursor textCursor = text.createTextCursor();

		// Insert test data
		insertPlainParagraph (text, textCursor, "", false);

		// Position view cursor
		XTextCursor viewCursor = OfficeUtil.viewCursorFor (this.textDocument.getCurrentController());
		viewCursor.gotoStart (false);
		viewCursor.gotoEnd (true);

		// Create sentence provider
		ParagraphIterator paragraphIterator = createSelectionParagraphIterator (this.textDocument);

		// Test sentence provider
		String[] expectedParagraphs = {
				"This is a plain paragraph",
		};

		testFixture (paragraphIterator, expectedParagraphs);

	}


	/**
	 * Paragraph permutation tests:
	 * Selection of plain text paragraph, start to middle
	 *
	 * @throws Exception
	 */
	@Test
	public void testSelectionPlainStartMiddle() throws Exception {

		XText text = this.textDocument.getText();
		XTextCursor textCursor = text.createTextCursor();

		// Insert test data
		insertPlainParagraph (text, textCursor, "", false);

		// Position view cursor
		XTextCursor viewCursor = OfficeUtil.viewCursorFor (this.textDocument.getCurrentController());
		viewCursor.gotoStart (false);
		viewCursor.goRight ((short)10, true);

		// Create sentence provider
		ParagraphIterator paragraphIterator = createSelectionParagraphIterator (this.textDocument);

		// Test sentence provider
		String[] expectedParagraphs = {
				"This is a ",
		};

		testFixture (paragraphIterator, expectedParagraphs);

	}


	/**
	 * Paragraph permutation tests:
	 * Selection of plain text paragraph, middle to middle
	 *
	 * @throws Exception
	 */
	@Test
	public void testSelectionPlainMiddleMiddle() throws Exception {

		XText text = this.textDocument.getText();
		XTextCursor textCursor = text.createTextCursor();

		// Insert test data
		insertPlainParagraph (text, textCursor, "", false);

		// Position view cursor
		XTextCursor viewCursor = OfficeUtil.viewCursorFor (this.textDocument.getCurrentController());
		viewCursor.gotoStart (false);
		viewCursor.goRight ((short)2, false);
		viewCursor.goRight ((short)10, true);

		// Create sentence provider
		ParagraphIterator paragraphIterator = createSelectionParagraphIterator (this.textDocument);

		// Test sentence provider
		String[] expectedParagraphs = {
				"is is a pl",
		};

		testFixture (paragraphIterator, expectedParagraphs);

	}


	/**
	 * Paragraph permutation tests:
	 * Selection of plain text paragraph, middle to end
	 *
	 * @throws Exception
	 */
	@Test
	public void testSelectionPlainMiddleEnd() throws Exception {

		XText text = this.textDocument.getText();
		XTextCursor textCursor = text.createTextCursor();

		// Insert test data
		insertPlainParagraph (text, textCursor, "", false);

		// Position view cursor
		XTextCursor viewCursor = OfficeUtil.viewCursorFor (this.textDocument.getCurrentController());
		viewCursor.gotoStart (false);
		viewCursor.goRight ((short)4, false);
		viewCursor.gotoEnd (true);

		// Create sentence provider
		ParagraphIterator paragraphIterator = createSelectionParagraphIterator (this.textDocument);

		// Test sentence provider
		String[] expectedParagraphs = {
				" is a plain paragraph",
		};

		testFixture (paragraphIterator, expectedParagraphs);

	}


	/**
	 * Paragraph permutation tests:
	 * Selection of two plain text paragraphs
	 *
	 * @throws Exception
	 */
	@Test
	public void testSelectionPlainPlain() throws Exception {

		XText text = this.textDocument.getText();
		XTextCursor textCursor = text.createTextCursor();

		// Insert test data
		insertPlainParagraph (text, textCursor, "", true);
		insertPlainParagraph (text, textCursor, "", false);

		// Position view cursor
		XTextCursor viewCursor = OfficeUtil.viewCursorFor (this.textDocument.getCurrentController());
		viewCursor.gotoStart (false);
		viewCursor.gotoEnd (true);

		// Create sentence provider
		ParagraphIterator paragraphIterator = createSelectionParagraphIterator (this.textDocument);

		// Test sentence provider
		String[] expectedParagraphs = {
				"This is a plain paragraph",
				"This is a plain paragraph"
		};

		testFixture (paragraphIterator, expectedParagraphs);

	}


	/**
	 * Paragraph permutation tests:
	 * Selection of two plain text paragraphs, middle to middle
	 *
	 * @throws Exception
	 */
	@Test
	public void testSelectionPlainPlainMiddleMiddle() throws Exception {

		XText text = this.textDocument.getText();
		XTextCursor textCursor = text.createTextCursor();

		// Insert test data
		insertPlainParagraph (text, textCursor, "", true);
		insertPlainParagraph (text, textCursor, "", false);

		// Position view cursor
		XTextCursor viewCursor = OfficeUtil.viewCursorFor (this.textDocument.getCurrentController());
		viewCursor.gotoStart (false);
		viewCursor.goRight ((short)2, false);
		viewCursor.goRight ((short)47, true);

		// Create sentence provider
		ParagraphIterator paragraphIterator = createSelectionParagraphIterator (this.textDocument);

		// Test sentence provider
		String[] expectedParagraphs = {
				"is is a plain paragraph",
				"This is a plain paragra"
		};

		testFixture (paragraphIterator, expectedParagraphs);

	}


	/**
	 * Paragraph permutation tests:
	 * Selection of three plain text paragraphs, middle to middle
	 *
	 * @throws Exception
	 */
	@Test
	public void testSelectionPlainPlainPlainMiddleMiddle() throws Exception {

		XText text = this.textDocument.getText();
		XTextCursor textCursor = text.createTextCursor();

		// Insert test data
		insertPlainParagraph (text, textCursor, "", true);
		insertPlainParagraph (text, textCursor, "", true);
		insertPlainParagraph (text, textCursor, "", false);

		// Position view cursor
		XTextCursor viewCursor = OfficeUtil.viewCursorFor (this.textDocument.getCurrentController());
		viewCursor.gotoStart (false);
		viewCursor.goRight ((short)2, false);
		viewCursor.goRight ((short)73, true);

		// Create sentence provider
		ParagraphIterator paragraphIterator = createSelectionParagraphIterator (this.textDocument);

		// Test sentence provider
		String[] expectedParagraphs = {
				"is is a plain paragraph",
				"This is a plain paragraph",
				"This is a plain paragra"
		};

		testFixture (paragraphIterator, expectedParagraphs);

	}


	/**
	 * Paragraph permutation tests:
	 * Selection within only cell of 1x1 table
	 * 
	 * @throws Exception
	 */
	@Test
	public void testSelection1x1TableInside() throws Exception {

		XText text = this.textDocument.getText();
		XTextCursor textCursor = text.createTextCursor();

		// Insert test data
		insertTable (this.textDocument, text, textCursor, 1, 1, "Test ");

		// Position view cursor
		XTextCursor viewCursor = OfficeUtil.viewCursorFor (this.textDocument.getCurrentController());

		viewCursor.gotoStart (false);
		viewCursor.goRight ((short)1, false);
		viewCursor.goRight ((short)4, true);

		// Create sentence provider
		ParagraphIterator paragraphIterator = createSelectionParagraphIterator (this.textDocument);

		// Test sentence provider
		String[] expectedParagraphs = {
				"est ",
		};

		testFixture (paragraphIterator, expectedParagraphs);

	}


	/**
	 * Paragraph permutation tests:
	 * Selection within first cell of 2x1 table
	 * 
	 * @throws Exception
	 */
	@Test
	public void testSelection1x2TableInsideFirst() throws Exception {

		XText text = this.textDocument.getText();
		XTextCursor textCursor = text.createTextCursor();

		// Insert test data
		insertTable (this.textDocument, text, textCursor, 2, 1, "Test ");

		// Position view cursor
		XTextCursor viewCursor = OfficeUtil.viewCursorFor (this.textDocument.getCurrentController());

		viewCursor.gotoStart (false);
		viewCursor.goRight ((short)1, false);
		viewCursor.goRight ((short)4, true);

		// Create sentence provider
		ParagraphIterator paragraphIterator = createSelectionParagraphIterator (this.textDocument);

		// Test sentence provider
		String[] expectedParagraphs = {
				"est ",
		};

		testFixture (paragraphIterator, expectedParagraphs);

	}


// TODO Hard to select single cell
//
//	/**
//	 * Paragraph permutation tests:
//	 * Selection of both cells of 1x2 table
//	 * 
//	 * @throws Exception
//	 */
//	@Test
//	public void testSelection1x2Table() throws Exception {
//
//		XText text = this.textDocument.getText();
//		XTextCursor textCursor = text.createTextCursor();
//
//		XTextTable table = insertTable (this.textDocument, text, textCursor, 1, 2, "Test ");
//		insertPlainParagraph (text, textCursor, "", false);
//
//		// Position view cursor
//		XCellRange range = As.XCellRange (table);
//		As.XSelectionSupplier (this.textDocument.getCurrentController()).select (range.getCellRangeByName ("A1:B1"));
//
////		XTextCursor viewCursor = OfficeUtil.viewCursorFor (this.textDocument.getCurrentController());
////		XPropertySet propertySet = (XPropertySet) UnoRuntime.queryInterface (XPropertySet.class, viewCursor);
////		System.out.println (propertySet.getPropertyValue ("TextTable"));
////		//for (Property property : propertySet.getPropertySetInfo().getProperties()) {
////		//	System.out.println (property.Name);
////		//}
//
//		// Create sentence provider
//		ParagraphIterator paragraphIterator = createSelectionParagraphIterator (this.textDocument);
//
//		// Test sentence provider
//		String[] expectedParagraphs = {
//				"Test A1",
//				"Test B1"
//		};
//
//		testFixture (paragraphIterator, expectedParagraphs);
//
//
//	}

	// TODO selection of multiple table cells


}
