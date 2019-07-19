package com.g0kla.track.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FileDialog;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerListModel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.g0kla.track.SatManager;
import com.g0kla.track.model.PositionCalcException;
import com.g0kla.track.model.PositionCalculator;
import com.g0kla.track.model.SatPositions;

import uk.me.g4dpz.satellite.GroundStationPosition;
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
@SuppressWarnings("serial")
public class MainWindow extends JFrame implements Runnable, WindowListener, ActionListener, ChangeListener {
	public static final String WINDOW_FC_WIDTH = "mainwindow_fc_width";
	public static final String WINDOW_FC_HEIGHT = "mainwindow_fc_height";
	public static final String WINDOW_CURRENT_DIR = "mainwindow_current_dir";
	public static final String USE_NATIVE_FILE_CHOOSER = "use_native_file_chooser";

	boolean startingUp = true;  // prevents events being processed in startup
	public SatManager satManager;
	PositionCalculator positionCalc;
	int fontSize;
	
	// satPositions;
	JButton butSettings, butEclipse, butOffsetMins, butUTC, butOutlinePlot, but30_60, butPlotAz;
	JSpinner spinTimeSlice, spinForecastPeriod, spinPastPeriod;
	SatPositionTimePlot satPositionTimePlot;
	
	public static JFrame frame; // a static frame to hang error dialogs from
	
	public static ConfigFile config;
	String kepsFile = null;
	File kepsFileDir = null;
	
	int pastPeriod = 60;
	int forecastPeriod = 3*60;
	int calcFreq = 10; 
	
	// Config
	//public static final String KEPS_FILE_DIR = "keps_file_dir";
	public static final String DATA_DIR = "data_dir";
	public static final String PAST = "past";
	public static final String FORECAST = "forecast";
	public static final String TIME_SLICE = "time_slice";
	public static final String MAINWINDOW_X = "mainwindow_x";
	public static final String MAINWINDOW_Y = "mainwindow_y";
	public static final String MAINWINDOW_WIDTH = "mainwindow_width";
	public static final String MAINWINDOW_HEIGHT = "mainwindow_height";
	
	Color background;
	Color backgroundDepth;
	Color fontColor;
	
	public MainWindow(String dataDir) {
		super("G0KLA Tracker");
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		addWindowListener(this);
		config = new ConfigFile(this, dataDir + File.separator + "klatracker.properties");
		config.set(DATA_DIR, dataDir);
		if (MainWindow.config.getBoolean(SettingsDialog.DARK_THEME)) {
			background = SatPositionTimePlot.base03;
			backgroundDepth = SatPositionTimePlot.base01;
		} else {
			background = SatPositionTimePlot.base3;
			backgroundDepth = SatPositionTimePlot.base2;
		}
		setBackground(background);
		fontSize = MainWindow.config.getInt(SatPositionTimePlot.GRAPH_AXIS_FONT_SIZE);
		if (fontSize == 0) {
			fontSize = 12;
			MainWindow.config.set(SatPositionTimePlot.GRAPH_AXIS_FONT_SIZE, fontSize);
		}
		
		if (config.getInt(MAINWINDOW_X) == 0) {
			config.set(MAINWINDOW_X, 100);
			config.set(MAINWINDOW_Y, 100);
			config.set(MAINWINDOW_WIDTH, 900);
			config.set(MAINWINDOW_HEIGHT, 250);
		}
		setBounds(config.getInt(MAINWINDOW_X), config.getInt(MAINWINDOW_Y), 
				config.getInt(MAINWINDOW_WIDTH), config.getInt(MAINWINDOW_HEIGHT));
				
		JPanel bottomPanel = new JPanel();
		bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.X_AXIS));
		getContentPane().add(bottomPanel, BorderLayout.SOUTH);
		bottomPanel.add(new Box.Filler(new Dimension(10,0), new Dimension(10,0), new Dimension(10,0)));
		butSettings = createIconButton("/setup_icon2.png","Setup","Setup and preferences");
		bottomPanel.add(butSettings);

		bottomPanel.add(new Box.Filler(new Dimension(10,0), new Dimension(100,0), new Dimension(100,0)));
		JLabel bar = new JLabel("|");
		bar.setForeground(SatPositionTimePlot.base01);
		bottomPanel.add(bar);
		bottomPanel.add(new Box.Filler(new Dimension(10,0), new Dimension(50,0), new Dimension(50,0)));

		butUTC = createButton("Local","Toggle between UTC and local time");
		bottomPanel.add(butUTC);
		if (config.getBoolean((SettingsDialog.USE_UTC)))
			butUTC.setText("UTC");

		butEclipse = createIconButton("/sun_icon2.png","Eclipse","Toggle Normal vs Sun/Eclipse view");
		bottomPanel.add(butEclipse);

		butOffsetMins = createButton("Time","Toggle units between minutes and time");
		bottomPanel.add(butOffsetMins);
		if (config.getBoolean((SettingsDialog.RELATIVE_TIME)))
				butOffsetMins.setText("Mins");

		butOutlinePlot = createIconButton("/outline_icon2.png","Outline","Toggle Outline vs Solid plot");
		bottomPanel.add(butOutlinePlot);

		but30_60 = createButton("30-60","Show or hide lines for 30 and 60 degrees elevation");
		bottomPanel.add(but30_60);

		butPlotAz = createButton("EL","Toggle between plotting elevation and Azimuth");
		bottomPanel.add(butPlotAz);
		if (config.getBoolean((SettingsDialog.PLOT_AZ)))
			butPlotAz.setText("AZ");

		
		bottomPanel.add(new Box.Filler(new Dimension(10,0), new Dimension(10000,0), new Dimension(10000,0)));

		JLabel lblPast = new JLabel("From past (mins)");
		lblPast.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, fontSize));
		lblPast.setForeground(SatPositionTimePlot.base01);
		bottomPanel.add(lblPast);
		lblPast.setBorder(new EmptyBorder(2, 10, 2, 10) ); // top left bottom right
		List<Integer> pastPeriodList = new ArrayList<Integer>();
		pastPeriodList.add(0);
		pastPeriodList.add(10);
		pastPeriodList.add(20);
		pastPeriodList.add(30);
		pastPeriodList.add(60);
		pastPeriodList.add(120);
		pastPeriodList.add(180);
		pastPeriodList.add(4*60);
		pastPeriodList.add(6*60);
		pastPeriodList.add(12*60);
		pastPeriodList.add(24*60);
		SpinnerListModel pastModel = new SpinnerListModel(pastPeriodList);
		spinPastPeriod = new JSpinner(pastModel);
		spinPastPeriod.getEditor().getComponent(0).setBackground(backgroundDepth);
		((JTextField) spinPastPeriod.getEditor().getComponent(0)).setColumns(3);
		spinPastPeriod.setBorder(BorderFactory.createEmptyBorder());
