package org.itadaki.openoffice;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Stack;

import net.java.sen.dictionary.Reading;

import org.itadaki.client.furigana.SentenceListener;
import org.itadaki.client.furigana.SentenceProvider;

import com.sun.star.beans.XPropertySet;
import com.sun.star.beans.XPropertyState;
import com.sun.star.container.XEnumeration;
import com.sun.star.container.XEnumerationAccess;
import com.sun.star.frame.XModel;
import com.sun.star.lang.WrappedTargetException;
import com.sun.star.lang.XServiceInfo;
import com.sun.star.text.RubyAdjust;
import com.sun.star.text.XParagraphCursor;
import com.sun.star.text.XText;
import com.sun.star.text.XTextContent;
import com.sun.star.text.XTextCursor;
import com.sun.star.text.XTextDocument;
import com.sun.star.text.XTextRange;
import com.sun.star.text.XTextTable;
import com.sun.star.uno.UnoRuntime;


/**
 * A SentenceProvider to iterate forwards (only) over a whole OpenOffice
 * Writer document.
 * It is assumed that this provider is used non-interactively, with
 * the model lock held.
 */
public class DocumentSentenceProvider implements SentenceProvider {

	/**
	 * The document being iterated over
	 */
	private XTextDocument textDocument;

	/**
	 * A stack of text content iterators above the current iterator's context
	 * in the document tree 
	 */
	private Stack<Iterator<XTextContent>> contextStack = new Stack<Iterator<XTextContent>>(); 

	/**
	 * A text content iterator for the context currently being examined
	 */
	private Iterator<XTextContent> currentIterator;

	/**
	 * The text paragraph currently under the SentenceProvider's cursor
	 */
	private XTextContent currentParagraph = null;

	/**
	 * The next text paragraph that will be returned. Calculated ahead of
	 * time to avoid duplicating work between {@link #hasNext} and
	 * {@link #next}, as {@link #hasNext} needs to calculate the next
	 * text paragraph in any case.
	 */
	private XTextContent nextParagraph = null;

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


	/**
	 * An iterator for an {@link XEnumeration} of {@link XTextContent}s
	 */
	private static class TextParagraphIterator implements Iterator<XTextContent> {

		/**
		 * The enumeration currently being iterated over
		 */
		private XEnumeration paragraphEnumeration;


		/* (non-Javadoc)
		 * @see java.util.Iterator#hasNext()
		 */
		@Override
		public boolean hasNext() {

			return this.paragraphEnumeration.hasMoreElements();

		}

		/* (non-Javadoc)
		 * @see java.util.Iterator#next()
		 */
		@Override
		public XTextContent next() throws NoSuchElementException {

			try {
				XTextContent nextParagraph = (XTextContent) UnoRuntime.queryInterface (
						XTextContent.class,
						this.paragraphEnumeration.nextElement()
				);
				return nextParagraph;
			} catch (WrappedTargetException e) {
				// TODO throw appropriate unchecked exception
				e.printStackTrace();
			} catch (com.sun.star.container.NoSuchElementException e) {
				throw new NoSuchElementException();
			}

			return null;

		}

		/* (non-Javadoc)
		 * @see java.util.Iterator#remove()
		 */
		@Override
		public void remove() {

			throw new IllegalArgumentException();

		}

		/**
		 * @param container An object implementing an enumeration of {@link XTextContent}s
		 */
		public TextParagraphIterator (Object container) {

			XEnumerationAccess paragraphEnumerationAccess = (XEnumerationAccess) UnoRuntime.queryInterface (
					XEnumerationAccess.class,
					container
			);
			this.paragraphEnumeration = paragraphEnumerationAccess.createEnumeration();

		}

	}


	/**
	 * An iterator for the paragraphs contained in the cells of an
	 * {@link XTextTable}. Proxy {@link TextParagraphIterator} are used to
	 * supply paragraphs from each individual cell.  
	 */
	private static class TableParagraphIterator implements Iterator<XTextContent> {

		/**
		 * The table being iterated over
		 */
		private XTextTable table = null;

		/**
		 * The names of all the cells in the table
		 */
		private String[] cellNames = null;

		/**
		 * The index in {@link #cellNames} of the next cell to examine
		 */
		private int nextCellIndex = 0;

		/**
		 * The proxy {@link TextParagraphIterator} for the paragraphs of the current cell
		 */
		private TextParagraphIterator currentCellIterator = null;


		/**
		 * Sets up the next cell for iteration of its paragraphs
		 */
		private void moveToNextCell() {

			XText nextCellText = (XText) UnoRuntime.queryInterface (
					XText.class,
					this.table.getCellByName (this.cellNames[this.nextCellIndex])
			);

			this.currentCellIterator = new TextParagraphIterator (nextCellText);

			this.nextCellIndex++;

		}

		/* (non-Javadoc)
		 * @see java.util.Iterator#hasNext()
		 */
		@Override
		public boolean hasNext() {

			return ((this.nextCellIndex < this.cellNames.length) || this.currentCellIterator.hasNext());

		}

		/* (non-Javadoc)
		 * @see java.util.Iterator#next()
		 */
		@Override
		public XTextContent next() {

			if (!this.currentCellIterator.hasNext()) {
				if (this.nextCellIndex < this.cellNames.length) {
					moveToNextCell();
				}
			}

			if (this.currentCellIterator.hasNext()) {
				return this.currentCellIterator.next();
			}

			return null;

		}

