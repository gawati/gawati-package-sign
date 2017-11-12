package org.gawati.crypto;

public class GwAlgorithmInfo {
	
	private String theAlgorithmName;
	private int theLength;
	
	public GwAlgorithmInfo() {
		setAlgorithmName("DSA");
		setLength(1024);
	}

	public String getAlgorithmName() {
		return theAlgorithmName;
	}

	public void setAlgorithmName(String theAlgorithmName) {
		this.theAlgorithmName = theAlgorithmName;
	}

	public int getLength() {
		return theLength;
	}

	public void setLength(int theLength) {
		this.theLength = theLength;
	}

}
