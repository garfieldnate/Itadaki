package org.itadaki.openoffice.util;

import com.sun.star.awt.XControl;
import com.sun.star.awt.XControlModel;
import com.sun.star.awt.XWindow;
import com.sun.star.beans.XPropertySet;
import com.sun.star.beans.XPropertyState;
import com.sun.star.container.XIndexAccess;
import com.sun.star.container.XIndexContainer;
import com.sun.star.container.XNameContainer;
import com.sun.star.frame.XComponentLoader;
import com.sun.star.frame.XFrame;
import com.sun.star.frame.XModel;
import com.sun.star.lang.XComponent;
import com.sun.star.lang.XMultiServiceFactory;
import com.sun.star.lang.XServiceInfo;
import com.sun.star.table.XCellRange;
import com.sun.star.text.XPageCursor;
import com.sun.star.text.XParagraphCursor;
import com.sun.star.text.XText;
import com.sun.star.text.XTextContent;
import com.sun.star.text.XTextDocument;
import com.sun.star.text.XTextRange;
import com.sun.star.text.XTextRangeCompare;
import com.sun.star.text.XTextSection;
import com.sun.star.text.XTextTable;
import com.sun.star.text.XTextTableCursor;
import com.sun.star.ui.XContextMenuInterception;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.view.XSelectionSupplier;


/**
 * Wrapper functions to reduce the clutter of making Uno casts
 */
public class As {

	/**
	 * Performs a Uno cast to present the given object as an {@link XTextDocument}
	 * 
	 * @param object The Uno object to cast
	 * @return The supplied object as an {@link XTextDocument}
	 */
	public static XTextDocument XTextDocument (Object object) {

			return (XTextDocument) UnoRuntime.queryInterface (XTextDocument.class, object);

	}


	/**
	 * Performs a Uno cast to present the given object as an {@link XModel}
	 * 
	 * @param object The Uno object to cast
	 * @return The supplied object as an {@link XModel}
	 */
	public static XModel XModel (Object object) {

			return (XModel) UnoRuntime.queryInterface (XModel.class, object);

	}


	/**
	 * Performs a Uno cast to present the given object as an {@link XComponent}
	 * 
	 * @param object The Uno object to cast
	 * @return The supplied object as an {@link XComponent}
	 */
	public static XComponent XComponent (Object object) {

			return (XComponent) UnoRuntime.queryInterface (XComponent.class, object);

	}


	/**
	 * Performs a Uno cast to present the given object as an {@link XMultiServiceFactory}
	 * 
	 * @param object The Uno object to cast
	 * @return The supplied object as an {@link XMultiServiceFactory}
	 */
	public static XMultiServiceFactory XMultiServiceFactory (Object object) {

			return (XMultiServiceFactory) UnoRuntime.queryInterface (XMultiServiceFactory.class, object);

	}


	/**
	 * Performs a Uno cast to present the given object as an {@link XPageCursor}
	 * 
	 * @param object The Uno object to cast
	 * @return The supplied object as an {@link XPageCursor}
	 */
	public static XPageCursor XPageCursor (Object object) {

			return (XPageCursor) UnoRuntime.queryInterface (XPageCursor.class, object);

	}


	/**
	 * Performs a Uno cast to present the given object as an {@link XServiceInfo}
	 * 
	 * @param object The Uno object to cast
	 * @return The supplied object as an {@link XServiceInfo}
	 */
	public static XServiceInfo XServiceInfo (Object object) {

		return (XServiceInfo) UnoRuntime.queryInterface (XServiceInfo.class, object);

	}


	/**
	 * Performs a Uno cast to present the given object as an {@link XTextContent}
	 * 
	 * @param object The Uno object to cast
	 * @return The supplied object as an {@link XTextContent}
	 */
	public static XTextContent XTextContent (Object object) {

		return (XTextContent) UnoRuntime.queryInterface (XTextContent.class, object);

	}


	/**
	 * Performs a Uno cast to present the given object as an {@link XTextTableCursor}
	 * 
	 * @param object The Uno object to cast
	 * @return The supplied object as an {@link XTextTableCursor}
	 */
	public static XTextTable XTextTable (Object object) {

		return (XTextTable) UnoRuntime.queryInterface (XTextTable.class, object);

	}


	/**
	 * Performs a Uno cast to present the given object as an {@link XTextTableCursor}
	 * 
	 * @param object The Uno object to cast
	 * @return The supplied object as an {@link XTextTableCursor}
	 */
	public static XTextTableCursor XTextTableCursor (Object object) {

		return (XTextTableCursor) UnoRuntime.queryInterface (XTextTableCursor.class, object);

	}


	/**
	 * Performs a Uno cast to present the given object as an {@link XPropertySet}
	 * 
	 * @param object The Uno object to cast
	 * @return The supplied object as an {@link XPropertySet}
	 */
	public static XPropertySet XPropertySet (Object object) {

		return (XPropertySet) UnoRuntime.queryInterface (XPropertySet.class, object);

	}


	/**
	 * Performs a Uno cast to present the given object as an {@link XNameContainer}
	 * 
	 * @param object The Uno object to cast
	 * @return The supplied object as an {@link XNameContainer}
	 */
	public static XNameContainer XNameContainer (Object object) {

		return (XNameContainer) UnoRuntime.queryInterface (XNameContainer.class, object);

	}


	/**
	 * Performs a Uno cast to present the given object as an {@link XControlModel}
	 * 
	 * @param object The Uno object to cast
	 * @return The supplied object as an {@link XControlModel}
	 */
	public static XControlModel XControlModel (Object object) {

		return (XControlModel) UnoRuntime.queryInterface (XControlModel.class, object);

	}


