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
        List<LineageNode> nodeList = LineageExtractor.extractLineageWithAnonymousTables(simpleSelect).getNodeList();

        // Anonymous table.
        LineageNode anonymousNode = new LineageNode("ANONYMOUS", "Anonymous0", "");
        Column anonymousColumn = new Column("a", "", "Anonymous0::a");
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

    @Test
    @DisplayName("testBypassAnonymousTables")
    void testBypassAnonymousTables() {
        String sql =
                "CREATE VIEW view AS " +
                        "SELECT b " +
                        "FROM (" +
                        "SELECT b " +
                        "FROM B" +
                        ") AS A" +
                        "###";

        // Source table.
        LineageNode source = new LineageNode("TABLE", "b");
        Column b = new Column("b");
        source.addColumn(b);

        // Anonymous table.
        LineageNode anonymous = new LineageNode("ANONYMOUS", "Anonymous0");
        anonymous.setAlias("A");
        b.addSource(DataLineage.makeId(source.getName(), b.getName()));
        anonymous.addColumn(b);

        // View.
        LineageNode view = new LineageNode("VIEW", "view");
        b = new Column("b");
        b.addSource(DataLineage.makeId(anonymous.getName(), b.getName()));
        view.addColumn(b);

        // First, verify that the anonymous table is produced correctly as the intermediate table.
        List<LineageNode> nodeList = LineageExtractor.extractLineageWithAnonymousTables(sql).getNodeList();
        Assertions.assertEquals(3, nodeList.size());
        Assertions.assertTrue(source.equals(nodeList.get(0)));
        Assertions.assertTrue(anonymous.equals(nodeList.get(1)));
        Assertions.assertTrue(view.equals(nodeList.get(2)));

        // Now extract the lineage, including the step where the anonymous tables are bypassed.
        nodeList = LineageExtractor.extractLineage(sql).getNodeList();

        // Adjust the view, it's column's sources have now bypassed the anonymous table.
        view = new LineageNode("VIEW", "view");
        b = new Column("b");
        b.addSource(DataLineage.makeId(source.getName(), b.getName()));
        view.addColumn(b);

        // Check the resultant lineage is as expected.
        Assertions.assertEquals(2,  nodeList.size());
        Assertions.assertTrue(source.equals(nodeList.get(0)));
        Assertions.assertTrue(view.equals(nodeList.get(1)));
    }
}
