/**
 * OfficialChEBIOboLoader.java
 *
 * 2013.02.11
 *
 * This file is part of the CheMet library
 * 
 * The CheMet library is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * CheMet is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with CheMet.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.sourceforge.metware.binche.loader;


import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import net.sourceforge.metware.binche.BiNChe;
import net.sourceforge.metware.binche.execs.PreProcessOboFile;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

/**
 * This class downloads and processes the ChEBI OBO file, to maximize its utility for the enrichment analysis. The
 * main processing class is {@link PreProcessOboFile}, which calls the reasoner to infer to assertions.
 *
 * @name    OfficialChEBIOboLoader
 * @date    2013.02.11
 * @author  Pablo Moreno
 * @brief   Handles the download and processing of the official ChEBI ontology OBO file.
 */
public class OfficialChEBIOboLoader {

    private static final Logger LOGGER = Logger.getLogger( OfficialChEBIOboLoader.class );
    
    private final String oboURL = "ftp://ftp.ebi.ac.uk/pub/databases/chebi/ontology/chebi.obo";

    /**
     * The constructor loads the OBO file from the ChEBI ftp and executes the reasoning steps.
     *
     * @throws IOException
     * @throws BackingStoreException
     */
    public OfficialChEBIOboLoader() throws IOException, BackingStoreException {
        Preferences binchePrefs = Preferences.userNodeForPackage(BiNChe.class);
        
        if(binchePrefs.keys().length==0) {
            binchePrefs = (new DefaultPreferenceSetter()).getDefaultSetPrefs();
        }
        
        PreProcessOboFile ppof = new PreProcessOboFile();
        File tmpFileObo = File.createTempFile("BiNChE", ".obo");
        FileUtils.copyURLToFile(new URL(oboURL), tmpFileObo);

        ppof.getTransitiveClosure(tmpFileObo.getAbsolutePath(), binchePrefs.get(BiNChEOntologyPrefs.RoleOntology.name(), null), 
                false, 
                true, 
                BiNChEOntologyPrefs.RoleOntology.getRootChEBIEntries(), 
                Arrays.asList("rdfs:label"), new ArrayList<String>());
        File tmpRoleOnt = new File(binchePrefs.get(BiNChEOntologyPrefs.RoleOntology.name(),null)+".temp");
        tmpRoleOnt.delete();
        
        ppof.getTransitiveClosure(tmpFileObo.getAbsolutePath(), binchePrefs.get(BiNChEOntologyPrefs.StructureOntology.name(), null), 
                false, 
                false, 
                BiNChEOntologyPrefs.StructureOntology.getRootChEBIEntries(), 
                Arrays.asList("rdfs:label","InChI"), 
                new ArrayList<String>());
        File tmpStructOnt = new File(binchePrefs.get(BiNChEOntologyPrefs.StructureOntology.name(),null)+".temp");
        tmpStructOnt.delete();
        
        ppof.getTransitiveClosure(tmpFileObo.getAbsolutePath(), binchePrefs.get(BiNChEOntologyPrefs.RoleAndStructOntology.name(),null), 
                true,
                true,
                BiNChEOntologyPrefs.RoleAndStructOntology.getRootChEBIEntries(), 
                Arrays.asList("rdfs:label","InChI"),
                Arrays.asList("http://purl.obolibrary.org/obo/RO_0000087"));
        File tmpStructRoleOnt = new File(binchePrefs.get(BiNChEOntologyPrefs.RoleAndStructOntology.name(),null)+".temp");
        tmpStructRoleOnt.delete();
        File structRoleAnnot = new File(binchePrefs.get(BiNChEOntologyPrefs.RoleAndStructOntology.name(),null).replace(".obo",".txt"));
        String fullPath = binchePrefs.get(BiNChEOntologyPrefs.RoleAndStructOntology.name(),null);
        structRoleAnnot.renameTo(new File(fullPath.substring(0,fullPath.lastIndexOf(File.separator))+File.separator+"chebi_roles.anno"));
        
        tmpFileObo.delete();
    }

    /**
     * Main method to run the obo file download and process.
     *
     * @param args
     * @throws BackingStoreException
     * @throws IOException
     */
    public static void main(String[] args) throws BackingStoreException, IOException {
        new OfficialChEBIOboLoader();
    }


}
