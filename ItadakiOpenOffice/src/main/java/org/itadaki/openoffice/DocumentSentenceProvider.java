package org.itadaki.openoffice;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.java.sen.dictionary.Reading;

import org.itadaki.client.furigana.SentenceListener;
import org.itadaki.client.furigana.SentenceProvider;
import org.itadaki.openoffice.util.As;
import org.itadaki.openoffice.util.OfficeUtil;

import com.sun.star.beans.XPropertySet;
import com.sun.star.beans.XPropertyState;
import com.sun.star.text.RubyAdjust;
import com.sun.star.text.XParagraphCursor;
import com.sun.star.text.XTextContent;
import com.sun.star.text.XTextCursor;
import com.sun.star.text.XTextDocument;
import com.sun.star.text.XTextRange;


/**
 * A SentenceProvider to iterate forwards (only) over a whole OpenOffice
 * Writer document.
 * It is assumed that this provider is used non-interactively, with
 * the model lock held.
 */
public class DocumentSentenceProvider implements SentenceProvider {

	/**
	 * The iterator used to retrieve text paragraphs
	 */
	private Iterator<XTextContent> paragraphIterator = null;

	/**
	 * The text paragraph currently under the SentenceProvider's cursor
	 */
	private XTextContent currentParagraph = null;

	/**
	 * If set, a lower bound beneath which readings should not be set within
	 * the current paragraph 
	 */
	private XTextRange paragraphLowerBound = null;

