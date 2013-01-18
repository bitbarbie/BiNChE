/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sourceforge.metware.binche.graph;

import BiNGO.BingoParameters;
import BiNGO.ParameterFactory;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.sourceforge.metware.binche.BiNChe;
import org.junit.*;

/**
 *
 * @author pmoreno
 */
public class MoleculeLeavesPrunerTest {

    public MoleculeLeavesPrunerTest() {
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
     * Test of prune method, of class MoleculeLeavesPruner.
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
        MoleculeLeavesPruner instance = new MoleculeLeavesPruner();
        int originalVertices = chebiGraph.getVertexCount();
        System.out.println("Number of nodes before prunning : "+originalVertices);

        System.out.println("Writing out graph ...");
        SvgWriter writer = new SvgWriter();

        writer.writeSvg(chebiGraph.getVisualisationServer(), "/tmp/beforePrune.svg");

        instance.prune(chebiGraph);

        SvgWriter writer2 = new SvgWriter();
        writer2.writeSvg(chebiGraph.getVisualisationServer(), "/tmp/afterPrune.svg");
        int finalVertices = chebiGraph.getVertexCount();
        
        System.out.println("Final vertices : " + (finalVertices));

    }
}
