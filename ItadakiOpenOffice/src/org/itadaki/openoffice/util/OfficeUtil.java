package org.itadaki.openoffice.util;

import com.sun.star.awt.XDialog;
import com.sun.star.awt.XToolkit;
import com.sun.star.container.XEnumeration;
import com.sun.star.container.XEnumerationAccess;
import com.sun.star.frame.XController;
import com.sun.star.frame.XDesktop;
import com.sun.star.lang.XMultiComponentFactory;
import com.sun.star.lang.XServiceInfo;
import com.sun.star.task.XStatusIndicator;
import com.sun.star.task.XStatusIndicatorFactory;
import com.sun.star.text.XTextViewCursor;
import com.sun.star.text.XTextViewCursorSupplier;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;
import com.sun.star.view.XSelectionSupplier;


/**
 * A collection of utility functions for common OpenOffice tasks
 */
public class OfficeUtil {

	/**
	 * Retrieves the view cursor for a given XController
	 * 
	 * @param controller The controller to get the view cursor for
	 * @return The controller's view cursor
	 */
	public static XTextViewCursor viewCursorFor (XController controller) {

		XTextViewCursorSupplier viewCursorSupplier = (XTextViewCursorSupplier) UnoRuntime.queryInterface (
				XTextViewCursorSupplier.class,
				controller
		);

		return viewCursorSupplier.getViewCursor();

	}


	/**
	 * Retrieves the current selection of a given XController
	 * 
	 * @param controller The controller to get the selection from
	 * @return The controller's selection. Depending on what is selected, the
	 *         resulting object may implement a service either of
	 *         com.sun.star.text.TextTableCursor or of
	 *         com.sun.star.text.TextRanges
	 */
	public static Object selectionFor (XController controller) {

		XSelectionSupplier selectionSupplier = (XSelectionSupplier) UnoRuntime.queryInterface (
				XSelectionSupplier.class,
				controller
		);

		return selectionSupplier.getSelection();

	}


	/**
	 * Creates an enumeration for the specified Uno object (which must be
	 * enumerable)
	 * 
	 * @param object The Uno object to create an enumeration for
	 * @return The created enumeration
	 */
	public static XEnumeration enumerationFor (Object object) {

		XEnumerationAccess enumerationAccess = (XEnumerationAccess) UnoRuntime.queryInterface (
				XEnumerationAccess.class,
				object
		);

		return enumerationAccess.createEnumeration();

	}


	/**
	 * Creates a status indicator for the specified controller
	 * 
	 * @param controller The controller to create a status indicator for
	 * @return The created status indicator
	 */
	public static XStatusIndicator statusIndicatorFor (XController controller) {

		XStatusIndicatorFactory xStatusIndicatorFactory = (XStatusIndicatorFactory) UnoRuntime.queryInterface (
				XStatusIndicatorFactory.class,
				controller.getFrame()
		);

		return xStatusIndicatorFactory.createStatusIndicator();

	}


	/**
	 * Creates a dialog for the specified component context
	 * 
	 * @param componentContext The component context to create a dialog for
	 * @return The created dialog
	 * @throws com.sun.star.uno.Exception 
	 */
	public static XDialog dialogFor (XComponentContext componentContext) throws com.sun.star.uno.Exception {

	    XDialog dialog = (XDialog) UnoRuntime.queryInterface (
	    		XDialog.class,
	    		componentContext.getServiceManager().createInstanceWithContext ("com.sun.star.awt.UnoControlDialog", componentContext)
	    );

	    return dialog;

	}


	/**
	 * Creates a toolkit for the specified component context
	 * 
	 * @param componentContext The component context to create a toolkit for
	 * @return The created toolkit
	 * @throws com.sun.star.uno.Exception 
	 */
	public static XToolkit toolkitFor (XComponentContext componentContext) throws com.sun.star.uno.Exception {

	    XToolkit toolkit = (XToolkit) UnoRuntime.queryInterface (
	    		XToolkit.class,
	    		componentContext.getServiceManager().createInstanceWithContext ("com.sun.star.awt.Toolkit", componentContext)
	    );

	    return toolkit;

	}


	/**
	 * Tests if a given object is a text table
	 * 
	 * @param textContent The object to be tested
	 * @return <code>true</code> if the object is a text table, otherwise <code>false</code>
	 */
	public static boolean isTextTable (Object textContent) {
	
		XServiceInfo textContentServiceInfo = As.XServiceInfo (textContent);
	
		return textContentServiceInfo.supportsService ("com.sun.star.text.TextTable");
	
	}


	/**
	 * Instantiates a desktop for the given component context
	 * 
	 * @param componentContext The component context
	 * @return The instantiated desktop
	 * @throws com.sun.star.uno.Exception
	 */
	public static XDesktop desktopFor (XComponentContext componentContext) throws com.sun.star.uno.Exception {

		XMultiComponentFactory multiComponentFactory = componentContext.getServiceManager();
		
		XDesktop desktop = (XDesktop) UnoRuntime.queryInterface (
				XDesktop.class,
				multiComponentFactory.createInstanceWithContext ("com.sun.star.frame.Desktop", componentContext)
		);

		return desktop;

	}


}
