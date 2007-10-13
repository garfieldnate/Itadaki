package org.itadaki.openoffice;

import java.io.File;
import java.io.FileInputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

import org.itadaki.client.dictionary.SystemListener;
import org.itadaki.client.dictionary.SystemProvider;

import com.sun.star.deployment.XPackage;
import com.sun.star.deployment.XPackageManager;
import com.sun.star.deployment.XPackageManagerFactory;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;
import com.sun.star.util.XMacroExpander;


/**
 * Provides a system environment to a DictionaryService
 */
public class OfficeDictionaryProvider implements SystemProvider {

	/**
	 * Installed system dictionaries
	 */
	private Map<String,String> systemDictionaries = new TreeMap<String,String>();

	/**
	 * Scans installed Uno packages for Itadaki dictionaries
	 *
	 * @param componentContext The XComponentContext to use to retrieve package
	 *                         data
	 * @return Map of installed system dictionaries
	 */
	private static Map<String,String> scanInstalledPackages (XComponentContext componentContext) {

		Map<String,String> systemDictionaries = new TreeMap<String,String>();

		try {

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


			for (XPackage onePackage : allPackages) {
	
				String packageURL = macroExpander.expandMacros (onePackage.getURL());
				packageURL = packageURL.replaceFirst ("^vnd\\.sun\\.star\\.expand:", "");

				if (onePackage.getName().startsWith ("itadaki-data-")) {

					File propertiesFile = new File (new URI (packageURL + "/itadaki-data.properties")).getAbsoluteFile();

					if (propertiesFile.exists()) {

						Properties properties = new Properties();

						try {
							FileInputStream inputStream = new FileInputStream (propertiesFile.getPath());
							properties.load (inputStream);
							inputStream.close();
						} catch (Exception e) {
							ExceptionHelper.dealWith (e);
						}
	
						String dictionaryRelativeFilename = properties.getProperty ("filename", null); 
						String dictionaryDisplayName = properties.getProperty ("displayName", null);
	
						if (dictionaryRelativeFilename != null) {
							String dictionaryFilename = new File (new URI (packageURL + "/" + dictionaryRelativeFilename)).getAbsolutePath();
							systemDictionaries.put (dictionaryFilename, dictionaryDisplayName);
						}

					}

				}
	
			}

		} catch (Throwable t) {
			ExceptionHelper.dealWith (t);
		}

		return systemDictionaries;

	}


	/* SystemProvider interface */

	/* (non-Javadoc)
	 * @see org.itadaki.client.dictionary.SystemProvider#getSystemDictionaries()
	 */
	public Map<String,String> getSystemDictionaries() {

		Map<String,String> systemDictionariesCopy = new TreeMap<String,String>();
		systemDictionariesCopy.putAll (this.systemDictionaries);

		return systemDictionariesCopy;

	}


	/* (non-Javadoc)
	 * @see org.itadaki.client.dictionary.SystemProvider#setSystemListener(org.itadaki.client.dictionary.SystemListener)
	 */
	public void setSystemListener (SystemListener systemListener) {

		// TODO

	}


	/* (non-Javadoc)
	 * @see org.itadaki.client.dictionary.SystemProvider#setSearchOnSelect(boolean)
	 */
	public void setSearchOnSelect (boolean searchOnSelect) {

		SelectionManager.setSearchOnSelect (searchOnSelect);
		
	}


	/**
	 * Default constructor 
	 * 
	 * @param componentContext The component context to use to scan for
	 *                         dictionary packages
	 */
	public OfficeDictionaryProvider (XComponentContext componentContext) {

		this.systemDictionaries = scanInstalledPackages (componentContext);

	}


}
