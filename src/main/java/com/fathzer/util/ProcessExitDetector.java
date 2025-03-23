package com.fathzer.util;

import java.util.function.Consumer;

/**
 * Detects when a process is finished and invokes the associated listeners.
 */
public class ProcessExitDetector implements Runnable {

    /** The process for which we have to detect the end. */
    private Process process;
    /** The associated listeners to be invoked at the end of the process. */
    private Consumer<Process> listener;
 
    /**
     * Starts the detection for the given process
     * @param process the process for which we have to detect when it is finished
     */
    public ProcessExitDetector(Process process, Consumer<Process> listener) {
    	this.process = process;
    	this.listener = listener;
    }

    @Override
    public void run() {
        try {
            // wait for the process to finish
            process.waitFor();
            // invokes the listener
            listener.accept(process);
        } catch (InterruptedException e) {
        	Thread.currentThread().interrupt();
        }
    }
    
    public void start(boolean daemon) {
    	final Thread thread = new Thread(this);
    	thread.setDaemon(daemon);
    	thread.start();
    }
}