/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package BiNGO.reader;

import cytoscape.data.annotation.ChEBIOntologyTerm;
import cytoscape.data.annotation.Ontology;
import cytoscape.data.annotation.OntologyTerm;
import java.io.BufferedReader;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

/**
 * Reads the ChEBI Ontology Obo file. It requires InChIs to asses whether an entry is a molecule or not.</br>
 * It extends the {@link BiNGOOntologyOboReader} to provide the necessary adjustments for the ChEBI Ontology.
 * 
 * @author Pablo Moreno
 */
public class BiNGOOntologyChebiOboReader extends BiNGOOntologyOboReader {

    /**
     * Initializes the reader with the given ChEBI obo file.
     * 
     * @param chebiOboFile
     * @param namespace
     * @throws IllegalArgumentException
     * @throws IOException
     * @throws Exception 
     */
    public BiNGOOntologyChebiOboReader(File chebiOboFile,
                                       String namespace) throws IllegalArgumentException, IOException, Exception {
        super(chebiOboFile, namespace);
    }

    /**
     * Initializes the reader with the given ChEBI obo file name (path).
     * 
     * @deprecated use {@link #BiNGOOntologyChebiOboReader(java.io.File, java.lang.String) } or 
     * {@link #BiNGOOntologyChebiOboReader(java.io.InputStream, java.lang.String) } instead.
     * 
     * @param chebiOboFileName
     * @param namespace
     * @throws IllegalArgumentException
     * @throws IOException
     * @throws Exception 
     */
    public BiNGOOntologyChebiOboReader(String chebiOboFileName,
                                       String namespace) throws IllegalArgumentException, IOException, Exception {
        super(chebiOboFileName, namespace);
    }
    
    public BiNGOOntologyChebiOboReader(InputStream ontologyInput, String namespace) throws IOException {
        super(ontologyInput, namespace);
    }

    @Override
    protected void parse(int c) throws Exception {

        ontology = new Ontology(curator, ontologyType);
        fullOntology = new Ontology(curator, ontologyType);
        int i = c;
        while (i < lines.length && !lines[i].trim().equals("[Typedef]")) {
            i++;
            String name = new String();
            String id = new String();
            // TODO we can remove this set
            Set<String> namespaceSet = new HashSet<String>();
            Set<String> alt_id = new HashSet<String>();
            Set<String> is_a = new HashSet<String>();            
            Set<String> has_part = new HashSet<String>();
            Set<String> has_role = new HashSet<String>();
            boolean obsolete = false;
            boolean molecule=false;
            // TODO we should change this for a buffered strategy.
            while (i < lines.length && !lines[i].trim().equals("[Term]") && !lines[i].trim().equals("[Typedef]") && !lines[i].trim().startsWith("!")) {
                if (!lines[i].trim().isEmpty()) {
                    String ref = lines[i].substring(0, lines[i].indexOf(":")).trim();
                    String value = lines[i].substring(lines[i].indexOf(":") + 1).trim();
                    if (ref.equals("name")) {
                        name = value.trim();
                    } else if (ref.equals("namespace")) {
                        namespaceSet.add(value.trim());
                    } else if (ref.equals("subset")) {
                        namespaceSet.add(value.trim());
                    } else if (ref.equals("id")) {
                        id = value.trim().substring(value.indexOf(":") + 1);
                    } else if (ref.equals("alt_id")) {
                        alt_id.add(value.trim().substring(value.indexOf(":") + 1));
                    } else if (ref.equals("is_a")) {
                        is_a.add(value.split("!")[0].trim().substring(value.indexOf(":") + 1));
                    } else if (ref.equals("relationship")) {
                        if (value.startsWith("has_part")) {
                            has_part.add(value.substring(value.indexOf(":") + 1));
                        } else if (value.startsWith("has_role")) {
                            has_role.add(value.substring(value.indexOf(":") + 1));
                        }
                    } else if (ref.equals("is_obsolete")) {
                        if (value.trim().equals("true")) {
                            obsolete = true;
                        }
                    //} else if (ref.equals("synonym") && value.contains("RELATED InChI")) {
                    } else if (ref.equals("hasRelatedSynonym") && value.startsWith("InChI=1")) {
                        molecule=true;
                    }
                }
                i++;
            }
            if (obsolete == false && !id.isEmpty()) {                
                // For the ChEBI namespace
                Integer id2 = new Integer(id);
                synonymHash.put(id2, id2);
                ChEBIOntologyTerm term;
                if (!ontology.containsTerm(id2)) {
                    term = new ChEBIOntologyTerm(name, id2);
                    ontology.add(term);
                    fullOntology.add(term);
                } else {
                    term = (ChEBIOntologyTerm)ontology.getTerm(id2);
                }
                term.setMolecule(molecule);
                for (String s : alt_id) {
                    synonymHash.put(new Integer(s), id2);
                }
                for (String s : is_a) {
                    term.addParent(new Integer(s));
                }
                for (String s : has_role) {
                    term.addContainer(new Integer(s));
                }
                for (String s : has_part) { 
                    // elements in has part
                    // are sub parts of the term that we are looking
                    // for. Hence, we get the "smaller" term and
                    // add the current term as a container for it.
                    Integer containedID = new Integer(s);
                    ChEBIOntologyTerm containedTerm;
                    if (ontology.containsTerm(containedID)) {
                        containedTerm = (ChEBIOntologyTerm)ontology.getTerm(containedID);
                    } else {
                        containedTerm = new ChEBIOntologyTerm(name, containedID);
                        ontology.add(containedTerm);
                        fullOntology.add(containedTerm);
                    }

                    containedTerm.addContainer(term.getId());
                }
            }
        }

    } // read
    
