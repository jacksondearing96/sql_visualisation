import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.intellij.lang.annotations.JdkConstants;
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

        // Anonymous table.
        LineageNode anonymousNode = new LineageNode("ANONYMOUS", "Anonymous0", "");
        Column anonymousColumn = new Column("a", "", "Anonymous0::a");
        anonymousColumn.addSource("b::a");
        anonymousNode.addColumn(anonymousColumn);

        // Source table.
        LineageNode sourceNode = new LineageNode("TABLE", "b", "");
        Column sourceColumn = new Column("a", "", "b::a");
        sourceNode.addColumn(sourceColumn);

        // Compare the pair.
        boolean success = true;
        success &= nodeList.get(1).equals(anonymousNode);
        success &= nodeList.get(0).equals(sourceNode);
        Assertions.assertTrue(success);
    }

    @Test
    @DisplayName("testBasicAnonymousTableGeneration")
    void testBasicAnonymousTableGeneration() {
        String statement = "select column1, column2, cast(someDate as date) as columnA from \"tableName\"";

        // Output table
        List<LineageNode> nodeList = LineageExtractor.extractLineage(statement).getNodeList();

        // Expected tables
        LineageNode table = new LineageNode("TABLE", "tableName", "");
        Column column1 = new Column("column1");
        Column column2 = new Column("column2");
        Column dateColumn = new Column("someDate");
        table.addListOfColumns(new ArrayList<>(Arrays.asList(column1, column2, dateColumn)));

        LineageNode anonymousTable = new LineageNode("ANONYMOUS", "Anonymous0", "");
        Column column1a = new Column("column1", "", "");
        column1a.setSources(new ArrayList<>(Arrays.asList("tableName::column1")));
        Column column2a = new Column("column2", "", "");
        column1a.setSources(new ArrayList<>(Arrays.asList("tableName::column2")));
        Column columnA = new Column("columnA", "", "");
        column1a.setSources(new ArrayList<>(Arrays.asList("tableName::someDate")));
        anonymousTable.addListOfColumns(new ArrayList<>(Arrays.asList(column1a, column2a, columnA)));

        // Compare the pair.
        boolean success = true;
        success &= nodeList.get(0).equals(table);
        success &= nodeList.get(1).equals(anonymousTable);
        Assertions.assertTrue(success);

    }

    @Test
    @DisplayName("testAliasForColumn")
    void testAliasForColumn() {
        String statement = "SELECT a AS b from c###";

        // Output
        List<LineageNode> nodeList = LineageExtractor.extractLineage(statement).getNodeList();

        // Expected tables.
        LineageNode table = new LineageNode("TABLE", "c", "");
        table.addColumn(new Column("a", "", "c::a"));

        LineageNode anonymousTable = new LineageNode("ANONYMOUS", "Anonymous0");
        Column aliasedColumn = new Column("a", "b", "Anonymous0::a");
        aliasedColumn.addSource("c::a");
        anonymousTable.addColumn(aliasedColumn);

        Assertions.assertTrue(nodeList.get(0).equals(table));
        Assertions.assertTrue(nodeList.get(1).equals(anonymousTable));
    }

    @Test
    @DisplayName("testAliasForTable")
    void testAliasForTable() {
        String statement = "SELECT a FROM b AS c###";

        // Output
        List<LineageNode> nodeList = LineageExtractor.extractLineage(statement).getNodeList();

        // Expected tables.
        LineageNode table = new LineageNode("TABLE", "b", "c");
        table.addColumn(new Column("a", "", "b::a"));

        LineageNode anonymousTable = new LineageNode("ANONYMOUS", "Anonymous0");
        Column aliasedColumn = new Column("a", "", "Anonymous0::a");
        anonymousTable.addColumn(aliasedColumn);
        aliasedColumn.addSource("b::a");

        Assertions.assertTrue(nodeList.get(0).equals(table));
        Assertions.assertTrue(nodeList.get(1).equals(anonymousTable));
    }

    @Test
    @DisplayName("testMultipleSelect")
    void testMultipleSelect() {
        String statement = "SELECT a, b FROM c###";

        // Output
        List<LineageNode> nodeList = LineageExtractor.extractLineage(statement).getNodeList();

        // Expected tables.
        LineageNode table = new LineageNode("TABLE", "c", "");
        table.addColumn(new Column("a", "", "c::a"));
        table.addColumn(new Column("b", "", "c::b"));

        LineageNode anonymousTable = new LineageNode("ANONYMOUS", "Anonymous0");
        Column columnA = new Column("a", "", "Anonymous0::a");
        columnA.addSource("c::a");
        Column columnB = new Column("b", "", "Anonymous0::b");
        columnB.addSource("c::b");
        anonymousTable.addColumn(columnA);
        anonymousTable.addColumn(columnB);

        Assertions.assertTrue(nodeList.get(0).equals(table));
        Assertions.assertTrue(nodeList.get(1).equals(anonymousTable));
    }

    @Test
    @DisplayName("testCreateView")
    void testCreateView() {
        String statement = "CREATE VIEW a AS SELECT b from c###";

        // Output
        List<LineageNode> nodeList = LineageExtractor.extractLineage(statement).getNodeList();

        // Expected tables.
        LineageNode table = new LineageNode("TABLE", "c", "");
        table.addColumn(new Column("b", "", "c::b"));

        LineageNode viewTable = new LineageNode("VIEW", "a");
        Column columnA = new Column("b", "", "c::b");
        columnA.addSource("c::b");
        viewTable.addColumn(columnA);

        Assertions.assertTrue(nodeList.get(0).equals(table));
        Assertions.assertTrue(nodeList.get(1).equals(viewTable));
    }

    @Test
    @DisplayName("testWildCardOperator")
    void testWildCardOperator() {
        String statement = "SELECT * from b###";

        // Output
        List<LineageNode> nodeList = LineageExtractor.extractLineage(statement).getNodeList();

        // Expected tables.
        LineageNode table = new LineageNode("TABLE", "b", "");
        LineageNode anonymousTable = new LineageNode("ANONYMOUS", "Anonymous0");
        Column columnA = new Column("*", "", "Anonymous0::*");
        columnA.addSource("");
        anonymousTable.addColumn(columnA);

        Assertions.assertTrue(nodeList.get(0).equals(table));
        Assertions.assertTrue(nodeList.get(1).equals(anonymousTable));
    }
}
