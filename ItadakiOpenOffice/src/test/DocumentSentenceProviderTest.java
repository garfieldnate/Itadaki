package test;

import static org.junit.Assert.*;

import org.itadaki.client.furigana.SentenceProvider;
import org.itadaki.openoffice.DocumentSentenceProvider;
import org.itadaki.openoffice.util.As;
import org.itadaki.openoffice.util.OfficeUtil;
import org.junit.Test;

import com.sun.star.beans.PropertyValue;
import com.sun.star.beans.XPropertySet;
import com.sun.star.comp.helper.Bootstrap;
import com.sun.star.container.XIndexAccess;
import com.sun.star.frame.XComponentLoader;
import com.sun.star.frame.XDesktop;
import com.sun.star.lang.XComponent;
import com.sun.star.lang.XMultiServiceFactory;
import com.sun.star.text.ControlCharacter;
import com.sun.star.text.XText;
import com.sun.star.text.XTextCursor;
import com.sun.star.text.XTextDocument;
import com.sun.star.text.XTextSection;
import com.sun.star.text.XTextTable;
import com.sun.star.uno.Any;
import com.sun.star.uno.XComponentContext;


/**
 * Test of DocumentSentenceProvider
 */
public class DocumentSentenceProviderTest {

	/**
	 * Reusable component context
	 */
	private static XComponentContext context = null;


