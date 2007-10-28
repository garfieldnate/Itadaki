package org.itadaki.openoffice;

import java.io.File;
import java.io.FileInputStream;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

import org.itadaki.client.dictionary.SystemListener;
import org.itadaki.client.dictionary.SystemProvider;


/**
 * Provides a system environment to a DictionaryService
 */
public class OfficeDictionaryProvider implements SystemProvider {

	/**
	 * Installed system dictionaries
	 */
	private Map<String,String> systemDictionaries = new TreeMap<String,String>();


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
	 * @param dictionaryConfigFilenames The filenames of the dictionary
	 *                                  configurations to use
	 */
	public OfficeDictionaryProvider (List<String> dictionaryConfigFilenames) {

		Map<String,String> systemDictionaries = new TreeMap<String,String>();

		try {

			for (String dictionaryConfigFilename : dictionaryConfigFilenames) {

				File propertiesFile = new File (dictionaryConfigFilename).getAbsoluteFile();
				String packageDirectoryURL = propertiesFile.getParentFile().toURI().toURL().toString();

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
						String dictionaryFilename = new File (new URI (packageDirectoryURL + "/" + dictionaryRelativeFilename)).getAbsolutePath();
						systemDictionaries.put (dictionaryFilename, dictionaryDisplayName);
					}

				}

			}

		} catch (Throwable t) {
			ExceptionHelper.dealWith (t);
		}

		this.systemDictionaries = systemDictionaries;

	}


}
