package org.itadaki.openoffice;
import com.sun.star.beans.PropertyValue;
import com.sun.star.beans.XPropertySet;
import com.sun.star.comp.helper.Bootstrap;
import com.sun.star.container.XIndexAccess;
import com.sun.star.frame.XComponentLoader;
import com.sun.star.frame.XController;
import com.sun.star.frame.XDesktop;
import com.sun.star.frame.XFrame;
import com.sun.star.frame.XModel;
import com.sun.star.lang.XComponent;
import com.sun.star.lang.XMultiServiceFactory;
import com.sun.star.text.ControlCharacter;
import com.sun.star.text.XText;
import com.sun.star.text.XTextCursor;
import com.sun.star.text.XTextDocument;
import com.sun.star.uno.Any;
import com.sun.star.uno.XComponentContext;

import org.itadaki.client.furigana.SentenceProvider;
import org.itadaki.openoffice.util.As;
import org.itadaki.openoffice.util.OfficeUtil;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;


/**
 * Test of OfficeSentenceProvider
 */
public class OfficeSentenceProviderTest {

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
			assertEquals ((i != 0), sentenceProvider.hasPrevious());
			assertEquals ((i != (expectedParagraphs.length - 1)), sentenceProvider.hasNext());
			if (i != (expectedParagraphs.length - 1)) {
				sentenceProvider.next();
			}
		}

		for (int i = expectedParagraphs.length - 1; i >= 0; i--) {
			assertEquals (expectedParagraphs[i], sentenceProvider.getText());
			assertEquals ((i != 0), sentenceProvider.hasPrevious());
			assertEquals ((i != (expectedParagraphs.length - 1)), sentenceProvider.hasNext());
			if (i != 0) {
				sentenceProvider.previous();
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
	 * @param startFromViewCursor If <code>true</code>, start from the current view cursor position
	 * @return The constructed sentence provider
	 */
	private OfficeSentenceProvider createSentenceProvider (XComponent component, boolean startFromViewCursor) {

		XTextDocument textDocument = As.XTextDocument (component);
		XModel model = As.XModel (textDocument);
		XController controller = model.getCurrentController();
		XFrame frame = controller.getFrame();

		OfficeSentenceProvider sentenceProvider = new OfficeSentenceProvider (frame, textDocument, false);

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


	/* hasPrevious() / hasNext() tests */

	// TODO
	// Blank
	// Plain
	// Blank, Blank
	// Blank, Plain
	// Plain, Blank
	// Plain, Plain
	// Blank, Blank, Blank
	// Blank, Blank, Plain
	// Blank, Plain, Blank
	// Blank, Plain, Plain
	// Plain, Blank, Blank
	// Plain, Blank, Plain
	// Plain, Plain, Blank
	// Plain, Plain, Plain


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
		OfficeSentenceProvider sentenceProvider = createSentenceProvider (component, false);

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
		OfficeSentenceProvider sentenceProvider = createSentenceProvider (component, false);

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
		OfficeSentenceProvider sentenceProvider = createSentenceProvider (component, false);

		// Test sentence provider
		String[] expectedParagraphs = {
				"This is a plain paragraph",
				""
		};

		sentenceProviderTestFixture (sentenceProvider, expectedParagraphs);


		// Discard and close document
		component.dispose();

	}


// TODO
//	/**
//	 * Paragraph permutation tests:
//	 * Blank paragraph
//	 * Plain text paragraph
//	 *
//	 * @throws Exception
//	 */
//	@Test
//	public void testBlankPlain() throws Exception {
//
//		// Create text document
//		XComponent component = createBlankDocument (true);
//		XTextDocument textDocument = As.XTextDocument (component);
//		XText text = textDocument.getText();
//		XTextCursor textCursor = text.createTextCursor();
//
//		// Insert test data
//		insertBlankParagraph (text, textCursor, true);
//		insertPlainParagraph (text, textCursor, false);
//
//		// Create sentence provider
//		OfficeSentenceProvider sentenceProvider = createSentenceProvider (component);
//
//		// Test sentence provider
//		String[] expectedParagraphs = {
//				"",
//				"This is a plain paragraph"
//		};
//
//		sentenceProviderTestFixture (sentenceProvider, expectedParagraphs);
//
//
//		// Discard and close document
//		component.dispose();
//
//	}

//	TODO
//	/**
//	 * Paragraph permutation tests:
//	 * Blank paragraph
//	 * Plain text paragraph
//	 * Blank paragraph
//	 *
//	 * @throws Exception
//	 */
//	@Test
//	public void testBlankPlainBlank() throws Exception {
//
//		// Create text document
//		XComponent component = createBlankDocument (true);
//		XTextDocument textDocument = As.XTextDocument (component);
//		XText text = textDocument.getText();
//		XTextCursor textCursor = text.createTextCursor();
//
//		// Insert test data
//		insertPlainParagraph (text, textCursor, true);
//		insertBlankParagraph (text, textCursor, true);
//		insertPlainParagraph (text, textCursor, false);
//
//		// Create sentence provider
//		OfficeSentenceProvider sentenceProvider = createSentenceProvider (component);
//
//		// Test sentence provider
//		String[] expectedParagraphs = {
//				"",
//				"This is a plain paragraph",
//				""
//		};
//
//		sentenceProviderTestFixture (sentenceProvider, expectedParagraphs);
//
//
//		// Discard and close document
//		component.dispose();
//
//	}


// TODO
//	/**
//	 * Paragraph permutation tests:
//	 * Plain text paragraph
//	 * Blank paragraph
//	 * Plain text paragraph
//	 *
//	 * @throws Exception
//	 */
//	@Test
//	public void testPlainBlankPlain() throws Exception {
//
//		// Create text document
//		XComponent component = createBlankDocument (true);
//		XTextDocument textDocument = As.XTextDocument (component);
//		XText text = textDocument.getText();
//		XTextCursor textCursor = text.createTextCursor();
//
//		// Insert test data
//		insertPlainParagraph (text, textCursor, true);
//		insertBlankParagraph (text, textCursor, true);
//		insertPlainParagraph (text, textCursor, false);
//
//		// Create sentence provider
//		OfficeSentenceProvider sentenceProvider = createSentenceProvider (component);
//
//		// Test sentence provider
//		String[] expectedParagraphs = {
//				"This is a plain paragraph",
//				"",
//				"This is a plain paragraph"
//		};
//
//		sentenceProviderTestFixture (sentenceProvider, expectedParagraphs);
//
//
//		// Discard and close document
//		component.dispose();
//
//	}


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
		OfficeSentenceProvider sentenceProvider = createSentenceProvider (component, false);

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
		OfficeSentenceProvider sentenceProvider = createSentenceProvider (component, false);

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
		OfficeSentenceProvider sentenceProvider = createSentenceProvider (component, false);

		// Test sentence provider
		String[] expectedParagraphs = {
				"This is a numbered paragraph",
				""
		};

		sentenceProviderTestFixture (sentenceProvider, expectedParagraphs);


		// Discard and close document
		component.dispose();

	}


//	// TODO
//	/**
//	 * Paragraph permutation tests:
//	 * Blank paragraph
//	 * Number paragraph
//	 *
//	 * @throws Exception
//	 */
//	@Test
//	public void testBlankNumber() throws Exception {
//
//		// Create text document
//		XComponent component = createBlankDocument (true);
//		XTextDocument textDocument = As.XTextDocument (component);
//		XText text = textDocument.getText();
//		XTextCursor textCursor = text.createTextCursor();
//
//		// Insert test data
//		insertBlankParagraph (text, textCursor, true);
//		insertNumberedParagraph (component, textDocument, text, textCursor, false);
//
//		// Create sentence provider
//		OfficeSentenceProvider sentenceProvider = createSentenceProvider (component);
//
//		// Test sentence provider
//		String[] expectedParagraphs = {
//				"",
//				"This is a numbered paragraph"
//		};
//
//		sentenceProviderTestFixture (sentenceProvider, expectedParagraphs);
//
//
//		// Discard and close document
//		component.dispose();
//
//	}


//	// TODO
//	/**
//	 * Paragraph permutation tests:
//	 * Blank paragraph
//	 * Number paragraph
//	 * Blank paragraph
//	 *
//	 * @throws Exception
//	 */
//	@Test
//	public void testBlankNumberBlank() throws Exception {
//
//		// Create text document
//		XComponent component = createBlankDocument (true);
//		XTextDocument textDocument = As.XTextDocument (component);
//		XText text = textDocument.getText();
//		XTextCursor textCursor = text.createTextCursor();
//
//		// Insert test data
//		insertBlankParagraph (text, textCursor, true);
//		insertNumberedParagraph (component, textDocument, text, textCursor, true);
//		insertBlankParagraph (text, textCursor, false);
//
//		// Create sentence provider
//		OfficeSentenceProvider sentenceProvider = createSentenceProvider (component);
//
//		// Test sentence provider
//		String[] expectedParagraphs = {
//				"",
//				"This is a numbered paragraph",
//				""
//		};
//
//		sentenceProviderTestFixture (sentenceProvider, expectedParagraphs);
//
//
//		// Discard and close document
//		component.dispose();
//
//	}


// TODO
//	/**
//	 * Paragraph permutation tests:
//	 * Number paragraph
//	 * Blank paragraph
//	 * Number paragraph
//	 *
//	 * @throws Exception
//	 */
//	@Test
//	public void testNumberBlankNumber() throws Exception {
//
//		// Create text document
//		XComponent component = createBlankDocument (true);
//		XTextDocument textDocument = As.XTextDocument (component);
//		XText text = textDocument.getText();
//		XTextCursor textCursor = text.createTextCursor();
//
//		// Insert test data
//		insertNumberedParagraph (component, textDocument, text, textCursor, true);
//		insertBlankParagraph (text, textCursor, true);
//		insertNumberedParagraph (component, textDocument, text, textCursor, false);
//
//		// Create sentence provider
//		OfficeSentenceProvider sentenceProvider = createSentenceProvider (component);
//
//		// Test sentence provider
//		String[] expectedParagraphs = {
//				"This is a numbered paragraph",
//				"",
//				"This is a numbered paragraph"
//		};
//
//		sentenceProviderTestFixture (sentenceProvider, expectedParagraphs);
//
//
//		// Discard and close document
//		component.dispose();
//
//	}


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
		OfficeSentenceProvider sentenceProvider = createSentenceProvider (component, false);

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

}
