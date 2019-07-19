package com.g0kla.track;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.swing.JOptionPane;

import com.g0kla.track.gui.MainWindow;
import com.g0kla.track.gui.SettingsDialog;

import uk.me.g4dpz.satellite.TLE;

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
public class SatManager {
	public static final String DEFAULT_WEB_SITE_URL = "https://www.amsat.org/amsat/ftp/keps/current/nasabare.txt";
	public static final String START_WEB_URL = "http";
	public static final String SELECTED_SATS = "selected_sats";
	static MainWindow mainWindow;
	List<TLE> TLEs;
	List<String> selectedSats;
	
	public SatManager(MainWindow mainWindow) {
		this.mainWindow = mainWindow;
		loadTLEFile();
		String satCsvList = mainWindow.config.get(SELECTED_SATS);
		if (satCsvList == null) {
			List<String> sats = new ArrayList<String>();
			sats.add("FalconSat-3");
			sats.add("AO-7");
			sats.add("AO-85");
			sats.add("AO-91");
			sats.add("AO-92");
			sats.add("AO-95");
			sats.add("AO-73");
			sats.add("AO-90");
			sats.add("ISS");
			
			setSelectedSats(sats);
		} else {
			String[] names = satCsvList.split(",");
			selectedSats = new ArrayList<String>();
			for (String name : names)
				selectedSats.add(name);
		}

	}
	
	public List<String> getSatNames() {
		List<String> names = new ArrayList<String>();
		if (TLEs == null) return names;
		for (TLE tle : TLEs)
			names.add(tle.getName());
		Collections.sort(names);
		return names;
	}
	
	public List<String> getSelectedSats() { return selectedSats; }
	public void setSelectedSats(List<String> sats) { 
		selectedSats = sats; 
		String satCsvList = "";
		for (int i=0; i<sats.size()-1; i++) {
			satCsvList = satCsvList + sats.get(i) + ",";
		}
		satCsvList = satCsvList + sats.get(sats.size()-1);
		mainWindow.config.set(SELECTED_SATS, satCsvList);
	}
	
	public List<TLE> getTleList() {
		if (selectedSats == null) return TLEs;
		List<TLE> selectedTLEs = new ArrayList<TLE>();
		for (TLE tle : TLEs) {
			if (selectedSats.contains(tle.getName()))
				selectedTLEs.add(tle);
		}
		return selectedTLEs; 
	}
	
	/*
	 * Load the TLE file.  First check if we are pulling from a URL or loading a local file
	 * 
	 */
	public void loadTLEFile() {

		String msg = "Downloading new keps ...                 ";
		ProgressPanel initProgress = null;

		String urlString = MainWindow.config.get(SettingsDialog.WEB_SITE_URL);
		if (urlString == null)
			urlString = SatManager.DEFAULT_WEB_SITE_URL;
		if (urlString.startsWith(SatManager.START_WEB_URL)) {
			initProgress = new ProgressPanel(mainWindow, msg, false);
			initProgress.setVisible(true);
			String[] urlParts = urlString.split("/");
			String kepsFile = urlParts[urlParts.length-1];
			String file = MainWindow.config.get(MainWindow.DATA_DIR) + File.separator + kepsFile;
			String filetmp = fetchTLEFile(urlString, file);
			if (filetmp != null)
				parseTLEFile(file, filetmp);
			
		} else {
			// Assume this is a local file as we do not have a URL
			msg = "Copying keps from file ...                 ";
			initProgress = new ProgressPanel(mainWindow, msg, false);
			initProgress.setVisible(true);
			String file = MainWindow.config.get(SettingsDialog.WEB_SITE_URL);
			String filetmp = file;
			parseTLEFile(file, filetmp);
		}
		initProgress.updateProgress(100);

	}
	
