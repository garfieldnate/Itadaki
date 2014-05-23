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

package org.itadaki.fasttextpane;


import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Transparency;
import java.awt.datatransfer.Clipboard;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.font.FontRenderContext;
import java.awt.font.LineBreakMeasurer;
import java.awt.font.TextAttribute;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.lang.ref.SoftReference;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;

import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JPopupMenu;
import javax.swing.UIManager;

import org.itadaki.fasttextpane.actions.CopyAction;
import org.itadaki.fasttextpane.actions.ScrollDownAction;
import org.itadaki.fasttextpane.actions.ScrollEndAction;
import org.itadaki.fasttextpane.actions.ScrollHomeAction;
import org.itadaki.fasttextpane.actions.ScrollPageDownAction;
import org.itadaki.fasttextpane.actions.ScrollPageUpAction;
import org.itadaki.fasttextpane.actions.ScrollUpAction;


/**
 * A fast, non-editable, append-only text display component<br><br>
 * 
 * Features:<br>
 *  - Thread safe
 *  - Essentially O(1) append speed<br>
 *  - Externally supplied paragraph storage (Document). Allows unlimited content size and on-demand
 *      realisation/formatting of text only as it becomes visible<br>
 *  - Smooth resize; Does not layout in the Swing event thread while resizing (waits until resize stops)<br>
 *  - Mandatory horizontal word wrap (no horizontal scrollbar)<br>
 *  - Vertical scrollbar with automatic visibility. Avoids "layout flash" when scrollbar materialises
 *      or dematerialises<br>
 *  - Scrollbar increments track paragraphs, eliminating the chief cause of append slowness in
 *      JTextComponent derivatives (namely, trying to maintain pixel-accurate scrollbar settings)<br>
 *  - Separate paragraph initial/subsequent indents<br>
 *  - Formatted text (anything an AttributedString can represent)<br>
 *  - Individual paragraph background colours<br>
 *  - Mouse text selection<br>
 *  - Copy to clipboard<br>
 *  - Action-based line scrolling<br>
 *  - Action-based page scrolling<br><br>
 *  
 * Todo:<br>
 *  - Keyboard selection<br><br>
 * 
 * Design<br><br>
 * 
 * Principle state:
 * 
 *   document
 *   layout
 *   selection
 *   position
 * 
 */
public class FastTextView extends JComponent implements DocumentListener, MouseListener, MouseMotionListener {

	/**
	 * Serial Version UID
	 */
	private static final long serialVersionUID = 1L;


	/* Debugging state */

	/**
	 * Debug setting. When enabled, various timing information is output to STDOUT
	 */
	static final boolean DEBUG = false;

	/**
	 * The time that the last paint started, in milliseconds
	 * Not thread safe, but hardly important
	 */
	private long debugLastPaintMilliseconds = 0;

	/**
	 * The time that the current Document was first set, in milliseconds
	 * Not thread safe, but hardly important
	 */
	private long debugCurrentDocumentSetMilliseconds = 0;


	/* Constants */

	/**
	 * Blank pixel border inside the component
	 */
	private static final int INSIDE_BORDER = 2;

	/**
	 * Indent for the first line of a paragraph
	 */
	private static final int INITIAL_INDENT = 2;

	/**
	 * Indent for the second and subsequent lines of a paragraph
	 */
	private static final int SUBSEQUENT_INDENT = 20;

	/**
	 * Additional space between lines
	 */
	private static final float LINE_SPACING = 1.0f;

	/**
	 * Additional space between paragraphs
	 */
	private static final float PARAGRAPH_SPACING = 0.0f;

	/**
	 * Background colour for highlighted text
	 * Thread safe by virtue of finality (although one day we may want to respond to theme changes...)
	 */
	private final Color highlightBackground;

	/**
	 * Foreground colour for highlighted text
	 * Thread safe by virtue of finality (although one day we may want to respond to theme changes...)
	 */
	private final Color highlightForeground;


	/* Listeners */

	/**
	 * Listeners for changes in the display overflow status
	 * Thread safe by virtue of Collections.synchronizedMap()
	 */
	private Map<FastTextViewListener, Integer> fastTextViewListeners = Collections.synchronizedMap (new WeakHashMap<FastTextViewListener, Integer>());

	/**
	 * Listeners for selection changes
	 * Thread safe by virtue of Collections.synchronizedMap()
	 */
	private Map<SelectionListener, Integer> selectionListeners = Collections.synchronizedMap (new WeakHashMap<SelectionListener, Integer>());

	/**
	 * Listeners for hyperlink invocations
	 * Thread safe by virtue of Collections.synchronizedMap()
	 */
	private Map<HyperlinkListener, Integer> hyperlinkListeners = Collections.synchronizedMap (new WeakHashMap<HyperlinkListener, Integer>());	


	/* Layout state */

	/**
	 * A thread responsible for asynchronously updating the visible layout after a resize
	 * Thread safe by finality
	 */
	private final LayoutThread layoutThread;

	/**
	 * Lock used to serialise layout and back buffer rendering
	 */
	private ReentrantLock layoutLock = new ReentrantLock();

	/**
	 * A suitable pre-calculated FontRenderContext for text layout 
	 * Thread safe by virtue of AtomicReference. Set only once but late - after the component is realised
	 */
	private AtomicReference<FontRenderContext> fontRenderContext = new AtomicReference<FontRenderContext>();

	/**
	 * Cache of previously created paragraph layouts
	 * Thread safe by virtue of Collections.synchronizedMap()
	 */
	private Map<Integer,SoftReference<ParagraphLayout>> paragraphLayoutCache = Collections.synchronizedMap (new HashMap<Integer,SoftReference<ParagraphLayout>>());

	/**
	 * Unified layout state
	 * Thread safe by virtue of AtomicStampedReference
	 */
	private AtomicReference<Layout> layout = new AtomicReference<Layout> (new Layout());


	/* Rendering state */

	/**
	 * Persistent double buffer. We manage this ourselves to avoid sucking during resize
	 * Thread safe by virtue of AtomicReference
	 */
	private AtomicReference<BufferedImage> bufferImage = new AtomicReference<BufferedImage>();


	/* Selection state */

	/**
	 * Unified selection state
	 * Thread safe by virtue of AtomicReference
	 */
	private AtomicReference<Selection> selection = new AtomicReference<Selection> (new Selection());


