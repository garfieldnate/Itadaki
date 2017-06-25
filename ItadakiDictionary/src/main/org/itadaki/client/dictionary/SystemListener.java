package org.itadaki.client.dictionary;

import java.util.Map;


/**
 * A listener for information of interest from the system environment
 */
public interface SystemListener {

	/**
	 * Indicates that the system dictionaries have been updated
	 * 
	 * @param dictionaryFilenames The new list of available system dictionaries 
	 */
	public void systemDictionariesUpdated (Map<String,String> dictionaryFilenames);

}
