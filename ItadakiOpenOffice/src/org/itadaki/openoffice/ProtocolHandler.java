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


import com.sun.star.beans.PropertyValue;
import com.sun.star.frame.DispatchDescriptor;
import com.sun.star.frame.XDesktop;
import com.sun.star.frame.XDispatch;
import com.sun.star.frame.XDispatchProvider;
import com.sun.star.frame.XStatusListener;
import com.sun.star.lang.XComponent;
import com.sun.star.lang.XInitialization;
import com.sun.star.lang.XMultiComponentFactory;
import com.sun.star.lang.XServiceInfo;
import com.sun.star.lib.uno.helper.WeakBase;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;
import com.sun.star.util.URL;


/**
 * OpenOffice command protocol handler
 */
public class ProtocolHandler extends WeakBase
                          implements XDispatchProvider, XDispatch, XInitialization, XServiceInfo
{

	/**
	 * XServiceInfo service names
	 */
	private static final String[] componentServiceNames = {
		"com.sun.star.frame.ProtocolHandler"
	};

	/**
	 * The component context to access services through
	 */
	private XComponentContext componentContext;

	/**
	 * The component context's desktop
	 */
	private XDesktop desktop;


	/* XInitialization interface */

	/* (non-Javadoc)
	 * @see com.sun.star.lang.XInitialization#initialize(java.lang.Object[])
	 */
	public void initialize (Object[] object) throws com.sun.star.uno.Exception {

		// Do nothing

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


	/* XDispatchProvider interface */

	/* (non-Javadoc)
	 * @see com.sun.star.frame.XDispatchProvider#queryDispatch(com.sun.star.util.URL, java.lang.String, int)
	 */
	public XDispatch queryDispatch (URL url, String targetFrameName, int searchFlags) {

		if (url.Protocol.equals ("org.itadaki.openoffice:")) {
			return this;
		}
		return null;

	}


	/* (non-Javadoc)
	 * @see com.sun.star.frame.XDispatchProvider#queryDispatches(com.sun.star.frame.DispatchDescriptor[])
	 */
	public XDispatch[] queryDispatches (DispatchDescriptor[] descriptors) {

		XDispatch[] dispatches = new XDispatch[descriptors.length];

		for (int i=0; i < descriptors.length; ++i)
		{
			dispatches[i] = queryDispatch (
					descriptors[i].FeatureURL,
					descriptors[i].FrameName,
					descriptors[i].SearchFlags
			);
		}

		return dispatches;

	}


	/* XDispatch interface */

	/* (non-Javadoc)
	 * @see com.sun.star.frame.XDispatch#dispatch(com.sun.star.util.URL, com.sun.star.beans.PropertyValue[])
	 */
	public void dispatch (URL url, PropertyValue[] arguments) {

		try {
			
			if (url.Protocol.equals ("org.itadaki.openoffice:")) {

				XComponent component = this.desktop.getCurrentComponent();

				if (url.Path.equals ("dictionary")) {

					Commands.dictionary (component);

				} else if (url.Path.equals ("furigana wizard")) {

					Commands.furiganaWizard (this.componentContext, component);

				} else if (url.Path.equals ("furigana document")) {

					Commands.furiganaWholeDocument (this.componentContext, component);

				} else if (url.Path.equals ("help")) {

					Commands.help();

				}

			}

		} catch (Throwable t) {
			ExceptionHelper.dealWith (t);
		}

	}




	/* (non-Javadoc)
	 * @see com.sun.star.frame.XDispatch#addStatusListener(com.sun.star.frame.XStatusListener, com.sun.star.util.URL)
	 */
	public void addStatusListener (XStatusListener arg0, URL arg1) {

		// Do nothing

	}


	/* (non-Javadoc)
	 * @see com.sun.star.frame.XDispatch#removeStatusListener(com.sun.star.frame.XStatusListener, com.sun.star.util.URL)
	 */
	public void removeStatusListener (XStatusListener arg0, URL arg1) {

		// Do nothing

	}


	/**
	 * Dispatch class constructor
	 * 
	 * @param componentContext The component context to access services through 
	 */
	public ProtocolHandler (XComponentContext componentContext) {

		this.componentContext = componentContext;

		try {

			XMultiComponentFactory multiComponentFactory = this.componentContext.getServiceManager();
			
			XDesktop desktop = (XDesktop) UnoRuntime.queryInterface (
					XDesktop.class,
					multiComponentFactory.createInstanceWithContext ("com.sun.star.frame.Desktop", this.componentContext)
			);

			this.desktop = desktop;

		} catch (Throwable t) {
			ExceptionHelper.dealWith (t);
		}

	}


}