	/*
	 * We Fetch a TLE file from amsat.org.  We then see if it contains TLEs for the Spacecraft we are interested in. If it does we
	 * check if there is a later TLE than the one we have.  If it is, then we append it to the TLE store for the given sat.
	 * We then load the TLEs for each Sat and store the, in the spacecraft class.  This can then be used to find the position of the spacecraft at 
	 * any time since launch
	 */
	public String fetchTLEFile(String urlString, String file) {
		//System.out.println("Checking for new Keps");
		
		String filetmp = file + ".tmp";
		File f1 = new File(filetmp);
		File f2 = new File(file);
		Date lm = new Date(f2.lastModified());
		Date now = new Date();
		if (now.getTime()-lm.getTime() < 24*60*60*1000) {
			System.out.println("Keps are less than 24 hrs old, not trying to download..");
			try {
				TLEs = loadTLE(file);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}

		System.out.println("Downloading new keps ..");
		URL website;
		FileOutputStream fos = null;
		ReadableByteChannel rbc = null;
		try {
			website = new URL(urlString);
			HttpURLConnection httpCon = (HttpURLConnection) website.openConnection();
			long date = httpCon.getLastModified();
			httpCon.disconnect();
			Date kepsDate = new Date(date);
			if (kepsDate.getTime() !=0 && (kepsDate.getTime() <= lm.getTime())) { // then dont try to update it
				System.out.println(".. keps are current");
				filetmp = file;
			} else {
				System.out.println(" ... open RBC ..");
				rbc = Channels.newChannel(website.openStream());
			}
			try {
				System.out.println(" ... open output file .." + filetmp);
				fos = new FileOutputStream(filetmp);
				System.out.println(" ... getting file ..");
				fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);

			} catch (IOException e) {
				MainWindow.errorDialog("ERROR","Could not open the Keps file " + file + "\n" + e);
				try { remove(file + ".tmp"); } catch (IOException e1) {e1.printStackTrace();}
				return null;
			} catch (IndexOutOfBoundsException e) {
				MainWindow.errorDialog("ERROR","Keps file is corrupt: " + file  + "\n" + e);
				try { remove(file + ".tmp"); } catch (IOException e1) {e1.printStackTrace();}
				return null;
			}
		} catch (MalformedURLException e) {
			MainWindow.errorDialog("ERROR","Invalid location for Keps file: " + urlString  + "\n" + e);
			try { remove(file + ".tmp"); } catch (IOException e1) {e1.printStackTrace();}
			return file; // try to use an existing file as we could not download it
		} catch (IOException e) {
			try { remove(file + ".tmp"); } catch (IOException e1) {e1.printStackTrace();}
			return file; // try to use the existing file as we could not download it
		} finally {
			try {
				if (fos != null) fos.close();
				if (rbc != null) rbc.close();
			} catch (IOException e) {
				// ignore
			}
		}
		return filetmp;
	}

	/**
	 * If tmp file has a differnt name we need to copy it, otherwise we just load the file as the keps are current
	 * @param file
	 * @param filetmp
	 */
	private void parseTLEFile(String file, String filetmp) {
		System.out.println(" ... parsing file ..");
		File f1 = new File(filetmp);
		File f2 = new File(file);
		try {
			TLEs = loadTLE(filetmp);
			// this is a good file so we can now use it as the default
			if (!filetmp.equals(file)) {
				System.out.println(" ... remove and rename ..");
				if (!file.equalsIgnoreCase(filetmp)) {
					// We downloaded a new file so rename tmp as the new file
					remove(file);
					copyFile(f1, f2);
				}
				try {
					Thread.sleep(20);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if (!file.equalsIgnoreCase(filetmp))
					remove(file + ".tmp");
			}
			return;
		} catch (IOException e) {
			MainWindow.errorDialog("ERROR","Could not open the Keps file: " + file + "\n" + e);
			try { if (!file.equalsIgnoreCase(filetmp)) remove(file + ".tmp"); } catch (IOException e1) {e1.printStackTrace();}
		} catch (IndexOutOfBoundsException e) {
			MainWindow.errorDialog("ERROR","Keps file is corrupt: " + file  + "\n" + e);
			try { if (!file.equalsIgnoreCase(filetmp)) remove(file + ".tmp"); } catch (IOException e1) {e1.printStackTrace();}
		} catch (NumberFormatException e) {
			MainWindow.errorDialog("ERROR","Keps file is corrupt: " + file  + "\n" + e);
			try { if (!file.equalsIgnoreCase(filetmp)) remove(file + ".tmp"); } catch (IOException e1) {e1.printStackTrace();}
		} finally {
			try { if (!file.equalsIgnoreCase(filetmp)) remove(file + ".tmp"); } catch (IOException e1) {e1.printStackTrace();}
		}

	}

	private List<TLE> loadTLE(String file) throws IOException {
		InputStream is = null;
		List<TLE> tles = null;
		try {
			is = new FileInputStream(file);
			tles = TLE.importSat(is);
		} finally {
			if (is != null) is.close();
		}
		return tles;
	}
	
	/**
	 * Remove a log file from disk and report any errors.
	 * @param f
	 * @throws IOException
	 */
	public static void remove(String f) throws IOException {
		try {
	        File file = new File(f);
	        if (file.exists())
	        	if(file.delete()){
	        		System.out.println(file.getName() + " is deleted!");
	        	}else{
	        		System.out.println("Delete operation failed for: "+ file.getName());
	        		throw new IOException("Could not delete file " + file.getName() + " Check the file system and remove it manually.");
	        	}
		} catch (Exception ex) {
			JOptionPane.showMessageDialog(mainWindow,
					ex.toString(),
					"Error Deleting File",
					JOptionPane.ERROR_MESSAGE) ;
		}
	}
	
	/**
	 * Utility function to copy a file
	 * @param sourceFile
	 * @param destFile
	 * @throws IOException
	 */
	@SuppressWarnings("resource") // because we have a finally statement and the checker does not seem to realize that
	public static void copyFile(File sourceFile, File destFile) throws IOException {
	    if(!destFile.exists()) {
	        destFile.createNewFile();
	    }

	    FileChannel source = null;
	    FileChannel destination = null;

	    try {
	        source = new FileInputStream(sourceFile).getChannel();
	        destination = new FileOutputStream(destFile).getChannel();
	        destination.transferFrom(source, 0, source.size());
	    }
	    finally {
	        if(source != null) {
	            source.close();
	        }
	        if(destination != null) {
	            destination.close();
	        }
	    }
	}

}
