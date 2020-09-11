import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.*;

import java.util.List;
import java.util.ArrayList;

public class TestRunner {

    @BeforeAll
    static void setup(){
        System.out.println("Testing for SIVT Back-end:");
    }

    @AfterAll
    static void afterAll(){
        System.out.println("All tests complete.");
    }

    @BeforeEach
    void beforeEach(TestInfo testInfo){
        System.out.println("Testing: " + testInfo.getDisplayName() + " - Started");
    }

    @AfterEach
    void afterEach(TestInfo testInfo){
        System.out.println("Testing: " + testInfo.getDisplayName() + " - Complete");
    }

    @Test
    @DisplayName("testFileReader")
    void testFileReader(){
        Assertions.assertEquals(" SELECT * FROM hello### SELECT a FROM goodbye",
                FileReader.ReadFile("./src/test/java/testInput.sql"));
    }

    @Test
    @DisplayName("testColumn")
    void testColumn() {
        // Constructors and getters.
        Column column = new Column("name", "alias", "id");
        Assertions.assertEquals("name", column.getName());
        Assertions.assertEquals("alias", column.getAlias());
        Assertions.assertEquals("id", column.getID());
        Assertions.assertTrue(column.getSources().isEmpty());

        column = new Column("otherName");
        Assertions.assertEquals("otherName", column.getName());
        Assertions.assertTrue(column.getAlias().isEmpty());
        Assertions.assertTrue(column.getID().isEmpty());

        // Setters.
        column.setName("newName");
        Assertions.assertEquals("newName", column.getName());
        column.setAlias("newAlias");
        Assertions.assertEquals("newAlias", column.getAlias());
        column.setID("newID");
        Assertions.assertEquals("newID", column.getID());

        // Set sources.
        ArrayList<String> sources = new ArrayList<>();
        sources.add("source1");
        sources.add("source2");
        column.setSources(sources);
        Assertions.assertEquals(sources, column.getSources());

        // Add single source.
        column.addSource("anotherSource");
        sources.add("anotherSource");
        Assertions.assertEquals(sources, column.getSources());

        // Add list of sources.
        ArrayList<String> moreSources = new ArrayList<>();
        moreSources.add("source3");
        moreSources.add("source4");
        sources.add("source3");
        sources.add("source4");
        column.addListOfSources(moreSources);
        Assertions.assertEquals(sources, column.getSources());

        // Equals.
        Column equalColumn = new Column("newName", "newAlias", "newID");
        equalColumn.addListOfSources(column.getSources());
        Assertions.assertTrue(column.equals(equalColumn));
        equalColumn.addSource("additionalSource");
        Assertions.assertFalse(column.equals(equalColumn));

        // Clone.
        try {
            Column clone = (Column) column.clone();
            Assertions.assertTrue(column.equals(clone));
        } catch(CloneNotSupportedException c) {
            Assertions.fail("Cloning column failure.");
        }
    }

    @Test
    @DisplayName("testLineageNodes")
    void testLineageNodes() {
        String simpleSelect = "SELECT a FROM b###";
        List<LineageNode> nodeList = LineageExtractor.extractLineage(simpleSelect).getNodeList();

        // Anonymous table.
        LineageNode anonymousNode = new LineageNode("ANONYMOUS", "Anonymous1", "");
        Column anonymousColumn = new Column("a", "", "Anonymous1::a");
        anonymousColumn.addSource("b::a");
        anonymousNode.addColumn(anonymousColumn);

        // Source table.
        LineageNode sourceNode = new LineageNode("TABLE", "b", "");
        Column sourceColumn = new Column("a", "", "b::a");
        sourceColumn.addSource("b::a");
        sourceNode.addColumn(sourceColumn);

        // Compare the pair.
        boolean success = true;
        success &= nodeList.get(1).equals(anonymousNode);
        success &= nodeList.get(0).equals(sourceNode);

        Assertions.assertTrue(success);
    }
}