//		 int n = spinPastPeriod.getComponentCount();
//		for (int i=0; i<n; i++)
//	    {
//	        Component c = spinPastPeriod.getComponent(i);
//	        if (c instanceof JButton)
//	        {
//	           // c.setForeground(foreground); // Has no effect
//	            c.setBackground(background);
//	        }
//	    }
		
		bottomPanel.add(spinPastPeriod);
		spinPastPeriod.addChangeListener(this);
		pastPeriod = config.getInt(PAST);
		spinPastPeriod.setValue(pastPeriod);
		
		JLabel lblForecast = new JLabel("to future (hrs)");
		lblForecast.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, fontSize));
		lblForecast.setForeground(SatPositionTimePlot.base01);
		bottomPanel.add(lblForecast);
		lblForecast.setBorder(new EmptyBorder(2, 10, 2, 10) ); // top left bottom right
		List<Integer> forecastPeriodList = new ArrayList<Integer>();
		forecastPeriodList.add(1);
		forecastPeriodList.add(2);
		forecastPeriodList.add(3);
		forecastPeriodList.add(4);
		forecastPeriodList.add(5);
		forecastPeriodList.add(6);
		forecastPeriodList.add(12);
		forecastPeriodList.add(24);
		forecastPeriodList.add(48);
		SpinnerListModel forecastModel = new SpinnerListModel(forecastPeriodList);
		spinForecastPeriod = new JSpinner(forecastModel);
		spinForecastPeriod.setBorder(BorderFactory.createEmptyBorder());
		spinForecastPeriod.getEditor().getComponent(0).setBackground(backgroundDepth);
		((JTextField) spinForecastPeriod.getEditor().getComponent(0)).setColumns(2);
		bottomPanel.add(spinForecastPeriod);
		spinForecastPeriod.addChangeListener(this);
		forecastPeriod = config.getInt(FORECAST);
		if (forecastPeriod == 0) forecastPeriod = 3*60;
		spinForecastPeriod.setValue(forecastPeriod/60);
		
		JLabel lblTimeSlice = new JLabel("every (secs)");
		lblTimeSlice.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, fontSize));
		lblTimeSlice.setForeground(SatPositionTimePlot.base01);
		bottomPanel.add(lblTimeSlice);
		lblTimeSlice.setBorder(new EmptyBorder(2, 10, 2, 10) ); // top left bottom right
		List<Integer> timeSliceList = new ArrayList<Integer>();
		timeSliceList.add(10);
		timeSliceList.add(30);
		timeSliceList.add(60);
		SpinnerListModel timeSliceModel = new SpinnerListModel(timeSliceList);
		spinTimeSlice = new JSpinner(timeSliceModel);
		spinTimeSlice.setBorder(BorderFactory.createEmptyBorder());
		spinTimeSlice.getEditor().getComponent(0).setBackground(backgroundDepth);
		((JTextField) spinTimeSlice.getEditor().getComponent(0)).setColumns(2);
		bottomPanel.add(spinTimeSlice);
		spinTimeSlice.addChangeListener(this);
		calcFreq = config.getInt(TIME_SLICE);
		if (calcFreq == 0) calcFreq = 10;
		spinTimeSlice.setValue(calcFreq);
		
