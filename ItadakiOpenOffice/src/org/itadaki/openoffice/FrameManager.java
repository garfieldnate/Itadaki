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

import java.util.HashSet;
import java.util.Set;

import com.sun.star.frame.XController;
import com.sun.star.frame.XFrame;
import com.sun.star.lang.EventObject;
import com.sun.star.lang.XEventListener;
import com.sun.star.ui.XContextMenuInterception;
import com.sun.star.uno.UnoRuntime;

/**
 * Manages the installation of context menu interceptors on new frames
 */
public class FrameManager implements XEventListener {

	/**
	 * A shared context menu interceptor that adds Itadaki commands to a frame
	 */
	private static final ContextMenuInterceptor contextMenuInterceptor = new ContextMenuInterceptor();

	/**
	 * A set of frames that have already been seen and set up
	 */
	private Set<XFrame> knownFrames = new HashSet<XFrame>();


	/**
	 * Installs our context menu interceptor on a frame
	 * 
	 * @param frame The frame to install the
	 */
	private void installContextMenuEntries (XFrame frame) {

		XController controller = frame.getController();
		XContextMenuInterception contextMenuInterception =	(XContextMenuInterception) UnoRuntime.queryInterface (
				XContextMenuInterception.class,
				controller
		);

		contextMenuInterception.registerContextMenuInterceptor (contextMenuInterceptor);

	}


	/**
	 * If the given frame has not previously been set up, installs our context
	 * menu interceptor 
	 *
	 * @param frame The frame to set up
	 */
	public void installFrame (XFrame frame) {

		synchronized (this.knownFrames) {

			if (!this.knownFrames.contains (frame)) {
				installContextMenuEntries (frame);
				frame.addEventListener (this);
				this.knownFrames.add (frame);
			}

		}

	}


	/* XEventListener interface */

	/* (non-Javadoc)
	 * @see com.sun.star.lang.XEventListener#disposing(com.sun.star.lang.EventObject)
	 */
	public void disposing (EventObject event) {

		synchronized (this.knownFrames) {

			XFrame frame = (XFrame) UnoRuntime.queryInterface (XFrame.class, event.Source);
			this.knownFrames.remove (frame);

		}
		
	}


}