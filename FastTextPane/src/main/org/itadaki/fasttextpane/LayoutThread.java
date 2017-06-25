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

import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;


/**
 * Thread to handle layout for a FastTextView
 * 
 * 
 */
/*
 * 
 * 
 * LAYOUT_NORMAL
 *   
 * 
 * LAYOUT_SUPPRESS_DURING_RESIZE
 *   
 * LAYOUT_SUPPRESS_UNTIL_RESIZE
 * 
 */
class LayoutThread extends Thread implements ComponentListener {

	/**
	 * Minimum time allowed between resizing and relayout/repaint 
	 */
	private static final int RESIZE_DELAY = 250;

	/**
	 * FastTextView to perform layout for
	 * Thread safe by virtue of finality
	 */
	private final FastTextView fastTextView;

	/**
	 * Time of the last resize, in milliseconds
	 * Thread safe by virtue of AtomicLong
	 */
	private AtomicLong resizeTime = new AtomicLong();

	/**
	 * Signals when a layout due to resize is required
	 * Inherently thread safe
	 */
	private BlockingQueue<Integer> queue = new ArrayBlockingQueue<Integer>(1);

	/**
	 * Causes the resize delay period to be waived for one cycle. Used to allow
	 * an instant layout when the resize was caused by a scrollbar materialising
	 * or dematerialising
	 * Thread safe by virtue of AtomicBoolean
	 */
	private AtomicBoolean resizeDelayLatch = new AtomicBoolean();

	/**
	 * The current layout thread state
	 */
	private State state = State.LAYOUT_NORMAL;


	/**
	 * Layout is handled according to a state machine with three possible states
	 */
	private enum State {

		/**
		 * Layout happens as soon as possible after being requested
		 */
		LAYOUT_NORMAL,

		/**
		 *   Layout is suppressed until no resize event has been received for a set
		 *   timeout after the last resize. This is used to avoid wastefully
		 *   reperforming layout while the FastTextView is resized by user dragging
		 */
		LAYOUT_SUPPRESS_DURING_RESIZE,

		/**
		 *   Layout is suppresed until one resize event has been received. This is
		 *   used to avoid "layout flash" on initially setting a Document into a
		 *   FastTextView. A layout that has a different overflow status to its
		 *   predecessor will call up to the containing FastTextPane through the
		 *   FastTextViewListener interface in order to trigger a change in the
		 *   scrollbar materialisation. It should then abort without requesting a
		 *   repaint to wait for the Swing Event Dispatch Thread to signal the
		 *   change in component bounds, which will in turn trigger a new layout
		 *   which will succeed. Between the abort and the final layout, all other
		 *   layout requests (for instance, from FastTextView#documentExpanded())
		 *   must be suppressed. For safety, although this should usually not
		 *   be necessary, a timeout also guards this state after which we revert
		 *   to LAYOUT_NORMAL
		 */
		LAYOUT_SUPPRESS_UNTIL_RESIZE

	}


	/**
	 * Resets the timer holding back re-layouts due to component resizes. Used
	 * by FastTextView.paintComponent to avoid resize and other layout requests
	 * tripping over each other unnecessarily.<br><br>
	 * 
	 * <b>Thread safety:</b> This method is thread safe
	 */
	public void resetTimer() {

		this.resizeTime.set (System.currentTimeMillis());

	}


	/**
	 * Signal that the resize delay should temporarily be ignored. Used by
	 * FastTextView layout to indicate that a following resize is due to
	 * scrollbar materialisation or dematerialisation and should not be
	 * subject to resize layout batching. This is not perfectly foolproof,
	 * but "nearly perfect" is enough to avoid exposing the user to
	 * "layout flash" when the scrollbar appears<br><br>
	 * TODO update docs
	 * 
	 * <b>Thread safety:</b> This method is thread safe
	 */
	public void setSuppressUntilResize() {

		this.resizeDelayLatch.set (false);
		synchronized (this) {
			this.state = State.LAYOUT_SUPPRESS_UNTIL_RESIZE;
		}

	}