//		JLabel lblSampleRate = new JLabel("From -" + pastPeriod/60 + " hrs To +" +forecastPeriod/60 +" hrs every: " + calcFreq + " secs");
//		lblSampleRate.setBorder(new EmptyBorder(2, 10, 2, 10) ); // top left bottom right
//		bottomPanel.add(lblSampleRate);
		bottomPanel.setBackground(background);

		satManager = new SatManager(this);
		
		try {
		startPositionCalc();
		} catch (Exception e) {
			errorDialog("ERROR", "Could not start the calculations.  Make sure the TLEs were downloaded.  They are stored in:\n"
					+ MainWindow.config.get(MainWindow.DATA_DIR) + "\n"
					+ "You can also manually copy a keps file into the directory or select a different keps file:\n"
					 + "\nRestart G0KLATracker once a new keps file is defined.\n");
			return;
		}
		satPositionTimePlot = new SatPositionTimePlot(positionCalc.getSatPositions());
		
		JPanel panelCenter = new JPanel();
		panelCenter.setLayout(new BoxLayout(panelCenter, BoxLayout.Y_AXIS));
		panelCenter.add(satPositionTimePlot);
		getContentPane().add(panelCenter, BorderLayout.CENTER);
	
		startingUp = false;
	}

	public JButton createButton(String name, String toolTip) {
		JButton btn;
		
		btn = new JButton(name);	
		btn.setForeground(SatPositionTimePlot.base01);
		btn.setMargin(new Insets(0,10,0,10)); //top left bottom right
		btn.setToolTipText(toolTip);
		btn.setOpaque(false);
		btn.setContentAreaFilled(false);
		btn.setBorderPainted(false);
		btn.addActionListener(this);
		return btn;
	}

	
	public JButton createIconButton(String icon, String name, String toolTip) {
		JButton btn;
		BufferedImage wPic = null;
		try {
			wPic = ImageIO.read(this.getClass().getResource(icon));
		} catch (Exception e) { // Should not be fatal
			//e.printStackTrace();
		}
		if (wPic != null) {
			btn = new JButton(new ImageIcon(wPic));
			btn.setMargin(new Insets(0,10,0,10));
		} else {
			btn = new JButton(name);	
			btn.setForeground(SatPositionTimePlot.base01);
		}
		btn.setToolTipText(toolTip);
		btn.setOpaque(false);
		btn.setContentAreaFilled(false);
		btn.setBorderPainted(false);
		btn.addActionListener(this);
		return btn;
	}

	public void startPositionCalc() {
		// Now start the position cal thread based on the user defined values
		List<TLE> tleList = satManager.getTleList();
		String lat = MainWindow.config.get(SettingsDialog.LATITUDE);
		if (lat == null) lat = SettingsDialog.DEFAULT_LATITUDE;
		String lon = MainWindow.config.get(SettingsDialog.LONGITUDE);
		if (lon == null) lon = SettingsDialog.DEFAULT_LONGITUDE;

		String alt = MainWindow.config.get(SettingsDialog.ALTITUDE);
		if (alt == null) alt = SettingsDialog.DEFAULT_ALTITUDE;

		float flat = Float.parseFloat(lat);
		float flon =  Float.parseFloat(lon);
		float falt = Integer.parseInt(alt);
		GroundStationPosition groundStation = new GroundStationPosition(flat, flon, falt);	
		SatPositions satPositions = null;
		try {
			positionCalc = new PositionCalculator(tleList, groundStation, pastPeriod, forecastPeriod, calcFreq);
			satPositions = positionCalc.getSatPositions();
		} catch (PositionCalcException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (satPositionTimePlot != null)
			satPositionTimePlot.setPositions(positionCalc.getSatPositions());
	}
	
	public void saveProperties() {
		config.set(MAINWINDOW_HEIGHT, this.getHeight());
		config.set(MAINWINDOW_WIDTH, this.getWidth());
		config.set(MAINWINDOW_X, this.getX());
		config.set(MAINWINDOW_Y, this.getY());
		
		config.save();
	}
	
	private void shutdownWindow() {
		saveProperties();
		this.dispose();
		System.exit(0);
	}

	@Override
	public void windowActivated(WindowEvent e) {
		// TODO Auto-generated method stub
	}

	@Override
	public void windowClosed(WindowEvent e) {
		shutdownWindow();
	}

	@Override
	public void windowClosing(WindowEvent e) {
		shutdownWindow();
	}

	@Override
	public void windowDeactivated(WindowEvent e) {
		// TODO Auto-generated method stub
	}

	@Override
	public void windowDeiconified(WindowEvent e) {
		// TODO Auto-generated method stub
	}

	@Override
	public void windowIconified(WindowEvent e) {
		// TODO Auto-generated method stub
	}

	@Override
	public void windowOpened(WindowEvent e) {
		// TODO Auto-generated method stub
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == butSettings) {
			SettingsDialog f = new SettingsDialog(this, true);
			f.setVisible(true);
		}
		if (e.getSource() == butEclipse) {
			config.set(SettingsDialog.SHOW_SUN, !config.getBoolean(SettingsDialog.SHOW_SUN)); 
			MainWindow.config.save();
			startPositionCalc();
		}
		if (e.getSource() == butOffsetMins) {
			config.set(SettingsDialog.RELATIVE_TIME, !config.getBoolean(SettingsDialog.RELATIVE_TIME)); 
			MainWindow.config.save();
			if (config.getBoolean((SettingsDialog.RELATIVE_TIME)))
				butOffsetMins.setText("Mins");
			else
				butOffsetMins.setText("Time");				
			startPositionCalc();
		}
		if (e.getSource() == butUTC) {
			config.set(SettingsDialog.USE_UTC, !config.getBoolean(SettingsDialog.USE_UTC)); 
			MainWindow.config.save();
			if (config.getBoolean((SettingsDialog.USE_UTC)))
				butUTC.setText("UTC");
			else
				butUTC.setText("Local");				
			startPositionCalc();
		}
		if (e.getSource() == butOutlinePlot) {
			config.set(SettingsDialog.OUTLINE_PLOT, !config.getBoolean(SettingsDialog.OUTLINE_PLOT)); 
			MainWindow.config.save();
			startPositionCalc();
		}
		if (e.getSource() == but30_60) {
			config.set(SettingsDialog.SHOW_30_60, !config.getBoolean(SettingsDialog.SHOW_30_60)); 
			MainWindow.config.save();
			startPositionCalc();
		}
		if (e.getSource() == butPlotAz) {
			config.set(SettingsDialog.PLOT_AZ, !config.getBoolean(SettingsDialog.PLOT_AZ)); 
			MainWindow.config.save();
			if (config.getBoolean((SettingsDialog.PLOT_AZ)))
				butPlotAz.setText("AZ");
			else
				butPlotAz.setText("EL");				
			startPositionCalc();
		}
		
	}
	
	DecimalFormat f2 = new DecimalFormat("0");
	DecimalFormat d3 = new DecimalFormat("0.000");
	
	@Override
	public void run() {
		Thread.currentThread().setName("MainWindow");
		setVisible(true);

		Date lastSlice = new Date();
		// Runs until we exit
		while(true) {
			// Sleep first to avoid race conditions at start up
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			Date now = new Date();
			if (now.getTime() - lastSlice.getTime() >= calcFreq*1000) {
				lastSlice = now;
				EventQueue.invokeLater(new Runnable() {
					public void run() {
						if (satPositionTimePlot!=null && positionCalc != null) {
							positionCalc.advanceTimeslice();
							satPositionTimePlot.setPositions(positionCalc.getSatPositions());
						}

					}
				});
			}
		}
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		if (!startingUp) {
			if (e.getSource() == spinForecastPeriod) {
				forecastPeriod = (int) spinForecastPeriod.getValue() * 60;
				startPositionCalc();
				config.set(FORECAST, forecastPeriod);
				config.save();
			}
			if (e.getSource() == spinPastPeriod) {
				pastPeriod = (int) spinPastPeriod.getValue();
				startPositionCalc();
				config.set(PAST, pastPeriod);
				config.save();
			}
			if (e.getSource() == spinTimeSlice) {
				calcFreq = (int) spinTimeSlice.getValue();
				startPositionCalc();
				config.set(TIME_SLICE, calcFreq);
				config.save();
			}
		}
	}
	
	public static void errorDialog(String title, String message) {
		dialog(title, message, JOptionPane.ERROR_MESSAGE );
	}
	
	public static void infoDialog(String title, String message) {
		dialog(title, message, JOptionPane.INFORMATION_MESSAGE );
	}
	
	public static void dialog(String title, String message, int type) {
		try {
		
		JOptionPane.showMessageDialog(frame,
				message.toString(),
				title,
			    type) ;
		} catch (Exception e) {
			// catch all exceptions at this point, to avoid popping up messages in a loop
			System.err.println("FATAL ERROR: Cannot show dialog: " + title + "\n" + message + "\n");
			System.exit(1);
		}
	}

}
