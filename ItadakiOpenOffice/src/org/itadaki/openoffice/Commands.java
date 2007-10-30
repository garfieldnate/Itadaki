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

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.itadaki.client.dictionary.DictionaryService;
import org.itadaki.client.furigana.FuriganaService;
import org.itadaki.client.furigana.MissingDictionaryException;
import org.itadaki.client.furigana.SentenceProvider;
import org.itadaki.openoffice.util.As;
import org.itadaki.openoffice.util.OfficeUtil;

import com.sun.star.awt.XControl;
import com.sun.star.awt.XDialog;
import com.sun.star.awt.XToolkit;
import com.sun.star.awt.XWindow;
import com.sun.star.beans.UnknownPropertyException;
import com.sun.star.beans.XPropertySet;
import com.sun.star.container.NoSuchElementException;
import com.sun.star.container.XEnumeration;
import com.sun.star.container.XNameContainer;
import com.sun.star.frame.XController;
import com.sun.star.frame.XFrame;
import com.sun.star.frame.XModel;
import com.sun.star.lang.WrappedTargetException;
import com.sun.star.lang.XComponent;
import com.sun.star.lang.XMultiComponentFactory;
import com.sun.star.lang.XMultiServiceFactory;
import com.sun.star.lang.XServiceInfo;
import com.sun.star.table.XCell;
import com.sun.star.task.XStatusIndicator;
import com.sun.star.text.XPageCursor;
import com.sun.star.text.XTextContent;
import com.sun.star.text.XTextCursor;
import com.sun.star.text.XTextDocument;
import com.sun.star.text.XTextRange;
import com.sun.star.text.XTextTable;
import com.sun.star.text.XTextTableCursor;
import com.sun.star.text.XTextViewCursor;
import com.sun.star.uno.Exception;
import com.sun.star.uno.XComponentContext;


/**
 * A helper class containing the implementation of the available OpenOffice
 * commands
 */
public class Commands {

	/**
	 * The maximum allowed length of a dictionary search
	 */
	public static final int MAX_DICTIONARY_SEARCH_LENGTH = 32;


	/**
	 * Returns the total page count of a document. The view cursor needs to be
	 * borrowed for this, so the document's model is passed in for controllers
	 * to be locked (and the cursor returned silently to its original position)
	 *
	 * @param model The document's model
	 * @param controller The document's controller
	 * @return The page number of the given text range
	 */
	private static int getPageCount (XModel model, XController controller) {

		XTextViewCursor viewCursor = OfficeUtil.viewCursorFor (controller);

		model.lockControllers();

		XTextCursor temporaryCursor = viewCursor.getText().createTextCursorByRange (viewCursor);

		XPageCursor pageCursor = As.XPageCursor (viewCursor);
		pageCursor.jumpToLastPage();
		int pageCount = pageCursor.getPage();

		viewCursor.gotoRange (temporaryCursor, false);
		model.unlockControllers();

		return pageCount;

	}


	/**
	 * Returns the page number of the given text range. The view cursor needs to
	 * be borrowed for this, so the document's model is passed in for
	 * controllers to be locked (and the cursor returned silently to its original
	 * position)
	 *
	 * @param model The document's model
	 * @param controller The document's controller
	 * @param textRange The text range to find the page number of
	 * @return The page number of the given text range
	 */
	private static int getPageNumber (XModel model, XController controller, XTextRange textRange) {

		XTextViewCursor viewCursor = OfficeUtil.viewCursorFor (controller);

		model.lockControllers();

		XTextCursor temporaryCursor = viewCursor.getText().createTextCursorByRange (viewCursor);
		viewCursor.gotoRange (textRange, false);

		XPageCursor pageCursor = As.XPageCursor (viewCursor);
		int pageNumber = pageCursor.getPage();

		viewCursor.gotoRange (temporaryCursor, false);
		model.unlockControllers();

		return pageNumber;

	}


	/**
	 * If the supplied controller's selection is of table cells, narrow the
	 * selection to the text within the first selected cell
	 *
	 * @param controller The controller to adjust the selection within
	 * @throws WrappedTargetException 
	 * @throws UnknownPropertyException 
	 * @throws NoSuchElementException 
	 */
	private static void narrowToTextSelection (XController controller)
	      throws UnknownPropertyException, WrappedTargetException, NoSuchElementException
	{

		XTextViewCursor viewCursor = OfficeUtil.viewCursorFor (controller);
		Object selection = OfficeUtil.selectionFor (controller);

		XServiceInfo info = As.XServiceInfo (selection);

		if (info.supportsService ("com.sun.star.text.TextTableCursor")) {

			XPropertySet cursorProperties = As.XPropertySet (viewCursor);
			XTextTable table = As.XTextTable (cursorProperties.getPropertyValue ("TextTable"));

			XTextTableCursor tableCursor = As.XTextTableCursor(selection);
			XCell cell = table.getCellByName (tableCursor.getRangeName().split(":")[0]);

			XEnumeration cellEnumeration = OfficeUtil.enumerationFor(cell);
			if (cellEnumeration.hasMoreElements()) {
				XTextContent textContent = As.XTextContent (cellEnumeration.nextElement());
				viewCursor.gotoRange (textContent.getAnchor(), false);
			}

		}

	}


