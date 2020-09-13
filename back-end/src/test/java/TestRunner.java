import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.junit.jupiter.api.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


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

    /**
     * Get the data from a column in the format:
     * "alias=columnAlias,id=columnID,name=columnName,sources={source1,source2,...}"
     * @param column The column which will have its data extracted and stringified.
     * @return The column data in the string form (above).
     */
    private String getColumnDataString(Column column)  {
        String columnData = ReflectionToStringBuilder.reflectionToString(column);
        return columnData.substring(columnData.indexOf("[")+1, columnData.indexOf("]"));
    }

    @Test
    @DisplayName("testColumn")
    void testColumn() {
        // Testing "getters"
        Column column = new Column("name", "alias", "id");
        Assertions.assertEquals("name", column.getName());
        Assertions.assertEquals("alias", column.getAlias());
        Assertions.assertEquals("id", column.getID());
        Assertions.assertTrue(column.getSources().isEmpty());

        // Testing "setters"
        ArrayList<String> sources = new ArrayList<>(Arrays.asList("source1", "source2"));
        column.setName("newName");
        column.setAlias("newAlias");
        column.setID("newID");
        column.setSources(sources);
        column.addSource("source3");
        column.addListOfSources(sources);
        Assertions.assertEquals(
                "alias=newAlias,id=newID,name=newName,sources={source1,source2,source3,source1,source2}",
                getColumnDataString(column));

        // Test Column cloning and equals
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

        // Source table.
        LineageNode sourceNode = new LineageNode("TABLE", "b");
        Column a = new Column("a");
        sourceNode.addColumn(a);

        // Anonymous table.
        LineageNode anonymousNode = new LineageNode("ANONYMOUS", "Anonymous0");
        a.addSource("b::a");
        anonymousNode.addColumn(a);

        // Compare the pair.
        boolean success = true;
        success &= nodeList.get(1).equals(anonymousNode);
        success &= nodeList.get(0).equals(sourceNode);

        Assertions.assertTrue(success);
    }
}
