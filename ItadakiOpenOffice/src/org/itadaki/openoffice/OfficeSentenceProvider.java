/*
 * Copyright (C) 2006-2007
 * Matt Francis <asbel@neosheffield.co.uk>
 * 
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 */

package org.itadaki.openoffice;

import java.util.ArrayList;
import java.util.List;

import net.java.sen.dictionary.Reading;

import org.itadaki.client.furigana.SentenceListener;
import org.itadaki.client.furigana.SentenceProvider;

import com.sun.star.beans.XPropertySet;
import com.sun.star.beans.XPropertyState;
import com.sun.star.frame.XController;
import com.sun.star.frame.XFrame;
import com.sun.star.frame.XModel;
import com.sun.star.lang.EventObject;
import com.sun.star.lang.XEventListener;
import com.sun.star.text.RubyAdjust;
import com.sun.star.text.XParagraphCursor;
import com.sun.star.text.XText;
import com.sun.star.text.XTextCursor;
import com.sun.star.text.XTextDocument;
import com.sun.star.text.XTextRange;
import com.sun.star.text.XTextRangeCompare;
import com.sun.star.text.XTextViewCursor;
import com.sun.star.text.XTextViewCursorSupplier;
import com.sun.star.uno.UnoRuntime;


/**
 * A SentenceProvider to pass sentences from OpenOffice to the Furigana Wizard
 */
public class OfficeSentenceProvider implements SentenceProvider {

	/**
	 * The XTextDocument the analysed text lies within
	 */
	private XTextDocument textDocument;

	/**
	 * The document's view cursor
	 */
	private XTextViewCursor viewCursor;

	/**
	 * The XParagraphCursor from which sentences are read
	 */
	private XParagraphCursor paragraphCursor;

	/**
	 * A SentenceListener to notify of changes to the source text
	 */
	private SentenceListener sentenceListener;

	/**
	 * An OfficeSentenceProviderListener to notify of changes in the selected
	 * region
	 */
	private OfficeSentenceProviderListener providerListener;


	/**
	 * Sets an OfficeSentenceProviderListener to notify of changes in the
	 * selected region
	 *
	 * @param providerListener The listener to set
	 */
	public void setOfficeSentenceProviderListener (OfficeSentenceProviderListener providerListener) {

		this.providerListener = providerListener;

	}


	/* (non-Javadoc)
	 * @see org.itadaki.client.furigana.SentenceProvider#getReadings()
	 */
	public List<Reading> getReadings() {

		ArrayList<Reading> readings = new ArrayList<Reading>();

		try {

			this.paragraphCursor.gotoStartOfParagraph (false);
			this.paragraphCursor.gotoEndOfParagraph (true);

			int position = 0;
			int rubyStart = 0;
			String rubyText = "";

			TextPortionIterator iterator = new TextPortionIterator (this.paragraphCursor);
			while (iterator.hasNext()) {

				Object portion = iterator.next();

				XPropertySet portionProperties = (XPropertySet) UnoRuntime.queryInterface (XPropertySet.class, portion);
				String textPortionType = (String)portionProperties.getPropertyValue ("TextPortionType");

				if (textPortionType.equals("Text")) {

					XTextRange textRange = (XTextRange) UnoRuntime.queryInterface (XTextRange.class, portion);
					position += textRange.getString().length();

				} else if (textPortionType.equals("Ruby")) {

					if (Boolean.TRUE.equals (portionProperties.getPropertyValue ("IsStart"))) {
						rubyStart = position;
						rubyText = portionProperties.getPropertyValue("RubyText").toString();
					} else {
						int rubyLength = position - rubyStart;
						if (!("".equals(rubyText))) {
							readings.add (new Reading (rubyStart, rubyLength, rubyText));
						}
					}

				}

			}


		} catch (Throwable t) {
			ExceptionHelper.dealWith (t);
		}

		return readings;

	}