	/**
	 * Perform FastTextView layout and repaint. Layout is performed immediately from the calling thread if:<br>
	 * 1) The layout lock is free (no other thread is simultaneously performing layout), and<br>
	 * 2) The caller is not in the Swing event dispatch thread<br><br>
	 * 
	 * In all other cases, a layout is queued to be performed through the layout thread at the
	 * earliest opportunity<br><br>
	 * 
	 * This strategy is optimal for single processor systems as it avoids unnecessary context switches.
	 * It may be slightly less than optimal for hyperthreading or SMP systems, although this is unlikely
	 * to make much practical difference.<br><br> 
	 * 
	 * <b>Thread safety:</b> This method is thread safe
	 */
	public void layout() {

		synchronized (this) {
			if (this.state != State.LAYOUT_SUPPRESS_UNTIL_RESIZE) {
				this.queue.offer (1);
			}
		}

//		if (!SwingUtilities.isEventDispatchThread() && this.FastTextView.renderBackBuffer (false)) {
//
//			if (FastTextView.DEBUG) {
//				System.out.println ("Layout: performed immediately from calling thread");
//			}
//
//			// If we succeeded in laying out immediately, queue a repaint
//			this.FastTextView.repaint();
//
//		} else {
//
//			if (FastTextView.DEBUG) {
//				if (SwingUtilities.isEventDispatchThread()) {
//					System.out.println ("Layout: delayed due to caller being in Swing EDT");
//				} else {
//					System.out.println ("Layout: delayed due to layout lock unavailability");
//				}
//			}
//
//			// Let the layout thread do the deed
//			this.resizeDelayLatch.set (false);
//			this.queue.offer (1);
//
//		}

	}


	/* (non-Javadoc)
	 * @see java.awt.event.ComponentAdapter#componentResized(java.awt.event.ComponentEvent)
	 * 
	 * Thread safety: This method is thread safe
	 */
	@Override
	public void componentResized(ComponentEvent e) {

		long currentTime = System.currentTimeMillis();

		synchronized (this) {

			switch (this.state) {

				case LAYOUT_NORMAL:
				case LAYOUT_SUPPRESS_DURING_RESIZE:
					this.state = State.LAYOUT_SUPPRESS_DURING_RESIZE;
					this.resizeTime.set (System.currentTimeMillis());
					this.queue.offer (1);
					break;

					
				case LAYOUT_SUPPRESS_UNTIL_RESIZE:
					this.state = State.LAYOUT_NORMAL;
					this.resizeTime.set (System.currentTimeMillis());
					this.queue.offer (1);
					break;

			}

			if (FastTextView.DEBUG) {
				System.out.println ("Resize at: " + currentTime + " milliseconds (-> " + this.state + ")");
			}

		}
		
	}


	/* (non-Javadoc)
	 * @see java.awt.event.ComponentAdapter#componentHidden(java.awt.event.ComponentEvent)
	 */
	@Override
	public void componentHidden(ComponentEvent e) {
		// Do nothing
	}


	/* (non-Javadoc)
	 * @see java.awt.event.ComponentAdapter#componentMoved(java.awt.event.ComponentEvent)
	 */
	@Override
	public void componentMoved(ComponentEvent e) {
		// Do nothing
	}


	/* (non-Javadoc)
	 * @see java.awt.event.ComponentAdapter#componentShown(java.awt.event.ComponentEvent)
	 */
	@Override
	public void componentShown(ComponentEvent e) {
		// Do nothing
	}


	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {

		while (true) {
			try {

				// TODO suspect thread safety       

				boolean ready = false;
				do {
					this.queue.take();
					State state;
					synchronized (this) {
						state = this.state;
					}
					switch (state) {
						case LAYOUT_NORMAL:
							ready = true;
							break;
						case LAYOUT_SUPPRESS_DURING_RESIZE:
							do {
								this.queue.poll (RESIZE_DELAY, TimeUnit.MILLISECONDS);
							} while (System.currentTimeMillis() < this.resizeTime.get() + RESIZE_DELAY);
							synchronized (this) {
								this.state = State.LAYOUT_NORMAL;
								ready = true;
							}
							break;
						case LAYOUT_SUPPRESS_UNTIL_RESIZE:
							do {
								this.queue.poll (RESIZE_DELAY, TimeUnit.MILLISECONDS);
								if (System.currentTimeMillis() >= this.resizeTime.get() + RESIZE_DELAY) {
									synchronized (this) {
										this.state = State.LAYOUT_NORMAL;
										ready = true;
									}
								}
								synchronized (this) {
									state = this.state;
								}
							} while ((state == State.LAYOUT_SUPPRESS_UNTIL_RESIZE) && !ready);
							break;
					}
				} while (!ready);
				

				if (FastTextView.DEBUG) {
					long now = System.currentTimeMillis();
					System.out.println ("Redraw: " + (now - this.resizeTime.get()) + " milliseconds since last resize");
				}

				this.fastTextView.renderBackBuffer (true);
				this.fastTextView.repaint();


			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

	}


	/**
	 * @param fastTextView The FastTextView to perform layout upon
	 */
	public LayoutThread (FastTextView fastTextView) {

		super ("FastTextView Layout");
		this.fastTextView = fastTextView;
		this.fastTextView.addComponentListener (this);

		setDaemon (true);

	}

}