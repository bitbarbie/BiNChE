/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package BiNGO.parser;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;

import BiNGO.BingoParameters;
import BiNGO.methods.BingoAlgorithm;
import BiNGO.reader.BiNGOAnnotationFlatFileReader;
import BiNGO.reader.BiNGOOntologyChebiOboReader;
import BiNGO.reader.BiNGOOntologyFlatFileReader;
import BiNGO.reader.BiNGOOntologyOboReader;
import cytoscape.data.annotation.Annotation;
import cytoscape.data.annotation.Ontology;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * This is an extension of the {@link AnnotationParser} to accommodate the ChEBI Ontology</br>
 * and related assumptions.
 *
 * @author Pablo Moreno
 * @author Janna Hastings
 */
public class ChEBIAnnotationParser extends AnnotationParser {

    public ChEBIAnnotationParser(BingoParameters params, Set<String> genes) {
        super(params, genes);
    }

    /**
     * Method that parses the custom annotation file into an annotation-object and
     * returns a string containing whether the operation is correct or not.
     * <p/>
     * In the ChEBI annotation parser, this will produce the chebi to chebi
     * entries file. The annotation in ChEBI needs to be loaded after the ontology is.
     *
     * @return string string with either loadcorrect or a parsing error.
     */
    @Override
    public String setCustomAnnotation() {
    	System.out.println("In ChEBIAnnotationParser.setCustomAnnotation() ");

        String resultString = LOADCORRECT;

        HashMap<Integer, String> ontologyIDs2Names = fullOntology.getTerms();


        annotation = new Annotation(params.getSpecies(), "ChEBI", fullOntology);

        alias = new HashMap<String, String>();
        for (Integer id : ontologyIDs2Names.keySet()) {
            String entityName = "CHEBI:" + id;
            annotation.add(entityName, id);
            HashSet tmp = new HashSet();
            tmp.add(entityName);
            alias.put(entityName, tmp);
        }

        params.setAlias(alias);

        this.consistency = true;

        return resultString;
    }

    /*
     * Method that parses the custom annotation file into an annotation-object and
     * returns a string containing whether the operation is correct or not.
     *
     * In the ChEBI annotation parser, this will produce the chebi to chebi
     * entries file. The annotation in ChEBI needs to be loaded after the ontology is.
     *
     * Note:This method is to be used where the annotation is provided separately from role/structure ontology.
     * If only ontology file is provided, use method setCustomAnnotation().
     *
     * @return string string with either loadcorrect or a parsing error.
     */
    private String setCustomAnnotationFromAnnotationFile() {
    	System.out.println("In ChEBIAnnotationParser.setCustomAnnotationFromAnnotationFile() ");

        String fileString = params.getAnnotationFile();
        annotation = null;

        String resultString;

        // flat file reader for custom annotation
        try {
            BiNGOAnnotationFlatFileReader readerAnnotation = new BiNGOAnnotationFlatFileReader(fileString, synonymHash);
            annotation = readerAnnotation.getAnnotation();
            if (readerAnnotation.getOrphans()) {
                orphansFound = true;
            }
            if (readerAnnotation.getConsistency()) {
                consistency = true;
            }
            alias = readerAnnotation.getAlias();
            resultString = LOADCORRECT;
        }
        catch (IllegalArgumentException e) {
            resultString = "ANNOTATION FILE PARSING ERROR, PLEASE CHECK FILE FORMAT:  \n" + e;
            e.printStackTrace();
        }
        catch (IOException e) {
            resultString = "Annotation file could not be located..."+e.getMessage();
            e.printStackTrace();
        }
        catch (Exception e) {
            System.out.println(e);
            e.printStackTrace();
            resultString = e.getMessage();
        }

        return resultString;

    }

    /**
     * Method that governs loading and remapping of annotation files
     */
    @Override
    public void calculate() {
    	System.out.println("In ChEBIAnnotationParser.calculate()");

        if (!params.isOntology_default()) {
            // always perform full remap for .obo files, allows definition of custom GOSlims
            if (params.getOntologyFile().endsWith(".obo")) {
                String loadFullOntologyString = setFullOntology();
                if (!loadFullOntologyString.equals(LOADCORRECT)) {
                    status = false;
                    System.out.println("Your full ontology file contains errors " + loadFullOntologyString);
                }
                if (status == true) {
                    //check for cycles
                    checkOntology(fullOntology);
                }
            }

            if (status == true) {
                String loadOntologyString = setCustomOntology();

                // loaded a correct ontology file?
                if (!loadOntologyString.equals(LOADCORRECT)) {
                    status = false;
                    System.out.println("Your ontology file contains errors " + loadOntologyString);
                }
                if (status == true) {
                    //check for cycles
                    checkOntology(ontology);
                    if (status = true) {
                        String loadAnnotationString;
                        if (!params.isAnnotation_default()) {

                            if (params.getAnnotationFile()==null) {
                                loadAnnotationString = setCustomAnnotation();
                            }                                
                            else {
                                //Changed method to read annotation from an annotation file and not from the ontology
                                loadAnnotationString = setCustomAnnotationFromAnnotationFile();
                            }

                        } else {
                            loadAnnotationString = "ERROR: Default annotation not supported in BiNChE";
                        	System.out.println("ERROR: Default annotation not supported in BiNChE");
                        }

                        // loaded a correct annotation file?
                        if (!loadAnnotationString.equals(LOADCORRECT)) {
                            status = false;
                            System.out.println("Your annotation file contains errors " + loadAnnotationString);
                        }
                        // annotation consistent with ontology ?
                        if ((status == true) && (consistency == false)) {
                            status = false;
                            System.out.println(
                                    "None of the labels in your annotation match with the chosen ontology, please check their compatibility.");
                        }
                        if (status == true) {
                            if (params.getOntologyFile().endsWith(".obo")) {
                                parsedAnnotation = remap(annotation, ontology, genes);
                            } else {
                                parsedAnnotation = customRemap(annotation, ontology, genes);
                            }
                        }
                    }
                }
            }
        } else {
        	System.out.println("ERROR: Cannot use default ontology setting with BiNChe.");
        	
        }
    }


