/*
 * created based on BiNCheExec, modified to evaluate data with known result
 */
package net.sourceforge.metware.binche.execs;

import BiNGO.BingoParameters;
import BiNGO.methods.BingoAlgorithm;
import net.sourceforge.metware.binche.BiNChe;
import net.sourceforge.metware.binche.graph.ChebiGraph;
import net.sourceforge.metware.binche.graph.SvgWriter;
import net.sourceforge.metware.binche.gui.SettingsPanel;
import org.apache.commons.cli.Option;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import net.sourceforge.metware.binche.graph.ColorGradient;
import net.sourceforge.metware.binche.graph.DotWriter;
import org.apache.commons.lang.StringUtils;

/**
 *
 * @author sarah
 * 
 */
public class EvaluationMain extends CommandLineMain {
    
    private static final Logger LOGGER = Logger.getLogger(EvaluationMain.class);

    // main
    public static void main(String[] args) {

        new EvaluationMain(args).process();
    }
    
    // constructor
    public EvaluationMain(String[] args) {

        super(args);
    }
    
    @Override public void setupOptions() {

        add(new Option("i", "file to load", true, "association file to load (format: .../CHEBI:<id>.tsv)"));
        add(new Option("o", "output directory", true, "directory to write output to (format: .../<id>)"));
        add(new Option("l", "file including in- and output filenames", true, "iterate a list of files"));
        add(new Option("p", "true", false, "calculate hypergeometric distribution rather that saddle sum method"));

    }
    