	/* Document state */

	/**
	 * The current Document
	 * Internally thread safe
	 */
	private AtomicReference<Document> document = new AtomicReference<Document> (new NullDocument());

	/**
	 * Current first visible position
	 * Thread safe by virtue of AtomicReference
	 */
	private AtomicReference<DocumentPosition> position = new AtomicReference<DocumentPosition> (new DocumentPosition (new NullDocument(), 0, new LayoutPosition (0, 0)));


	/**
	 * Pre-create a suitable FontRenderContext for text layout
	 */
	private void createFontRenderContext() {

		RenderingHints renderHints = new RenderingHints (RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
		renderHints.put (RenderingHints.KEY_RENDERING,RenderingHints.VALUE_RENDER_QUALITY);

		Graphics2D g2d = (Graphics2D) getGraphics();
		BufferedImage scratchImage = g2d.getDeviceConfiguration().createCompatibleImage (1, 1, Transparency.OPAQUE);
		Graphics2D scratchg2d = scratchImage.createGraphics();
		scratchg2d.setRenderingHints(renderHints);

		this.fontRenderContext.set (scratchg2d.getFontRenderContext());

	}


	/**
	 * Notifies FastTextViewListeners of a change in overflow status
	 * 
	 * @param overflowing The overflow status to notify
	 *  
	 * @return true if the FastTextView may have been resized
	 */
	private boolean notifyViewListenersOverflowChanged (boolean overflowing) {

		boolean maybeResized = false;
		for (FastTextViewListener listener : this.fastTextViewListeners.keySet()) {
			if (listener.displayOverflowing (overflowing)) {
				maybeResized = true;
			}
		}

		return maybeResized;

	}


	/**
	 * Notifies FastTextViewListeners of a change in view position
	 * 
	 * @param newPosition The new position 
	 */
	private void notifyViewListenersPositionChanged (LayoutPosition newPosition) {

		for (FastTextViewListener listener : this.fastTextViewListeners.keySet()) {
			listener.positionChanged (newPosition.getParagraphIndex(), newPosition.getLineIndex());
		}

	}


	/**
	 * Notifies FastTextViewListeners of a change in Document size
	 * 
	 * @param newSize The new size 
	 */
	private void notifyViewListenersDocumentSizeChanged (int newSize) {

		for (FastTextViewListener listener : this.fastTextViewListeners.keySet()) {
			listener.documentSizeChanged (newSize);
		}

	}


	/**
	 * Clear the back buffer if it exists
	 * 
	 * @param bufferImage The current buffer 
	 */
	private void clearBackBuffer (BufferedImage bufferImage) {

		if (bufferImage != null) {
			Graphics2D g2d = bufferImage.createGraphics();
			g2d.setBackground (Color.WHITE);
			g2d.clearRect (0, 0, bufferImage.getWidth(), bufferImage.getHeight());
		}

	}


	/**
	 * Pre-create a backing store BufferedImage and Graphics2D
	 * 
	 * @param bufferImage The current buffer 
	 * @return A new buffer (if our size has changed), or the cleared current buffer
	 */
	private BufferedImage createOrUpdateBackBuffer (BufferedImage bufferImage) {

		Graphics2D g2d = (Graphics2D) getGraphics();

		if ((bufferImage == null) || (getWidth() > bufferImage.getWidth()) || (getHeight() > bufferImage.getHeight())) {
			bufferImage = g2d.getDeviceConfiguration().createCompatibleImage (getWidth(), getHeight(), Transparency.OPAQUE); 
		}

		clearBackBuffer (bufferImage);

		return bufferImage;

	}


	/**
	 * Renders the currently laid out text
	 * 
	 * @param bufferImage The current buffer 
	 * @param layout The Layout to render 
	 * @param selection The current selection state
	 * @param firstParagraph The first paragraph to render
	 * @param lastParagraph The last paragraph to render
	 */
	private void renderText (BufferedImage bufferImage, Layout layout, Selection selection, int firstParagraph, int lastParagraph) {

		List<ParagraphLayout> paragraphLayoutList = layout.getParagraphLayoutList();

		if (paragraphLayoutList.size() == 0) {
			return;
		}

		float drawPosX = 0;
		float drawPosY = 0;

		Graphics2D g2d = bufferImage.createGraphics();
		
		float paragraphY1 = layout.getFirstParagraphTop();

		for (ParagraphLayout paragraphLayout : paragraphLayoutList) {

			if ((paragraphLayout.paragraphIndex >= firstParagraph) && (paragraphLayout.paragraphIndex <= lastParagraph)) {

				Color background;
				boolean highlight;
				if (selection.paragraphSelectionContains (paragraphLayout.paragraphIndex)) {
					background = this.highlightBackground;
					highlight = true;
				} else {
					background = Color.WHITE;
					highlight = false;
				}
				g2d.setBackground (background);
	
				float lineY1 = paragraphY1;
				for (LineLayout lineLayout : paragraphLayout.lines) {

					drawPosX = lineLayout.leftIndent;
					drawPosY = lineY1;
		
					float ascent = lineLayout.textLayouts[0].getAscent();

					drawPosY += ascent;
					for (int i = 0; i < lineLayout.textLayouts.length; i++) {
						TextLayout textLayout = lineLayout.textLayouts[i];
						Color foreground = highlight ? this.highlightForeground : lineLayout.foregrounds[i];
						g2d.setColor (foreground);
						textLayout.draw (g2d, drawPosX, drawPosY);
						drawPosX += textLayout.getAdvance();
					}
	
					lineY1 += lineLayout.height;
	
				}

			}

			paragraphY1 += paragraphLayout.height;

		}

	}


	/**
	 * Renders the currently laid out text. Convenience method to render all currently laid out lines
	 * 
	 * @param bufferImage The current buffer 
	 * @param layout The Layout to render 
	 * @param selection The current selection state
	 */
	private void renderText (BufferedImage bufferImage, Layout layout, Selection selection) {

		renderText (bufferImage, layout, selection, layout.getLayoutPosition().getParagraphIndex(), layout.getLayoutLastParagraphIndex());

	}


	/**
	 * Clears the back buffer and renders the paragraph background highlights
	 * 
	 * @param bufferImage The current buffer 
	 * @param layout The Layout to render 
	 * @param selection The Selection to render
	 */
	private void renderParagraphBackgrounds (BufferedImage bufferImage, Layout layout, Selection selection) {

		List<ParagraphLayout> paragraphLayoutList = layout.getParagraphLayoutList();

		if (paragraphLayoutList.size() == 0) {
			return;
		}

		Graphics2D g2d = bufferImage.createGraphics();

		Dimension size = getSize();

		float paragraphY1 = layout.getFirstParagraphTop();
		int intParagraphY1 = 0;

		// The running position is tracked with integers in this loop to avoid
		// leaving gaps between the paragraph backgrounds
		for (ParagraphLayout paragraphLayout : paragraphLayoutList) {

			int intParagraphY2 = (int)(paragraphY1 + paragraphLayout.height);

			Color background;
			if (selection.paragraphSelectionContains (paragraphLayout.paragraphIndex)) {
				background = this.highlightBackground;
			} else {
				background = paragraphLayout.background;
			}
			if (!Color.WHITE.equals (background)) {
				g2d.setBackground (background);
				g2d.clearRect (0, intParagraphY1, size.width, (intParagraphY2 - intParagraphY1 + 1));
			}

			paragraphY1 += paragraphLayout.height;
			intParagraphY1 = intParagraphY2;

		}

	}


	/**
	 * Create a ParagraphLayout for a given document index and component size
	 *
	 * @param document The Document to creat the layout from 
	 * @param documentIndex The document index
	 * @param size The component size
	 * @return An appropriate ParagraphLayout, or null if the size was too small
	 */
	private ParagraphLayout createParagraphLayout (Document document, int documentIndex, Dimension size) {

		List<LineLayout> lines = new ArrayList<LineLayout>();

		AttributedString attributedString = document.getParagraph (documentIndex);
		if (attributedString == null) {
			return null;
		}

		AttributedCharacterIterator iterator = attributedString.getIterator();
		Color background = document.getBackground (documentIndex);
		background = (background != null) ? background : Color.WHITE;

		List<ParagraphLayout.Hyperlink> hyperlinks = new ArrayList<ParagraphLayout.Hyperlink>();
		int hyperlinkRunStart = iterator.getRunStart (DocumentAttribute.HYPERLINK);
		do {
			iterator.setIndex (hyperlinkRunStart);
			Object hyperlinkValue = iterator.getAttribute (DocumentAttribute.HYPERLINK);
			int hyperlinkRunLimit = iterator.getRunLimit (DocumentAttribute.HYPERLINK);
			if (hyperlinkValue != null) {
				hyperlinks.add (new ParagraphLayout.Hyperlink (hyperlinkRunStart, hyperlinkRunLimit - 1, hyperlinkValue));
			}
			hyperlinkRunStart = hyperlinkRunLimit;
		} while (hyperlinkRunStart < iterator.getEndIndex());

		LineBreakMeasurer measurer = new LineBreakMeasurer (iterator, this.fontRenderContext.get());
		List<TextLayout> textLayouts = new ArrayList<TextLayout>();
		List<Color> foregrounds = new ArrayList<Color>();

		int endIndex = iterator.getEndIndex();
		int runStart = iterator.getBeginIndex();
		int runLimit;
		int startCharacterIndex = iterator.getBeginIndex();
		int endCharacterIndex = startCharacterIndex - 1;

		float paragraphHeight = 0;

		// Set indent and width for first line
		int indent = INITIAL_INDENT;
		float lineWidth = size.width - indent - (2 * INSIDE_BORDER);
		int maxExtent = indent;

		if (lineWidth <= 0) {
			return null;
		}

		boolean requireNextWord = false;

		iterator.setIndex (startCharacterIndex);
		do {
			runLimit = iterator.getRunLimit();
	
			// Record run colour
			Color foreground = (Color) iterator.getAttribute (TextAttribute.FOREGROUND);
			foreground = (foreground == null) ? Color.BLACK : foreground;

			// Clear colour attribute from original attributed string before segmentation
			attributedString.addAttribute (TextAttribute.FOREGROUND, null, runStart, runLimit);

			TextLayout layout;
			int position = runStart;

			measurer = new LineBreakMeasurer (iterator, this.fontRenderContext.get());
			measurer.setPosition (position);

			while (position < runLimit) {

				do {
					layout = measurer.nextLayout (lineWidth, runLimit, requireNextWord);
					requireNextWord = true;
					if (layout != null) {
						textLayouts.add (layout);
						foregrounds.add (foreground);
						lineWidth -= layout.getAdvance();
						maxExtent += layout.getAdvance();
						position += layout.getCharacterCount();
						endCharacterIndex += layout.getCharacterCount();
					}
				} while ((layout != null) && (position < runLimit));

				if (position < runLimit) {

					if (textLayouts.size() == 0) {
						return null;
					}

					// Commit line
					layout = textLayouts.get(0);
					float lineHeight = layout.getAscent() + layout.getDescent() + layout.getLeading() + LINE_SPACING;
					lines.add (new LineLayout (INSIDE_BORDER + indent, maxExtent, lineHeight, textLayouts.toArray (new TextLayout[]{}), foregrounds.toArray (new Color[]{}), startCharacterIndex, endCharacterIndex));
					paragraphHeight += lineHeight;
					startCharacterIndex = endCharacterIndex + 1;

					// New line
					requireNextWord = false;
					textLayouts = new ArrayList<TextLayout>();
					foregrounds = new ArrayList<Color>();
					
					// Set indent and width for first line
					indent = SUBSEQUENT_INDENT;
					maxExtent = indent;
					lineWidth = size.width - indent - (2 * INSIDE_BORDER);

				} else if (position == endIndex) {

					// Commit line
					layout = textLayouts.get(0);
					float lineHeight = layout.getAscent() + layout.getDescent() + layout.getLeading() + LINE_SPACING;
					lines.add (new LineLayout (INSIDE_BORDER + indent, maxExtent, lineHeight, textLayouts.toArray (new TextLayout[]{}), foregrounds.toArray (new Color[]{}), startCharacterIndex, endCharacterIndex));
					paragraphHeight += lineHeight;
					startCharacterIndex = endCharacterIndex + 1;

				}

			}

			iterator.setIndex (runLimit);
			runStart = runLimit;

		} while (runLimit < endIndex);

		paragraphHeight += PARAGRAPH_SPACING;

		return new ParagraphLayout (documentIndex, attributedString, background, lines, size.width, paragraphHeight, hyperlinks.toArray (new ParagraphLayout.Hyperlink[]{}));

	}


	/**
	 * Gets a ParagraphLayout for a given document index and component size, creating it if necessary
	 *
	 * @param document The Document to creat the layout from 
	 * @param documentIndex The document index
	 * @param size The component size
	 * @return An appropriate ParagraphLayout, or null if the size was too small
	 */
	ParagraphLayout getParagraphLayout (Document document, int documentIndex, Dimension size) {

		ParagraphLayout paragraphLayout = null;

		SoftReference<ParagraphLayout> layoutReference = this.paragraphLayoutCache.get (documentIndex);
		if (layoutReference != null) {
			paragraphLayout = layoutReference.get();
			if ((paragraphLayout != null) && (paragraphLayout.validWidth != size.width)) {
				paragraphLayout = null;
				this.paragraphLayoutCache.remove (documentIndex);
			}
		}

		if (paragraphLayout == null) {
			paragraphLayout = createParagraphLayout (document, documentIndex, size);
			this.paragraphLayoutCache.put (documentIndex, new SoftReference<ParagraphLayout>(paragraphLayout));
		}

		return paragraphLayout;

	}


	/**
	 * Lay out the visible text
	 * 
	 * @param document 
	 * @param previousLayout 
	 * @param size 
	 * @param position 
	 * @return true if layout was successful
	 */
	private Layout layoutLines (Document document, Layout previousLayout, Dimension size, LayoutPosition position) {

		if (this.fontRenderContext.get() == null) {
			createFontRenderContext();
		}

		if ((size.width <= 0) || (size.height <= 0)) {
			return null;
		}
	
		ArrayList<ParagraphLayout> paragraphLayoutList = new ArrayList<ParagraphLayout>();

		float posY = INSIDE_BORDER;
		ParagraphLayout paragraphLayout = getParagraphLayout (document, position.getParagraphIndex(), size);
		if (paragraphLayout != null) {
			for (int j = 0; j < position.getLineIndex(); j++) {
				posY -= paragraphLayout.lines.get(j).height;
			}
		}

		float firstParagraphTop = posY;

		int documentSize = document.getSize();
		int documentIndex;
		for (documentIndex = position.getParagraphIndex() ; (documentIndex < documentSize) && (posY < size.height); documentIndex++) {

			paragraphLayout = getParagraphLayout (document, documentIndex, size);

			// If we failed to create a layout, abort and leave the layout as it is.
			// This can happen if the view width is very thin
			if (paragraphLayout == null) {
				return null;
			}

			posY += paragraphLayout.height;
			paragraphLayoutList.add (paragraphLayout);

		}

		boolean newOverflowStatus;
		if ((position.getParagraphIndex() == 0) && (posY < size.height)) {
			newOverflowStatus = false;
		} else {
			newOverflowStatus = true;
		}

		Layout newLayout = new Layout (document, paragraphLayoutList, position, size, firstParagraphTop, documentIndex, newOverflowStatus);

		this.layout.set (newLayout);

		return newLayout;

	}


	/**
	 * Perform layout of the currently visible text. If necessary, expand the persistent backing store
	 * 
	 * @param waitForLock If true, block if necessary and wait for the layout
	 * lock to become available. If false, perform layout only if the layout
	 * lock is available 
	 * @return true if layout has taken place
	 *  
	 */
	boolean renderBackBuffer (boolean waitForLock) {

		long t0 = 0, t1 = 0, t2 = 0, t3 = 0, t4 = 0;

		if (waitForLock) {
			this.layoutLock.lock();
		} else {
			if (!this.layoutLock.tryLock()) {
				return false;
			}
		}

		// TODO thread safety
		Dimension newSize = getSize();

		Layout newLayout = null;

		boolean signalResize = false;

		try {

			BufferedImage bufferImage = this.bufferImage.get();
	
			t0 = System.currentTimeMillis();

			Document document = this.document.get();
			Layout previousLayout = this.layout.get();
			DocumentPosition position = getValidDocumentPosition (document, newSize);
			Selection selection = getValidSelection(document);

			newLayout = layoutLines (document, previousLayout, newSize, position.getLayoutPosition());
	
			if (newLayout != null) {

				signalResize = (newLayout.isOverflowing() != previousLayout.isOverflowing());
	
				t1 = System.currentTimeMillis();
		
				if (!signalResize) {
		
					BufferedImage newBufferImage = createOrUpdateBackBuffer (bufferImage);
		
					t2 = System.currentTimeMillis();
		
					renderParagraphBackgrounds (newBufferImage, newLayout, selection);
		
					t3 = System.currentTimeMillis();
		
					renderText (newBufferImage, newLayout, selection);
					paintSelection (newBufferImage, newLayout, selection);
		
					this.bufferImage.set (newBufferImage);
	
				}

			}
	
		} finally {

			this.layoutLock.unlock();

		}


		if (signalResize) {

			// TODO marginal race here
			if (notifyViewListenersOverflowChanged (newLayout.isOverflowing())) {
				// A scrollbar has probably just appeared or disappeared
				// Forego the usual delay betwen resize and redraw
				this.layoutThread.setSuppressUntilResize();
			}

		}

		t4 = System.currentTimeMillis();
		
		if (DEBUG) {
			System.out.print ("Rendering Time: " + (t4 - t0) + " (");
			// Overflow check is approximate...
			System.out.print ("Layout: " + (t1 - t0) + ((newLayout == null) ? " [Overflow]" : ""));
			
			if (newLayout != null) {
				System.out.print ("; Buffer: " + (t2 - t1));
				System.out.print ("; Background: " + (t3 - t2));
				System.out.print ("; Text: " + (t4 - t3));
			} else {
				System.out.print (" [Aborted]");
			}
			System.out.println (")");
		}


		return (newLayout != null);

	}


	/**
	 * Gets a position valid for the given document. If the stored position is against the
	 * current document and layout width it is returned as is. If it is against the current
	 * document but not the given size, a position at the beginning of the current paragraph
	 * is returned. If the stored position is not against the current document, a position
	 * at the start of the document is returned
	 *
	 * @param document The document to get a selection for
	 * @param size The valid size to get a selection for
	 * @return The valid selection
	 */
	private DocumentPosition getValidDocumentPosition (Document document, Dimension size) {

		DocumentPosition position = this.position.get();
		if (position.getDocument() != document) {
			position = new DocumentPosition (document, size.width, new LayoutPosition (0, 0));
		} else if (position.getLayoutWidth() != size.width) {
			position = new DocumentPosition (document, size.width, new LayoutPosition (position.getLayoutPosition().getParagraphIndex(), 0));
		}

		return position;

	}


	/**
	 * Gets a selection valid for the given document. If the stored selection is against the current
	 * document, that selection is returned; otherwise, a blank selection is returned
	 *
	 * @param document The document to get a selection for
	 * @return The valid selection
	 */
	private Selection getValidSelection (Document document) {

		Selection selection;
		selection = this.selection.get();
		if (selection.document != document) {
			selection = new Selection();
		}

		return selection;

	}


	/**
	 * Immediately paint the given text selection
	 * 
	 * @param bufferImage The current buffer
	 * @param layout The Layout to render from
	 * @param selection The selection to paint
	 */
	private void paintSelection (BufferedImage bufferImage, Layout layout, Selection selection) {

		if (layout.getParagraphLayoutList().size() == 0) {
			return;
		}

		Graphics2D g2d = bufferImage.createGraphics();

		float paragraphY1 = layout.getFirstParagraphTop();
		int intParagraphY1 = 0;

		for (ParagraphLayout paragraphLayout : layout.getParagraphLayoutList()) {

			int intParagraphY2 = (int)(paragraphY1 + paragraphLayout.height);

			if (selection.paragraphSelectionContains (paragraphLayout.paragraphIndex)) {

				Color background;
				if (selection.paragraphSelectionContains (paragraphLayout.paragraphIndex)) {
					background = this.highlightBackground;
				} else {
					background = paragraphLayout.background;
				}

				g2d.setBackground (background);
				g2d.clearRect (0, intParagraphY1, bufferImage.getWidth(), (intParagraphY2 - intParagraphY1 + 1));

				
			} else if (selection.textSelectionWithin (paragraphLayout.paragraphIndex)) {

				int startCharacterIndex = selection.selectionRange.getStartCharacterIndex();
				int endCharacterIndex = selection.selectionRange.getEndCharacterIndex();

				float lineY1 = paragraphY1;
				for (LineLayout lineLayout : paragraphLayout.lines) {

					if ((startCharacterIndex <= lineLayout.endCharacterIndex)
							&& (endCharacterIndex >= lineLayout.startCharacterIndex))
					{
						int highlightStartIndex = (startCharacterIndex < lineLayout.startCharacterIndex ? 0 : startCharacterIndex - lineLayout.startCharacterIndex);
						int highlightEndIndex = (endCharacterIndex > lineLayout.endCharacterIndex ? lineLayout.endCharacterIndex - lineLayout.startCharacterIndex + 1 : endCharacterIndex - lineLayout.startCharacterIndex + 1);

						if (highlightEndIndex >= highlightStartIndex) {

							int subLayoutStartIndex = 0;
							int startX = lineLayout.leftIndent;
							for (TextLayout textLayout : lineLayout.textLayouts) {
								int subLayoutEndIndex = subLayoutStartIndex + textLayout.getCharacterCount();
								if ((highlightStartIndex <= subLayoutEndIndex) && (highlightEndIndex >= subLayoutStartIndex))
								{
									int subHighlightStart = Math.max (subLayoutStartIndex, highlightStartIndex) - subLayoutStartIndex;
									int subHighlightEnd = Math.min (subLayoutEndIndex, highlightEndIndex) - subLayoutStartIndex;
									Shape rawHighlight = textLayout.getLogicalHighlightShape (subHighlightStart, subHighlightEnd);
									AffineTransform transform = AffineTransform.getTranslateInstance (startX, lineY1 + textLayout.getAscent());
									Shape highlight = transform.createTransformedShape (rawHighlight);
									g2d.setColor (this.highlightBackground);
									g2d.setClip (0, (int)paragraphY1, paragraphLayout.validWidth, (int)paragraphLayout.height);
									g2d.fill (highlight);
									g2d.setColor (this.highlightForeground);
									g2d.setClip (highlight);
									textLayout.draw (g2d, startX, lineY1 + textLayout.getAscent());
								}
								startX += textLayout.getAdvance();
								subLayoutStartIndex += textLayout.getCharacterCount();
							}

							g2d.setClip (null);
						}
					}

					lineY1 += lineLayout.height;

				}

			}

			intParagraphY1 = intParagraphY2;
			paragraphY1 += paragraphLayout.height;
		}

		if (selection.isParagraphSelection()) {
			int startIndex = Math.min (selection.selectionStart.getParagraphIndex(), selection.selectionEnd.getParagraphIndex());
			int endIndex = Math.max (selection.selectionStart.getParagraphIndex(), selection.selectionEnd.getParagraphIndex());
			renderText (bufferImage, layout, selection, startIndex, endIndex);
		}

	}


	/**
	 * Notifies all attached SelectionListeners that the text selection has changed
	 */
	private void notifySelectionListeners() {

		for (SelectionListener listener : this.selectionListeners.keySet()) {
			listener.selectionChanged();
		}

	}


	/**
	 * Notifies all attached HyperlinkListeners that a hyperlink was invoked
	 * 
	 * @param value The hyperlink value 
	 */
	private void notifyHyperlinkListenersInvoked (Object value) {

		for (HyperlinkListener listener : this.hyperlinkListeners.keySet()) {
			listener.hyperlinkInvoked (value);
		}

	}


	/**
	 * Sets the first visible position within the Document
	 * 
	 * @param newPosition The new position
	 * @return true if the position was changed
	 */
	private boolean setPosition (LayoutPosition newPosition) {

		if (this.position.get().equals (newPosition)) {
			return false;
		}

		// TODO etc
		this.position.set (new DocumentPosition (this.document.get(), this.layout.get().getSize().width , newPosition));

		notifyViewListenersPositionChanged (newPosition);

		this.layoutThread.layout();

		return true;

	}


	/**
	 * Starts a new selection
	 * 
	 * @param x The x coordinate of the startpoint
	 * @param y The y coordinate of the startpoint
	 */
	private void startSelection (int x, int y) {

		Layout layout = this.layout.get();
		Selection selection = this.selection.get();

		Selection newSelectionState = new Selection (this.document.get(), true, SelectionAddress.create (layout, x, y, null), null, null);

		if (this.selection.compareAndSet (selection, newSelectionState)) {

			this.layoutThread.layout();

		}

	}


	/**
	 * Updates the current selection with a new endpoint
	 * 
	 * @param x The x coordinate of the new endpoint
	 * @param y The y coordinate of the new endpoint
	 * @param setStarted true if the selection should be set in progress
	 * @param setCompleted true if the selection should be set completed
	 */
	private void updateSelection (int x, int y, boolean setStarted, boolean setCompleted) {

		Layout layout = this.layout.get();
		Selection selection =  this.selection.get();
		Selection selectionCopy = selection.clone();

		if (setStarted) {
			selectionCopy.selecting = true;
		}

		if (selectionCopy.selecting) {
			selectionCopy.selectionEnd = SelectionAddress.create (layout, x, y, selectionCopy.selectionStart);
			selectionCopy.selectionRange = SelectionRange.create (selectionCopy.selectionStart, selectionCopy.selectionEnd);
		}

		if (setCompleted) {
			selectionCopy.selecting = false;
		}

		if (this.selection.compareAndSet (selection, selectionCopy)) {

			if (setCompleted) {
				notifySelectionListeners();
			}

			this.layoutThread.layout();

		}

	}


	/**
	 * Show the component's popup menu, if set, if the given MouseEvent is a popup trigger
	 * 
	 * <b>Thread safety:</b> This method is not thread safe, and should only be called from the
	 * Event Dispatch Thread
	 *
	 * @param e
	 */
	private void showPopupMenuIfNeeded (MouseEvent e) {

		if (e.isPopupTrigger()) {
			JPopupMenu menu = getComponentPopupMenu();
			if (menu != null) {
				menu.show (this, e.getX(), e.getY());
			}
		}

	}


	/**
	 * Returns the current Document<br><br>
	 * 
	 * <b>Thread safety:</b> This method is thread safe
	 *
	 * @return The Document
	 */
	public Document getDocument() {

		return this.document.get();

	}


	/**
	 * Sets a new Document for display<br><br>
	 * 
	 * <b>Thread safety:</b> This method is thread safe
	 *
	 * @param document The Document to display
	 * 
	 */
	public void setDocument (Document document) {

		if (DEBUG) {
			System.out.println ("New Document: " + ((document == null)  ? "null" : document.getClass().getCanonicalName()));
			this.debugCurrentDocumentSetMilliseconds = System.currentTimeMillis();
		}

		if (document == null) {
			document = new NullDocument();
		}

		this.layoutLock.lock();

		Document currentDocument = this.document.get();

		if (this.document.compareAndSet (currentDocument, document)) {

			currentDocument.removeListener (this);
			document.addListener (this);

			this.paragraphLayoutCache.clear();

			this.selection.set (new Selection());
			notifySelectionListeners();
			notifyViewListenersDocumentSizeChanged (document.getSize());

			if (!setPosition (new LayoutPosition (0, 0))) {
				this.layoutThread.layout();
			}

		}

		this.layoutLock.unlock();

	}


	/**
	 * Gets the first visible paragraph<br><br>
	 * 
	 * <b>Thread safety:</b> This method is thread safe, although it provides no locking against Document replacement
	 *
	 * @return Index of the first visible paragraph
	 */
	public int getParagraph() {
	
		return this.position.get().getLayoutPosition().getParagraphIndex();
	
	}


	/**
	 * Sets the first visible paragraph<br><br>
	 * 
	 * <b>Thread safety:</b> This method is thread safe
	 *
	 * @param paragraphIndex Index of the first visible paragraph
	 */
	public void setParagraph (int paragraphIndex) {

		Document document = this.document.get();
		Layout layout = this.layout.get();

		if (layout.getDocument() != document) {
			return;
		}

		if (layout.isOverflowing()) {
	
			paragraphIndex = Math.max (0, Math.min (document.getSize() - 1, paragraphIndex));	
			setPosition (new LayoutPosition (paragraphIndex, 0));
	
		}
	
	}


	/**
	 * Reports if there is currently a text or paragraph selection<br><br>
	 * 
	 * <b>Thread safety:</b> This method is thread safe, although it provides
	 * no locking against selection or Document changes. A later call to
	 * getSelectedText() is not guaranteed to return any text
	 * 
	 * @return True if there is a selection; false otherwise
	 */
	public boolean hasSelection() {

		return (this.selection.get().isTextSelection() || this.selection.get().isParagraphSelection());

	}


	/**
	 * Reports if there is currently a text selection<br><br>
	 * 
	 * <b>Thread safety:</b> This method is thread safe, although it provides
	 * no locking against selection or Document changes. A later call to
	 * getSelectedText() is not guaranteed to return any text
	 *
	 * @return True if there is a selection; false otherwise
	 */
	public boolean hasTextSelection() {

		return this.selection.get().isTextSelection();

	}


	/**
	 * Gets the text of the current selection. Returns null if the current
	 * selection is empty<br><br>
	 * 
	 * <b>Thread safety:</b> This method is thread safe
	 * 
	 * @return The selected text
	 */
	public String getSelectedText() {

		Selection selection = this.selection.get();

		StringBuilder selectionStringBuilder = new StringBuilder();

		if (selection.isTextSelection()) {

			SelectionRange selectionRange = selection.selectionRange;
	
			AttributedString paragraph = selection.document.getParagraph (selectionRange.getParagraphIndex());
			AttributedCharacterIterator iterator = paragraph.getIterator();
			char c = iterator.setIndex (selectionRange.getStartCharacterIndex());
			while ((c != AttributedCharacterIterator.DONE)
					&& (iterator.getIndex() <= selectionRange.getEndCharacterIndex()))
			{
				selectionStringBuilder.append (c);
				c = iterator.next();
			}

		} else if (selection.isParagraphSelection()) {

			int startIndex = Math.min (selection.selectionStart.getParagraphIndex(), selection.selectionEnd.getParagraphIndex());
			int endIndex = Math.max (selection.selectionStart.getParagraphIndex(), selection.selectionEnd.getParagraphIndex());

			try {

				for (int i = startIndex; i <= endIndex; i++) {
					String paragraphText = selection.document.getPlainParagraph (i);
					selectionStringBuilder.append (paragraphText);
				}

				// See SelectionTransferable#getTransferData()
				new StringBuilder().ensureCapacity ((int) (selectionStringBuilder.capacity() * 1.5));

			} catch (OutOfMemoryError e) {

				selectionStringBuilder = new StringBuilder();

			}
		} else {

			return null;

		}

		return selectionStringBuilder.toString();

	}


	/**
	 * Scrolls lines of text up or down<br><br>
	 * 
	 * <b>Thread safety:</b> This method is thread safe
	 * 
	 * @param numLines Number of lines to scroll 
	 */
	public void scrollLines (int numLines) {

		Document document = this.document.get();
		Layout layout = this.layout.get();

		if (layout.getDocument() != document) {
			return;
		}

		LayoutPosition layoutPosition = getValidDocumentPosition(document, layout.getSize()).getLayoutPosition();

		LayoutIterator iterator = new LayoutIterator (this, document, layout.getSize(), layoutPosition);

		if (numLines < 0) {

			int i = numLines;
			while (iterator.hasPrevious() && (i < 0)) { 
				iterator.previous();
				i++;
			}
	
		} else {
	
			if (layout.isOverflowing()) {
	
				int i = 0;
				while (iterator.hasNext() && (i < numLines)) { 
					iterator.next();
					i++;
				}
	
			}
	
		}
	
		setPosition (iterator.currentPosition());
	
	}


	/**
	 * Scrolls a single page of text up or down<br><br>
	 * 
	 * <b>Thread safety:</b> This method is thread safe
	 *
	 * @param scrollUp true to scroll page up; false to scroll page down
	 */
	public void scrollPage (boolean scrollUp) {

		Document document = this.document.get();
		Layout layout = this.layout.get();

		if (layout.getDocument() != document) {
			return;
		}

		LayoutPosition layoutPosition = getValidDocumentPosition(document, layout.getSize()).getLayoutPosition();

		Dimension size = getSize();

		LayoutIterator iterator = new LayoutIterator (this, document, layout.getSize(), layoutPosition);
		LayoutPosition newPosition = iterator.currentPosition();

		if (scrollUp) {

			float lineY1 = -INSIDE_BORDER;

			LineLayout lineLayout = iterator.current();
			lineY1 -= lineLayout.height;
			boolean done = false;
			while (!done) {
				if (!iterator.hasPrevious()) {
					done = true;
				} else {
					lineLayout = iterator.previous();
					if ((lineY1 - lineLayout.height) <= -size.height) {
						done = true;
					} else {
						lineY1 -= lineLayout.height;
						newPosition = iterator.currentPosition();
					}
				}
			}

		} else {

			if (layout.isOverflowing()) {
				float lineY1 = INSIDE_BORDER;
	
				LineLayout lineLayout = iterator.current();
				lineY1 += lineLayout.height;
				boolean done = false;
				while (!done) {
					if (!iterator.hasNext()) {
						done = true;
					} else {
						lineLayout = iterator.next();
						if ((lineY1 + lineLayout.height) > size.height) {
							done = true;
						} else {
							lineY1 += lineLayout.height;
							newPosition = iterator.currentPosition();
						}
					}
				}
			}

		}

		setPosition (newPosition);

	}


	/**
	 * Scrolls to the end of the current document<br><br>
	 * 
	 * <b>Thread safety:</b> This method is thread safe
	 */
	public void scrollEnd () {

		Document document = this.document.get();
		Layout layout = this.layout.get();

		if (layout.getDocument() != document) {
			return;
		}


		float lineY1 = -INSIDE_BORDER;

		ParagraphLayout lastParagraphLayout = getParagraphLayout (document, document.getSize() - 1, layout.getSize());

		LayoutPosition originalPosition = new LayoutPosition (lastParagraphLayout.paragraphIndex, lastParagraphLayout.lines.size() - 1);

		LayoutIterator iterator = new LayoutIterator (this, document, layout.getSize(), originalPosition);

		LayoutPosition newPosition = iterator.currentPosition();
		LineLayout lineLayout = iterator.current();
		lineY1 -= lineLayout.height;
		boolean done = false;
		while (!done) {
			if (!iterator.hasPrevious()) {
				done = true;
			} else {
				lineLayout = iterator.previous();
				if ((lineY1 - lineLayout.height) <= -layout.getSize().height) {
					done = true;
				} else {
					lineY1 -= lineLayout.height;
					newPosition = iterator.currentPosition();
				}
			}
		}

		setPosition (newPosition);
		
	}


	/**
	 * Adds a listener for changes in selected text<br><br>
	 * 
	 * <b>Thread safety:</b> This method is thread safe
	 *
	 * @param listener The SelectionListener to add
	 */
	public void addSelectionListener (SelectionListener listener) {
	
		this.selectionListeners.put (listener, 1);
	
	}


	/**
	 * Adds a listener for changes in the display overflow status<br><br>
	 * 
	 * <b>Thread safety:</b> This method is thread safe
	 *
	 * @param listener The listener to add
	 */
	public void addFastTextViewListener (FastTextViewListener listener) {
	
		this.fastTextViewListeners.put (listener, 1);
	
	}


	/**
	 * Adds a listener for hyperlink invocation<br><br>
	 * 
	 * <b>Thread safety:</b> This method is thread safe
	 *
	 * @param listener The listener to add
	 */
	public void addHyperlinkListener (HyperlinkListener listener) {
	
		this.hyperlinkListeners.put (listener, 1);
	
	}


	/* (non-Javadoc)
	 * @see java.awt.Component#repaint()
	 * 
	 * A debugging override to allow quick tracking of the source of repaint() requests
	 */
	@Override
	public void repaint() {

		if (DEBUG) {
			System.out.print ("Repaint commanded from: ");
			StackTraceElement[] elements = new Exception().getStackTrace();
			if (elements.length >= 2) {
				System.out.print (elements[1]);
			}
			System.out.println();
		}

		super.repaint();

	}


	/* (non-Javadoc)
	 * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
	 * 
	 * Copies the current back buffer to screen, outfilling with background
	 * colour if necessary
	 * 
	 * Thread safety: This method is not thread safe. It should only be called
	 * by Swing from the Event Dispatch Thread
	 */
	@Override
	public void paintComponent (Graphics g) {

		long t0 = 0, t1 = 0;

		if (DEBUG) {
			t0 = System.currentTimeMillis();
		}

		// Not ideal to take a lock here. We could avoid it with a pair of back buffers
		this.layoutLock.lock();

		if (DEBUG) {
			t1 = System.currentTimeMillis();
			System.out.println ("Paint waited for lock: " + (t1 - t0));
		}

		try {

			Graphics2D g2d = (Graphics2D) g;
	
			if (DEBUG) {
				t0 = System.currentTimeMillis();
			}
	
			BufferedImage bufferImage = this.bufferImage.get();
	
			// If we have resized upwards since the last layout, clear the background to cover the area not filled by the double buffer 
			if ((bufferImage == null) || (getWidth() > bufferImage.getWidth()) || (getWidth() > bufferImage.getWidth())) {
				g2d.setBackground (Color.WHITE);
				g2d.clearRect (0, 0, getWidth(), getHeight());
			}
	
			// Copy the double buffer to screen
			if (bufferImage != null) {
				g2d.drawImage (bufferImage, 0, 0, bufferImage.getWidth(), bufferImage.getHeight(), null);
			}
	
			if (DEBUG) {
				t1 = System.currentTimeMillis();
				System.out.println ("Paint Time: " + (t1 - t0) + " (" + (t0 - this.debugLastPaintMilliseconds) + " since last paint completed)");
				this.debugLastPaintMilliseconds = t1;
		
				if (this.debugCurrentDocumentSetMilliseconds > 0) {
					System.out.println ("Set-to-paint time: " + (t1 - this.debugCurrentDocumentSetMilliseconds));
				}
			}
	
			this.layoutThread.resetTimer();

		} finally {

			this.layoutLock.unlock();

		}

	}


	/* (non-Javadoc)
	 * @see org.itadaki.fasttextpane.DocumentListener#documentAppended()
	 * 
	 * Thread safety: This method is thread safe
	 */
	@Override
	public void documentExpanded (int oldSize, int newSize) {

		notifyViewListenersDocumentSizeChanged (newSize);

		if (oldSize <= this.layout.get().getLayoutLastParagraphIndex()) {
			this.layoutThread.layout();
		}
		
	}


	/* (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseClicked (MouseEvent e) {

		Object hyperlinkValue = this.layout.get().getHyperlinkValue (e.getX(), e.getY());
		if (hyperlinkValue != null) {
			notifyHyperlinkListenersInvoked (hyperlinkValue);
		}

	}


	/* (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseEntered (MouseEvent e) {
		// Do nothing
	}


	/* (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseExited (MouseEvent e) {
		// Do nothing
	}


	/* (non-Javadoc)
	 * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
	 * 
	 * Thread safety: This method is thread safe, but should only be called by
	 * Swing from the Event Dispatch Thread
	 */
	@Override
	public void mousePressed (MouseEvent e) {

		showPopupMenuIfNeeded (e);

		if (e.getButton() == MouseEvent.BUTTON1) {

			setCursor (new Cursor (Cursor.DEFAULT_CURSOR));

			if (!e.isShiftDown()) {

				startSelection (e.getX(), e.getY());

			} else {

				updateSelection (e.getX(), e.getY(), true, true);

				Clipboard selectionClipboard = getToolkit().getSystemSelection();
				Selection selection = this.selection.get();
				if ((selectionClipboard != null) && (selection != null)) {
					selectionClipboard.setContents (new SelectionTransferable (selection), null);
				}

			}

		}

	}


	/* (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
	 * 
	 * Thread safety: This method is thread safe, but should only be called by
	 * Swing from the Event Dispatch Thread
	 */
	@Override
	public void mouseReleased (MouseEvent e) {

		showPopupMenuIfNeeded (e);

		int x = e.getX();
		int y = e.getY();

		if (y < 0) {
			y = 5;
		}

		if ((e.getButton() == MouseEvent.BUTTON1)
			&& !e.isShiftDown())
		{

			updateSelection (x, y, false, true);

			Clipboard selectionClipboard = getToolkit().getSystemSelection();
			Selection selection = this.selection.get();
			if ((selectionClipboard != null) && (selection != null)) {
				selectionClipboard.setContents (new SelectionTransferable (selection), null);
			}

		}

	}


	/* (non-Javadoc)
	 * @see java.awt.event.MouseMotionListener#mouseDragged(java.awt.event.MouseEvent)
	 * 
	 * Thread safety: This method is thread safe, but should only be called by
	 * Swing from the Event Dispatch Thread
	 */
	@Override
	public void mouseDragged (MouseEvent e) {

		int x = e.getX();
		int y = e.getY();

		int numLines = 0;
		if (y < 0) {
			numLines = -Math.max (1, -y / 25);			
			y = 5;
		}
		if (y > getHeight()) {
			numLines = Math.max (1, (y - getHeight()) / 25);
		}

		updateSelection (x, y, false, false);

		if (numLines != 0) {
			scrollLines (numLines);
		}

	}


	/* (non-Javadoc)
	 * @see java.awt.event.MouseMotionListener#mouseMoved(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseMoved (MouseEvent e) {

		Object hyperlinkValue = this.layout.get().getHyperlinkValue (e.getX(), e.getY());
		if (hyperlinkValue != null) {
			setCursor (new Cursor (Cursor.HAND_CURSOR));
		} else{
			setCursor (new Cursor (Cursor.DEFAULT_CURSOR));
		}

	}

	
	/**
	 * Default constructor 
	 */
	public FastTextView() {

		this.highlightBackground = UIManager.getDefaults().getColor ("textHighlight");
		this.highlightForeground = UIManager.getDefaults().getColor ("textHighlightText");

		this.layoutThread = new LayoutThread (this);
		this.layoutThread.start();

		setOpaque (true);
		setDoubleBuffered (false);

		addMouseListener (this);
		addMouseMotionListener (this);

		Action actions[] = new Action[] {
				new CopyAction (this),
				new ScrollUpAction (this),
				new ScrollDownAction (this),
				new ScrollPageUpAction (this),
				new ScrollPageDownAction (this),
				new ScrollHomeAction (this),
				new ScrollEndAction (this)
		};

		for (Action action : actions) {
			getActionMap().put (action.getValue (Action.NAME), action);
		}

	}

}
