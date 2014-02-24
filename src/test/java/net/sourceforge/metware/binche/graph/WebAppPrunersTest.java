package net.sourceforge.metware.binche.graph;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import net.sourceforge.metware.binche.BiNChe;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import BiNGO.BingoParameters;
import BiNGO.ParameterFactory;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import net.sourceforge.metware.binche.loader.BiNChEOntologyPrefs;
import net.sourceforge.metware.binche.loader.OfficialChEBIOboLoader;

public class WebAppPrunersTest {

    public WebAppPrunersTest() {
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
     * Test of prune method, of class WebAppPrunersTest.
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
        String ontologyFile = binchePrefs.get(BiNChEOntologyPrefs.RoleAndStructOntology.name(), null);
        //String ontologyFile = BiNChe.class.getResource("/BiNGO/data/chebiInferred_chemEnt.obo").getFile();
        String elementsForEnrichFile = "/testdata_for_webapp.txt";

        System.out.println("Setting default parameters ...");
        BingoParameters parametersChEBIBin = ParameterFactory.makeParametersForWeightedAnalysis(ontologyFile);

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

        List<ChEBIGraphPruner> pruners = Arrays.asList(
                //new MoleculeLeavesPruner(), 
                new HighPValueBranchPruner(0.05),
                new LinearBranchCollapserPruner(),
                new RootChildrenPruner(3, true),
                new ZeroDegreeVertexPruner());
        int originalVertices = chebiGraph.getVertexCount();
        System.out.println("Number of nodes before prunning : " + originalVertices);

        SvgWriter writer = new SvgWriter();

        writer.writeSvg(chebiGraph.getVisualisationServer(), "/tmp/beforePrune.svg");

        int prunes = 0;
        for (ChEBIGraphPruner chEBIGraphPruner : pruners) {
            chEBIGraphPruner.prune(chebiGraph);
            prunes++;
            System.out.println(chEBIGraphPruner.getClass().getCanonicalName());
            System.out.println("Removed vertices : " + (originalVertices - chebiGraph.getVertexCount()));
            originalVertices = chebiGraph.getVertexCount();
            System.out.println("Writing out graph ...");
            writer.writeSvg(chebiGraph.getVisualisationServer(), "/tmp/after" + prunes + "prunes.svg");
        }

        int finalVertices = chebiGraph.getVertexCount();

        System.out.println("Final vertices : " + (finalVertices));
    }
}
