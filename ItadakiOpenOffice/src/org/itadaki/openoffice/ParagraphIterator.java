package org.itadaki.openoffice;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Stack;

import org.itadaki.openoffice.util.As;
import org.itadaki.openoffice.util.OfficeUtil;

import com.sun.star.container.XEnumeration;
import com.sun.star.lang.WrappedTargetException;
import com.sun.star.text.XText;
import com.sun.star.text.XTextContent;
import com.sun.star.text.XTextDocument;
import com.sun.star.text.XTextTable;


/**
 * An iterator for the text paragraphs of a Writer document or selection
 */
public class ParagraphIterator implements Iterator<XTextContent> {

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
	 * The next text paragraph that will be returned. Calculated ahead of
	 * time to avoid duplicating work between {@link #hasNext} and
	 * {@link #next}, as {@link #hasNext} needs to calculate the next
	 * text paragraph in any case.
	 */
	private XTextContent nextParagraph = null;


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
				return As.XTextContent (this.paragraphEnumeration.nextElement());
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

			this.paragraphEnumeration = OfficeUtil.enumerationFor(container);

		}

	}


	/**
	 * An iterator for the paragraphs contained in the cells of an
	 * {@link XTextTable}. Proxy {@link TextParagraphIterator}s are used to
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

			XText nextCellText = As.XText (this.table.getCellByName (this.cellNames[this.nextCellIndex]));

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

			this.table = As.XTextTable (content);
			this.cellNames = this.table.getCellNames();

			moveToNextCell();

		}

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

			while ((candidateTextContent == null) || OfficeUtil.isTextTable (candidateTextContent)) {

				candidateTextContent = this.currentIterator.next();

				if (!OfficeUtil.isTextTable (candidateTextContent)) {
					this.nextParagraph = candidateTextContent;
					return;
				}
	
				this.contextStack.push (this.currentIterator);
				this.currentIterator = new TableParagraphIterator (candidateTextContent);

			}

		}

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
	 * @see org.itadaki.client.furigana.SentenceProvider#next()
	 */
	@Override
	public XTextContent next() {

		if (this.nextParagraph == null) {
			findNext();
		}

		XTextContent paragraph = this.nextParagraph;
		this.nextParagraph = null;

		return paragraph;

	}


	/* (non-Javadoc)
	 * @see java.util.Iterator#remove()
	 */
	@Override
	public void remove() {

		// We don't do that
		throw new UnsupportedOperationException();

	}


	/**
	 * @param textDocument The XTextDocument to process
	 */
	public ParagraphIterator (XTextDocument textDocument) {

		this.currentIterator = new TextParagraphIterator (textDocument.getText());

	}


}