    @Override
    protected void parse(BufferedReader inputReader) throws IOException {
        // process OBO header first
        String line = inputReader.readLine();
        while(!line.trim().equals("[Term]") && line!=null){
            line = inputReader.readLine();
        }
        
        ontology = new Ontology(curator, ontologyType);
        fullOntology = new Ontology(curator, ontologyType);
        
        while(line!=null && !line.trim().equals("[Typedef]")){       
            line = inputReader.readLine();
            String name = new String();
            String id = new String();
            // TODO we can remove this set
            Set<String> namespaceSet = new HashSet<String>();
            Set<String> alt_id = new HashSet<String>();
            Set<String> is_a = new HashSet<String>();            
            Set<String> has_part = new HashSet<String>();
            Set<String> has_role = new HashSet<String>();
            boolean obsolete = false;
            boolean molecule=false;
            // Buffered strategy
            while (line!=null && !line.trim().equals("[Term]") && !line.trim().equals("[Typedef]") && !line.trim().startsWith("!")) {
                if (!line.trim().isEmpty()) {
                    String ref = line.substring(0, line.indexOf(":")).trim();
                    String value = line.substring(line.indexOf(":") + 1).trim();
                    if (ref.equals("name")) {
                        name = value.trim();
                    } else if (ref.equals("namespace")) {
                        namespaceSet.add(value.trim());
                    } else if (ref.equals("subset")) {
                        namespaceSet.add(value.trim());
                    } else if (ref.equals("id")) {
                        id = value.trim().substring(value.indexOf(":") + 1);
                    } else if (ref.equals("alt_id")) {
                        alt_id.add(value.trim().substring(value.indexOf(":") + 1));
                    } else if (ref.equals("is_a")) {
                        is_a.add(value.split("!")[0].trim().substring(value.indexOf(":") + 1));
                    } else if (ref.equals("relationship")) {
                        if (value.startsWith("has_part")) {
                            has_part.add(value.substring(value.indexOf(":") + 1));
                        } else if (value.startsWith("has_role")) {
                            has_role.add(value.substring(value.indexOf(":") + 1));
                        }
                    } else if (ref.equals("is_obsolete")) {
                        if (value.trim().equals("true")) {
                            obsolete = true;
                        }
                    } else if (ref.equals("synonym") && value.contains("InChI=")) {
                        molecule=true;
                    } else if (ref.equals("hasRelatedSynonym") && value.startsWith("InChI=1")) {
                        molecule=true;
                    }
                }
                line = inputReader.readLine();
            }
            if (obsolete == false && !id.isEmpty()) {                
                // For the ChEBI namespace
                Integer id2 = new Integer(id);
                synonymHash.put(id2, id2);
                ChEBIOntologyTerm term;
                if (!ontology.containsTerm(id2)) {
                    term = new ChEBIOntologyTerm(name, id2);
                    ontology.add(term);
                    fullOntology.add(term);
                } else {
                    term = (ChEBIOntologyTerm)ontology.getTerm(id2);
                }
                term.setMolecule(molecule);
                for (String s : alt_id) {
                    synonymHash.put(new Integer(s), id2);
                }
                for (String s : is_a) {
                    term.addParent(new Integer(s));
                }
                for (String s : has_role) {
                    term.addContainer(new Integer(s));
                }
                for (String s : has_part) { 
                    // elements in has part
                    // are sub parts of the term that we are looking
                    // for. Hence, we get the "smaller" term and
                    // add the current term as a container for it.
                    Integer containedID = new Integer(s);
                    ChEBIOntologyTerm containedTerm;
                    if (ontology.containsTerm(containedID)) {
                        containedTerm = (ChEBIOntologyTerm)ontology.getTerm(containedID);
                    } else {
                        containedTerm = new ChEBIOntologyTerm(name, containedID);
                        ontology.add(containedTerm);
                        fullOntology.add(containedTerm);
                    }

                    containedTerm.addContainer(term.getId());
                }
            }
        }

    }
}