	/* (non-Javadoc)
	 * @see org.itadaki.client.furigana.SentenceProvider#getText()
	 */
	public String getText() {

		StringBuilder builder = new StringBuilder();

		try {

			this.paragraphCursor.gotoStartOfParagraph (false);
			this.paragraphCursor.gotoEndOfParagraph (true);
			this.viewCursor.gotoRange (this.paragraphCursor, false);
			this.viewCursor.gotoRange (this.paragraphCursor, true);


			TextPortionIterator iterator = new TextPortionIterator (this.paragraphCursor);
			while (iterator.hasNext()) {

				Object portion = iterator.next();

				XPropertySet portionProperties = (XPropertySet) UnoRuntime.queryInterface (XPropertySet.class, portion);
				String textPortionType = (String)portionProperties.getPropertyValue ("TextPortionType");

				if (textPortionType.equals ("Text")) {

					XTextRange textRange = (XTextRange) UnoRuntime.queryInterface (XTextRange.class, portion);
					String content = textRange.getString();
					builder.append (content);

				}

			}

		} catch (Throwable t) {
			ExceptionHelper.dealWith (t);
		}

		return builder.toString();

	}


	/* (non-Javadoc)
	 * @see org.itadaki.client.furigana.SentenceProvider#hasNext()
	 */
	public boolean hasNext() {

		boolean hasNext = false;

    	try {

    		XTextRange originalViewRange = this.viewCursor.getText().createTextCursorByRange (this.viewCursor);
        	XTextRange originalParagraphRange = this.paragraphCursor.getText().createTextCursorByRange (this.viewCursor);

        	this.paragraphCursor.gotoEndOfParagraph (false);
        	this.viewCursor.gotoRange (this.paragraphCursor, false);
    		if (this.viewCursor.goRight ((short)1, false)) {
    			hasNext = true;
    		}

    		this.viewCursor.gotoRange (originalViewRange, false);
    		this.paragraphCursor.gotoRange (originalParagraphRange, false);

    	} catch (Throwable t) {
        	ExceptionHelper.dealWith (t);
        }

		return hasNext;

	}


	/* (non-Javadoc)
	 * @see org.itadaki.client.furigana.SentenceProvider#hasPrevious()
	 */
	public boolean hasPrevious() {

		boolean hasPrevious = false;

        try {

        	XTextRange originalPosition = this.viewCursor.getEnd();

        	XText text;
        	XTextRangeCompare comparator;

        	this.paragraphCursor.gotoStartOfParagraph (false);
        	this.viewCursor.gotoRange (this.paragraphCursor.getStart(), false);
    		if (this.viewCursor.goLeft ((short)1, false)) {
    			XTextCursor textCursor = this.viewCursor.getText().createTextCursor();
    			XParagraphCursor paragraphCursor = (XParagraphCursor) UnoRuntime.queryInterface (XParagraphCursor.class, textCursor);
    			paragraphCursor.gotoRange (this.viewCursor, false);

    			// If there is a paragraph bullet or numbering, we need to go back one further
    			if (!paragraphCursor.isEndOfParagraph()) {
    				if (this.viewCursor.goLeft ((short)1, false)) {
    					// Final inner magic: funny bullet behaviour at start of document. Check if we're there
    					text = this.viewCursor.getText();
    					comparator = (XTextRangeCompare) UnoRuntime.queryInterface (XTextRangeCompare.class, text);
    					if (comparator.compareRegionStarts(this.viewCursor.getStart(), text.getStart()) != 0) { 
    						hasPrevious = true;
    					}
    				}
    			} else {
    				hasPrevious = true;
    			}
    		}

    		this.viewCursor.gotoRange (originalPosition, false);

        } catch (Throwable t) {
        	ExceptionHelper.dealWith (t);
        }

		return hasPrevious;

	}


