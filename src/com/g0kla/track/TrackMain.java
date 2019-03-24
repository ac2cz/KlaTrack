package com.g0kla.track;

import java.io.File;

import com.g0kla.track.gui.MainWindow;

public class TrackMain {

	static String HELP = "Usage: TrackMain <dataDirectory>";
	public static String VERSION_NUM = "0.01";
	public static String VERSION = VERSION_NUM + " - 24 Mar 2019";
	
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
				dataDir = home.getAbsolutePath();
			}
		} else {
			File data = new File(dataDir);
			if (!data.exists()) {
				MainWindow.infoDialog("INITIAL SETUP", "Data will be stored in:\n" + data.getAbsolutePath());
				data.mkdirs();
			}
		}

		File dir = new File(dataDir);
		MainWindow window = new MainWindow(dir.getAbsolutePath());
		Thread klaTrack = new Thread(window);
		klaTrack.start();
	}

}
