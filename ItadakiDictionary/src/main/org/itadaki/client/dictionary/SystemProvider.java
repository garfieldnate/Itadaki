package org.itadaki.client.dictionary;

import java.util.Map;


/**
 * Provider interface for system services expected by the DictionaryService<br>
 * <br>
 * Currently provides:<br>
 * <ul>
 *   <li> A list of system-defined dictionaries
 *   <li> A listener interface for updates to the system-defined dictionaries
 * </ul>
 * 
 */
public interface SystemProvider {

	/**
	 * Retrieves a list of system dictionary filenames
	 * 
	 * @return A list of system dictionary filenames
	 */
	public Map<String,String> getSystemDictionaries();


	/**
	 * Sets a listener for information of interest from the system environment
	 * 
	 * @param listener The listener to set 
	 */
	public void setSystemListener (SystemListener listener);


	/**
	 * Propagates a new search-on-select setting to the system
	 *
	 * @param searchOnSelect The new search-on-select setting
	 */
	public void setSearchOnSelect (boolean searchOnSelect);


}