    /**
     * Main processing method
     */
    @Override public void process() {

        if (!(hasOption("l") || (hasOption("i") && hasOption("o")))) {
            printHelp();
            System.exit(0);
        }

        if (hasOption("l")) {
            
            String infile = getCommandLine().getOptionValue("l");
            try {
                BufferedReader br = new BufferedReader(new FileReader(new File(infile)));
                BufferedWriter bw = new BufferedWriter(new FileWriter(new File("errors.out")));
                while(br.ready()) {
                 
                    String inputPath = br.readLine();
                    String outputPath = br.readLine();
                                    
                    System.out.print("Iterating "+inputPath+"...");
                    try{
                    runDefault(inputPath,outputPath);
                    System.out.print("succeeded\n");
                    } catch (Exception ex) {
                        bw.write("################\n");
                        bw.write("# ERROR SADDLE #\n");
                        bw.write("################\n");
                        bw.write("in "+ inputPath +"\n");
                        bw.write("***********************\n");
                        bw.write(ex.toString()+"\n");
                        bw.write("------------------\n");
                        bw.write(ex.getMessage()+"\n");
                        bw.write("***********************\n\n");
                        bw.flush();
                        System.out.print("error\n");
                        continue;
                    }
    
                    try{
                    runHyper(inputPath,outputPath);
                    System.out.print("succeeded\n");
                    } catch (Exception ex) {
                        bw.write("###############\n");
                        bw.write("# ERROR HYPER #\n");
                        bw.write("###############\n");
                        bw.write("in "+ inputPath +"\n");
                        bw.write("***********************\n");
                        bw.write(ex.toString()+"\n");
                        bw.write("------------------\n");
                        bw.write(ex.getMessage()+"\n");
                        bw.write("***********************\n\n");
                        bw.flush();
                        System.out.print("error\n");
                        continue;
                    }
         //    break;
                }
                br.close();
                bw.close();
            } catch (Exception ex) {
                java.util.logging.Logger.getLogger(BiNCheExec.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
            }
            LOGGER.log(Level.INFO, "############ Stop ############");
            
        } else {
            
            String inputPath = getCommandLine().getOptionValue("i");
            String outputPath = getCommandLine().getOptionValue("o");

            if( hasOption("p")) {
                runHyper(inputPath, outputPath);
            } else{
                runDefault(inputPath, outputPath);
            }
            LOGGER.log(Level.INFO, "############ Stop ############");   
        }
    }
   
    private void runDefault(String inputPath, String outputPath) {

        LOGGER.log(Level.INFO, "############ Start ############");

        String ontologyFile = getClass().getResource("/BiNGO/data/chebi_clean.obo").getFile();
        String elementsForEnrichFile = inputPath;
        
        BingoParameters parametersSaddle;
        if(hasOption("p")) {
            LOGGER.log(Level.INFO, "Setting hypergeometric parameters ...");
            parametersSaddle = getHyperParameters(ontologyFile);
        }
        else {
            LOGGER.log(Level.INFO, "Setting default parameters ...");
            parametersSaddle = getDefaultParameters(ontologyFile);
        }

        BiNChe binche = new BiNChe();
        binche.setParameters(parametersSaddle);

        LOGGER.log(Level.INFO, "Reading input file ...");
        try {
            binche.loadDesiredElementsForEnrichmentFromFile(elementsForEnrichFile);
        } catch (IOException exception) {
            LOGGER.log(Level.ERROR, "Error reading file: " + exception.getMessage());
            System.exit(1);
        }

        binche.execute();

        // get ids
        String[] splits = inputPath.split(":");
        String chebID =  StringUtils.chomp(splits[1],".tsv");
        // modify folder
        splits = outputPath.split("/");
        outputPath="";
        for(int i=0;i<splits.length-1;i++){
            outputPath = outputPath.concat(splits[i]+"/");
        }
        outputPath = outputPath.concat("saddle/dot/"+splits[splits.length-1]);
        
        ChebiGraph chebiGraph =
                new ChebiGraph(binche.getPValueMap(), binche.getOntology(), binche.getNodes());
        
        LOGGER.log(Level.INFO, "Writing out graph ...");
        DotWriter writer = new DotWriter();
        ColorGradient cG = new ColorGradient(binche.getPValueMap().values(), 0.05);
        
        writer.writeEvaluationDot(chebiGraph, cG , outputPath+".dot", chebID);
    
    }

    private BingoParameters getDefaultParameters(String ontologyFile) {

        BingoParameters parametersSaddle = new BingoParameters();

        parametersSaddle.setTest(BingoAlgorithm.SADDLESUM);
        parametersSaddle.setCorrection(BingoAlgorithm.NONE);
        parametersSaddle.setOntologyFile(ontologyFile);
        parametersSaddle.setOntology_default(false);
        parametersSaddle.setNameSpace("chebi_ontology");
        parametersSaddle.setOverOrUnder("Overrepresentation");
        parametersSaddle.setSignificance(new BigDecimal(0.05));
        parametersSaddle.setCategory(BingoAlgorithm.CATEGORY_CORRECTION);
        parametersSaddle.setReferenceSet(BingoAlgorithm.GENOME);
        parametersSaddle.setAllNodes(null);
        parametersSaddle.setSelectedNodes(null);

        return parametersSaddle;
    }
    
    private BingoParameters getHyperParameters(String ontologyFile) {

        BingoParameters parametersSaddle = new BingoParameters();

        parametersSaddle.setTest(BingoAlgorithm.HYPERGEOMETRIC);
        parametersSaddle.setCorrection(BingoAlgorithm.NONE);
        parametersSaddle.setOntologyFile(ontologyFile);
        parametersSaddle.setOntology_default(false);
        parametersSaddle.setNameSpace("chebi_ontology");
        parametersSaddle.setOverOrUnder("Overrepresentation");
        parametersSaddle.setSignificance(new BigDecimal(0.05));
        parametersSaddle.setCategory(BingoAlgorithm.CATEGORY_CORRECTION);
        parametersSaddle.setReferenceSet(BingoAlgorithm.GENOME);
        parametersSaddle.setAllNodes(null);
        parametersSaddle.setSelectedNodes(null);

        return parametersSaddle;
    }


    private void runHyper(String inputPath, String outputPath) {

        LOGGER.log(Level.INFO, "############ Start ############");

        String ontologyFile = getClass().getResource("/BiNGO/data/chebi_clean.obo").getFile();
        String elementsForEnrichFile = inputPath;
        
        BingoParameters parametersSaddle;
        LOGGER.log(Level.INFO, "Setting hypergeometric parameters ...");
        parametersSaddle = getHyperParameters(ontologyFile);
      
        BiNChe binche = new BiNChe();
        binche.setParameters(parametersSaddle);

        LOGGER.log(Level.INFO, "Reading input file ...");
        try {
            binche.loadDesiredElementsForEnrichmentFromFile(elementsForEnrichFile);
        } catch (IOException exception) {
            LOGGER.log(Level.ERROR, "Error reading file: " + exception.getMessage());
            System.exit(1);
        }

        binche.execute();

        // get ids
        String[] splits = inputPath.split(":");
        String chebID =  StringUtils.chomp(splits[1],".tsv");
        // modify folder
        splits = outputPath.split("/");
        outputPath="";
        for(int i=0;i<splits.length-1;i++){
            outputPath = outputPath.concat(splits[i]+"/");
        }
        outputPath = outputPath.concat("hyper/dot/"+splits[splits.length-1]);
        
        ChebiGraph chebiGraph =
                new ChebiGraph(binche.getPValueMap(), binche.getOntology(), binche.getNodes());
        
        LOGGER.log(Level.INFO, "Writing out graph ...");
        DotWriter writer = new DotWriter();
        ColorGradient cG = new ColorGradient(binche.getPValueMap().values(), 0.05);
        writer.writeEvaluationDot(chebiGraph, cG , outputPath+".dot", chebID);
    }

}
