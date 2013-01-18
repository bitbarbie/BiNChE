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
import net.sourceforge.metware.binche.BiNChe;
import org.junit.*;
import static org.junit.Assert.*;

/**
 *
 * @author pmoreno
 */
public class LowPValueBranchPrunerTest {
    
    public LowPValueBranchPrunerTest() {
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
     * Test of prune method, of class LowPValueBranchPruner.
     */
    @Test
    public void testPrune() {
        System.out.println("prune");

        String ontologyFile = BiNChe.class.getResource("/BiNGO/data/chebi_clean.obo").getFile();
        String elementsForEnrichFile = "/BiNGO/data/enrich_set_flavonoids.txt";

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
                new ChebiGraph(binche.getPValueMap(), binche.getOntology(), binche.getNodes());

        List<ChEBIGraphPruner> pruners = Arrays.asList(new MoleculeLeavesPruner(), new LowPValueBranchPruner(0.05));
        //MoleculeLeavesPruner instance = new MoleculeLeavesPruner();
        int originalVertices = chebiGraph.getVertexCount();
        System.out.println("Number of nodes before prunning : " + originalVertices);

        //System.out.println("Writing out graph ...");
        //SvgWriter writer = new SvgWriter();

        //writer.writeSvg(chebiGraph.getVisualisationServer(), "/tmp/beforePrune.svg");

        for (ChEBIGraphPruner chEBIGraphPruner : pruners) {
            chEBIGraphPruner.prune(chebiGraph);
            System.out.println(chEBIGraphPruner.getClass().getCanonicalName());
            System.out.println("Removed vertices : " + (originalVertices - chebiGraph.getVertexCount()));
            originalVertices = chebiGraph.getVertexCount();
        }
        
        int finalVertices = chebiGraph.getVertexCount();
        
        System.out.println("Final vertices : " + (finalVertices));
    }
}
