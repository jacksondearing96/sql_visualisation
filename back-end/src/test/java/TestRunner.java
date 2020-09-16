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
    @DisplayName("testMultipleStatements")
    void testMultipleStatements() {
        String multipleStatements = "SELECT a FROM b### SELECT c FROM d###";
        List<LineageNode> nodeList = LineageExtractor.extractLineage(multipleStatements).getNodeList();

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

        for (LineageNode node : nodeList) {
            PrettyPrinter.printLineageNode(node);
        }

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
        List<LineageNode> nodeList = LineageExtractor.extractLineage(multipleReferences).getNodeList();

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
}
