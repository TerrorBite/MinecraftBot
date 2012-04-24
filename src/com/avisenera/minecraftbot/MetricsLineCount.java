package com.avisenera.minecraftbot;

/**
 * Metrics plotter that reports the amount of lines relayed.
 */
public class MetricsLineCount extends Metrics.Plotter {
    public MetricsLineCount() {
        super("Lines Relayed");
        this.count = 0;
    }
    private int count;
    
    @Override
    public int getValue() {
        return this.count;
    }
    
    @Override
    public void reset() {
        this.count = 0;
    }
    
    public void increment() {
        this.count++;
    }
}