package com.g0kla.track.model;

@SuppressWarnings("serial")
public class PositionCalcException extends Exception {

	public double errorCode = -999;
	
	public PositionCalcException(String s) {
		super(s);
	}
	
	public PositionCalcException(double exceptionCode) {
		super("Error code: " + exceptionCode);
		errorCode = exceptionCode;
	}
}
