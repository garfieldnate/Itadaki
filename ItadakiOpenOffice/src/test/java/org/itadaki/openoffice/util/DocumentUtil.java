package org.itadaki.openoffice.util;

import org.itadaki.openoffice.util.As;
import org.itadaki.openoffice.util.OfficeUtil;

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
 * A class of static utility functions for creating test documents
 */
public class DocumentUtil {

	/**
	 * Reusable component context
	 */
	public static XComponentContext context = null;


	/**
	 * Creates a blank document
	 *
	 * @param show If <code>true</code>, the document will be shown
	 * @return The XComponent of the blank document
	 * @throws Exception
	 */
	public static XTextDocument createBlankDocument (boolean show) throws Exception {

		if (context == null) {
			context = Bootstrap.bootstrap();
		}

		XDesktop desktop = OfficeUtil.desktopFor (context);

		PropertyValue[] properties = new PropertyValue[1];
		properties[0] = new PropertyValue();
		properties[0].Name = "Hidden";
		properties[0].Value = new Boolean (!show);

		XComponentLoader componentLoader = As.XComponentLoader (desktop);
		XComponent component = componentLoader.loadComponentFromURL ("private:factory/swriter", "_blank", 0, properties);

		return As.XTextDocument (component);

	}


	/**
	 * Insert a blank paragraph
	 * @param text The text to insert into
	 * @param textCursor The text cursor to navigate with
	 * @param appendNewParagraph If <code>true</code>, a new blank paragraph will be appended
	 *
	 * @throws Exception
	 */
	public static void insertBlankParagraph (XText text, XTextCursor textCursor, boolean appendNewParagraph) throws Exception {

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
	public static void insertPlainParagraph (XText text, XTextCursor textCursor, String extraText, boolean appendNewParagraph) throws Exception {

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
	 * @param textDocument The document to insert into
	 * @param text The text to insert into
	 * @param textCursor The text cursor to navigate with
	 * @param extraText Additional text to append
	 * @param appendNewParagraph If <code>true</code>, a new blank paragraph will be appended
	 *
	 * @throws Exception
	 */
	public static void insertNumberedParagraph (XTextDocument textDocument, XText text, XTextCursor textCursor, String extraText, boolean appendNewParagraph) throws Exception {

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
	 * Insert a text section
	 *
	 * @param textDocument The document to insert into
	 * @param text The text to insert into
	 * @param textCursor The text cursor to navigate with
	 * @param content Textual content to insert into the text section
	 * @return The created text section
	 * @throws Exception
	 */
	public static XTextSection insertTextSection (XTextDocument textDocument, XText text, XTextCursor textCursor, String content) throws Exception {

		XMultiServiceFactory multiServiceFactory = As.XMultiServiceFactory (textDocument);

		XTextSection textSection = As.XTextSection (multiServiceFactory.createInstance ("com.sun.star.text.TextSection"));

		text.insertTextContent (textCursor, textSection, false);

		textSection.getAnchor().setString (content);

		return textSection;

	}


	/**
	 * Insert a table with textual content
	 *
	 * @param textDocument The document to insert into
	 * @param text The text to insert into
	 * @param textCursor The text cursor to navigate with
	 * @param rows The number of rows to create in the table
	 * @param columns The number of columns to create in the table
	 * @param cellTextPrefix A prefix to use for each cell's text
	 * @return The created table
	 * @throws Exception
	 */
	public static XTextTable insertTable (XTextDocument textDocument, XText text, XTextCursor textCursor, int rows, int columns, String cellTextPrefix) throws Exception {

		XMultiServiceFactory multiServiceFactory = As.XMultiServiceFactory (textDocument);

		XTextTable textTable = As.XTextTable (multiServiceFactory.createInstance ("com.sun.star.text.TextTable"));

		textTable.initialize (rows, columns);
		text.insertTextContent (textCursor, textTable, false);

		String[] cellNames = textTable.getCellNames();

		for (int i = 0; i < cellNames.length; i++) {

			XText cellText = As.XText (textTable.getCellByName (cellNames[i]));
			cellText.setString (cellTextPrefix + cellNames[i]);

		}

		return textTable;

	}


}