	/* (non-Javadoc)
	 * @see org.itadaki.client.furigana.SentenceProvider#next()
	 */
	public void next() {

        try {

        	this.paragraphCursor.gotoEndOfParagraph (false);
			this.viewCursor.gotoRange (this.paragraphCursor, false);
			this.viewCursor.goRight ((short)1, false);

			XTextCursor textCursor = this.viewCursor.getText().createTextCursor();
			this.paragraphCursor = (XParagraphCursor) UnoRuntime.queryInterface (XParagraphCursor.class, textCursor);

			this.paragraphCursor.gotoRange (this.viewCursor.getEnd(), false);
			this.paragraphCursor.gotoStartOfParagraph (false);
			this.paragraphCursor.gotoEndOfParagraph (true);

			if (this.providerListener != null) {
				this.providerListener.regionChanged (textCursor);
			}

        } catch (Throwable t) {
        	ExceptionHelper.dealWith (t);
        }

	}


	/* (non-Javadoc)
	 * @see org.itadaki.client.furigana.SentenceProvider#previous()
	 */
	public void previous() {

        try {

        	this.paragraphCursor.gotoStartOfParagraph (false);
			this.viewCursor.gotoRange (this.paragraphCursor.getStart(), false);
			this.viewCursor.goLeft ((short)1, false);

			XTextCursor textCursor = this.viewCursor.getText().createTextCursor();
			this.paragraphCursor = (XParagraphCursor) UnoRuntime.queryInterface (XParagraphCursor.class, textCursor);
			this.paragraphCursor.gotoRange (this.viewCursor, false);

			// If there is a paragraph bullet or numbering, we need to go back one further
			while ((!this.paragraphCursor.isEndOfParagraph()) || (this.paragraphCursor.isStartOfParagraph() && this.paragraphCursor.isEndOfParagraph())) {
				this.viewCursor.goLeft ((short)1, false);
				textCursor = this.viewCursor.getText().createTextCursor();
				this.paragraphCursor = (XParagraphCursor) UnoRuntime.queryInterface (XParagraphCursor.class, textCursor);
				this.paragraphCursor.gotoRange (this.viewCursor, false);
			}

			this.paragraphCursor.gotoStartOfParagraph (false);
			this.paragraphCursor.gotoEndOfParagraph (true);

			if (this.providerListener != null) {
				this.providerListener.regionChanged (textCursor);
			}

        } catch (Throwable t) {
        	ExceptionHelper.dealWith (t);
        }

	}


	/* (non-Javadoc)
	 * @see org.itadaki.client.furigana.SentenceProvider#setReadings(java.util.List)
	 */
	public void setReadings (List<Reading> readings) {

		XModel model = null;

		try {

			model = (XModel) UnoRuntime.queryInterface(XModel.class, this.textDocument); 
			model.lockControllers();


			this.paragraphCursor.gotoStartOfParagraph (false);
			this.paragraphCursor.gotoEndOfParagraph (true);
			this.viewCursor.gotoRange (this.paragraphCursor, false);
			XTextCursor textCursor = this.viewCursor.getText().createTextCursorByRange (this.paragraphCursor);
			XPropertySet propertySet = (XPropertySet)UnoRuntime.queryInterface (XPropertySet.class, textCursor);

			XPropertyState propertyState = (com.sun.star.beans.XPropertyState) UnoRuntime.queryInterface (XPropertyState.class, textCursor);
			propertyState.setPropertyToDefault ("RubyText");
			propertyState.setPropertyToDefault ("RubyAdjust");


			int index = 0;
			TextPortionIterator textPortionIterator = new TextPortionIterator (this.paragraphCursor);
			Object textPortion = textPortionIterator.nextTextPortion();
			for (Reading reading : readings) {

				// Find start
				XTextRange readingStart = null;
				XTextRange readingEnd = null;

				boolean startFound = false;
				while (!startFound && (textPortion != null)) {
					XTextRange textRange = (XTextRange) UnoRuntime.queryInterface (XTextRange.class, textPortion);
					String content = textRange.getString();
					if (reading.start < (index + content.length())) {
						textCursor.gotoRange (textRange.getStart(), false);
						textCursor.goRight ((short)(reading.start - index), false);
						readingStart = textCursor.getStart();
						startFound = true;
					} else {
						index += content.length();
						textPortion = textPortionIterator.nextTextPortion();
					}
				}


				// Find end
				boolean endFound = false;
				while (!endFound && (textPortion != null)) {
					XTextRange textRange = (XTextRange) UnoRuntime.queryInterface (XTextRange.class, textPortion);
					String content = textRange.getString();
					if ((reading.start + reading.length - 1) < (index + content.length())) {
						textCursor.gotoRange (textRange.getStart(), false);
						textCursor.goRight ((short)(reading.start + reading.length - index), false);
						readingEnd = textCursor.getStart();
						endFound = true;
					} else {
						index += content.length();
						textPortion = textPortionIterator.nextTextPortion();
					}
				}


				// Set reading
				textCursor.gotoRange (readingStart, false);
				textCursor.gotoRange (readingEnd, true);
				propertySet.setPropertyValue ("RubyText", reading.text);
				propertySet.setPropertyValue ("RubyAdjust", new Short ((short)RubyAdjust.CENTER.getValue()));

			}

        } catch (Throwable t) {
        	ExceptionHelper.dealWith (t);
        } finally {
        	if (model != null) {
        		model.unlockControllers();
        	}
        }

	}