	/**
	 * If set, an upper bound above which readings should not be set within
	 * the current paragraph 
	 */
	private XTextRange paragraphUpperBound = null;

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
	@Override
	public List<Reading> getReadings() {

		ArrayList<Reading> readings = new ArrayList<Reading>();

		try {

			XParagraphCursor paragraphCursor = OfficeUtil.paragraphCursorFor (this.currentParagraph.getAnchor());

			paragraphCursor.gotoEndOfParagraph (true);

			int position = 0;
			int rubyStart = 0;
			String rubyText = "";

			TextPortionIterator iterator = new TextPortionIterator (paragraphCursor);
			while (iterator.hasNext()) {

				Object portion = iterator.next();

				XPropertySet portionProperties = As.XPropertySet (portion);
				String textPortionType = (String)portionProperties.getPropertyValue ("TextPortionType");

				if (textPortionType.equals ("Text")) {

					XTextRange textRange = As.XTextRange (portion);
					position += textRange.getString().length();

				} else if (textPortionType.equals ("Ruby")) {

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
	@Override
	public String getText() {

		StringBuilder builder = new StringBuilder();

		try {

			XParagraphCursor paragraphCursor = OfficeUtil.paragraphCursorFor (this.currentParagraph.getAnchor());
			paragraphCursor.gotoEndOfParagraph (true);

			TextPortionIterator iterator = new TextPortionIterator (paragraphCursor);
			while (iterator.hasNext()) {

				Object portion = iterator.next();

				XPropertySet portionProperties = As.XPropertySet (portion);
				String textPortionType = (String)portionProperties.getPropertyValue ("TextPortionType");

				if (textPortionType.equals ("Text")) {

					XTextRange textRange = As.XTextRange (portion);
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
	@Override
	public boolean hasNext() {

		return this.paragraphIterator.hasNext();

	}


	/* (non-Javadoc)
	 * @see org.itadaki.client.furigana.SentenceProvider#hasPrevious()
	 */
	@Override
	public boolean hasPrevious() {

		// We don't do "backwards"
		return false;

	}


	/* (non-Javadoc)
	 * @see org.itadaki.client.furigana.SentenceProvider#next()
	 */
	@Override
	public void next() {

		XTextContent nextParagraph = this.paragraphIterator.next();
		this.paragraphLowerBound = null;
		this.paragraphUpperBound = null;

		try {

			TextPortionIterator boundsIterator = null;
			Object textPortion = null;

			if ((this.currentParagraph == null) || !hasNext()) {
				boundsIterator = new TextPortionIterator (nextParagraph);
			}

			// Set lower bound if we are at the first paragraph
			if (this.currentParagraph == null) {
				textPortion = boundsIterator.next();
				this.paragraphLowerBound = As.XTextRange(textPortion).getStart();
			}

			// Set upper bound if we are at the last paragraph
			if (!hasNext()) {
				while (boundsIterator.hasNext()) {
					textPortion = boundsIterator.next();
				}
				this.paragraphUpperBound = As.XTextRange(textPortion).getEnd();
			}

		} catch (Exception e) {
			throw new IllegalStateException(e);
		}

		this.currentParagraph = nextParagraph;

		if (this.providerListener != null) {
			XParagraphCursor paragraphCursor = OfficeUtil.paragraphCursorFor (As.XTextRange (this.currentParagraph.getAnchor()));
			paragraphCursor.gotoEndOfParagraph (true);
			
			this.providerListener.regionChanged (paragraphCursor);
		}

	}


	/* (non-Javadoc)
	 * @see org.itadaki.client.furigana.SentenceProvider#previous()
	 */
	@Override
	public void previous() {

		// Can't
		throw new UnsupportedOperationException();

	}

	
	/* (non-Javadoc)
	 * @see org.itadaki.client.furigana.SentenceProvider#setReadings(java.util.List)
	 */
	@Override
	public void setReadings (List<Reading> readings) {

		try {

			XParagraphCursor paragraphCursor = OfficeUtil.paragraphCursorFor (this.currentParagraph.getAnchor());

			if (this.paragraphLowerBound != null) {
				paragraphCursor.gotoRange (this.paragraphLowerBound, false);
			}

			if (this.paragraphUpperBound != null) {
				paragraphCursor.gotoRange (this.paragraphUpperBound, true);
			} else {
				paragraphCursor.gotoEndOfParagraph (true);
			}

			XTextCursor textCursor = OfficeUtil.textCursorFor (paragraphCursor);
			XPropertySet propertySet = As.XPropertySet (textCursor);

			// Clear existing readings
			XPropertyState propertyState = As.XPropertyState (textCursor);
			propertyState.setPropertyToDefault ("RubyText");
			propertyState.setPropertyToDefault ("RubyAdjust");

			// Set new readings
			int index = 0;
			paragraphCursor.gotoRange(this.currentParagraph.getAnchor(), false);
			TextPortionIterator textPortionIterator = new TextPortionIterator (paragraphCursor);
			Object textPortion = textPortionIterator.nextTextPortion();
			for (Reading reading : readings) {

				// Find start
				XTextRange readingStart = null;
				XTextRange readingEnd = null;

				boolean startFound = false;
				while (!startFound && (textPortion != null)) {
					XTextRange textRange = As.XTextRange (textPortion);
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
					XTextRange textRange = As.XTextRange (textPortion);
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


				// Set single reading
				if (
						!(
								   ((this.paragraphLowerBound != null) && (OfficeUtil.compareRegionStarts (this.paragraphLowerBound, readingStart.getStart()) < 0))
								|| ((this.paragraphUpperBound != null) && (OfficeUtil.compareRegionStarts (this.paragraphUpperBound, readingEnd.getStart()) > 0))
						 )
				   )
				{
					textCursor.gotoRange (readingStart, false);
					textCursor.gotoRange (readingEnd, true);
					propertySet.setPropertyValue ("RubyText", reading.text);
					propertySet.setPropertyValue ("RubyAdjust", new Short ((short)RubyAdjust.CENTER.getValue()));
				}

			}

        } catch (Throwable t) {
        	ExceptionHelper.dealWith (t);
        }


	}

	
	/* (non-Javadoc)
	 * @see org.itadaki.client.furigana.SentenceProvider#setSentenceListener(org.itadaki.client.furigana.SentenceListener)
	 */
	@Override
	public void setSentenceListener (SentenceListener arg0) {

		// Do nothing. It is assumed that this iterator is invoked non-
		// interactively, with a model lock held. Document close events
		// should be impossible

	}


	/* (non-Javadoc)
	 * @see org.itadaki.client.furigana.SentenceProvider#dispose()
	 */
	@Override
	public void dispose() {

		// Nothing to do

	}


	/**
	 * @param selection A selection object implementing TextRanges or TableCellCursor
	 */
	public DocumentSentenceProvider (Object selection) {

		// Set up cursor over first paragraph
		this.paragraphIterator = new ParagraphIterator (selection);
		next();

	}


	/**
	 * @param textDocument The XTextDocument to process
	 */
	public DocumentSentenceProvider (XTextDocument textDocument) {

		// Set up cursor over first paragraph
		this.paragraphIterator = new ParagraphIterator (textDocument);
		next();

	}


}
