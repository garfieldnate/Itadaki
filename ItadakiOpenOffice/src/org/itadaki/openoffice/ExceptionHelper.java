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

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.text.DateFormat;
import java.util.Date;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;


/**
 * Helper class to deal with exceptions from from within Itadaki. Throwing
 * exceptions out to OpenOffice is generally a bad thing, so we intercept
 * them and display an error.
 */
public class ExceptionHelper {

	/**
	 * Dialog used to display caught exceptions
	 */
	private static JDialog exceptionDialog = null;

	/**
	 * Text area to contain the exception text
	 */
	private static JTextArea stackTraceTextArea = null;


	/**
	 * Creates a dialog to display the exception
	 */
	private synchronized static void createDialog() {

		exceptionDialog = new JDialog ((Frame)null, "Oops!");

		JLabel northLabel = new JLabel ("Something's gone a bit wrong. Debugging details follow:");

		stackTraceTextArea = new JTextArea();
		stackTraceTextArea.setFont (new Font ("Monospaced", Font.PLAIN, 12));
		stackTraceTextArea.setEditable (false);
		JScrollPane stackTraceScrollPane = new JScrollPane (stackTraceTextArea);

		JButton continueButton = new JButton ("Continue");
		continueButton.addActionListener (new ActionListener() {

			/* (non-Javadoc)
			 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
			 */
			public void actionPerformed (ActionEvent e) {

				exceptionDialog.setVisible (false);
				
			}

		});


		exceptionDialog.add (northLabel, BorderLayout.NORTH);
		exceptionDialog.add (stackTraceScrollPane, BorderLayout.CENTER);
		exceptionDialog.add (continueButton, BorderLayout.SOUTH);

		exceptionDialog.setSize (500,400);

	}

	/**
	 * Does something appropriate with the supplied Throwable
	 *
	 * @param t The Throwable to deal with
	 */
	public synchronized static void dealWith (final Throwable t) {

		// First, print the stack trace

		t.printStackTrace();


		// Second, write the exception to a log file

		String osName = System.getProperty ("os.name");
		String userHome = System.getProperty ("user.home");
		String propertiesDirectory;

		if (osName.startsWith ("Windows")) {
			String applicationDataName = System.getenv ("APPDATA");
			if (applicationDataName == null) {
				applicationDataName = "Application Data";
			}
			propertiesDirectory = userHome + File.separator + applicationDataName + File.separator + "Itadaki";
		} else {
			propertiesDirectory = userHome + File.separator + ".itadaki";
		}

		File propertiesDirectoryFile = new File (propertiesDirectory);
		if (!propertiesDirectoryFile.exists()) {
			propertiesDirectoryFile.mkdirs();
		}

		String logFilename = propertiesDirectory  + File.separator + "itadaki.log";
		FileWriter fileWriter = null;
		try {
			fileWriter = new FileWriter (new File (logFilename), true);
			PrintWriter printWriter = new PrintWriter (fileWriter);
			printWriter.println ("Exception caught at " + DateFormat.getDateTimeInstance().format (new Date()));
			t.printStackTrace (printWriter);
			printWriter.println();
			printWriter.println();
		} catch (IOException e) {
			// Not much to do about it really
		} finally {
			try {
				if (fileWriter != null) {
					fileWriter.close();
				}
			} catch (IOException e) {
				// Ignore
			}
		}
		

		// Finally, attempt to show an explanatory dialog

		SwingUtilities.invokeLater (new Runnable() {

			/* (non-Javadoc)
			 * @see java.lang.Runnable#run()
			 */
			public void run() {

				if (exceptionDialog == null) {
					createDialog();
				}

				if (!exceptionDialog.isVisible()) {
					stackTraceTextArea.setText("");
				}

				Writer stringWriter = new StringWriter();
				t.printStackTrace (new PrintWriter (stringWriter));
				stackTraceTextArea.append (stringWriter.toString());
				stackTraceTextArea.append ("\n\n");

				exceptionDialog.setVisible (true);

			}
			
		});

	}

}
