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
        column.addListOfSources(Arrays.asList("source4", "source5"));
        column.addListOfSources(sources);
        column.addSource("source1");
        Assertions.assertEquals(
                "alias=newAlias,id=newID,name=newName,sources={source1,source2,source3,source4,source5}",
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
    @DisplayName("testMultipleIdentifiers")
    void testMultipleIdentifiers() {
        String multipleIdentifiersSelectStatement = "select a * b as c, d from mytable###";
        List<LineageNode> nodeList = LineageExtractor.extractLineageWithAnonymousTables(multipleIdentifiersSelectStatement).getNodeList();

        // Expected source table.
        LineageNode myTable = new LineageNode("TABLE", "mytable");
        Column a = new Column("a");
        Column b = new Column("b");
        Column d = new Column("d");
        myTable.addListOfColumns(new ArrayList<>(Arrays.asList(a, b, d)));

        // Expected anonymous table.
        LineageNode anonymousTable = new LineageNode("ANONYMOUS");
        anonymousTable.setName("Anonymous0");
        Column c = new Column("c");
        c.addListOfSources(new ArrayList<>(Arrays.asList("mytable::a", "mytable::b")));
        d.addSource("mytable::d");
        anonymousTable.addListOfColumns(new ArrayList<>(Arrays.asList(c, d)));

        Assertions.assertEquals(2, nodeList.size(), "nodeList size");
        Assertions.assertTrue(myTable.equals(nodeList.get(0)));
        Assertions.assertTrue(anonymousTable.equals(nodeList.get(1)));

        // While we have the expected tables constructed, test more statements with the same expected output
        // with variations to the SQL syntax.
        multipleIdentifiersSelectStatement = "select someFunction(a, b) as c, d from mytable###";
        nodeList = LineageExtractor.extractLineageWithAnonymousTables(multipleIdentifiersSelectStatement).getNodeList();
        Assertions.assertEquals(2, nodeList.size(), "nodeList size");
        Assertions.assertTrue(myTable.equals(nodeList.get(0)));
        Assertions.assertTrue(anonymousTable.equals(nodeList.get(1)));
    }

    @Test
    @DisplayName("testLineageNodes")
    void testLineageNodes() {
        String simpleSelect = "SELECT a FROM b###";
        List<LineageNode> nodeList = LineageExtractor.extractLineageWithAnonymousTables(simpleSelect).getNodeList();

        // Source table.
        LineageNode sourceNode = new LineageNode("TABLE", "b");
        Column a = new Column("a");
        sourceNode.addColumn(a);

        // Anonymous table.
        LineageNode anonymousNode = new LineageNode("ANONYMOUS", "Anonymous0");
        a.addSource("b::a");
        anonymousNode.addColumn(a);

        // Compare the pair.
        Assertions.assertEquals(2, nodeList.size());
        Assertions.assertTrue(anonymousNode.equals(nodeList.get(1)));
        Assertions.assertTrue(sourceNode.equals(nodeList.get(0)));
    }

    @Test
    @DisplayName("testBasicAnonymousTableGeneration")
    void testBasicAnonymousTableGeneration() {
        String statement = "select column1, column2, cast(someDate as date) as columnA from \"tablename\"###";

        // Output table
        List<LineageNode> nodeList = LineageExtractor.extractLineageWithAnonymousTables(statement).getNodeList();

        // Expected tables
        LineageNode table = new LineageNode("TABLE", "tablename");
        Column column1 = new Column("column1");
        Column column2 = new Column("column2");
        Column dateColumn = new Column("someDate");
        table.addListOfColumns(Arrays.asList(column1, column2, dateColumn));

        LineageNode anonymousTable = new LineageNode("ANONYMOUS", "Anonymous0");
        Column column1a = new Column("column1");
        Column column2a = new Column("column2");
        Column columnA = new Column("columnA");
        column1a.addSource("tablename::column1");
        column2a.addSource("tablename::column2");
        columnA.addSource("tablename::someDate");
        anonymousTable.addListOfColumns(Arrays.asList(column1a, column2a, columnA));

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

    @DisplayName("testMultipleStatements")
    void testMultipleStatements() {
        String multipleStatements = "SELECT a FROM b### SELECT c FROM d###";
        List<LineageNode> nodeList = LineageExtractor.extractLineageWithAnonymousTables(multipleStatements).getNodeList();

        // Source table (first statement).
        LineageNode firstSource = new LineageNode("TABLE", "b");
        Column a = new Column("a");
        firstSource.addColumn(a);

        // Anonymous table (first statement).
        LineageNode firstAnonymous = new LineageNode("ANONYMOUS", "Anonymous0");
        a.addSource("b::a");
        firstAnonymous.addColumn(a);

        // Source table (second statement).
        LineageNode secondSource = new LineageNode("TABLE", "d");
        Column c = new Column("c");
        secondSource.addColumn(c);

        // Anonymous table (second statement).
        LineageNode secondAnonymous = new LineageNode("ANONYMOUS", "Anonymous1");
        c.addSource("d::c");
        secondAnonymous.addColumn(c);

        Assertions.assertEquals(4, nodeList.size());
        Assertions.assertTrue(firstSource.equals(nodeList.get(0)));
        Assertions.assertTrue(firstAnonymous.equals(nodeList.get(1)));
        Assertions.assertTrue(secondSource.equals(nodeList.get(2)));
        Assertions.assertTrue(secondAnonymous.equals(nodeList.get(3)));
    }

    @Test
    @DisplayName("testMultipleReferences")
    void testMultipleReferences() {
        String multipleReferences = "SELECT a FROM b### SELECT c FROM b###";
        List<LineageNode> nodeList = LineageExtractor.extractLineageWithAnonymousTables(multipleReferences).getNodeList();

        // Source table (both statements).
        LineageNode source = new LineageNode("TABLE", "b");
        Column a = new Column("a");
        Column c = new Column("c");
        source.addListOfColumns(Arrays.asList(a, c));

        // Anonymous table (first statement).
        LineageNode firstAnonymous = new LineageNode("ANONYMOUS", "Anonymous0");
        a.addSource("b::a");
        firstAnonymous.addColumn(a);

        // Anonymous table (second statement).
        LineageNode secondAnonymous = new LineageNode("ANONYMOUS", "Anonymous1");
        c.addSource("b::c");
        secondAnonymous.addColumn(c);

        Assertions.assertEquals(3, nodeList.size());
        Assertions.assertTrue(source.equals(nodeList.get(0)));
        Assertions.assertTrue(firstAnonymous.equals(nodeList.get(1)));
        Assertions.assertTrue(secondAnonymous.equals(nodeList.get(2)));
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