	/**
	 * Performs a Uno cast to present the given object as an {@link XControl}
	 * 
	 * @param object The Uno object to cast
	 * @return The supplied object as an {@link XControl}
	 */
	public static XControl XControl (Object object) {

		return (XControl) UnoRuntime.queryInterface (XControl.class, object);

	}


	/**
	 * Performs a Uno cast to present the given object as an {@link XWindow}
	 * 
	 * @param object The Uno object to cast
	 * @return The supplied object as an {@link XWindow}
	 */
	public static XWindow XWindow (Object object) {

		return (XWindow) UnoRuntime.queryInterface (XWindow.class, object);

	}


	/**
	 * Performs a Uno cast to present the given object as an {@link XIndexContainer}
	 * 
	 * @param object The Uno object to cast
	 * @return The supplied object as an {@link XIndexContainer}
	 */
	public static XIndexContainer XIndexContainer (Object object) {

		return (XIndexContainer) UnoRuntime.queryInterface (XIndexContainer.class, object);

	}


	/**
	 * Performs a Uno cast to present the given object as an {@link XText}
	 * 
	 * @param object The Uno object to cast
	 * @return The supplied object as an {@link XText}
	 */
	public static XText XText (Object object) {

		return (XText) UnoRuntime.queryInterface (XText.class, object);

	}


	/**
	 * Performs a Uno cast to present the given object as an {@link XTextRange}
	 * 
	 * @param object The Uno object to cast
	 * @return The supplied object as an {@link XTextRange}
	 */
	public static XTextRange XTextRange (Object object) {

		return (XTextRange) UnoRuntime.queryInterface (XTextRange.class, object);

	}


	/**
	 * Performs a Uno cast to present the given object as an {@link XTextRangeCompare}
	 * 
	 * @param object The Uno object to cast
	 * @return The supplied object as an {@link XTextRange}
	 */
	public static XTextRangeCompare XTextRangeCompare (Object object) {

		return (XTextRangeCompare) UnoRuntime.queryInterface (XTextRangeCompare.class, object);

	}


	/**
	 * Performs a Uno cast to present the given object as an {@link XParagraphCursor}
	 * 
	 * @param object The Uno object to cast
	 * @return The supplied object as an {@link XParagraphCursor}
	 */
	public static XParagraphCursor XParagraphCursor (Object object) {

		return (XParagraphCursor) UnoRuntime.queryInterface (XParagraphCursor.class, object);

	}


	/**
	 * Performs a Uno cast to present the given object as an {@link XPropertyState}
	 * 
	 * @param object The Uno object to cast
	 * @return The supplied object as an {@link XPropertyState}
	 */
	public static XPropertyState XPropertyState (Object object) {

		return (XPropertyState) UnoRuntime.queryInterface (XPropertyState.class, object);

	}


	/**
	 * Performs a Uno cast to present the given object as an {@link XContextMenuInterception}
	 * 
	 * @param object The Uno object to cast
	 * @return The supplied object as an {@link XContextMenuInterception}
	 */
	public static XContextMenuInterception XContextMenuInterception (Object object) {

		return (XContextMenuInterception) UnoRuntime.queryInterface (XContextMenuInterception.class, object);

	}


	/**
	 * Performs a Uno cast to present the given object as an {@link XFrame}
	 * 
	 * @param object The Uno object to cast
	 * @return The supplied object as an {@link XFrame}
	 */
	public static XFrame XFrame (Object object) {

		return (XFrame) UnoRuntime.queryInterface (XFrame.class, object);

	}


	/**
	 * Performs a Uno cast to present the given object as an {@link XIndexAccess}
	 * 
	 * @param object The Uno object to cast
	 * @return The supplied object as an {@link XIndexAccess}
	 */
	public static XIndexAccess XIndexAccess (Object object) {

		return (XIndexAccess) UnoRuntime.queryInterface (XIndexAccess.class, object);

	}


	/**
	 * Performs a Uno cast to present the given object as an {@link XSelectionSupplier}
	 * 
	 * @param object The Uno object to cast
	 * @return The supplied object as an {@link XSelectionSupplier}
	 */
	public static XSelectionSupplier XSelectionSupplier (Object object) {

		return (XSelectionSupplier) UnoRuntime.queryInterface (XSelectionSupplier.class, object);

	}


	/**
	 * Performs a Uno cast to present the given object as an {@link XTextSection}
	 * 
	 * @param object The Uno object to cast
	 * @return The supplied object as an {@link XTextSection}
	 */
	public static XTextSection XTextSection (Object object) {

		return (XTextSection) UnoRuntime.queryInterface (XTextSection.class, object);

	}


	/**
	 * Performs a Uno cast to present the given object as an {@link XComponentLoader}
	 * 
	 * @param object The Uno object to cast
	 * @return The supplied object as an {@link XComponentLoader}
	 */
	public static XComponentLoader XComponentLoader (Object object) {

		return (XComponentLoader) UnoRuntime.queryInterface (XComponentLoader.class, object);

	}


	/**
	 * Performs a Uno cast to present the given object as an {@link XCellRange}
	 * 
	 * @param object The Uno object to cast
	 * @return The supplied object as an {@link XCellRange}
	 */
	public static XCellRange XCellRange (Object object) {

		return (XCellRange) UnoRuntime.queryInterface (XCellRange.class, object);

	}


}
