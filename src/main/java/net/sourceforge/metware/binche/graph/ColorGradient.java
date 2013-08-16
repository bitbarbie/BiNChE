/*
 * Copyright (c) 2012, Stephan Beisken. All rights reserved.
 *
 * This file is part of BiNChe.
 *
 * BiNChe is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * BiNChe is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with BiNChe. If not, see <http://www.gnu.org/licenses/>.
 */
package net.sourceforge.metware.binche.graph;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Utility class to return a shade of green based on the linear function calculated from the input values.
 */
public class ColorGradient {

    private final double m;
    private final double b;
    //private static final double MAX_ALPHA = 150; // a little transparent -> we want this for the lower p-values
    private static final double MIN_ALPHA = 255; // no transparency
    private Double pValueThreshold;
    /**
     * For us the fools: lowering ALPHA_TOPUP_FOR_B gives a wider range of transparency of red. Also, p-values close
     * to the threshold became more transparent.
     */
    private int ALPHA_TOPUP_FOR_B = 25;

    /**
     * Constructs the gradient calculator and retrieves the linear function.
     * 
     * TODO : the constructor only requires the minimum value, we could avoid passing them all.
     *
     * @param values set of values containing the minimum and maximum possible value
     */
    public ColorGradient(Collection<Double> values, double pValueThres) {

        
        
        List<Double> valuesSorted = new ArrayList<Double>(values);
        Collections.sort(valuesSorted);

        pValueThreshold = pValueThres;

        double minInValues = 0;
        for (Double val : valuesSorted) {
            if(val!=0) {
                minInValues = val;
                break;
            }
        }
        
        double max = Math.log10(pValueThres);  
        double min = Math.min(Math.log10(minInValues), max - 1);
        //System.out.println("VS : "+minInValues);
        //System.out.println("CG Max : "+max);
        //System.out.println("Cg Min : "+min);

        /*
        m = (MAX_ALPHA - MIN_ALPHA) / (max - min);
        b = -1 * m * max + ALPHA_TOPUP_FORB;
        * 
        */
        m = (MIN_ALPHA - ALPHA_TOPUP_FOR_B)/min;
        b = ALPHA_TOPUP_FOR_B;
        //System.out.println("m : "+m);
        //System.out.println("b : "+b);
    }

    /**
     * Gets the shade of green with alpha based on value
     *
     * @param value the input value
     * @return the shade of green
     */
    public Color getGradientColor(double value) {

        if (value <= pValueThreshold) {
            
            int alpha = (int)(m * Math.log10(value) + b);
            alpha = Math.min((int)MIN_ALPHA, alpha);
            return new Color(255, 69, 0, alpha);
        } else {
            // use alpha corresponding to p-value 0.5 for p-values above the threshold (non significant)
            // return new Color(255,255,255,(int)(m * Math.log10(0.09) + b));
            return new Color(255,255,255,255);
        }
    }
}
