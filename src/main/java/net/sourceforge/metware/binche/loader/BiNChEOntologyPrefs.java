/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sourceforge.metware.binche.loader;

import java.util.Arrays;
import java.util.List;

/**
 * Enumeration that stores the root nodes of the different parts of the ChEBI ontology that can be used for the
 * enrichment analysis.
 *
 * @author Pablo Moreno
 */
public enum BiNChEOntologyPrefs {
    RoleOntology("CHEBI:50906"),StructureOntology("CHEBI:24431"),RoleAndStructOntology("CHEBI:50906","CHEBI:24431"),RoleAnnot;

    private List<String> rootNodes;
    
    private BiNChEOntologyPrefs(String... chebiRootNodes) {
        rootNodes = Arrays.asList(chebiRootNodes);
    }

    /**
     * Returns the root nodes (CHEBI:<digits>) for the ontology in use.
     *
     * @return a list with root nodes.
     */
    public List<String> getRootChEBIEntries() {
        return rootNodes;
    }
    
}
