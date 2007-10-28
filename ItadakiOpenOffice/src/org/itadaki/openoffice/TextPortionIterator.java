package org.itadaki.openoffice;

import com.sun.star.beans.UnknownPropertyException;
import com.sun.star.beans.XPropertySet;
import com.sun.star.container.NoSuchElementException;
import com.sun.star.container.XEnumeration;
import com.sun.star.container.XEnumerationAccess;
import com.sun.star.lang.WrappedTargetException;
import com.sun.star.lang.XServiceInfo;
import com.sun.star.text.XTextContent;
import com.sun.star.text.XTextCursor;
import com.sun.star.text.XTextRange;
import com.sun.star.uno.UnoRuntime;

/**
 * Iterates over the text portions of a single text paragraph
 */
public class TextPortionIterator {

	/**
	 * An enumeration of text portions within the currently traversed
	 * paragraph
	 */
	private XEnumeration portionEnumeration;





	/**
	 * Determines if there are more portions to return
	 *
	 * @return <code>true</code> if there are more portions, otherwise
	 *         <code>false</code>
	 */
	public boolean hasNext() {

		return (this.portionEnumeration != null) && this.portionEnumeration.hasMoreElements();

	}


	/**
	 * Returns the next available text portion if any, or <code>null</code>
	 *
	 * @return The next available text portion if any, or <code>null</code>
	 * @throws NoSuchElementException
	 * @throws WrappedTargetException
	 */
	public Object next() throws NoSuchElementException, WrappedTargetException {

		return this.portionEnumeration.nextElement();

	}


	/**
	 * Returns the next text portion of type "Text"
	 *
	 * @return The next text portion if any, or <code>null</code>
	 * @throws WrappedTargetException 
	 * @throws NoSuchElementException 
	 * @throws UnknownPropertyException 
	 */
	public Object nextTextPortion() throws NoSuchElementException, WrappedTargetException, UnknownPropertyException {

		while (this.portionEnumeration.hasMoreElements()) {
			Object portion = this.portionEnumeration.nextElement();
			XPropertySet portionProperties = (XPropertySet) UnoRuntime.queryInterface (XPropertySet.class, portion);
			String textPortionType = (String)portionProperties.getPropertyValue ("TextPortionType");

			if (textPortionType.equals ("Text")) {
				return portion;
			}
		}
		
		return null;

	}


	/**
	 * Returns a limited prefix of the text contained in the given model cursor
	 *
	 * @param textCursor The model cursor to retrieve text from 
	 * @param maxLength The maximum length of text to return
	 * @return A string containing at most <code>maxLength</code> characters of
	 *         the model cursor's selection
	 * @throws UnknownPropertyException 
	 * @throws WrappedTargetException 
	 * @throws NoSuchElementException 
	 */
	public static String getText (XTextCursor textCursor, int maxLength) throws NoSuchElementException, WrappedTargetException, UnknownPropertyException {

		TextPortionIterator iterator = new TextPortionIterator (textCursor);
		StringBuilder builder = new StringBuilder();

		Object portion;
		while (((portion = iterator.nextTextPortion()) != null) && (builder.length() < maxLength)) {
			XTextRange textRange = (XTextRange) UnoRuntime.queryInterface (XTextRange.class, portion);
			builder.append (textRange.getString());
		}

		String prefix;
		if (builder.length() <= maxLength) {
			prefix = builder.toString();
		} else {
			prefix = builder.substring (0, maxLength);
		}

		// TODO http://www.openoffice.org/issues/show_bug.cgi?id=74054
		// Iterating over text sections inside table cells has some issues.
		if (prefix.equals ("")) {
			prefix = textCursor.getString();
			if (prefix.length() > maxLength) {
				prefix = prefix.substring (0, maxLength);
			}
		}

		return prefix;

	}


	/**
	 * @param paragraph A text paragraph to iterate the portions of
	 */
	public TextPortionIterator (XTextContent paragraph) {

		XServiceInfo serviceInfo = (XServiceInfo) UnoRuntime.queryInterface (XServiceInfo.class, paragraph);
		if (serviceInfo.supportsService ("com.sun.star.text.TextTable")) {
			throw new IllegalArgumentException();
		}

		XEnumerationAccess portionEnumerationAccess = (XEnumerationAccess) UnoRuntime.queryInterface (
				XEnumerationAccess.class,
				paragraph
		);
		this.portionEnumeration = portionEnumerationAccess.createEnumeration();

	}


	/**
	 * @param textCursor A model cursor to iterate the portions of. Only the
	 *                   first paragraph under the cursor is used.
	 * @throws WrappedTargetException 
	 */
	public TextPortionIterator (XTextCursor textCursor) throws WrappedTargetException {

		try {

			XEnumerationAccess paragraphEnumerationAccess = (XEnumerationAccess) UnoRuntime.queryInterface (
					XEnumerationAccess.class,
					textCursor
			);
			XEnumeration paragraphEnumeration = paragraphEnumerationAccess.createEnumeration();

			if (paragraphEnumeration.hasMoreElements()) {

				Object paragraph = paragraphEnumeration.nextElement();
				XServiceInfo paragraphServiceInfo = (XServiceInfo) UnoRuntime.queryInterface (XServiceInfo.class, paragraph);
	
				if (!paragraphServiceInfo.supportsService ("com.sun.star.text.TextTable")) {
	
					XEnumerationAccess portionEnumerationAccess = (XEnumerationAccess) UnoRuntime.queryInterface (
							XEnumerationAccess.class,
							paragraph
					);
					this.portionEnumeration = portionEnumerationAccess.createEnumeration();
	
				}

			}

		} catch (NoSuchElementException e) {
			// TODO http://www.openoffice.org/issues/show_bug.cgi?id=74054
			// Iterating over text sections inside table cells has some issues.
		}


	}

}