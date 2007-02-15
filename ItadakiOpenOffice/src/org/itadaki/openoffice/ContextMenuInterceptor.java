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

import com.sun.star.beans.XPropertySet;
import com.sun.star.container.XIndexContainer;
import com.sun.star.lang.XMultiServiceFactory;
import com.sun.star.ui.ActionTriggerSeparatorType;
import com.sun.star.ui.ContextMenuExecuteEvent;
import com.sun.star.ui.ContextMenuInterceptorAction;
import com.sun.star.ui.XContextMenuInterceptor;
import com.sun.star.uno.UnoRuntime;


/**
 * A context menu interceptor that adds Itadaki commands to text documents
 */
public class ContextMenuInterceptor implements XContextMenuInterceptor {

	public ContextMenuInterceptorAction notifyContextMenuExecute (ContextMenuExecuteEvent event) {

		try {
	
			// Fetch multi service factory for the context menu
			XIndexContainer contextMenuIndexContainer = event.ActionTriggerContainer;
			XMultiServiceFactory multiServiceFactory = (XMultiServiceFactory) UnoRuntime.queryInterface (
					XMultiServiceFactory.class,
					contextMenuIndexContainer
			);
	
	
			// Create dictionary entry
			XPropertySet dictionaryMenuEntryPropertySet = (XPropertySet) UnoRuntime.queryInterface ( 
					XPropertySet.class,
					multiServiceFactory.createInstance ("com.sun.star.ui.ActionTrigger")
			);
			dictionaryMenuEntryPropertySet.setPropertyValue ("Text", new String ("Dictionary..." ));
			dictionaryMenuEntryPropertySet.setPropertyValue ("CommandURL", new String ("org.itadaki.openoffice:dictionary"));
	
	
			// Create furigana wizard entry
			XPropertySet wizardMenuEntryPropertySet = (XPropertySet) UnoRuntime.queryInterface ( 
					XPropertySet.class,
					multiServiceFactory.createInstance ("com.sun.star.ui.ActionTrigger")
			);
			wizardMenuEntryPropertySet.setPropertyValue ("Text", new String ("Furigana Wizard..."));
			wizardMenuEntryPropertySet.setPropertyValue ("CommandURL", new String ("org.itadaki.openoffice:furigana wizard"));
	
	
			// Create sub menu
			XIndexContainer subMenuIndexContainer = (XIndexContainer)UnoRuntime.queryInterface ( 
						XIndexContainer.class,
						multiServiceFactory.createInstance ("com.sun.star.ui.ActionTriggerContainer" )
			);
			subMenuIndexContainer.insertByIndex (0, dictionaryMenuEntryPropertySet);
			subMenuIndexContainer.insertByIndex (1, wizardMenuEntryPropertySet);
	
	
			// Create a separator menu entry
			XPropertySet separatorPropertySet = (XPropertySet) UnoRuntime.queryInterface ( 
						XPropertySet.class, 
						multiServiceFactory.createInstance ("com.sun.star.ui.ActionTriggerSeparator")
			);
			separatorPropertySet.setPropertyValue ("SeparatorType", new Short (ActionTriggerSeparatorType.LINE));
	
	
			// create top level menu
			XPropertySet topMenuPropertySet = (XPropertySet) UnoRuntime.queryInterface (
						XPropertySet.class, 
						multiServiceFactory.createInstance ("com.sun.star.ui.ActionTrigger")
			);
			topMenuPropertySet.setPropertyValue ("Text", new String ("Itadaki"));
			topMenuPropertySet.setPropertyValue ("SubContainer", subMenuIndexContainer);
			contextMenuIndexContainer.insertByIndex (0, topMenuPropertySet);
			contextMenuIndexContainer.insertByIndex (1, separatorPropertySet);
	
			return ContextMenuInterceptorAction.EXECUTE_MODIFIED;
	
		}
		catch (Exception e)
		{
			ExceptionHelper.dealWith (e);
		}
	
		return ContextMenuInterceptorAction.IGNORED;
	
	}
}