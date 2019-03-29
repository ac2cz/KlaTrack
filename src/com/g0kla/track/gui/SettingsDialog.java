package com.g0kla.track.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.DefaultListSelectionModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListModel;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import com.g0kla.track.SatManager;
import com.g0kla.track.TrackMain;

public class SettingsDialog extends JDialog implements ActionListener, WindowListener, FocusListener, ItemListener {
	private static final long serialVersionUID = 1L;

	public static final String NONE = "NONE";
	
	public static final String SETTINGS_WINDOW_X = "settings_window_x";
	public static final String SETTINGS_WINDOW_Y = "settings_window_y";
	public static final String SETTINGS_WINDOW_WIDTH = "settings_window_width";
	public static final String SETTINGS_WINDOW_HEIGHT = "settings_window_height";
	
	public static final String WEB_SITE_URL = "web_site_url";
	public static final String MAIDENHEAD_LOC = "maidenhead_locator";
	public static final String LATITUDE = "latitude";
	public static final String LONGITUDE = "longitude";
	public static final String ALTITUDE = "altitude";
	public static final String SHOW_SUN = "show_sun";
	public static final String USE_UTC = "use_utc";
	public static final String RELATIVE_TIME = "relative_time";
	public static final String PLOT_AZ = "plot_az";
	public static final String SHOW_EL = "show_el";
	public static final String SHOW_VERT_AXIS = "show_vert_axis";
	public static final String OUTLINE_PLOT = "outline_plot";
	public static final String DARK_THEME = "dark_theme";

	public static final String DEFAULT_LATITUDE = "0";//"45.4920";
	public static final String DEFAULT_LONGITUDE = "0"; //"-73.5042";
	public static final String DEFAULT_ALTITUDE = "0";
	public static final String DEFAULT_LOCATOR = "NONE";

	JButton btnSave, btnCancel, btnBrowse, btnClear, btnSelectAll;
	JTextField txtServerUrl, txtLatitude, txtLongitude, txtMaidenhead, txtAltitude, txtFont;
	JCheckBox cbShowSun, cbUseUtc, cbRelativeTime, cbPlotAz, cbSolidPlot, cbShowEl, cbShowVertAxis, cbDarkTheme;
	JList list;
		
	/**
	 * Create the Dialog
	 */
	public SettingsDialog(JFrame owner, boolean modal) {
		super(owner, modal);
		setTitle("Settings");
		addWindowListener(this);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		loadProperties();
		
		JPanel contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);