    /**
     * Method that parses the ontology file into an ontology-object and
     * returns a string containing whether the operation is correct or not.
     *
     * @return string string with either loadcorrect or a parsing error.
     */
    @Override
    public String setCustomOntology() {
    	System.out.println("In ChEBIAnnotationParser.setCustomOntology()");

        String fileString = params.getOntologyFile();
        String namespace = params.getNameSpace();
        ontology = null;
        String resultString = "";

        if (fileString.endsWith(".obo")) {
            try {
                BiNGOOntologyOboReader readerOntology = new BiNGOOntologyChebiOboReader(new File(fileString), namespace);
                ontology = readerOntology.getOntology();
                if (ontology.size() == 0) {
                    throw (new IllegalArgumentException("" ));
                } else {
                    resultString = LOADCORRECT;
                }
            } catch (IllegalArgumentException e) {
                resultString =
                        "ONTOLOGY FILE PARSING ERROR, PLEASE CHECK FILE FORMAT AND VALIDITY OF NAMESPACE:  \n" + e;
            } catch (IOException e) {
                resultString = "Ontology file could not be located... : "+params.getOntologyFile();
            } catch (Exception e) {
                resultString = e.getMessage();
            }
        } else {
            this.synonymHash = null;
            // flat file.
            try {
                BiNGOOntologyFlatFileReader readerOntology = new BiNGOOntologyFlatFileReader(fileString);
                ontology = readerOntology.getOntology();
                this.synonymHash = readerOntology.getSynonymHash();
                resultString = LOADCORRECT;
            } catch (IllegalArgumentException e) {
                resultString = "ONTOLOGY FILE PARSING ERROR, PLEASE CHECK FILE FORMAT:  \n" + e;
            } catch (IOException e) {
                resultString = "Ontology file could not be located...";
            } catch (Exception e) {
                resultString = e.getMessage();
            }
        }

        return resultString;
    }



    /**
     * Method that parses the ontology file into an ontology-object and
     * returns a string containing whether the operation is correct or not.
     *
     * @return string string with either loadcorrect or a parsing error.
     */
    @Override
    public String setFullOntology() {
    	System.out.println("In ChEBIAnnotationParser.setFullOntology()");

        fullOntology = null;
        synonymHash = null;
        String resultString = "";

        if (params.getOntologyFile().endsWith(".obo")) {
            // read full ontology.
            try {
                BiNGOOntologyOboReader readerOntology =
                        new BiNGOOntologyChebiOboReader(new File(params.getOntologyFile()), BingoAlgorithm.NONE);
                fullOntology = readerOntology.getOntology();
                if (fullOntology.size() == 0) {
                    throw (new IllegalArgumentException());
                } else {
                    synonymHash = readerOntology.getSynonymHash();
                    resultString = LOADCORRECT;
                }
            } catch (IllegalArgumentException e) {
                resultString =
                        "ONTOLOGY FILE PARSING ERROR, PLEASE CHECK FILE FORMAT AND VALIDITY OF NAMESPACE:  \n" + e;
            } catch (IOException e) {
                resultString = "Ontology file could not be located...: "+e.getMessage();
            } catch (Exception e) {
                resultString = e.getMessage();
            }
        } else {
            // deserialize object
            try {
                BiNGOOntologyFlatFileReader readerOntology = new BiNGOOntologyFlatFileReader(params.getOntologyFile());
                fullOntology = readerOntology.getOntology();
                synonymHash = readerOntology.getSynonymHash();
                resultString = LOADCORRECT;
            } catch (IllegalArgumentException e) {
                resultString = "FULL ONTOLOGY FILE PARSING ERROR, PLEASE CHECK FILE FORMAT:  \n" + e;
            } catch (IOException e) {
                resultString = "Full ontology file could not be located... :"+e.getMessage();
            } catch (Exception e) {
                resultString = e.getMessage();
            }
        }
        return resultString;

    }
}
