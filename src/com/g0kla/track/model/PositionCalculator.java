package com.g0kla.track.model;

import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import com.g0kla.track.gui.SatPositionTimePlot;

import uk.me.g4dpz.satellite.GroundStationPosition;
import uk.me.g4dpz.satellite.SatPos;
import uk.me.g4dpz.satellite.Satellite;
import uk.me.g4dpz.satellite.SatelliteFactory;
import uk.me.g4dpz.satellite.TLE;

/**
 * The position calculator calculates the positions for all of the sats in the keps list
 * If the keps list is updated then this is destroyed and recreated.  The positions are
 * stored in SatPositions, which can be passed to the GUI for display.
 * Hence this is the Calculator, the GUI is the view and we have data abstracted in a data model
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
	
	/**
	 * Initial start up.  For every position in the buffer calculate the sat positions.
	 * After this we just need to calculate the latest position and move the nowPointer.
	 * 
	 */
	public void fillTheBuffer() {
		DateTime timeNow = new DateTime(DateTimeZone.UTC);
		satPositions.setNow(timeNow);
		// Offsets are all in seconds, with s as the offset
		
		// Calcualate from the start of the oldPeriod to now
		int oldTimeSlices = oldPeriod*60/calcFreq;
//		System.out.println("Old Slices: " + oldTimeSlices);
		for (int s=0; s<oldTimeSlices; s++) {
			int secsOffset = oldPeriod*60-s*calcFreq;
			DateTime newTime = timeNow.minusSeconds(secsOffset);
//			System.out.print("Calc OLD slice: " + s + " at time " + newTime);
			SatPos[] satPos = new SatPos[tleList.size()];
			for(int i=0; i < tleList.size(); i++) {
				try {
					satPos[i] = calcualteCurrentPosition(newTime, tleList.get(i), groundStation);
//					System.out.println("SAT: " + i + " EL: " + SatPositionTimePlot.radToDeg(satPos[i].getElevation()));
				} catch (PositionCalcException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			satPositions.add(s, satPos);
		}

		// Calculate from now to end of the forecastPeriod
		int newTimeSlices = forecastPeriod*60/calcFreq;
//		System.out.println("New Slices: " + newTimeSlices);
		for (int s=0; s<newTimeSlices; s++) {
			int secsOffset = s*calcFreq;
			DateTime newTime = timeNow.plusSeconds(secsOffset);
//			System.out.print("Calc NEW slice: " + (s+oldTimeSlices) + " at time " + newTime);
			SatPos[] satPos = new SatPos[tleList.size()];
			for(int i=0; i < tleList.size(); i++) {
				try {
					satPos[i] = calcualteCurrentPosition(newTime, tleList.get(i), groundStation);
//					System.out.println("SAT: " + i + " EL: " + SatPositionTimePlot.radToDeg(satPos[i].getElevation()));
				} catch (PositionCalcException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			satPositions.add(s+oldTimeSlices, satPos);
		}
	}
	
	
}
