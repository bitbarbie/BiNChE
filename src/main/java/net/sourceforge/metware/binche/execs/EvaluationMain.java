/*
 * adapted from BiNCheExec.java
 */

package net.sourceforge.metware.binche.execs;

import BiNGO.BingoParameters;
import BiNGO.methods.BingoAlgorithm;
import java.awt.*;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import javax.swing.*;
import net.sourceforge.metware.binche.BiNChENode;
import net.sourceforge.metware.binche.BiNChe;
import net.sourceforge.metware.binche.graph.ChebiGraph;
import net.sourceforge.metware.binche.graph.ColorGradient;
import net.sourceforge.metware.binche.graph.DotWriter;
import net.sourceforge.metware.binche.gui.SettingsPanel;
import net.sourceforge.metware.binche.loader.BiNChEOntologyPrefs;
import net.sourceforge.metware.binche.loader.OfficialChEBIOboLoader;
import org.apache.commons.cli.Option;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 * @author Sarah Scharfenberg
 */
public class EvaluationMain extends CommandLineMain {

    private static final Logger LOGGER = Logger.getLogger(EvaluationMain.class);

    public static void main(String[] args) {

        new EvaluationMain(args).process();
    }

    public EvaluationMain(String[] args) {

        super(args);
    }

    @Override public void setupOptions() {

        add(new Option("i", "file to load", true, "association file to load (format: .../CHEBI:<id>.tsv)"));
        add(new Option("o", "output directory", true, "directory to write output to (format: .../<id>)"));
        add(new Option("l", "file including in- and output filenames", true, "iterate a list of files"));
        add(new Option("p", "true", false, "calculate hypergeometric distribution rather than saddle sum method"));

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

        Preferences binchePrefs = Preferences.userNodeForPackage(BiNChe.class);
        try {
            if (binchePrefs.keys().length == 0) {
                new OfficialChEBIOboLoader();
            }
        } catch (BackingStoreException e) {
            LOGGER.error("Problems loading preferences", e);
            return;
        } catch (IOException e) {
            LOGGER.error("Problems loading preferences", e);
            return;
        }

        //String ontologyFile = getClass().getResource("/BiNGO/data/chebi_clean.obo").getFile();
        
        //String ontologyFile = binchePrefs.get(BiNChEOntologyPrefs.RoleAndStructOntology.name(), null);
        String ontologyFile = getClass().getResource("/BiNGO/data/out_test_eclipse.obo").getFile();
        
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
                new ChebiGraph(binche.getEnrichedNodes(), binche.getOntology(), binche.getInputNodes());
        //ChebiGraph chebiGraph = new ChebiGraph(binche.getPValueMap(), binche.getOntology(), binche.getNodes());
        
        LOGGER.log(Level.INFO, "Writing out graph ...");
        DotWriter writer = new DotWriter();
        ColorGradient cG = new ColorGradient(getListOfPValues(binche.getEnrichedNodes()), 0.05);
        
        
        writer.writeEvaluationDot(chebiGraph, cG , outputPath+".dot", chebID);
    }

    /**
     * This should be set through the parameters factory. This should be removed.
     *
     * @param ontologyFile
     * @return
     */
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

        Preferences binchePrefs = Preferences.userNodeForPackage(BiNChe.class);
        try {
            if (binchePrefs.keys().length == 0) {
                new OfficialChEBIOboLoader();
            }
        } catch (BackingStoreException e) {
            LOGGER.error("Problems loading preferences", e);
            return;
        } catch (IOException e) {
            LOGGER.error("Problems loading preferences", e);
            return;
        }

        //String ontologyFile = getClass().getResource("/BiNGO/data/chebi_clean.obo").getFile();
        String ontologyFile = binchePrefs.get(BiNChEOntologyPrefs.RoleAndStructOntology.name(), null);
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
                new ChebiGraph(binche.getEnrichedNodes(), binche.getOntology(), binche.getInputNodes());
        //ChebiGraph chebiGraph = new ChebiGraph(binche.getPValueMap(), binche.getOntology(), binche.getNodes());
             
        LOGGER.log(Level.INFO, "Writing out graph ...");
        DotWriter writer = new DotWriter();
        ColorGradient cG = new ColorGradient(getListOfPValues(binche.getEnrichedNodes()), 0.05);
        
        writer.writeEvaluationDot(chebiGraph, cG , outputPath+".dot", chebID);
    }
       
       
    /**
     * This will be part of a different class, something like a BiNCheNode list processor.
     * 
     * @param enrichmentNodes
     * @return 
     */
    private Collection<Double> getListOfPValues(java.util.List<BiNChENode> enrichmentNodes) {
        // for this application, we only need the different values.
        Set<Double> pValues = new HashSet<Double>();
        boolean usedCorr = false;
        // either we use all corrected or all non-corrected, but never a mixture!!!
        if(enrichmentNodes.size()>0 && enrichmentNodes.get(0).getCorrPValue()!=null) {
            usedCorr=true;
        }
        for (BiNChENode biNChENode : enrichmentNodes) {
            if(usedCorr) {
                pValues.add(biNChENode.getCorrPValue());
            }
            else {
                pValues.add(biNChENode.getPValue());                
            }
        }
        
        return pValues;
    }

}