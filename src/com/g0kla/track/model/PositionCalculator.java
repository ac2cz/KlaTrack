package com.g0kla.track.model;

import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import com.g0kla.track.ProgressPanel;
import com.g0kla.track.gui.MainWindow;
import com.g0kla.track.gui.SatPositionTimePlot;

import uk.me.g4dpz.satellite.GroundStationPosition;
import uk.me.g4dpz.satellite.SatPos;
import uk.me.g4dpz.satellite.Satellite;
import uk.me.g4dpz.satellite.SatelliteFactory;
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
 
 * The position calculator calculates the positions for all of the sats in the keps list
 * If the keps list is updated then this is destroyed and recreated.  The positions are
 * stored in SatPositions, which can be passed to the GUI for display.
 * Hence this is the Calculator, the GUI is the view and we have data abstracted in SatPositions
 * 
 * @author chris
 *
 */
public class PositionCalculator {
	SatPositions satPositions; // the data structure to store the resulting positions in
	List<TLE> tleList; // the list of keps to calculate
	int calcFreq; // the freq in seconds for the calculation.  e.g. once every calcFreq seconds
	boolean running = true;
	GroundStationPosition groundStation;
	int oldPeriod; // number of mins after current time that we keep positions for
	int forecastPeriod; // number of mins into the future we calculate positions for
	int numberOfSamples; // the number of position samples to store - should equal the length of the satPositions queue
	
	public PositionCalculator(List<TLE> tleList, GroundStationPosition groundStation, 
			int oldPeriod, int forecastPeriod, int calcFreq) throws PositionCalcException {
		this.tleList = tleList;
		this.calcFreq = calcFreq;
		this.groundStation = groundStation;
		this.oldPeriod = oldPeriod;
		this.forecastPeriod = forecastPeriod;
		this.numberOfSamples = (60*oldPeriod + 60*forecastPeriod) / calcFreq;
		// Integrity checks here and throw exceptions.  e.g. negative numbers, zero samples, too large
		if (oldPeriod < 0) throw new PositionCalcException("Historical Period can't be negative");
		if (oldPeriod > 48*60) throw new PositionCalcException("Historical Period can't be larger than 48 hours");
		if (forecastPeriod < 0) throw new PositionCalcException("Forecast Period can't be negative");
		if (forecastPeriod > 48*60) throw new PositionCalcException("Forecast Period can't be greater than 48 hours");
		if (numberOfSamples < 0) throw new PositionCalcException("Number of samples can't be negative");
		if (numberOfSamples > 999999) throw new PositionCalcException("Number of samples can't be greater than 999999");
		satPositions = new SatPositions(tleList, oldPeriod, forecastPeriod, calcFreq);
		fillTheBuffer();
	}
	
	public SatPositions getSatPositions() { return satPositions; }
	
	public void stopProcessing() {
		running = false;
	}

	/**
	 * Calculate the current position and cache it
	 * @param tle 
	 * @param GROUND_STATION 
	 * @return
	 * @throws PositionCalcException
	 */
	protected SatPos calcualteCurrentPosition(DateTime timeNow, TLE tle, GroundStationPosition GROUND_STATION) throws PositionCalcException {
		SatPos pos = null;
		final Satellite satellite = SatelliteFactory.createSatellite(tle);
        final SatPos satellitePosition = satellite.getPosition(GROUND_STATION, timeNow.toDate());
		return satellitePosition;
	}
	
	boolean bruteForce = false;
	
	/**
	 * We have already filled the buffer.  This calculates the newest timeslice and purges 
	 * the oldest and increments now.  This repeats until now is equal to the actual time
	 */
	public void advanceTimeslice() {
		if (bruteForce)
			fillTheBuffer();
		else {
			DateTime timeNow = new DateTime(DateTimeZone.UTC); // this is the actual time now.  Advance the now point to equal this
			while (satPositions.getNow().isBeforeNow()) {
				satPositions.incNow();
				int newTimeSlice = forecastPeriod*60;
				DateTime newTime = satPositions.getNow().plusSeconds(newTimeSlice);
				addSlice(newTime, newTimeSlice);
			}
		}
	}
	
	/**
	 * Initial start up.  For every position in the buffer calculate the sat positions.
	 * After this we just need to calculate the latest position and move the nowPointer.
	 * 
	 */
	private void fillTheBuffer() {
		ProgressPanel initProgress = null;
		initProgress = new ProgressPanel(MainWindow.frame, "Calculating initial positions ..", false);
		initProgress.setVisible(true);
		
		DateTime timeNow = new DateTime(DateTimeZone.UTC);
		satPositions.setNow(timeNow);
		// Offsets are all in seconds, with s as the offset
		
		// Calcualate from the start of the oldPeriod to now
		int oldTimeSlices = oldPeriod*60/calcFreq;
		int newTimeSlices = forecastPeriod*60/calcFreq;
		//System.out.println("Old Slices: " + oldTimeSlices);
		for (int s=0; s<oldTimeSlices; s++) {
			int secsOffset = oldPeriod*60-s*calcFreq;
			DateTime newTime = timeNow.minusSeconds(secsOffset);
			addSlice(newTime, secsOffset);
			initProgress.updateProgress((int) (100*(s/(double)(oldTimeSlices+newTimeSlices))));
		}

		// Calculate from now to end of the forecastPeriod
		
		//System.out.println("New Slices: " + newTimeSlices);
		for (int s=0; s<newTimeSlices; s++) {
			int secsOffset = s*calcFreq;
			DateTime newTime = timeNow.plusSeconds(secsOffset);
			addSlice(newTime, secsOffset);
			initProgress.updateProgress((int)(100*(s+oldTimeSlices)/(double)(oldTimeSlices+newTimeSlices)));
		}
		initProgress.updateProgress(100);
	}
	
	private void addSlice(DateTime newTime, int secsOffset) {
		SatPos[] satPos = new SatPos[tleList.size()];
		for(int i=0; i < tleList.size(); i++) {
			try {
				satPos[i] = calcualteCurrentPosition(newTime, tleList.get(i), groundStation);
//				System.out.println("SAT: " + i + " EL: " + SatPositionTimePlot.radToDeg(satPos[i].getElevation()));
			} catch (PositionCalcException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		satPositions.add(satPos);

	}
	
}
