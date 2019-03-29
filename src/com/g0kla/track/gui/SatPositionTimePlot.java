package com.g0kla.track.gui;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.time.LocalDateTime;

import javax.swing.JLabel;
import javax.swing.JPanel;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.g0kla.track.model.SatPositions;

import uk.me.g4dpz.satellite.SatPos;

/**
 * Uses the solarized color scheme which is Copyright (c) 2011 Ethan Schoonover and subject to the following:

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated 
documentation files (the "Software"), to deal in the Software without restriction, including without limitation 
the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and 
to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED 
TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL 
THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF 
CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER 
DEALINGS IN THE SOFTWARE.

 * @author chris
 *
 */
public class SatPositionTimePlot extends JPanel {

	private static final long serialVersionUID = 1L;
	JLabel title;
	public static final Color sunyellow = new Color(0xcccc00); //yellow

	// Solarized light
	public static final Color base01 = new Color(0x586e75); //base01 - grey
	public static final Color base0 = new Color(0x839496); //base01 - light grey
	public static final Color base03 = new Color(0x002b36); //base03 - dark gray
	public static final Color base2 = new Color(0xeee8d5); // slightly darker light background
	public static final Color base3 = new Color(0xfdf6e3); // light background
	public static final Color yellow = new Color(0xb58900); //yellow
	public static final Color orange = new Color(0xcb4b16); // orange

	Color background;
	Color backgroundDepth;
	
	public static final Color[] satColors = {
			yellow, //yellow
			orange, // orange
			new Color(0xd33682), // magenta
			new Color(0x6c71c4), // violet
			new Color(0x268bd2), // blue
			new Color(0x2aa198), // cyan
			new Color(0xdc322f), // red
			new Color(0x859900), // green
	};
	
	int sideborder = 5;
	int bottomborder = 12;
	int topborder = 10;
	public static final String GRAPH_AXIS_FONT_SIZE = "graph_axis_font_size";
	int fontSize;
	SatPositions satPositions;
	
	public SatPositionTimePlot(SatPositions satPositions) {
		this.satPositions = satPositions;
		fontSize = MainWindow.config.getInt(GRAPH_AXIS_FONT_SIZE);
		if (fontSize == 0) fontSize = 12;

		if (MainWindow.config.getBoolean(SettingsDialog.DARK_THEME)) {
			background = base03;
			backgroundDepth = base01;
		} else {
			background = base3;
			backgroundDepth = base2;
		}
		setBackground(background);
	}
	
	public void setPositions(SatPositions satPositions) {
		this.satPositions = satPositions;
		repaint();
	}