	/**
	 * Tests the output of a sentence provider by moving from start to end
	 * and end to start, checking the expected state at each point
	 *
	 * @param sentenceProvider The sentence provider to test
	 * @param expectedParagraphs The expected textual paragraph contents
	 */
	private void sentenceProviderTestFixture (SentenceProvider sentenceProvider, String[] expectedParagraphs) {
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
	 * Creates a blank document
	 *
	 * @param hidden If <code>true</code>, the document will be created hidden 
	 * @return The XComponent of the blank document
	 * @throws Exception
	 */
	private XComponent createBlankDocument (boolean hidden) throws Exception {
		
		if (context == null) {
			context = Bootstrap.bootstrap();
		}

		XDesktop desktop = OfficeUtil.desktopFor (context);

		PropertyValue[] properties = new PropertyValue[1];
		properties[0] = new PropertyValue();
		properties[0].Name = "Hidden";
		properties[0].Value = new Boolean (hidden); 

		XComponentLoader componentLoader = As.XComponentLoader (desktop);
		XComponent component = componentLoader.loadComponentFromURL ("private:factory/swriter", "_blank", 0, properties);

		return component;

	}


	/**
	 * Creates a sentence provider for the given component
	 *
	 * @param component The component to create the sentence provider for
	 * @return The constructed sentence provider
	 */
	private DocumentSentenceProvider createSentenceProvider (XComponent component) {

		XTextDocument textDocument = As.XTextDocument (component);

		DocumentSentenceProvider sentenceProvider = new DocumentSentenceProvider (textDocument);

		return sentenceProvider;

	}


	/**
	 * Insert a blank paragraph
	 * @param text The text to insert into
	 * @param textCursor The text cursor to navigate with
	 * @param appendNewParagraph If <code>true</code>, a new blank paragraph will be appended
	 *
	 * @throws Exception 
	 */
	private void insertBlankParagraph (XText text, XTextCursor textCursor, boolean appendNewParagraph) throws Exception {

		XPropertySet propertySet = As.XPropertySet (textCursor);
		textCursor.collapseToEnd();
		propertySet.setPropertyValue ("NumberingRules", Any.VOID);

		if (appendNewParagraph) {
			text.insertControlCharacter (textCursor.getEnd(), ControlCharacter.PARAGRAPH_BREAK, false);
		}

	}


	/**
	 * Insert a plain text paragraph
	 * @param text The text to insert into
	 * @param textCursor The text cursor to navigate with
	 * @param extraText Additional text to append
	 * @param appendNewParagraph If <code>true</code>, a new blank paragraph will be appended
	 *
	 * @throws Exception 
	 */
	private void insertPlainParagraph (XText text, XTextCursor textCursor, String extraText, boolean appendNewParagraph) throws Exception {

		XPropertySet propertySet = As.XPropertySet (textCursor);
		textCursor.collapseToEnd();
		propertySet.setPropertyValue ("NumberingRules", Any.VOID);
		textCursor.setString ("This is a plain paragraph" + extraText);

		if (appendNewParagraph) {
			text.insertControlCharacter (textCursor.getEnd(), ControlCharacter.PARAGRAPH_BREAK, false);
		}

	}


	/**
	 * Insert a numbered text paragraph
	 *
	 * @param component The component to insert into
	 * @param textDocument The document to insert into
 	 * @param text The text to insert into
	 * @param textCursor The text cursor to navigate with
	 * @param extraText Additional text to append
	 * @param appendNewParagraph If <code>true</code>, a new blank paragraph will be appended
	 * @throws Exception
	 */
	private void insertNumberedParagraph (XComponent component, XTextDocument textDocument, XText text, XTextCursor textCursor, String extraText, boolean appendNewParagraph) throws Exception {

		textCursor.collapseToEnd();
		textCursor.setString ("This is a numbered paragraph" + extraText);

		XMultiServiceFactory multiServiceFactory = As.XMultiServiceFactory (textDocument);

		XIndexAccess rulesIndexAccess = As.XIndexAccess (multiServiceFactory.createInstance ("com.sun.star.text.NumberingRules"));

		XPropertySet propertySet = As.XPropertySet (textCursor);
		propertySet.setPropertyValue ("NumberingRules", rulesIndexAccess);

		if (appendNewParagraph) {
			text.insertControlCharacter (textCursor.getEnd(), ControlCharacter.PARAGRAPH_BREAK, false);
		}

	}


	/**
	 * Insert a table with textual content
	 * 
	 * @param textDocument The document to insert into
 	 * @param text The text to insert into
	 * @param textCursor The text cursor to navigate with
	 * @param rows The number of rows to create in the table 
	 * @param columns The number of columns to create in the table
	 * @param cellContents An array of strings to insert into the table's cells
	 * @return The created table
	 * @throws Exception
	 */
	private XTextTable insertTable (XTextDocument textDocument, XText text, XTextCursor textCursor, int rows, int columns, String[] cellContents) throws Exception {

		XMultiServiceFactory multiServiceFactory = As.XMultiServiceFactory (textDocument);

		XTextTable textTable = As.XTextTable (multiServiceFactory.createInstance ("com.sun.star.text.TextTable"));

		textTable.initialize (rows, columns);
		text.insertTextContent (textCursor, textTable, false);

		String[] cellNames = textTable.getCellNames();

		for (int i = 0; i < cellContents.length; i++) {

			XText cellText = As.XText (textTable.getCellByName (cellNames[i]));
			cellText.setString (cellContents[i]);

		}

		return textTable;

	}


	/**
	 * Insert a text section
	 * 
	 * @param textDocument The document to insert into
 	 * @param text The text to insert into
	 * @param textCursor The text cursor to navigate with
	 * @param content Textual content to insert into the text section 
	 * @return The created text section
	 * @throws Exception
	 */
	private XTextSection insertTextSection (XTextDocument textDocument, XText text, XTextCursor textCursor, String content) throws Exception {

		XMultiServiceFactory multiServiceFactory = As.XMultiServiceFactory (textDocument);

		XTextSection textSection = As.XTextSection (multiServiceFactory.createInstance ("com.sun.star.text.TextSection"));
		
		text.insertTextContent (textCursor, textSection, false);

		textSection.getAnchor().setString (content);

		return textSection;

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

		// Create text document
		XComponent component = createBlankDocument (true);

		// Create sentence provider
		DocumentSentenceProvider sentenceProvider = createSentenceProvider (component);

		// Test sentence provider
		assertEquals ("", sentenceProvider.getText());
		assertFalse (sentenceProvider.hasPrevious());
		assertFalse (sentenceProvider.hasNext());


		// Discard and close document
		component.dispose();

	}


	/**
	 * Paragraph permutation tests:
	 * Plain text paragraph
	 *
	 * @throws Exception
	 */
	@Test
	public void testPlain() throws Exception {

		// Create text document
		XComponent component = createBlankDocument (true);
		XTextDocument textDocument = As.XTextDocument (component);
		XText text = textDocument.getText();
		XTextCursor textCursor = text.createTextCursor();

		// Insert test data
		insertPlainParagraph (text, textCursor, "", false);

		// Create sentence provider
		DocumentSentenceProvider sentenceProvider = createSentenceProvider (component);

		// Test sentence provider
		String[] expectedParagraphs = {
				"This is a plain paragraph",
		};

		sentenceProviderTestFixture (sentenceProvider, expectedParagraphs);


		// Discard and close document
		component.dispose();

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

		// Create text document
		XComponent component = createBlankDocument (true);
		XTextDocument textDocument = As.XTextDocument (component);
		XText text = textDocument.getText();
		XTextCursor textCursor = text.createTextCursor();

		// Insert test data
		insertPlainParagraph (text, textCursor, "", true);
		insertBlankParagraph (text, textCursor, false);

		// Create sentence provider
		DocumentSentenceProvider sentenceProvider = createSentenceProvider (component);

		// Test sentence provider
		String[] expectedParagraphs = {
				"This is a plain paragraph",
				""
		};

		sentenceProviderTestFixture (sentenceProvider, expectedParagraphs);


		// Discard and close document
		component.dispose();

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

		// Create text document
		XComponent component = createBlankDocument (true);
		XTextDocument textDocument = As.XTextDocument (component);
		XText text = textDocument.getText();
		XTextCursor textCursor = text.createTextCursor();

		// Insert test data
		insertBlankParagraph (text, textCursor, true);
		insertPlainParagraph (text, textCursor, "", false);

		// Create sentence provider
		DocumentSentenceProvider sentenceProvider = createSentenceProvider (component);

		// Test sentence provider
		String[] expectedParagraphs = {
				"",
				"This is a plain paragraph"
		};

		sentenceProviderTestFixture (sentenceProvider, expectedParagraphs);


		// Discard and close document
		component.dispose();

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

		// Create text document
		XComponent component = createBlankDocument (true);
		XTextDocument textDocument = As.XTextDocument (component);
		XText text = textDocument.getText();
		XTextCursor textCursor = text.createTextCursor();

		// Insert test data
		insertBlankParagraph (text, textCursor, true);
		insertPlainParagraph (text, textCursor, "", true);
		insertBlankParagraph (text, textCursor, false);

		// Create sentence provider
		DocumentSentenceProvider sentenceProvider = createSentenceProvider (component);

		// Test sentence provider
		String[] expectedParagraphs = {
				"",
				"This is a plain paragraph",
				""
		};

		sentenceProviderTestFixture (sentenceProvider, expectedParagraphs);


		// Discard and close document
		component.dispose();

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

		// Create text document
		XComponent component = createBlankDocument (true);
		XTextDocument textDocument = As.XTextDocument (component);
		XText text = textDocument.getText();
		XTextCursor textCursor = text.createTextCursor();

		// Insert test data
		insertPlainParagraph (text, textCursor, "", true);
		insertBlankParagraph (text, textCursor, true);
		insertPlainParagraph (text, textCursor, "", false);

		// Create sentence provider
		DocumentSentenceProvider sentenceProvider = createSentenceProvider (component);

		// Test sentence provider
		String[] expectedParagraphs = {
				"This is a plain paragraph",
				"",
				"This is a plain paragraph"
		};

		sentenceProviderTestFixture (sentenceProvider, expectedParagraphs);


		// Discard and close document
		component.dispose();

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

		// Create text document
		XComponent component = createBlankDocument (true);
		XTextDocument textDocument = As.XTextDocument (component);
		XText text = textDocument.getText();
		XTextCursor textCursor = text.createTextCursor();

		// Insert test data
		insertPlainParagraph (text, textCursor, " 1", true);
		insertPlainParagraph (text, textCursor, " 2", true);
		insertPlainParagraph (text, textCursor, " 3", false);

		// Create sentence provider
		DocumentSentenceProvider sentenceProvider = createSentenceProvider (component);

		// Test sentence provider
		String[] expectedParagraphs = {
				"This is a plain paragraph 1",
				"This is a plain paragraph 2",
				"This is a plain paragraph 3"
		};

		sentenceProviderTestFixture (sentenceProvider, expectedParagraphs);


		// Discard and close document
		component.dispose();

	}


	/**
	 * Paragraph permutation tests:
	 * Number paragraph
	 *
	 * @throws Exception
	 */
	@Test
	public void testNumber() throws Exception {

		// Create text document
		XComponent component = createBlankDocument (true);
		XTextDocument textDocument = As.XTextDocument (component);
		XText text = textDocument.getText();
		XTextCursor textCursor = text.createTextCursor();
		
		// Insert test data
		insertNumberedParagraph (component, textDocument, text, textCursor, "", false);

		// Create sentence provider
		DocumentSentenceProvider sentenceProvider = createSentenceProvider (component);

		// Test sentence provider
		String[] expectedParagraphs = {
				"This is a numbered paragraph",
		};

		sentenceProviderTestFixture (sentenceProvider, expectedParagraphs);


		// Discard and close document
		component.dispose();

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

		// Create text document
		XComponent component = createBlankDocument (true);
		XTextDocument textDocument = As.XTextDocument (component);
		XText text = textDocument.getText();
		XTextCursor textCursor = text.createTextCursor();
		
		// Insert test data
		insertNumberedParagraph (component, textDocument, text, textCursor, "", true);
		insertBlankParagraph (text, textCursor, false);

		// Create sentence provider
		DocumentSentenceProvider sentenceProvider = createSentenceProvider (component);

		// Test sentence provider
		String[] expectedParagraphs = {
				"This is a numbered paragraph",
				""
		};

		sentenceProviderTestFixture (sentenceProvider, expectedParagraphs);


		// Discard and close document
		component.dispose();

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

		// Create text document
		XComponent component = createBlankDocument (true);
		XTextDocument textDocument = As.XTextDocument (component);
		XText text = textDocument.getText();
		XTextCursor textCursor = text.createTextCursor();
		
		// Insert test data
		insertBlankParagraph (text, textCursor, true);
		insertNumberedParagraph (component, textDocument, text, textCursor, "", false);

		// Create sentence provider
		DocumentSentenceProvider sentenceProvider = createSentenceProvider (component);

		// Test sentence provider
		String[] expectedParagraphs = {
				"",
				"This is a numbered paragraph"
		};

		sentenceProviderTestFixture (sentenceProvider, expectedParagraphs);


		// Discard and close document
		component.dispose();

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

		// Create text document
		XComponent component = createBlankDocument (true);
		XTextDocument textDocument = As.XTextDocument (component);
		XText text = textDocument.getText();
		XTextCursor textCursor = text.createTextCursor();
		
		// Insert test data
		insertBlankParagraph (text, textCursor, true);
		insertNumberedParagraph (component, textDocument, text, textCursor, "", true);
		insertBlankParagraph (text, textCursor, false);

		// Create sentence provider
		DocumentSentenceProvider sentenceProvider = createSentenceProvider (component);

		// Test sentence provider
		String[] expectedParagraphs = {
				"",
				"This is a numbered paragraph",
				""
		};

		sentenceProviderTestFixture (sentenceProvider, expectedParagraphs);


		// Discard and close document
		component.dispose();

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

		// Create text document
		XComponent component = createBlankDocument (true);
		XTextDocument textDocument = As.XTextDocument (component);
		XText text = textDocument.getText();
		XTextCursor textCursor = text.createTextCursor();
		
		// Insert test data
		insertNumberedParagraph (component, textDocument, text, textCursor, "", true);
		insertBlankParagraph (text, textCursor, true);
		insertNumberedParagraph (component, textDocument, text, textCursor, "", false);

		// Create sentence provider
		DocumentSentenceProvider sentenceProvider = createSentenceProvider (component);

		// Test sentence provider
		String[] expectedParagraphs = {
				"This is a numbered paragraph",
				"",
				"This is a numbered paragraph"
		};

		sentenceProviderTestFixture (sentenceProvider, expectedParagraphs);


		// Discard and close document
		component.dispose();

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

		// Create text document
		XComponent component = createBlankDocument (true);
		XTextDocument textDocument = As.XTextDocument (component);
		XText text = textDocument.getText();
		XTextCursor textCursor = text.createTextCursor();
		
		// Insert test data
		insertNumberedParagraph (component, textDocument, text, textCursor, " 1", true);
		insertNumberedParagraph (component, textDocument, text, textCursor, " 2", true);
		insertNumberedParagraph (component, textDocument, text, textCursor, " 3", false);

		// Create sentence provider
		DocumentSentenceProvider sentenceProvider = createSentenceProvider (component);

		// Test sentence provider
		String[] expectedParagraphs = {
				"This is a numbered paragraph 1",
				"This is a numbered paragraph 2",
				"This is a numbered paragraph 3"
		};

		sentenceProviderTestFixture (sentenceProvider, expectedParagraphs);

		// Discard and close document
		component.dispose();

	}


	/**
	 * 1x1 table with text
	 * 
	 * @throws Exception
	 */
	@Test
	public void testOneOneTable() throws Exception {

		XComponent component = createBlankDocument (true);

		XTextDocument textDocument = As.XTextDocument (component);
		XText text = textDocument.getText();
		XTextCursor textCursor = text.createTextCursor();

		insertTable (textDocument, text, textCursor, 1, 1, new String[] {"Test"});


		// Create sentence provider
		DocumentSentenceProvider sentenceProvider = createSentenceProvider (component);


		// Test sentence provider
		String[] expectedParagraphs = {
				"Test",
				""
		};

		sentenceProviderTestFixture (sentenceProvider, expectedParagraphs);


		// Discard and close document
		component.dispose();

	}


	/**
	 * 3x3 table with text
	 * 
	 * @throws Exception
	 */
	@Test
	public void testThreeThreeTable() throws Exception {

		XComponent component = createBlankDocument (true);

		XTextDocument textDocument = As.XTextDocument (component);
		XText text = textDocument.getText();
		XTextCursor textCursor = text.createTextCursor();

		insertTable (textDocument, text, textCursor, 3, 3, new String[] {
					"Test 1", "Test 2", "Test 3",
					"Test 4", "Test 5", "Test 6",
					"Test 7", "Test 8", "Test 9"
		});


		// Create sentence provider
		DocumentSentenceProvider sentenceProvider = createSentenceProvider (component);


		// Test sentence provider
		String[] expectedParagraphs = {
				"Test 1",
				"Test 2",
				"Test 3",
				"Test 4",
				"Test 5",
				"Test 6",
				"Test 7",
				"Test 8",
				"Test 9",
				""
		};

		sentenceProviderTestFixture (sentenceProvider, expectedParagraphs);


		// Discard and close document
		component.dispose();

	}


	/**
	 * 3x3 table with embedded table in centre cell
	 * 
	 * @throws Exception
	 */
	@Test
	public void testEmbeddedTable() throws Exception {

		XComponent component = createBlankDocument (true);

		XTextDocument textDocument = As.XTextDocument (component);
		XText text = textDocument.getText();
		XTextCursor textCursor = text.createTextCursor();

		XTextTable outerTable = insertTable (textDocument, text, textCursor, 3, 3, new String[] {
					"Test 1", "Test 2", "Test 3",
					"Test 4", "", "Test 6",
					"Test 7", "Test 8", "Test 9"
		});

		XText cellText = As.XText (outerTable.getCellByName ("B2"));
		insertTable (textDocument, cellText, cellText.createTextCursor(), 1, 1, new String[] {
				"Inner Test 1"
		});


		// Create sentence provider
		DocumentSentenceProvider sentenceProvider = createSentenceProvider (component);

		// Test sentence provider
		String[] expectedParagraphs = {
				"Test 1",
				"Test 2",
				"Test 3",
				"Test 4",
				"Inner Test 1",
				"",
				"Test 6",
				"Test 7",
				"Test 8",
				"Test 9",
				""
		};

		sentenceProviderTestFixture (sentenceProvider, expectedParagraphs);


		// Discard and close document
		component.dispose();

	}


	/**
	 * Single text section
	 * 
	 * @throws Exception
	 */
	@Test
	public void testTextSection() throws Exception {

		XComponent component = createBlankDocument (true);

		XTextDocument textDocument = As.XTextDocument (component);
		XText text = textDocument.getText();
		XTextCursor textCursor = text.createTextCursor();

		insertTextSection (textDocument, text, textCursor, "Test");


		// Create sentence provider
		DocumentSentenceProvider sentenceProvider = createSentenceProvider (component);

		// Test sentence provider
		String[] expectedParagraphs = {
				"Test",
				""
		};

		sentenceProviderTestFixture (sentenceProvider, expectedParagraphs);


		// Discard and close document
		component.dispose();

	}


	/**
	 * 1x1 table with embedded text section
	 * 
	 * @throws Exception
	 */
	@Test
	public void testOneOneTableTextSection() throws Exception {

		XComponent component = createBlankDocument (true);

		XTextDocument textDocument = As.XTextDocument (component);
		XText text = textDocument.getText();
		XTextCursor textCursor = text.createTextCursor();

		XTextTable textTable = insertTable (textDocument, text, textCursor, 1, 1, new String[] {""});
		XText cellText = As.XText (textTable.getCellByName ("A1"));
		insertTextSection (textDocument, cellText, cellText.createTextCursor(), "Test");


		// Create sentence provider
		DocumentSentenceProvider sentenceProvider = createSentenceProvider (component);


		// Test sentence provider
		String[] expectedParagraphs = {
				"Test",
				"",
				""
		};

		sentenceProviderTestFixture (sentenceProvider, expectedParagraphs);


		// Discard and close document
		component.dispose();

	}

}
