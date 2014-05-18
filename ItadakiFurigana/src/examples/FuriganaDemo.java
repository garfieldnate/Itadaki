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

package examples;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedOutputStream;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import net.java.sen.dictionary.Reading;

import org.itadaki.client.furigana.FuriganaService;
import org.itadaki.client.furigana.MissingDictionaryException;
import org.itadaki.client.furigana.SentenceListener;
import org.itadaki.client.furigana.SentenceProvider;


/**
 * Standalone furigana service demo
 */
public class FuriganaDemo {

	/**
	 * Demonstration SentenceProvider that breaks sentences on "\n"
	 */
	private class DemoSentenceProvider implements SentenceProvider {

		/**
		 * The sentences provided
		 */
		private String[] sentences;

		/**
		 * The index of the current sentence
		 */
		private int sentenceIndex;

		/**
		 * Cache of readings that have previously been set. Used to reflow
		 * readings back to the processor
		 */
		private List<Reading>[] readingCache;

		/* (non-Javadoc)
		 * @see org.itadaki.client.furigana.SentenceProvider#getReadings()
		 */
		@Override
		public List<Reading> getReadings() {

			if (this.readingCache[this.sentenceIndex] != null) {
				return this.readingCache[this.sentenceIndex];
			}

			return new ArrayList<Reading>();

		}


		/* (non-Javadoc)
		 * @see org.itadaki.client.furigana.SentenceProvider#getText()
		 */
		@Override
		public String getText() {

			return this.sentences[this.sentenceIndex];

		}


		/* (non-Javadoc)
		 * @see org.itadaki.client.furigana.SentenceProvider#hasNext()
		 */
		@Override
		public boolean hasNext() {

			if (this.sentenceIndex < (this.sentences.length - 1)) {
				return true;
			}

			return false;

		}


		/* (non-Javadoc)
		 * @see org.itadaki.client.furigana.SentenceProvider#hasPrevious()
		 */
		@Override
		public boolean hasPrevious() {

			if (this.sentenceIndex > 0) {
				return true;
			}

			return false;

		}


		/* (non-Javadoc)
		 * @see org.itadaki.client.furigana.SentenceProvider#next()
		 */
		@Override
		public void next() {

			this.sentenceIndex++;
			
		}


		/* (non-Javadoc)
		 * @see org.itadaki.client.furigana.SentenceProvider#previous()
		 */
		@Override
		public void previous() {

			this.sentenceIndex--;
			
		}


		/* (non-Javadoc)
		 * @see org.itadaki.client.furigana.SentenceProvider#setReadings(java.util.List)
		 */
		@Override
		public void setReadings (List<Reading> readings) {

			this.readingCache[this.sentenceIndex] = readings;

			for (Reading reading : readings) {
				System.out.println ("Reading set: " + reading);
			}
			System.out.println();
			
		}


		/* (non-Javadoc)
		 * @see org.itadaki.client.furigana.SentenceProvider#setSentenceListener(org.itadaki.client.furigana.SentenceListener)
		 */
		@Override
		public void setSentenceListener (SentenceListener listener) {
			// Do nothing
		}


		/* (non-Javadoc)
		 * @see org.itadaki.client.furigana.SentenceProvider#dispose()
		 */
		@Override
		public void dispose() {
			// Do nothing
		}


		/**
		 * @param text The text to analyse
		 */
		@SuppressWarnings("unchecked")
		public DemoSentenceProvider (String text) {

			this.sentences = text.split("\n");
			this.sentenceIndex = 0;
			this.readingCache = new List[this.sentences.length];

		}

	}


	/**
	 * Set a native-look Look-And-Feel appropriate to the system
	 * (Dear Sun, printing unnecessary warnings to stderr is rude!)
	 */
	private static void setSystemLookAndFeel() {

		System.setErr (new PrintStream (new OutputStream() {
			@Override
			public void write (int b) throws IOException {
			}
		}));

		Exception e = null;

		try {
			UIManager.setLookAndFeel (UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e1) {
			// Or whatever
			e = e1;
		}

		System.setErr (new PrintStream (new BufferedOutputStream (new FileOutputStream (FileDescriptor.out)), true));

		if (e != null) {
			e.printStackTrace();
		}

	}


	/**
	 * Create the demo's user interface
	 *
	 * @param configFilename The Sen config filename
	 */
	public void createUI (final String configFilename) {

		JFrame frame = new JFrame ("Furigana Demo");


		// Input text area

		final JTextPane textPane = new JTextPane();

		textPane.setBackground (Color.WHITE);
		textPane.setText("「情報スーパーハイウェイ」の真のインパクトは、\n" +
				"情報インフラの構築により経済が従来のハードやモノづくり中心の実体経済から知識、\n" +
				"情報、ソフトを主体とした経済に移行し、そこから生まれる新しい産業や経済活動にある。");


		// Analysis button

		JButton analyseButton = new JButton ("Analyse");

		analyseButton.addActionListener (new ActionListener() {

			@Override
			public void actionPerformed (ActionEvent event) {

				try {
					FuriganaService.getInstance().processInteractively (new DemoSentenceProvider (textPane.getText()));
				} catch (MissingDictionaryException e) {
					e.printStackTrace();
				}

			}
			
		});


		// Assemble frame

		frame.add (textPane, BorderLayout.CENTER);
		frame.add (analyseButton, BorderLayout.SOUTH);

		frame.setSize (400, 300);
		frame.setDefaultCloseOperation (JFrame.EXIT_ON_CLOSE);

		frame.setVisible (true);

	}


	/**
	 * @param configFilename The Sen config filename
	 */
	public FuriganaDemo (final String configFilename) {

		SwingUtilities.invokeLater (new Runnable() {
			@Override
			public void run() {

				setSystemLookAndFeel();

				FuriganaService.getInstance().setConfiguration (configFilename);

				createUI (configFilename);

			}
		});

	}


	/**
	 * Main method
	 *
	 * @param args Ignored
	 */
	public static void main(final String[] args) {

		if (args.length != 1) {
			System.out.println ("Syntax: java FuriganaDemo <config file>");
		}
		else {
			new FuriganaDemo (args[0]);
		}
	}

}
