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

import com.sun.star.lang.XSingleComponentFactory;
import com.sun.star.lib.uno.helper.Factory;
import com.sun.star.registry.XRegistryKey;


/**
 * UNO component provider
 */
public class ComponentProvider {

	/**
	 * The UNO components registered by this provider
	 */
	public static final Class[] componentImplementationClasses = {
		ProtocolHandler.class,
		StartupJob.class,
		NewFrameJob.class
	};

	/**
	 * The services provided by each component
	 */
	public static final String[][] componentServiceNames = {

		// ProtocolHandler
		{
			"com.sun.star.frame.ProtocolHandler"
		},

		// StartupJob
		{
			"com.sun.star.task.Job"
		},

		// NewFrameJob
		{
			"com.sun.star.task.Job"
		}


	};


	/**
	 * UNO component factory interface. Creates component factories for our
	 * components
	 *
	 * @param implementationName The requested implementation name
	 * @return A component factory for the requested implementation
	 */
	public static XSingleComponentFactory __getComponentFactory (String implementationName) {

		for (int i = 0; i < componentImplementationClasses.length; i++) {
			
			Class implementationClass = componentImplementationClasses[i];
			String[] serviceNames = componentServiceNames[i];

			if (implementationName.equals (implementationClass.getName())) {
				return Factory.createComponentFactory (implementationClass, serviceNames);
			}

		}

		return null;

	}


	/**
	 * UNO registry interface. Called by UNO to register our
	 * components and the service names they support
	 *
	 * @param registryKey Supplied registery key
	 * @return <code>true</code> on success, <code>false</code> otherwise
	 */
	public static boolean __writeRegistryServiceInfo (XRegistryKey registryKey) {

		boolean success = true;

		for (int i = 0; i < componentImplementationClasses.length; i++) {
			
			Class implementationClass = componentImplementationClasses[i];
			String[] serviceNames = componentServiceNames[i];

			success &= Factory.writeRegistryServiceInfo (
					implementationClass.getName(),
					serviceNames,
					registryKey
			);

		}

		return success;

	}


}