		// South panel for the buttons
		JPanel southpanel = new JPanel();
		contentPane.add(southpanel, BorderLayout.SOUTH);
		southpanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));

		btnSave = new JButton("Save");
		btnSave.addActionListener(this);
		southpanel.add(btnSave);
		getRootPane().setDefaultButton(btnSave);

		btnCancel = new JButton("Cancel");
		btnCancel.addActionListener(this);
		southpanel.add(btnCancel);

		// North panel for settings that span across the window
		JPanel northpanel = new JPanel();
		contentPane.add(northpanel, BorderLayout.NORTH);
		northpanel.setLayout(new BoxLayout(northpanel, BoxLayout.Y_AXIS));
		
		TitledBorder northTitle = title("Server and Directories");
		northpanel.setBorder(northTitle);
		
		JPanel northpanelB = new JPanel();
		northpanel.add(northpanelB);
		northpanelB.setLayout(new BorderLayout());

		JLabel lblServerUrl = new JLabel("Server Data URL  ");
		lblServerUrl.setToolTipText("This sets the URL we use to fetch and download server data");
		lblServerUrl.setBorder(new EmptyBorder(5, 2, 5, 5) );
		northpanelB.add(lblServerUrl, BorderLayout.WEST);
		
		String url = MainWindow.config.get(WEB_SITE_URL);
		if (url == null)
			url = SatManager.DEFAULT_WEB_SITE_URL;
		txtServerUrl = new JTextField(url);
		northpanelB.add(txtServerUrl, BorderLayout.CENTER);
		txtServerUrl.setColumns(30);
		
		txtServerUrl.addActionListener(this);

		// Center panel for 2 columns of settings
		JPanel centerpanel = new JPanel();
		contentPane.add(centerpanel, BorderLayout.CENTER);
		//centerpanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
		centerpanel.setLayout(new BoxLayout(centerpanel, BoxLayout.X_AXIS));

		// Add left column
		JPanel leftcolumnpanel = new JPanel();
		centerpanel.add(leftcolumnpanel);
		leftcolumnpanel.setLayout(new BoxLayout(leftcolumnpanel, BoxLayout.Y_AXIS));

		JPanel leftcolumnpanel1 = new JPanel();
		leftcolumnpanel.add(leftcolumnpanel1);

		leftcolumnpanel1.setLayout(new BoxLayout(leftcolumnpanel1, BoxLayout.Y_AXIS));
		TitledBorder leftTitle = title("Ground station");
		leftcolumnpanel1.setBorder(leftTitle);
		
		String lat = MainWindow.config.get(LATITUDE);
		if (lat == null) lat = DEFAULT_LATITUDE;
		String lon = MainWindow.config.get(LONGITUDE);
		if (lon == null) lon = DEFAULT_LONGITUDE;
		txtLatitude = addSettingsRow(leftcolumnpanel1, 10, "Lat (S is -ve)", 
				"Latitude / Longitude or Locator need to be specified if you supply decoded data to AMSAT", 
				lat); // South is negative
		txtLongitude = addSettingsRow(leftcolumnpanel1, 10, 
				"Long (W is -ve)", "Latitude / Longitude or Locator need to be specified if you supply decoded data to AMSAT", 
				lon); // West is negative
		JPanel locatorPanel = new JPanel();
		JLabel lblLoc = new JLabel("Lat Long gives Locator: ");
		String loc = MainWindow.config.get(MAIDENHEAD_LOC);
		if (loc == null) loc = DEFAULT_LOCATOR;
		txtMaidenhead = new JTextField(loc);
		txtMaidenhead.addActionListener(this);
		txtMaidenhead.addFocusListener(this);

		txtMaidenhead.setColumns(10);
		leftcolumnpanel1.add(locatorPanel);
		locatorPanel.add(lblLoc);
		locatorPanel.add(txtMaidenhead);

		String alt = MainWindow.config.get(ALTITUDE);
		if (alt == null) alt = DEFAULT_ALTITUDE;
		txtAltitude = addSettingsRow(leftcolumnpanel1, 15, 
				"Altitude (m)", "Altitude will be supplied to AMSAT along with your data if you specify it", 
				alt);

		leftcolumnpanel1.add(new Box.Filler(new Dimension(10,10), new Dimension(50,400), new Dimension(100,500)));

		JPanel leftcolumnpanel2 = new JPanel();
		leftcolumnpanel.add(leftcolumnpanel2);

		leftcolumnpanel2.setLayout(new BoxLayout(leftcolumnpanel2, BoxLayout.Y_AXIS));
		TitledBorder optTitle = title("Options");
		leftcolumnpanel2.setBorder(optTitle);
		
		cbShowSun = addCheckBoxRow(leftcolumnpanel2, "Show when Eclipsed", "Color spacecraft timeline according to sun exposure",
				MainWindow.config.getBoolean(SHOW_SUN) );
		cbRelativeTime = addCheckBoxRow(leftcolumnpanel2, "Show time relative to now", "Show time as a delta from current time",
				MainWindow.config.getBoolean(RELATIVE_TIME) );
		cbUseUtc = addCheckBoxRow(leftcolumnpanel2, "Use UTC", "Use UTC time, vs local time",
				MainWindow.config.getBoolean(USE_UTC) );
		cbPlotAz = addCheckBoxRow(leftcolumnpanel2, "Plot azimuth", "Plot Azimuth instead of Elevation",
				MainWindow.config.getBoolean(PLOT_AZ) );
		cbSolidPlot = addCheckBoxRow(leftcolumnpanel2, "Outline elevation humps", "Plot the elevation without a solid background",
					MainWindow.config.getBoolean(OUTLINE_PLOT) );
		cbShowEl = addCheckBoxRow(leftcolumnpanel2, "Print max elevation with name", "Print the maximum eleveation for that pass under the sat name",
				MainWindow.config.getBoolean(SHOW_EL) );
		cbShowVertAxis = addCheckBoxRow(leftcolumnpanel2, "Show vertical axis labels", "Display the vertical axis on left side of display",
				MainWindow.config.getBoolean(SHOW_VERT_AXIS) );
		cbDarkTheme = addCheckBoxRow(leftcolumnpanel2, "Dark Theme", "Color the display with a dark background",
				MainWindow.config.getBoolean(DARK_THEME) );
		txtFont = addSettingsRow(leftcolumnpanel2, 10, "Font Size", 
				"Change the Font size to make things easier to read", 
				MainWindow.config.get(SatPositionTimePlot.GRAPH_AXIS_FONT_SIZE)); // South is negative

		////////  show vertical scale
		///////  print elevation or not
		/////  show the spacecraft name or not
		
		leftcolumnpanel2.add(new Box.Filler(new Dimension(10,10), new Dimension(50,400), new Dimension(100,500)));

		leftcolumnpanel.add(new Box.Filler(new Dimension(10,10), new Dimension(50,400), new Dimension(100,500)));
		JLabel version = new JLabel("G0KLA Tracker Version " + TrackMain.VERSION);
		leftcolumnpanel.add(version);
		
		// Add right column
		JPanel rightcolumnpanel = new JPanel();
		centerpanel.add(rightcolumnpanel);
		rightcolumnpanel.setLayout(new BoxLayout(rightcolumnpanel, BoxLayout.Y_AXIS));
		TitledBorder rightTitle = title("Spacecraft to plot");
		rightcolumnpanel.setBorder(rightTitle);
		
		JPanel rightcolumnbutpanel = new JPanel();
		rightcolumnbutpanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		rightcolumnpanel.add(rightcolumnbutpanel);

		btnSelectAll = new JButton("Select all");
		rightcolumnbutpanel.add(btnSelectAll);
		btnClear = new JButton("Clear");
		rightcolumnbutpanel.add(btnClear);
		btnSelectAll.addActionListener(this);
		btnClear.addActionListener(this);	
		
		DefaultListModel listModel = new DefaultListModel();
		List<String> names = ((MainWindow)getParent()).satManager.getSatNames();
		for (String name : names)
			listModel.addElement(name);

		list = new JList(listModel);
		list.setSelectionModel(new DefaultListSelectionModel() 
		{
		    @Override
		    public void setSelectionInterval(int index0, int index1) 
		    {
		        if(list.isSelectedIndex(index0)) 
		        {
		            list.removeSelectionInterval(index0, index1);
		        }
		        else 
		        {
		            list.addSelectionInterval(index0, index1);
		        }
		    }
		});
		//list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION );
		list.setLayoutOrientation(JList.HORIZONTAL_WRAP);
		list.setVisibleRowCount(-1);
		setSelectedValues(list, ((MainWindow)getParent()).satManager.getSelectedSats());
		
		JScrollPane listScroller = new JScrollPane(list);
		listScroller.setPreferredSize(new Dimension(600, 900));
		//listScroller.setAlignmentX(LEFT_ALIGNMENT);
		
		rightcolumnpanel.add(listScroller);

		
		rightcolumnpanel.add(new Box.Filler(new Dimension(10,10), new Dimension(100,10), new Dimension(100,500)));
	}
	
	public void setSelectedValues(JList list, List<String> values) {
	    list.clearSelection();
	    for (String value : values) {
	        //list.setSelectedValue(value, false);
	        int index = getIndex(list.getModel(), value);
	        if (index >=0) {
	            list.addSelectionInterval(index, index);
	        }
	    }
	}
	
	public int getIndex(ListModel model, Object value) {
	    if (value == null) return -1;
	    if (model instanceof DefaultListModel) {
	        return ((DefaultListModel) model).indexOf(value);
	    }
	    for (int i = 0; i < model.getSize(); i++) {
	        if (value.equals(model.getElementAt(i))) return i;
	    }
	    return -1;
	}

	private JCheckBox addCheckBoxRow(JPanel parent, String name, String tip, boolean value) {
		JCheckBox checkBox = new JCheckBox(name);
		checkBox.setEnabled(true);
		checkBox.addItemListener(this);
		checkBox.setToolTipText(tip);
		parent.add(checkBox);
		if (value) checkBox.setSelected(true); else checkBox.setSelected(false);
		return checkBox;
	}
	private JTextField addSettingsRow(JPanel column, int length, String name, String tip, String value) {
		JPanel panel = new JPanel();
		column.add(panel);
		panel.setLayout(new GridLayout(1,2,5,5));
		JLabel lblDisplayModuleFont = new JLabel(name);
		lblDisplayModuleFont.setToolTipText(tip);
		panel.add(lblDisplayModuleFont);
		JTextField textField = new JTextField(value);
		panel.add(textField);
		textField.setColumns(length);
		textField.addActionListener(this);
		textField.addFocusListener(this);

		column.add(new Box.Filler(new Dimension(10,5), new Dimension(10,5), new Dimension(10,5)));

		return textField;
	
	}

	private TitledBorder title(String s) {
		Border empty = new EmptyBorder(15, 10, 15, 10); // top left bottom right

		TitledBorder title = new TitledBorder(empty, s);
		title.setTitleFont(new Font("SansSerif", Font.BOLD, 16));
		//title.setTitleJustification(TitledBorder.LEADING);
		title.setTitlePosition(TitledBorder.TOP);
		return title;
	}
	
	private boolean validLatLong() {
		float lat = 0, lon = 0;
		try {
			lat = Float.parseFloat(txtLatitude.getText());
			lon = Float.parseFloat(txtLongitude.getText());
			if (lat == Float.parseFloat(DEFAULT_LATITUDE) || 
					txtLatitude.getText().equals("")) return false;
			if (lon == Float.parseFloat(DEFAULT_LONGITUDE) || 
					txtLongitude.getText().equals("")) return false;
		} catch (NumberFormatException n) {
			JOptionPane.showMessageDialog(this,
					"Only numerical values are valid for the latitude and longitude.",
					"Format Error\n",
					JOptionPane.ERROR_MESSAGE);
			return false;
		}
		if ((Float.isNaN(lon)) ||
		(Math.abs(lon) > 180) ||
		(Float.isNaN(lat)) ||
		(Math.abs(lat) == 90.0) ||
		(Math.abs(lat) > 90)) {
			JOptionPane.showMessageDialog(this,
					"Invalid latitude or longitude.",
					"Error\n",
					JOptionPane.ERROR_MESSAGE);
			return false;
		}

		return true;
	}
	
	private boolean validLocator() {
		if (txtMaidenhead.getText().equalsIgnoreCase(DEFAULT_LOCATOR) || 
				txtMaidenhead.getText().equals("")) {
			JOptionPane.showMessageDialog(this,
					"Enter a latitude/longitude or set the locator to a valid value",
					"Format Error\n",
					JOptionPane.ERROR_MESSAGE);
			return false;
		}
		return true;
	}
	private boolean validAltitude() {
		int alt = 0;
			try {
				alt = Integer.parseInt(txtAltitude.getText());
			} catch (NumberFormatException n) {
				JOptionPane.showMessageDialog(this,
						"Only integer values are valid for the altitude. Specify it to the nearest meter, but with no units.",
						"Format Error\n",
						JOptionPane.ERROR_MESSAGE);
				return false;
			}
		if ((alt < 0) ||(alt > 8484)) {
			JOptionPane.showMessageDialog(this,
					"Invalid altitude.  Must be between 0 and 8484m.",
					"Format Error\n",
					JOptionPane.ERROR_MESSAGE);
			return false;
		}
		return true;
	}
		
	private void updateLocator() throws Exception {
		if (validLatLong()) {
			Location l = new Location(txtLatitude.getText(), txtLongitude.getText());
			txtMaidenhead.setText(l.maidenhead);
		}
	}
	
	private void updateLatLong() throws Exception {
		if (validLocator()) {
			Location l = new Location(txtMaidenhead.getText());
			txtLatitude.setText(Float.toString(l.latitude)); 
			txtLongitude.setText(Float.toString(l.longitude));
		}
	}

	private void enableDependentParams() {
		if (validLatLong() && validAltitude()) {
						
		} else {
			
		}
	}

	public void loadProperties() {
		if (MainWindow.config.getInt(SETTINGS_WINDOW_X) == 0) {
			MainWindow.config.set(SETTINGS_WINDOW_X, 100);
			MainWindow.config.set(SETTINGS_WINDOW_Y, 100);
			MainWindow.config.set(SETTINGS_WINDOW_WIDTH, 1000);
			MainWindow.config.set(SETTINGS_WINDOW_HEIGHT, 650);
		}
		setBounds(MainWindow.config.getInt(SETTINGS_WINDOW_X), MainWindow.config.getInt(SETTINGS_WINDOW_Y), 
				MainWindow.config.getInt(SETTINGS_WINDOW_WIDTH), MainWindow.config.getInt(SETTINGS_WINDOW_HEIGHT));
	}
	
	public void saveProperties() {
		MainWindow.config.set(SETTINGS_WINDOW_HEIGHT, this.getHeight());
		MainWindow.config.set(SETTINGS_WINDOW_WIDTH, this.getWidth());
		MainWindow.config.set(SETTINGS_WINDOW_X, this.getX());
		MainWindow.config.set(SETTINGS_WINDOW_Y, this.getY());
		
		MainWindow.config.save();
	}
	
