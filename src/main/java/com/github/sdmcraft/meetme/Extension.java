package com.github.sdmcraft.meetme;

public class Extension {
	
	private final String context;
	private final String number;
	private final String callerId;
	
	public Extension(String context, String number, String callerId) {
		super();
		this.context = context;
		this.number = number;
		this.callerId = callerId;
	}
	/**
	 * @return the context
	 */
	public String getContext() {
		return context;
	}
	/**
	 * @return the number
	 */
	public String getNumber() {
		return number;
	}
	public String getCallerId() {
		return callerId;
	}

}