	/* (non-Javadoc)
	 * @see org.itadaki.client.furigana.SentenceProvider#setSentenceListener(org.itadaki.client.furigana.SentenceListener)
	 */
	public void setSentenceListener (SentenceListener sentenceListener) {

		synchronized (this) {
			this.sentenceListener = sentenceListener;
		}

	}

	/* (non-Javadoc)
	 * @see org.itadaki.client.furigana.SentenceProvider#dispose()
	 */
	public void dispose() {

		SelectionManager.unlockSearchOnSelect();

	}


	/**
	 * @param frame The XFrame that contains the text's document
	 * @param textDocument The XTextDocument the analysed text lies within
	 * @param startFromViewCursor If <code>true</code>, start from the current view cursor position
	 */
	public OfficeSentenceProvider (XFrame frame, XTextDocument textDocument, boolean startFromViewCursor) {

		// Temporarily lock dictionary search-on-select. This lock will be held
		// until dispose() is called
		SelectionManager.lockSearchOnSelect();

		try {

			this.textDocument = textDocument;

			// Fetch view cursor
			XModel model = (XModel) UnoRuntime.queryInterface (XModel.class, textDocument);
			XController controller = model.getCurrentController();
			XTextViewCursorSupplier viewCursorSupplier = (XTextViewCursorSupplier) UnoRuntime.queryInterface(
					XTextViewCursorSupplier.class,
					controller
			);
			this.viewCursor = viewCursorSupplier.getViewCursor();

			// Create paragraph cursor
			XTextCursor textCursor;
			if (startFromViewCursor) {
				textCursor = this.viewCursor.getText().createTextCursor();
			} else {
				textCursor = textDocument.getText().createTextCursor();
			}

			this.paragraphCursor = (XParagraphCursor) UnoRuntime.queryInterface (XParagraphCursor.class, textCursor);

			if (startFromViewCursor) {
				this.viewCursor.collapseToStart();
				this.paragraphCursor.gotoRange (this.viewCursor, false);
			} else {
				this.paragraphCursor.gotoRange (textDocument.getText().getStart(), false);
			}

			this.paragraphCursor.gotoStartOfParagraph (false);
			this.paragraphCursor.gotoEndOfParagraph (true);


			XEventListener closeListener = new XEventListener() {

				/* (non-Javadoc)
				 * @see com.sun.star.lang.XEventListener#disposing(com.sun.star.lang.EventObject)
				 */
				public void disposing (EventObject arg0) {

					synchronized (this) {
						if (OfficeSentenceProvider.this.sentenceListener != null) {
							OfficeSentenceProvider.this.sentenceListener.documentClosed();
						}
					}

				}

			};

			// Attach listeners for document close events
			textDocument.addEventListener (closeListener);
			frame.addEventListener (closeListener);

		} catch (Throwable t) {
			ExceptionHelper.dealWith (t);
		}

	}


}
