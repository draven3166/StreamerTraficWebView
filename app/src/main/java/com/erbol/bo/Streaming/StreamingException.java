package com.erbol.bo.Streaming;

/**
 * Exception for streaming error events
 * (c) Commando Coder Ltd. 2016
 * @author Oscar Lopez Espinoza
 */
public class StreamingException extends Exception {

	public StreamingException(String string) {
		super(string);
	}

	public StreamingException(Throwable t) {
		super(t);
	}

}
