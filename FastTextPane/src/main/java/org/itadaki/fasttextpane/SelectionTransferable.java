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

package org.itadaki.fasttextpane;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;


/**
 * A Transferable for the contents of a Selection
 */
public class SelectionTransferable implements Transferable {

	/**
	 * The Selection that will be transferred
	 */
	private Selection selection;


	/* (non-Javadoc)
	 * @see java.awt.datatransfer.Transferable#getTransferData(java.awt.datatransfer.DataFlavor)
	 */
	@Override
	public Object getTransferData (DataFlavor flavor) throws UnsupportedFlavorException, IOException {

		StringBuilder selectionStringBuilder = new StringBuilder();

		if (this.selection.isTextSelection()) {

			SelectionRange selectionRange = this.selection.selectionRange;
	
			AttributedString paragraph = this.selection.document.getParagraph (selectionRange.getParagraphIndex());
			AttributedCharacterIterator iterator = paragraph.getIterator();
			char c = iterator.setIndex (selectionRange.getStartCharacterIndex());
			while ((c != AttributedCharacterIterator.DONE)
					&& (iterator.getIndex() <= selectionRange.getEndCharacterIndex()))
			{
				selectionStringBuilder.append (c);
				c = iterator.next();
			}

		} else if (this.selection.isParagraphSelection()) {

			int startIndex = Math.min (this.selection.selectionStart.getParagraphIndex(), this.selection.selectionEnd.getParagraphIndex());
			int endIndex = Math.max (this.selection.selectionStart.getParagraphIndex(), this.selection.selectionEnd.getParagraphIndex());

			try {

				for (int i = startIndex; i <= endIndex; i++) {
					String paragraphText = this.selection.document.getPlainParagraph (i);
					selectionStringBuilder.append (paragraphText);
				}

				// This is an awful, awful hack and a fairly poor heuristic to boot.
				// If we successfully build a string containing a large selection and
				// return it, if there is then not at least as much heap space left
				// over as the built string occupies, then the Swing EDT will proceed
				// to explode fatally and not come back. To try to avoid this, we
				// attempt to allocate a second StringBuilder somewhat larger than the
				// first. If we succeed, it passes out of scope and gets GC'd
				// (hopefully leaving the EDT enough headroom to do its thing); if
				// not, we return an empty string rather than the selected text we
				// were going to.
				// See also FastTextView#getSelectedText()
				// TODO Provide visual feedback for a failed clipboard action
				// TODO Boink Sun over the head until they provide a fix or at least a sensible strategy for dealing with this
				new StringBuilder().ensureCapacity ((int) (selectionStringBuilder.capacity() * 1.5));

			} catch (OutOfMemoryError e) {

				selectionStringBuilder = new StringBuilder();

			}

		}

		return selectionStringBuilder.toString();
		
	}


	/* (non-Javadoc)
	 * @see java.awt.datatransfer.Transferable#getTransferDataFlavors()
	 */
	@Override
	public DataFlavor[] getTransferDataFlavors() {

        return new DataFlavor[]{
            DataFlavor.stringFlavor
        };

	}


	/* (non-Javadoc)
	 * @see java.awt.datatransfer.Transferable#isDataFlavorSupported(java.awt.datatransfer.DataFlavor)
	 */
	@Override
	public boolean isDataFlavorSupported(DataFlavor flavor) {

        return DataFlavor.stringFlavor.equals(flavor);

    }


	/**
	 * @param selection The Selection that will be transferred
	 */
	public SelectionTransferable (Selection selection) {

		this.selection = selection;

	}

}