	/**
	 * Displays a message warning of a missing furigana dictionary
	 * 
	 * @param componentContext The UNO component context used to access services
	 * @throws Exception 
	 */
	public static void missingFuriganaDictionaryMessage (XComponentContext componentContext) throws Exception {

		String okButtonName = "OK Button";
		String messageLabelName = "Message Label";

	    XMultiComponentFactory multiComponentFactory = componentContext.getServiceManager();

	    // Create the dialog
	    Object dialogModel = multiComponentFactory.createInstanceWithContext ("com.sun.star.awt.UnoControlDialogModel", componentContext);
	    XPropertySet dialogPropertySet = As.XPropertySet (dialogModel);      
	    dialogPropertySet.setPropertyValue ("PositionX", new Integer (100));
	    dialogPropertySet.setPropertyValue ("PositionY", new Integer (100));
	    dialogPropertySet.setPropertyValue ("Width", new Integer (160));
	    dialogPropertySet.setPropertyValue ("Height", new Integer (90));
	    dialogPropertySet.setPropertyValue ("Title", new String ("Warning Message"));

	    XMultiServiceFactory multiServiceFactory = As.XMultiServiceFactory (dialogModel);


	    // Create the message label
	    Object labelModel = multiServiceFactory.createInstance ("com.sun.star.awt.UnoControlFixedTextModel");
	    XPropertySet labelPropertySet = As.XPropertySet (labelModel);
	    labelPropertySet.setPropertyValue ("PositionX", new Integer (5));
	    labelPropertySet.setPropertyValue ("PositionY", new Integer (15));
	    labelPropertySet.setPropertyValue ("Width", new Integer (140));
	    labelPropertySet.setPropertyValue ("Height", new Integer (56));
	    labelPropertySet.setPropertyValue ("Name", okButtonName);
	    labelPropertySet.setPropertyValue ("TabIndex", new Short ((short)0));
	    labelPropertySet.setPropertyValue ("MultiLine", Boolean.TRUE);
	    labelPropertySet.setPropertyValue ("BackgroundColor", Integer.MAX_VALUE);
	    labelPropertySet.setPropertyValue ("Label", new String ("The Furigana Wizard requires the Ipadic sentence analysis dictionary.\n"
						+ "Please install the \"itadaki-data-ipadic\" package using the OpenOffice package manager."));

	    // create the OK button
	    Object buttonModel = multiServiceFactory.createInstance ("com.sun.star.awt.UnoControlButtonModel");
	    XPropertySet buttonPropertySet = As.XPropertySet (buttonModel);
	    buttonPropertySet.setPropertyValue ("PositionX", new Integer (55));
	    buttonPropertySet.setPropertyValue ("PositionY", new Integer (70));
	    buttonPropertySet.setPropertyValue ("Width", new Integer (50));
	    buttonPropertySet.setPropertyValue ("Height", new Integer (14));
	    buttonPropertySet.setPropertyValue ("Name", messageLabelName);
	    buttonPropertySet.setPropertyValue ("TabIndex", new Short ((short)1));
	    buttonPropertySet.setPropertyValue ("PushButtonType", new Short ((short)1));
	    buttonPropertySet.setPropertyValue ("Label", "OK");

	    // Assemble the dialog
	    XNameContainer xNameCont = As.XNameContainer (dialogModel);
	    xNameCont.insertByName (okButtonName, buttonModel);
	    xNameCont.insertByName (messageLabelName, labelModel);

	    // Show the dialog
	    XDialog dialog = OfficeUtil.dialogFor (componentContext);

	    XControl control = As.XControl (dialog);
	    control.setModel (As.XControlModel (dialogModel));
	    XToolkit toolkit = OfficeUtil.toolkitFor(componentContext);
	    XWindow window = As.XWindow (control);
	    window.setVisible (false);
	    control.createPeer (toolkit, null);
	    dialog.execute();

	    // Dispose of the completed dialog
	    XComponent dialogComponent = As.XComponent (dialog);
	    dialogComponent.dispose();

	}