//	private File pickFile(String title, String buttonText, int type) {
//		File file = null;
//		File dir = kepsFileDir;
//		if (kepsFileDir == null)
//			dir = new File(".");
//		
//			FileDialog fd = new FileDialog(this, title, type);
//			// use the native file dialog on the mac
//			if (dir != null) {
//				fd.setDirectory(dir.getAbsolutePath());
//			}
//			fd.setVisible(true);
//			String filename = fd.getFile();
//			String dirname = fd.getDirectory();
//			if (filename == null)
//				;//Log.println("You cancelled the choice");
//			else {
//				System.out.println("File: " + filename);
//				System.out.println("DIR: " + dirname);
//				file = new File(dirname + filename);
//				kepsFileDir = new File(dirname);
//				config.set(KEPS_FILE_DIR, kepsFileDir.getAbsolutePath());
//			}
//		return file;
//	}
	
	@Override
	public void windowActivated(WindowEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowClosed(WindowEvent arg0) {
		saveProperties();
		
	}

	@Override
	public void windowClosing(WindowEvent arg0) {
		saveProperties();
		
	}

	@Override
	public void windowDeactivated(WindowEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowDeiconified(WindowEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowIconified(WindowEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowOpened(WindowEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == btnCancel) {
			this.dispose();
		}
		if (e.getSource() == btnSelectAll) {
			ListModel model = list.getModel();
			list.setSelectionInterval(0, ((DefaultListModel) model).getSize()-1);
		}
		if (e.getSource() == btnClear) {
			list.clearSelection();
		}
		if (e.getSource() == txtLatitude || e.getSource() == txtLongitude ) {
			try {
				updateLocator();
			} catch (Exception e1) {
				MainWindow.errorDialog("ERROR", e1.getMessage());
			}
			enableDependentParams();
		}
		if (e.getSource() == txtMaidenhead) {
			try {
				updateLatLong();
			} catch (Exception e1) {
				MainWindow.errorDialog("ERROR", e1.getMessage());
			}
			enableDependentParams();
			
		}
		if (e.getSource() == txtAltitude) {
			validAltitude();
			enableDependentParams();
		}
		
		if (e.getSource() == btnSave) {
			boolean dispose = true;
			MainWindow mainWindow = ((MainWindow)getParent());
			List<String> sats = list.getSelectedValuesList();
			if (sats.isEmpty()) {
				MainWindow.errorDialog("Error", "You must pick at least one spacecraft to plot");
				return;
			}
			mainWindow.satManager.setSelectedSats(sats);
			
			if (validLatLong()) {
				mainWindow.config.set(LATITUDE,txtLatitude.getText());
				mainWindow.config.set(LONGITUDE, txtLongitude.getText());
			} else {
				if (txtLatitude.getText().equalsIgnoreCase(DEFAULT_LATITUDE) && txtLongitude.getText().equalsIgnoreCase(DEFAULT_LONGITUDE))
					dispose = true;
				else 
					dispose = false;
			}
			if (validLocator()) {
				mainWindow.config.set(MAIDENHEAD_LOC,txtMaidenhead.getText());
			} else dispose = false;
			if (validAltitude()) {
				mainWindow.config.set(ALTITUDE,txtAltitude.getText());
			} else dispose = false;
			
			if (mainWindow.config.get(WEB_SITE_URL) == null || !mainWindow.config.get(WEB_SITE_URL).equalsIgnoreCase(txtServerUrl.getText())) {
				mainWindow.config.set(WEB_SITE_URL, txtServerUrl.getText());
				mainWindow.satManager = new SatManager(mainWindow);
			}
			if (mainWindow.config.get(SatPositionTimePlot.GRAPH_AXIS_FONT_SIZE) == null || 
					!mainWindow.config.get(SatPositionTimePlot.GRAPH_AXIS_FONT_SIZE).equalsIgnoreCase(txtFont.getText())) {
				mainWindow.config.set(SatPositionTimePlot.GRAPH_AXIS_FONT_SIZE, txtFont.getText());
				MainWindow.infoDialog("New Font Size", "Restart to see the effect of the new font");
			}			
			mainWindow.config.set(SHOW_SUN, cbShowSun.isSelected());
			mainWindow.config.set(USE_UTC, cbUseUtc.isSelected());
			mainWindow.config.set(RELATIVE_TIME, cbRelativeTime.isSelected());
			mainWindow.config.set(PLOT_AZ, cbPlotAz.isSelected());
			mainWindow.config.set(OUTLINE_PLOT, cbSolidPlot.isSelected());
			mainWindow.config.set(SHOW_EL, cbShowEl.isSelected());
			mainWindow.config.set(SHOW_VERT_AXIS, cbShowVertAxis.isSelected());
			if (mainWindow.config.getBoolean(DARK_THEME) != cbDarkTheme.isSelected()) {
				mainWindow.config.set(DARK_THEME, cbDarkTheme.isSelected());
				MainWindow.infoDialog("New Theme", "Restart to see the effect of the new color scheme");
			}			

			
			if (dispose) {
				((MainWindow)getParent()).startPositionCalc();
				MainWindow.config.save();
				this.dispose();
			}
		}
	}

	@Override
	public void focusGained(FocusEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void focusLost(FocusEvent e) {
		if (e.getSource() == txtLatitude || e.getSource() == txtLongitude ) {
			try {
				updateLocator();
			} catch (Exception e1) {
				MainWindow.errorDialog("ERROR", e1.getMessage());
			}
			enableDependentParams();
		}
		if (e.getSource() == txtMaidenhead) {
			try {
				updateLatLong();
			} catch (Exception e1) {
				MainWindow.errorDialog("ERROR", e1.getMessage());
			}
			enableDependentParams();
		}
		if (e.getSource() == txtAltitude) {
			validAltitude();
			enableDependentParams();
		}
		
	}

	@Override
	public void itemStateChanged(ItemEvent arg0) {
		// TODO Auto-generated method stub
		
	}

}