	/**
	 * Paint on a drawing canvas with x running from 0 to getWidth() horizontally
	 * and y running from 0 to getHeight vertically
	 */
	public void paintComponent(Graphics g) {
		super.paintComponent( g ); // call superclass's paintComponent  
		try {
		Graphics2D g2 = ( Graphics2D ) g; // cast g to Graphics2D  
		g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_GASP);	

		g.setFont(new Font("SansSerif", Font.PLAIN, fontSize));

		setBackground(background);
//		g.drawString("Spacecraft Timeline", 5, fontSize*2);

		
		// Have 5 pix border
		int graphHeight = getHeight() - topborder - bottomborder;
		int graphWidth = getWidth() - sideborder*2;

		// axis color
		g.setColor(base01);
		plotTimeAxis(g, graphHeight, graphWidth);
		
		int lineWidth = 1+(int) (graphWidth/(double)satPositions.getNumberOfSamples());
		if (lineWidth < 1) lineWidth = 1;
		int lineheight = 5+(int) (graphHeight/(double)180);
		if (lineheight < 1) lineheight = 1;

		// For each sat splot the positions, default is elevation
		boolean plotElevation = true;
		double[] maxy = new double[satPositions.getNumberOfSats()];
		boolean[] wroteName = new boolean[satPositions.getNumberOfSats()];
		int lasty[] = new int[satPositions.getNumberOfSats()];
		for (int s=0; s<satPositions.getNumberOfSamples(); s++) {
			SatPos[] satPos = satPositions.get(s);
			
			for (int i=0; i < satPos.length; i++) {
				double el = radToDeg(satPos[i].getElevation());
				double az = radToDeg(satPos[i].getAzimuth());
				if (el > 0) {
					int y = 0;
					if (!MainWindow.config.getBoolean(SettingsDialog.PLOT_AZ)) {
						y = getRatioPosition(0,90, el, graphHeight-topborder-bottomborder);
					} else {
						y = getRatioPosition(0,360, az, graphHeight-topborder-bottomborder);
					}
					int x = getRatioPosition(0.0, (double)satPositions.getNumberOfSamples(), (double)s, graphWidth);
					//					g2.setColor(satColors[i%satColors.length]);
					if (MainWindow.config.getBoolean(SettingsDialog.SHOW_SUN)) {
						if (satPos[i].isEclipsed())
							g2.setColor(base03);
						else
							g2.setColor(sunyellow);
					} else
						g2.setColor(satColors[i%satColors.length]);
					if (!MainWindow.config.getBoolean(SettingsDialog.PLOT_AZ)) {
						if (MainWindow.config.getBoolean(SettingsDialog.OUTLINE_PLOT)) {
							if (lasty[i] == 0) lasty[i] = graphHeight-topborder-y;
							g2.drawLine(x, lasty[i], x, graphHeight-topborder-y);
							lasty[i] = graphHeight-topborder-y;
						} else {
							g2.fillRect(x, graphHeight-topborder-y, lineWidth, y);							
						}
					} else // plot azimuth
						g2.fillRect(x, y-lineheight, lineWidth, lineheight);
					//g2.drawLine(x, y, x, graphHeight-border);
					if (!wroteName[i])
						if (el > maxy[i])
							maxy[i] = el;
						else {
							// We peaked, write the sat name and elevation
							int max = (int) Math.round(maxy[i]);
							int horOffset = satPositions.getSatName(i).length()*2*fontSize/8;
							if (!MainWindow.config.getBoolean(SettingsDialog.PLOT_AZ)) {
								// Plotting for elevation, 
								
								int offset = 0;
								if (maxy[i] > 80)
									offset = fontSize;
								if (MainWindow.config.getBoolean(SettingsDialog.SHOW_EL)) {
									g.drawString("   "+max, x-fontSize*4/3, graphHeight-topborder-y-fontSize+offset);
								} else 
									offset = offset + fontSize;
								g.drawString(satPositions.getSatName(i), x-horOffset, graphHeight-topborder-y-2*fontSize+offset);
							} else {
								g.drawString(satPositions.getSatName(i), x-horOffset, y-2*fontSize);
								g.drawString("   "+max, x-fontSize*4/3, y-fontSize);
							}
							wroteName[i] = true;
						}
				} else {
					// not in a pass. Reset the pass check so we write the name next time
					maxy[i] = 0;
					wroteName[i] = false;
				}
			}
		}
		
		if (MainWindow.config.getBoolean(SettingsDialog.SHOW_VERT_AXIS)) {
			g2.setColor(base01);
			int labelHeight = fontSize*4;
			int numVertLabels = (graphHeight-topborder-bottomborder) / labelHeight;
			if (MainWindow.config.getBoolean(SettingsDialog.PLOT_AZ)) {
				//int step = 360/numVertLabels;
				int step = (int)getStep(360, numVertLabels, true);
				for (int e=0; e<=360; e+=step) {
					int y = getRatioPosition(0,360, e, graphHeight-topborder-bottomborder);
					g.drawString(""+e, sideborder, y+topborder-lineheight);
				}
			} else {
				//int step = 90/numVertLabels;
				int step = (int)getStep(90, numVertLabels, true);
				for (int e=0; e<=90; e+=step) {
					int y = getRatioPosition(0,90, e, graphHeight-topborder-bottomborder);
					g.drawString(""+e, sideborder, graphHeight-y-topborder);
				}
			}
		}
		
		// Vertical "now" line
		g2.setColor(base01);
		int now = getRatioPosition(0.0, (double)satPositions.getNumberOfSamples(), (double)satPositions.nowPointer, graphWidth);
		g2.drawLine(now, topborder, now, graphHeight-bottomborder);
		} catch (Exception e) {
			MainWindow.errorDialog("Oops", "Crashed the rendering loop.  Guess you need to restart\n" + e);
		}

	}
	
	private static double getStep(double range, int ticks, boolean intStep) {
		double step = 0;
		
		if (!intStep && range/ticks <= 0.01) step = 0.01d;
		else if (range/ticks <= 1) step = 1.00d;
		else if (range/ticks <= 5) step = 5.00d;
		else if (range/ticks <= 10) step = 10.00d;
		else if (range/ticks <= 15) step = 15.00d;
		else if (range/ticks <= 30) step = 30.00d;
		else if (range/ticks <= 45) step = 45.00d;
		else if (range/ticks <= 90) step = 90.00d;
		else if (range/ticks <= 180) step = 180.00d;
		return step;
	}
	
	private void plotTimeAxis(Graphics g, int graphHeight, int graphWidth) {
		// Draw baseline with enough space for text under it
		g.drawLine(0, graphHeight-bottomborder, graphWidth+sideborder*2, graphHeight-bottomborder);

		DateTime now = satPositions.getNow();
		
		DateTimeFormatter timefmt = DateTimeFormat.forPattern("HH:mm:ss");
		DateTimeFormatter datefmt = DateTimeFormat.forPattern("d MMM YY");
		int calcFreq = satPositions.getCalcFreq();
		int labelWidth = fontSize * 6;
		int numberOfLabels = (int) (graphWidth / (double)(labelWidth));
		int delta= (int) ((satPositions.getNumberOfSamples()*calcFreq)/(double)numberOfLabels); // time in seconds per label
		
		for (int i=0; i<numberOfLabels; i++) {
			
			double timeSlice = (double)i*(delta/calcFreq);
			//if (timeSlice > satPositions.nowPointer)
			//	timeSlice = (double)satPositions.nowPointer+i*(delta/calcFreq);
			int timepos = getRatioPosition(0.0, (double)satPositions.getNumberOfSamples(), timeSlice, graphWidth);
			DateTime then = now.plusSeconds(delta*i-satPositions.getPastPeriod()*60);

			if (!MainWindow.config.getBoolean(SettingsDialog.USE_UTC)) {
				then = then.withZone(DateTimeZone.getDefault());
			}
			
			int offset = 0;
			String time = timefmt.print(then);
			String dt = datefmt.print(then);
			if (MainWindow.config.getBoolean(SettingsDialog.RELATIVE_TIME)) {
				time = "" + (int)Math.round(delta*(i)/60.0-satPositions.getPastPeriod());
				dt = "";
				offset = 15;
			} 
			g.drawLine(timepos, graphHeight-bottomborder+2, timepos, graphHeight-bottomborder-2);
			g.drawString(time, timepos-labelWidth/3+offset, graphHeight+3);
			g.drawString(dt, timepos-labelWidth/3+offset, graphHeight+3+fontSize);
		}

	}
	
	public static int getRatioPosition(double min, double max, double value, int dimension) {
		if (max == min) return 0;
		double ratio = (max - value) / (max - min);
		int position = (int)Math.round(dimension * ratio);
		return dimension-position;
	}
	
	public static double radToDeg(Double rad) {
		return 180 * (rad / Math.PI);
	}
	public static double latRadToDeg(Double rad) {
		return radToDeg(rad);
	}

	public static double lonRadToDeg(Double rad) {
		double lon = radToDeg(rad);
		if (lon > 180)
			return lon -360;
		else
			return lon;
	}
}
