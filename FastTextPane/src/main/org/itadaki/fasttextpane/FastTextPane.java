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

import java.awt.BorderLayout;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollBar;
import javax.swing.SwingUtilities;


/**
 * A scrolling container for a FastTextView
 */
public class FastTextPane extends JPanel implements AdjustmentListener, FastTextViewListener, MouseWheelListener {

	/**
	 * Serial version UID
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Contained FastTextView
	 * Thread safe by virtue of finality
	 */
	private final FastTextView fastTextView;

	/**
	 * The current Document size
	 */
	private AtomicInteger documentSize = new AtomicInteger();

	/**
	 * The Pane's scrollbar
	 * Thread safe by access serialisation through the Swing EDT
	 */
	private JScrollBar scrollBar;

	/**
	 * Prevents recursing back into FastTextView when notified of a viewport change
	 * Thread safe by access serialisation through the Swing EDT
	 */
	private boolean ignoreAdjustmentChange = false;


	/**
	 * Updates the scrollbar to reflect the current size of the document
	 */
	private void updateScrollBar () {

		final int value, maximum;

		int documentSize = this.documentSize.get();

		value = this.fastTextView.getParagraph();
		maximum = Math.max (0, documentSize - 1) + 10;

		Runnable scrollbarUpdater = new Runnable() {

			@Override
			public void run() {

				FastTextPane.this.ignoreAdjustmentChange = true;
				FastTextPane.this.scrollBar.setValues (value, 10, 0, maximum);
				FastTextPane.this.ignoreAdjustmentChange = false;
				
			}
			
		};

		if (SwingUtilities.isEventDispatchThread()) {
			scrollbarUpdater.run();
		} else {
			SwingUtilities.invokeLater (scrollbarUpdater);
		}

	}


	/* (non-Javadoc)
	 * @see javax.swing.JComponent#setComponentPopupMenu(javax.swing.JPopupMenu)
	 * 
	 * Thread safety: This method is not thread safe and should only be called
	 * from the Swing Event Dispatch Thread
	 */
	@Override
	public void setComponentPopupMenu (JPopupMenu popup) {

		super.setComponentPopupMenu (popup);
		this.fastTextView.setComponentPopupMenu (popup);
	}


	/* (non-Javadoc)
	 * @see javax.swing.JComponent#setInheritsPopupMenu(boolean)
	 * 
	 * Thread safety: This method is not thread safe and should only be called
	 * from the Swing Event Dispatch Thread
	 */
	@Override
	public void setInheritsPopupMenu (boolean value) {

		super.setInheritsPopupMenu (value);
		this.fastTextView.setInheritsPopupMenu (value);

	}


	/* (non-Javadoc)
	 * @see java.awt.event.AdjustmentListener#adjustmentValueChanged(java.awt.event.AdjustmentEvent)
	 * 
	 * Thread safety: This method is thread safe
	 */
	@Override
	public void adjustmentValueChanged (AdjustmentEvent e) {

		if (!this.ignoreAdjustmentChange) {
			this.fastTextView.setParagraph (e.getValue());
		}

	}


	/* (non-Javadoc)
	 * @see org.itadaki.fasttextpane.FastTextViewListener#displayOverflowing(boolean)
	 * 
	 * Thread safety: This method is thread safe
	 */
	@Override
	public boolean displayOverflowing (final boolean isOverflowing) {

		// FIXME not Swing threadsafe

//		return updateScrollBar (true, isOverflowing);

		boolean currentlyVisible = this.scrollBar.isVisible();

		Runnable scrollbarUpdater = new Runnable() {

			@Override
			public void run() {

				FastTextPane.this.ignoreAdjustmentChange = true;
				FastTextPane.this.scrollBar.setVisible (isOverflowing);

				FastTextPane.this.ignoreAdjustmentChange = false;
				
			}
			
		};

//		if (SwingUtilities.isEventDispatchThread()) {
			scrollbarUpdater.run();
//		} else {
//			SwingUtilities.invokeLater (scrollbarUpdater);
//		}

		return (isOverflowing != currentlyVisible);

	}


	/* (non-Javadoc)
	 * @see org.itadaki.fasttextpane.FastTextViewListener#positionChanged(int, int)
	 * 
 	 * Thread safety: This method is thread safe
	 */
	@Override
	public void positionChanged (int paragraphIndex, int lineIndex) {

		updateScrollBar ();

	}


	/* (non-Javadoc)
	 * @see org.itadaki.fasttextpane.FastTextViewListener#documentSizeChanged(int)
	 * Thread safety: This method is thread safe
	 */
	@Override
	public void documentSizeChanged (int newSize) {

		this.documentSize.set (newSize);

		updateScrollBar ();

	}


	/* (non-Javadoc)
	 * @see java.awt.event.MouseWheelListener#mouseWheelMoved(java.awt.event.MouseWheelEvent)
	 * 
	 * Thread safety: This method is thread safe
	 */
	@Override
	public void mouseWheelMoved (MouseWheelEvent e) {

		int lines = e.getWheelRotation();
		if (e.isControlDown()) {
			this.fastTextView.scrollPage (lines < 0);
		} else {
			this.fastTextView.scrollLines (2 * lines);
		}
		
	}


	/**
	 * Default constructor
	 * 
	 * @param fastTextView The FastTextView to wrap 
	 */
	public FastTextPane (FastTextView fastTextView) {

		super (new BorderLayout());

		this.scrollBar = new JScrollBar (JScrollBar.VERTICAL, 0, 10, 0, 10);
		this.scrollBar.addAdjustmentListener (this);
		this.scrollBar.setVisible (false);

		add (this.scrollBar, BorderLayout.EAST);

		setBorder (BorderFactory.createLoweredBevelBorder());
		this.fastTextView = fastTextView;
		this.fastTextView.addFastTextViewListener (this);
		this.fastTextView.addMouseWheelListener (this);

		add (this.fastTextView);


	}

}
