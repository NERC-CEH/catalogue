package uk.ac.ceh.gateway.catalogue.model;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;

/**
 * This class defines the a colour ramp which generates arbitary colours from
 * the range.
 */
public class ColourRamp {
    private final List<Range> steps;
    private int total;

    public ColourRamp(Color base) {
        this.steps = new ArrayList<>();
        this.steps.add(new Range(0, base));
    }

    /**
     * Obtains a colour from the colour range where max represents the maximum
     * value of pos which will be requested
     * @param pos value from 0 to max
     * @param max value which pos will ever be
     * @return a colour object represented as the requested value in the range
     */
    public Color getColour(int pos, int max) {
        double scaledI = (pos*total)/(double)max;

        //Locate the two colours to get the range between
        for(int i=0; i<steps.size(); i++) {
            Range me = steps.get(i);
            Range next = steps.get(i+1);
            scaledI -= me.steps;

            if(scaledI <= next.steps) {
                return new Color(
                        getMidValue(scaledI, next.steps, me.stop.getRed(),   next.stop.getRed()),
                        getMidValue(scaledI, next.steps, me.stop.getGreen(), next.stop.getGreen()),
                        getMidValue(scaledI, next.steps, me.stop.getBlue(),  next.stop.getBlue())
                );
            }
        }
        return null;
    }

    /**
     * Adds a step in this colour range which occurs after the last set step
     * by the given weight
     * @param weight distance from the last colour step to this one
     * @param colour to add in the range
     */
    public void addStep(int weight, Color colour) {
        steps.add(new Range(weight, colour));
        total += weight;
    }

    @Data
    private static class Range {
        private final int steps;
        private final Color stop;
    }

    private static int getMidValue(double position, int amount, int start, int end) {
        return (int)(start - ((start-end) * position)/amount);
    }
}
