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

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.itadaki.client.dictionary.DictionaryService;
import org.itadaki.client.furigana.FuriganaService;

import com.sun.star.awt.Key;
import com.sun.star.awt.KeyEvent;
import com.sun.star.awt.KeyModifier;
import com.sun.star.beans.NamedValue;
import com.sun.star.deployment.DeploymentException;
import com.sun.star.deployment.XPackage;
import com.sun.star.deployment.XPackageManager;
import com.sun.star.deployment.XPackageManagerFactory;
import com.sun.star.lang.IllegalArgumentException;
import com.sun.star.lang.XMultiComponentFactory;
import com.sun.star.lang.XMultiServiceFactory;
import com.sun.star.lang.XServiceInfo;
import com.sun.star.lib.uno.helper.WeakBase;
import com.sun.star.task.XJob;
import com.sun.star.ucb.CommandAbortedException;
import com.sun.star.ucb.CommandFailedException;
import com.sun.star.ui.XAcceleratorConfiguration;
import com.sun.star.ui.XModuleUIConfigurationManagerSupplier;
import com.sun.star.ui.XUIConfigurationManager;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;
import com.sun.star.util.XMacroExpander;


/**
 * Job that sets up Itadaki on application startup
 */
public class StartupJob extends WeakBase implements XServiceInfo, XJob {

	/**
	 * <code>true</code> if startup has been completed
	 */
	private static boolean startupComplete = false;

	/**
	 * XServiceInfo service names
	 */
	private static final String[] componentServiceNames = {
		"com.sun.star.task.Job"
	};

	/**
	 * Component context used to access UNO services
	 */
	private XComponentContext componentContext;


	/**
	 * @param componentContext The component context used to access UNO services 
	 */
	public static synchronized void startup (XComponentContext componentContext) {

		if (!startupComplete) {

			try {

				// Connect services with their data packages
				scanInstalledPackages (componentContext);
	
				// Install command key shortcuts
				installKeyShortcuts (componentContext);
	
			} catch (Throwable t) {
				ExceptionHelper.dealWith (t);
			}

			startupComplete = true;

		}

	}


	/**
	 * Scans installed Uno packages for Itadaki data
	 *
	 * @param componentContext The component context used to access UNO services 
	 * @throws IllegalArgumentException
	 * @throws DeploymentException
	 * @throws CommandFailedException
	 * @throws CommandAbortedException
	 * @throws URISyntaxException 
	 */
	private static void scanInstalledPackages(XComponentContext componentContext)
	             throws IllegalArgumentException, DeploymentException, CommandFailedException,
	                    CommandAbortedException, URISyntaxException
	{

		String furiganaConfigFilename = null;
		List<String> dictionaryConfigFilenames = new ArrayList<String>();


		// Build list of all available user and system packages

		XPackageManagerFactory packageManagerFactory = (XPackageManagerFactory) UnoRuntime.queryInterface (
				XPackageManagerFactory.class,
				componentContext.getValueByName ("/singletons/com.sun.star.deployment.thePackageManagerFactory")
		);

		XMacroExpander macroExpander = (XMacroExpander) UnoRuntime.queryInterface (
				XMacroExpander.class,
				componentContext.getValueByName ("/singletons/com.sun.star.util.theMacroExpander")
		);

		List<XPackage> allPackages = new ArrayList<XPackage>();

		XPackageManager sharedPackageManager = packageManagerFactory.getPackageManager ("shared");
		allPackages.addAll (Arrays.asList (sharedPackageManager.getDeployedPackages (null, null)));

		XPackageManager userPackageManager = packageManagerFactory.getPackageManager ("user");
		allPackages.addAll (Arrays.asList (userPackageManager.getDeployedPackages (null, null)));


		// Find dictionaries and furigana data

		for (XPackage onePackage : allPackages) {

			String packageURL = macroExpander.expandMacros (onePackage.getURL());
			packageURL = packageURL.replaceFirst ("^vnd\\.sun\\.star\\.expand:", "");

			if (onePackage.getName().startsWith ("itadaki-data-ipadic-")) {

				furiganaConfigFilename = new File (new URI (packageURL + "/dictionary/dictionary.xml")).getAbsolutePath();

			} else if (onePackage.getName().startsWith ("itadaki-data-")) {

				dictionaryConfigFilenames.add (new File (new URI (packageURL + "/itadaki-data.properties")).getAbsolutePath());

			}


		}


		// Connect services with their configurations

		if (furiganaConfigFilename != null) {
			FuriganaService.getInstance().setConfiguration (furiganaConfigFilename);
		}

		DictionaryService.createInstance (new OfficeDictionaryProvider (dictionaryConfigFilenames));

		
	}


	/**
	 * Installs application key shortcuts
	 * 
	 * @param componentContext The component context used to access UNO services 
	 */
	private static void installKeyShortcuts (XComponentContext componentContext) {
	
		try {
	
			XMultiComponentFactory multiComponentFactory = componentContext.getServiceManager();

			XMultiServiceFactory xMSF = (XMultiServiceFactory) UnoRuntime.queryInterface (
					XMultiServiceFactory.class,
					multiComponentFactory
			);
			XModuleUIConfigurationManagerSupplier configurationManagerSupplier =
					(XModuleUIConfigurationManagerSupplier) UnoRuntime.queryInterface (
							XModuleUIConfigurationManagerSupplier.class,
							xMSF.createInstance ("com.sun.star.ui.ModuleUIConfigurationManagerSupplier")
					);
			XUIConfigurationManager configurationManager = configurationManagerSupplier.getUIConfigurationManager (
					"com.sun.star.text.TextDocument"
			);
	
			XAcceleratorConfiguration acceleratorConfig = (XAcceleratorConfiguration) UnoRuntime.queryInterface (
					XAcceleratorConfiguration.class, configurationManager.getShortCutManager()
			);
	
			KeyEvent keyEvent;
			
			keyEvent = new KeyEvent();
			keyEvent.KeyCode = Key.G;
			keyEvent.Modifiers = KeyModifier.MOD1;
			acceleratorConfig.setKeyEvent (keyEvent, "org.itadaki.openoffice:dictionary");
	
			keyEvent = new KeyEvent();
			keyEvent.KeyCode = Key.H;
			keyEvent.Modifiers = KeyModifier.MOD1;
			acceleratorConfig.setKeyEvent (keyEvent, "org.itadaki.openoffice:furigana wizard");
	
			acceleratorConfig.store();
	
		} catch (Throwable t) {
			ExceptionHelper.dealWith (t);
		}
	
	}


	/* XJob interface */

	/* (non-Javadoc)
	 * @see com.sun.star.task.XJob#execute(com.sun.star.beans.NamedValue[])
	 */
	public synchronized Object execute (NamedValue[] arguments) throws IllegalArgumentException {

		startup (this.componentContext);

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
	public StartupJob (XComponentContext componentContext) {

		this.componentContext = componentContext;

	}


}