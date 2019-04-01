package com.g0kla.track;

import java.awt.EventQueue;
import java.awt.Frame;
import java.awt.Toolkit;
import java.io.File;

import javax.swing.JOptionPane;
import javax.swing.UIManager;

import com.g0kla.track.gui.MainWindow;

/**
 * 
 * @author g0kla@arrl.net
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
public class TrackMain {

	static String HELP = "Usage: TrackMain <dataDirectory>";
	public static String VERSION_NUM = "0.03";
	public static String VERSION = VERSION_NUM + " - 30 Mar 2019";
	
	public static void main(String[] args) {
		int arg = 0;
		String dataDir = null;
		
		while (arg < args.length) {
			if (args[arg].startsWith("-")) { // this is a switch
				if ((args[arg].equalsIgnoreCase("-h")) || (args[arg].equalsIgnoreCase("-help")) || (args[arg].equalsIgnoreCase("--help"))) {
					System.out.println(HELP);
					System.exit(0);
				}
				if ((args[arg].equalsIgnoreCase("-v")) || (args[arg].equalsIgnoreCase("-version"))) {
					System.out.println("KLATrack. Version " + VERSION);
					System.exit(0);
				}
			} else {
				// we have no more switches, so start reading command line paramaters
				dataDir = args[arg];
			}
			arg++;

		}
		if (dataDir == null) {
			// Grab the users home dir
			File home = new File(System.getProperty("user.home") + File.separator + "KLATrack");
			if (!home.exists()) {
				MainWindow.infoDialog("INITIAL SETUP", "Data will be stored in:\n" + home.getAbsolutePath());
				home.mkdirs();
			}
			dataDir = home.getAbsolutePath();
		} else {
			File data = new File(dataDir);
			if (!data.exists()) {
				MainWindow.infoDialog("INITIAL SETUP", "Data will be stored in:\n" + data.getAbsolutePath());
				data.mkdirs();
			}
		}

		File dir = new File(dataDir);
		invokeGUI(dir);
	}
	
	/**
	 * Start the GUI
	 */
	public static void invokeGUI(File dir) {
		
		// Need to set the apple menu property in the main thread.  This is ignored on other platforms
		System.setProperty("apple.laf.useScreenMenuBar", "true");
		//System.setProperty("apple.awt.fileDialogForDirectories", "true");  // fix problems with the file chooser
		Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler());
	    System.setProperty("sun.awt.exception.handler",
	                       ExceptionHandler.class.getName());
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try { //to set the look and feel to be the same as the platform it is run on
					UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
				} catch (Exception e) {
					// Silently fail if we can't do this and go with default look and feel
					//e.printStackTrace();
				}

				try {
					MainWindow window = new MainWindow(dir.getAbsolutePath());
					Thread klaTrack = new Thread(window);
					klaTrack.start();
					try {
						window.setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/kla_track_icon.jpg")));
					} catch (Exception e) { }; // ignore, means we have no icon
				} catch (Exception e) {
					System.err.println("SERIOUS ERROR - Uncaught and thrown from GUI");
					String stacktrace = makeShortTrace(e.getStackTrace());  
					String seriousErrorMsg = "Something is preventing the KLA Track from running: " + e.getMessage() + "\n"
							+ "If you recently changed something\n"
							+ "try reverting to an older version, or install the standard files.  \n"
							+ "If that does not work then you can try deleting the properties\n"
							+ "file in your home directory though this will delete your settings\n"  + stacktrace;
					e.printStackTrace();
					EventQueue.invokeLater(new Runnable() {
				        @Override
				        public void run(){
				        	Frame frm = new Frame();
				        	JOptionPane.showMessageDialog(frm,
				        			seriousErrorMsg,
									"SERIOUS ERROR - Uncaught and thrown from GUI",
									JOptionPane.ERROR_MESSAGE) ;
				        	System.exit(99);
							
				        }
					});
				
					
				}
			}
		});		
	}

	/**
	 * Inner class to handle exceptions in the Event Dispatch Thread (EDT)
	 * @author chris.e.thompson
	 *
	 */
	public static class ExceptionHandler
	implements Thread.UncaughtExceptionHandler {

		public void handle(Throwable thrown) {
			// for EDT exceptions
			handleException(Thread.currentThread().getName(), thrown);
		}

		public void uncaughtException(Thread thread, Throwable thrown) {
			// for other uncaught exceptions
			handleException(thread.getName(), thrown);
		}

		protected void handleException(String tname, Throwable thrown) {
			String stacktrace = makeShortTrace(thrown.getStackTrace());  
			MainWindow.errorDialog("SERIOUS EDT ERROR", "Exception on " + tname + ":" + thrown +"\n" + stacktrace);
		}
	}
	
	public static String makeShortTrace(StackTraceElement[] elements) {
		String stacktrace = "";  
        int limit = 13;
        for (int i=0; i< limit && i< elements.length; i++) {
        	stacktrace =  stacktrace + elements[i] + "\n";
        }
        if (elements.length > limit)
        	stacktrace = stacktrace + " ... " + (elements.length - limit) + " items not shown .... ";
        return stacktrace;
	}

}
