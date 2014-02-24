/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sourceforge.metware.binche.graph;

import BiNGO.BingoParameters;
import BiNGO.ParameterFactory;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import net.sourceforge.metware.binche.BiNChe;
import net.sourceforge.metware.binche.loader.BiNChEOntologyPrefs;
import net.sourceforge.metware.binche.loader.OfficialChEBIOboLoader;
import org.junit.*;

/**
 *
 * @author pmoreno
 */
public class HighPValueBranchPrunerTest {
    
    public HighPValueBranchPrunerTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of prune method, of class HighPValueBranchPruner.
     */
    @Test
    public void testPrune() {
        System.out.println("prune");
        
        Preferences binchePrefs = Preferences.userNodeForPackage(BiNChe.class);
        try {
            if (binchePrefs.keys().length == 0) {
                new OfficialChEBIOboLoader();
            }
        } catch (BackingStoreException e) {
            System.err.println("Problems loading preferences");
            e.printStackTrace();
            return;
        } catch (IOException e) {
            System.err.println("Problems loading preferences");
            e.printStackTrace();
            return;
        }

        //String ontologyFile = getClass().getClassLoader().getResource("chebi_clean.obo").getFile();
        String ontologyFile = binchePrefs.get(BiNChEOntologyPrefs.RoleAndStructOntology.name(),null);
        String elementsForEnrichFile = "/enrich_set_flavonoids.txt";

        System.out.println("Setting default parameters ...");
        BingoParameters parametersChEBIBin = ParameterFactory.makeParametersForChEBIBinomialOverRep(ontologyFile);

        BiNChe binche = new BiNChe();
        binche.setParameters(parametersChEBIBin);

        System.out.println("Reading input file ...");
        try {
            binche.loadDesiredElementsForEnrichmentFromFile(elementsForEnrichFile);
        } catch (IOException exception) {
            System.out.println("Error reading file: " + exception.getMessage());
            System.exit(1);
        }

        binche.execute();

        ChebiGraph chebiGraph =
                new ChebiGraph(binche.getEnrichedNodes(), binche.getOntology(), binche.getInputNodes());

        List<ChEBIGraphPruner> pruners = Arrays.asList(new MoleculeLeavesPruner(), new HighPValueBranchPruner(0.05));
        //MoleculeLeavesPruner instance = new MoleculeLeavesPruner();
        int originalVertices = chebiGraph.getVertexCount();
        System.out.println("Number of nodes before prunning : " + originalVertices);

        //System.out.println("Writing out graph ...");
        SvgWriter writer = new SvgWriter();

        writer.writeSvg(chebiGraph.getVisualisationServer(), "/tmp/beforePrune.svg");

        for (ChEBIGraphPruner chEBIGraphPruner : pruners) {
            chEBIGraphPruner.prune(chebiGraph);
            System.out.println(chEBIGraphPruner.getClass().getCanonicalName());
            System.out.println("Removed vertices : " + (originalVertices - chebiGraph.getVertexCount()));
            originalVertices = chebiGraph.getVertexCount();
            writer.writeSvg(chebiGraph.getVisualisationServer(), "/tmp/"+chEBIGraphPruner.getClass().getCanonicalName()+".svg");
        }
        
        int finalVertices = chebiGraph.getVertexCount();
        
        System.out.println("Final vertices : " + (finalVertices));
    }
}
