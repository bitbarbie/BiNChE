/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sourceforge.metware.binche.graph;

import edu.uci.ics.jung.visualization.VisualizationImageServer;
import java.awt.Color;
import java.awt.Dimension;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import org.apache.log4j.Logger;

/**
 *
 * @author sarah
 */
public class DotWriter {
    
    private static final Logger LOGGER = Logger.getLogger(DotWriter.class);

    /**
     * 
     * @param graph
     * @param color either an object of class ColorGradient or null to get a discrete color scheme
     * @param filenameOut
     * @param correctID 
     */
    public void writeEvaluationDot(ChebiGraph graph, ColorGradient colorGrad, String filenameOut, LinkedList<String> correctID){    

        try {
            BufferedWriter out = new BufferedWriter(new FileWriter(new File(filenameOut)));
      
            String indent = "  ";
            String connector = " -> ";
            DecimalFormat form = new DecimalFormat("0.###E0");

            
            out.write("digraph G {\n");
            // all nodes with label
            if(colorGrad != null) {
                for(ChebiVertex v : graph.getVertices()) {
                    String ID = v.getChebiId();
                    String box ="";
                    if(correctID.contains(ID)){
                        box=" shape=box ";
                    }
                    Double pval = v.getpValue();
                    Double saturation = 0.0;
                    Color col = colorGrad.getGradientColor(pval);
                    if(!(col.equals(new Color(255,255,255,255))))
                    {
                        saturation= col.getAlpha()/255.0; 
                    }
                    
                    out.write(indent + ID + " [ label=\""+ v.getChebiName() +"\\npvalue="+ ((pval==0.0) ? 0 : (pval==1.0) ? 1.0 : form.format(pval)) +"\""  
                                + box 
                                + " style=filled fillcolor=" 
                                // invert saturation value alpha and scale in [0,1]
                                + "\".0 "+ saturation +" 1.0\""
                                +" ]\n");
                }
            }
            else {
                for(ChebiVertex v : graph.getVertices()) {
                    String ID = v.getChebiId();
                    String box ="";
                    if(correctID.contains(ID)){
                        box=" shape=box ";
                    }
                    Double pval = v.getpValue();
                    out.write(indent + ID + " [ label=\""+v.getChebiName()+"\\npvalue="+ ((pval==0.0) ? 0 : (pval==1.0) ? 1.0 : form.format(pval))+"\""  
                                + box 
                                + " style=filled fillcolor=" 
                                + getFillcolor(pval)
                                +" ]\n");
                }
            }
            // alle edges
           for(ChebiEdge e : graph.getEdges()) {
               String[] splits =  e.getId().split("-");
               out.write(indent + splits[1] + connector + splits[0] +" [ ]\n");
           }
           out.write("}");
            out.close();
        }
        catch(IOException ex) {
            System.out.println("file not found");
        }
    
    }
    /*
     * @params pval - p Value
     * @return fillcolor value (format either "0.0 0.1 1.0" or lightgrey)
     * both fitting to fillcolor = * in dot format
     */
    private String getFillcolor(Double pval) {
        
        if(pval<0) {
            return "blue";
        }
        if(pval == Double.POSITIVE_INFINITY || pval == Double.NEGATIVE_INFINITY) {
            return "lightgrey";
        }
        if(pval == Double.NaN) {
            return "lightgrey";
        }
        
        if(pval<0.005){ return "\".0 1.0 1.0\""; }
        if(pval<0.010){ return "\".0 0.9 1.0\""; }
        if(pval<0.015){ return "\".0 0.8 1.0\""; }
        if(pval<0.020){ return "\".0 0.7 1.0\""; }
        if(pval<0.025){ return "\".0 0.6 1.0\""; }
        if(pval<0.030){ return "\".0 0.5 1.0\""; }
        if(pval<0.035){ return "\".0 0.4 1.0\""; }
        if(pval<0.040){ return "\".0 0.3 1.0\""; }
        if(pval<0.045){ return "\".0 0.2 1.0\""; }
        if(pval<0.050){ return "\".0 0.1 1.0\""; }
        
        return "\".0 0.0 1.0\"";
    }
}