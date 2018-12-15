package com.erbol.bo.Streaming;

/**
 * Interface for actitivites listening to streaming events
 * (c) Commando Coder Ltd. 2016
 * @author @author Oscar Lopez Espinoza
 */
public interface StreamingListener {

    void buffering(int percentage);

	void streamingStarted();

    void streamingStopping();

	void streamingStopped();

	void streamingException(Exception e);

	void streamingTitle(String title);

}