		/* (non-Javadoc)
		 * @see java.util.Iterator#remove()
		 */
		@Override
		public void remove() {

			throw new IllegalArgumentException();

		}

		/**
		 * @param content The table to iterate over
		 */
		public TableParagraphIterator (XTextContent content) {

			this.table = (XTextTable) UnoRuntime.queryInterface (XTextTable.class, content);
			this.cellNames = this.table.getCellNames();

			moveToNextCell();

		}

	}


	/**
	 * Tests if a given object is a text table
	 * 
	 * @param textContent The object to be tested
	 * @return <code>true</code> if the object is a text table, otherwise <code>false</code>
	 */
	private static boolean isTextTable (Object textContent) {

		XServiceInfo textContentServiceInfo = (XServiceInfo) UnoRuntime.queryInterface (XServiceInfo.class, textContent);

		return textContentServiceInfo.supportsService ("com.sun.star.text.TextTable");

	}


	/**
	 * Finds and caches the next available text paragraph
	 */
	private void findNext() {

		this.nextParagraph = null;

		// Wind back down the stack until we find an iterator with content left, or the bottom
		while (!this.currentIterator.hasNext() && !this.contextStack.isEmpty()) {
			this.currentIterator = this.contextStack.pop();
		}

		// Walk into the document tree (if necessary) until we find a text paragraph 
		XTextContent candidateTextContent = null; 

		if (this.currentIterator.hasNext()) {

			while ((candidateTextContent == null) || isTextTable (candidateTextContent)) {

				candidateTextContent = this.currentIterator.next();

				if (!isTextTable (candidateTextContent)) {
					this.nextParagraph = candidateTextContent;
					return;
				}
	
				this.contextStack.push (this.currentIterator);
				this.currentIterator = new TableParagraphIterator (candidateTextContent);

			}

		}

	}


	/* (non-Javadoc)
	 * @see org.itadaki.client.furigana.SentenceProvider#getReadings()
	 */
	@Override
	public List<Reading> getReadings() {

		ArrayList<Reading> readings = new ArrayList<Reading>();

		try {

			XParagraphCursor paragraphCursor = (XParagraphCursor) UnoRuntime.queryInterface (
					XParagraphCursor.class,
					this.currentParagraph.getAnchor().getText().createTextCursorByRange (this.currentParagraph.getAnchor())
			);

			paragraphCursor.gotoEndOfParagraph (true);

			int position = 0;
			int rubyStart = 0;
			String rubyText = "";

			TextPortionIterator iterator = new TextPortionIterator (paragraphCursor);
			while (iterator.hasNext()) {

				Object portion = iterator.next();

				XPropertySet portionProperties = (XPropertySet) UnoRuntime.queryInterface (XPropertySet.class, portion);
				String textPortionType = (String)portionProperties.getPropertyValue ("TextPortionType");

				if (textPortionType.equals ("Text")) {

					XTextRange textRange = (XTextRange) UnoRuntime.queryInterface (XTextRange.class, portion);
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

			TextPortionIterator iterator = new TextPortionIterator (this.currentParagraph);
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
	@Override
	public boolean hasNext() {

		if (this.nextParagraph == null) {
			findNext();
		}
		return (this.nextParagraph != null);

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

		if (this.nextParagraph == null) {
			findNext();
		}
		this.currentParagraph = this.nextParagraph;
		this.nextParagraph = null;

		if (this.providerListener != null) {
			XParagraphCursor paragraphCursor = (XParagraphCursor) UnoRuntime.queryInterface (
					XParagraphCursor.class,
					this.currentParagraph.getAnchor().getText().createTextCursorByRange (this.currentParagraph.getAnchor())
			);
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
		throw new IllegalArgumentException();

	}

	
	/* (non-Javadoc)
	 * @see org.itadaki.client.furigana.SentenceProvider#setReadings(java.util.List)
	 */
	@Override
	public void setReadings (List<Reading> readings) {

		XModel model = null;

		try {

			model = (XModel) UnoRuntime.queryInterface(XModel.class, this.textDocument); 
			model.lockControllers();

			XParagraphCursor paragraphCursor = (XParagraphCursor) UnoRuntime.queryInterface (
					XParagraphCursor.class,
					this.currentParagraph.getAnchor().getText().createTextCursorByRange (this.currentParagraph.getAnchor())
			);

			paragraphCursor.gotoEndOfParagraph (true);

			XTextCursor textCursor = paragraphCursor.getText().createTextCursorByRange (paragraphCursor);
			XPropertySet propertySet = (XPropertySet)UnoRuntime.queryInterface (XPropertySet.class, textCursor);

			XPropertyState propertyState = (com.sun.star.beans.XPropertyState) UnoRuntime.queryInterface (XPropertyState.class, textCursor);
			propertyState.setPropertyToDefault ("RubyText");
			propertyState.setPropertyToDefault ("RubyAdjust");


			int index = 0;
			TextPortionIterator textPortionIterator = new TextPortionIterator (paragraphCursor);
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
	 * @param textDocument The XTextDocument to process
	 */
	public DocumentSentenceProvider (XTextDocument textDocument) {

		this.textDocument = textDocument;


		// Set up cursor over first paragraph

		XText text = textDocument.getText();

		this.currentIterator = new TextParagraphIterator (text);

		findNext();
		next();

	}


}