	/**
	 * Searches in the dictionary for the current selection of the document
	 * represented by the given component
	 *
	 * @param component The document's component
	 * @throws UnknownPropertyException
	 * @throws WrappedTargetException
	 * @throws NoSuchElementException
	 */
	public static void dictionary (XComponent component) throws UnknownPropertyException, WrappedTargetException, NoSuchElementException {

		XModel model = As.XModel (component);
		XController controller = model.getCurrentController();
		narrowToTextSelection (controller);

		XTextViewCursor viewCursor = OfficeUtil.viewCursorFor (controller);
		XTextCursor textCursor = viewCursor.getText().createTextCursorByRange (viewCursor);
		String searchQuery = TextPortionIterator.getText (textCursor, MAX_DICTIONARY_SEARCH_LENGTH);

		DictionaryService.getInstance().search (searchQuery, true);
		
	}


	/**
	 * Adds furigana to the whole document represented by the given component
	 *
	 * @param componentContext The UNO component context used to access services 
	 * @param component The document's component
	 */
	public static void furiganaWholeDocument (final XComponentContext componentContext, XComponent component) {

		final XTextDocument textDocument = As.XTextDocument (component);
		final XModel model = As.XModel (component);
		final XController controller = model.getCurrentController();
		final XFrame frame = controller.getFrame();

		final XStatusIndicator statusIndicator = OfficeUtil.statusIndicatorFor (controller);

		// Create a thread to process the document. Without using a thread, the
		// OpenOffice menu sticks open
		Runnable runnable = new Runnable() {

			public void run() {

				try {

					try {

						frame.getComponentWindow().setEnable (false);

						// Store the current view cursor position so we can put it back afterwards
						XTextViewCursor viewCursor = OfficeUtil.viewCursorFor (controller);
						XTextCursor temporaryCursor = viewCursor.getText().createTextCursorByRange (viewCursor);

						// Start the progress indicator
						int pageCount = getPageCount (model, controller);
				  	    statusIndicator.start ("Adding furigana...", pageCount);
				  	    statusIndicator.setValue (1);


				  	    // Process the document
						model.lockControllers();

						DocumentSentenceProvider sentenceProvider = new DocumentSentenceProvider (textDocument);
						sentenceProvider.setOfficeSentenceProviderListener (new OfficeSentenceProviderListener() {

							private long lastUpdate = System.currentTimeMillis();

							public void regionChanged (XTextRange newTextRange) {

								try {
									long now = System.currentTimeMillis();
									if ((now - this.lastUpdate) > 2000) {
										statusIndicator.setValue (getPageNumber (model, controller, newTextRange));
										this.lastUpdate = now;
									}
								} catch (Throwable t) {
									ExceptionHelper.dealWith (t);
								}
								
							}
							
						});

						FuriganaService.getInstance().processAll (sentenceProvider);


						// Restore the original view cursor position
						viewCursor.gotoRange (temporaryCursor, false);

					} catch (MissingDictionaryException e) {
						missingFuriganaDictionaryMessage (componentContext);
					} finally {
						if (model != null) {
							model.unlockControllers();
						}
						frame.getComponentWindow().setEnable (true);

						statusIndicator.end();

					}

				} catch (Throwable t) {
					ExceptionHelper.dealWith (t);
				}

			}
			
		};

		new Thread (runnable).start();

	}


	/**
	 * Adds furigana interactively to the document represented by the given
	 * component
	 *
	 * @param componentContext The UNO component context used to access services 
	 * @param component The document's component
	 * @throws Exception
	 */
	public static void furiganaWizard (XComponentContext componentContext, XComponent component) throws Exception {

		XTextDocument textDocument = As.XTextDocument (component);
		XModel model = As.XModel (component);
		XController controller = model.getCurrentController();
		XFrame frame = controller.getFrame();

		narrowToTextSelection (controller);

		SentenceProvider sentenceProvider = new OfficeSentenceProvider (frame, textDocument, true);
		try {
			FuriganaService.getInstance().processInteractively (sentenceProvider);
		} catch (MissingDictionaryException e) {
			missingFuriganaDictionaryMessage (componentContext);
		}

	}


	/**
	 * Displays a system help dialog
	 */
	public static void help () {

		SwingUtilities.invokeLater (new Runnable() {

			public void run() {

				JOptionPane.showMessageDialog (null, "Itadaki version 1.0\n\nCopyright 2006-2007 Matt Francis");

			}

		});

	}


}
