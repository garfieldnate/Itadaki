package org.itadaki.openoffice;

import org.itadaki.openoffice.util.As;
import org.itadaki.openoffice.util.OfficeUtil;

import com.sun.star.beans.UnknownPropertyException;
import com.sun.star.beans.XPropertySet;
import com.sun.star.container.NoSuchElementException;
import com.sun.star.container.XEnumeration;
import com.sun.star.lang.WrappedTargetException;
import com.sun.star.text.XTextContent;
import com.sun.star.text.XTextCursor;
import com.sun.star.text.XTextRange;


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
			XPropertySet portionProperties = As.XPropertySet (portion);
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
			XTextRange textRange = As.XTextRange (portion);
			builder.append (textRange.getString());
		}

		String prefix;
		if (builder.length() <= maxLength) {
			prefix = builder.toString();
		} else {
			prefix = builder.substring (0, maxLength);
		}

		return prefix;

	}


	/**
	 * Returns The complete text contained in the given paragraph
	 *
	 * @param paragraph The paragraph to read text from 
	 * @return A string the complete text of the paragraph
	 * @throws UnknownPropertyException 
	 * @throws WrappedTargetException 
	 * @throws NoSuchElementException 
	 */
	public static String getText (XTextContent paragraph) throws NoSuchElementException, WrappedTargetException, UnknownPropertyException {

		TextPortionIterator iterator = new TextPortionIterator (paragraph);
		StringBuilder builder = new StringBuilder();

		Object portion;
		while ((portion = iterator.nextTextPortion()) != null) {
			XTextRange textRange = As.XTextRange (portion);
			builder.append (textRange.getString());
		}

		return builder.toString();

	}


	/**
	 * @param paragraph A text paragraph to iterate the portions of
	 */
	public TextPortionIterator (XTextContent paragraph) {

		if (OfficeUtil.isTextTable (paragraph)) {
			throw new IllegalArgumentException();
		}

		this.portionEnumeration = OfficeUtil.enumerationFor (paragraph);

	}


	/**
	 * @param textCursor A model cursor to iterate the portions of. Only the
	 *                   first paragraph under the cursor is used.
	 * @throws WrappedTargetException 
	 */
	public TextPortionIterator (XTextCursor textCursor) throws WrappedTargetException {

		try {

			XEnumeration paragraphEnumeration = OfficeUtil.enumerationFor (textCursor);

			if (paragraphEnumeration.hasMoreElements()) {

				Object paragraph = paragraphEnumeration.nextElement();
	
				if (!OfficeUtil.isTextTable (paragraph)) {
	
					this.portionEnumeration = OfficeUtil.enumerationFor (paragraph);
	
				}

			}

		} catch (NoSuchElementException e) {
			// TODO http://www.openoffice.org/issues/show_bug.cgi?id=74054
			// Iterating over text sections inside table cells has some issues.
		}


	}

}