package com.avisenera.minecraftbot;

/**
 * Metrics plotter that reports the amount of lines relayed.
 */
public class MetricsLineCount extends Metrics.Plotter {
    private static Object lock = new Object();
    
    public MetricsLineCount() {
        super("Lines Relayed");
        this.count = 0;
    }
    private int count;
    
    @Override
    public int getValue() {
        synchronized (lock) {
            return this.count;            
        }
        
    }
    
    @Override
    public void reset() {
        synchronized(lock) {
            this.count = 0;
        }
    }
    
    public void increment() {
        synchronized(lock) {
            this.count++;
        }
    }
}