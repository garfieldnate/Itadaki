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

import com.sun.star.beans.NamedValue;
import com.sun.star.frame.XFrame;
import com.sun.star.frame.XModel;
import com.sun.star.lang.IllegalArgumentException;
import com.sun.star.lang.XServiceInfo;
import com.sun.star.lib.uno.helper.WeakBase;
import com.sun.star.task.XJob;
import com.sun.star.text.XTextDocument;
import com.sun.star.uno.AnyConverter;
import com.sun.star.uno.Type;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;

/**
 * Job that sets up new frames for Itadaki
 */
public class NewFrameJob extends WeakBase implements XServiceInfo, XJob {

	/**
	 * XServiceInfo service names
	 */
	private static final String[] componentServiceNames = {
		"com.sun.star.task.Job"
	};

	/**
	 * Manages context menu interceptors on frames
	 */
	private static final FrameManager frameManager = new FrameManager();

	/**
	 * Manages selection monitoring on frames
	 */
	private static final SelectionManager selectionManager = new SelectionManager();

	/**
	 * Component context used to access UNO services
	 */
	private XComponentContext componentContext;


	/* XJob interface */

	/* (non-Javadoc)
	 * @see com.sun.star.task.XJob#execute(com.sun.star.beans.NamedValue[])
	 */
	public synchronized Object execute (NamedValue[] arguments) throws IllegalArgumentException {

		// Ensure application startup has been completed
		StartupJob.startup (this.componentContext);


		// Find the frame that has been focused

		NamedValue[] environment   = null;

		try {

			for (NamedValue namedValue : arguments) {
				if (namedValue.Name.equals ("Environment")) {
					environment = (NamedValue[]) AnyConverter.toArray (namedValue.Value);
				}
			}


			XModel model = null;

			for (NamedValue namedValue : environment) {
				if (namedValue.Name.equals ("Model")) {
					model = (XModel) AnyConverter.toObject (new Type (XModel.class), namedValue.Value);
				}
			}


			XFrame frame = model.getCurrentController().getFrame();


			// Set up the frame if it is a text document frame
			if (UnoRuntime.queryInterface (XTextDocument.class, model) != null) {
				frameManager.installFrame (frame);
				selectionManager.installFrame (frame);
			}

		} catch (Throwable t) {
			ExceptionHelper.dealWith (t);
		}

		return null;

	}


	/* XServiceInfo interface */

	/* (non-Javadoc)
	 * @see com.sun.star.lang.XServiceInfo#getImplementationName()
	 */
	public String getImplementationName() {

		return this.getClass().getName();

	}

	/* (non-Javadoc)
	 * @see com.sun.star.lang.XServiceInfo#getSupportedServiceNames()
	 */
	public String[] getSupportedServiceNames() {

		return componentServiceNames;

	}


	/* (non-Javadoc)
	 * @see com.sun.star.lang.XServiceInfo#supportsService(java.lang.String)
	 */
	public boolean supportsService (String service) {

		for (String serviceName : componentServiceNames) {
			if (serviceName.equals (service)) {
				return true;
			}
		}

		return false;

	}


	/**
	 * Job implementation constructor
	 * 
	 * @param componentContext The component context to access services through 
	 */
	public NewFrameJob (XComponentContext componentContext) {

		this.componentContext = componentContext;

	}


}