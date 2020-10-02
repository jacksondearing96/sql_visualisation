import org.junit.jupiter.api.DisplayName;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

public class TestMultiple {

    @Test
    @DisplayName("testMultipleIdentifiers")
    public void testMultipleIdentifiers() {
        String multipleIdentifiersSelectStatement = "select a * b as c, d from mytable###";
        List<LineageNode> nodeList = LineageExtractor
                .extractLineageWithAnonymousTables(multipleIdentifiersSelectStatement).getNodeList();

        // Expected source table.
        LineageNode myTable = new LineageNode(Constants.Node.TYPE_TABLE, "mytable");
        myTable.addListOfColumns(Column.arrayToColumns(Arrays.asList("a", "b", "d")));

        // Expected anonymous table.
        LineageNode anonymousTable = new LineageNode(Constants.Node.TYPE_ANON, Constants.Node.TYPE_ANON.concat("0"));
        anonymousTable.addListOfColumns(Column.arrayToColumns(
                Arrays.asList("c", "d"), Arrays.asList("mytable::a,mytable::b", "mytable::d")));
        LineageNode.testNodeListEquivalency(Arrays.asList(myTable, anonymousTable), nodeList);

        // While we have the expected tables constructed, test more statements with the
        // same expected output
        // with variations to the SQL syntax.
        multipleIdentifiersSelectStatement = "select someFunction(a, b) as c, d from mytable###";
        nodeList = LineageExtractor.extractLineageWithAnonymousTables(multipleIdentifiersSelectStatement).getNodeList();

        LineageNode.testNodeListEquivalency(Arrays.asList(myTable, anonymousTable), nodeList);
    }

    @Test
    @DisplayName("testMultipleSelect")
    public void testMultipleSelect() {
        String statement = "SELECT a, b FROM c###";

        // Output
        List<LineageNode> nodeList = LineageExtractor.extractLineageWithAnonymousTables(statement).getNodeList();

        // Expected tables.
        LineageNode table = new LineageNode(Constants.Node.TYPE_TABLE, "c");
        table.addListOfColumns(Column.arrayToColumns(Arrays.asList("a", "b")));

        LineageNode anonymousTable = new LineageNode(Constants.Node.TYPE_ANON, Constants.Node.TYPE_ANON.concat("0"));
        anonymousTable.addListOfColumns(Column.arrayToColumns(Arrays.asList("a", "b"), Arrays.asList("c::a", "c::b")));

        LineageNode.testNodeListEquivalency(Arrays.asList(table, anonymousTable), nodeList);
    }

    @Test
    @DisplayName("testMultipleStatements")
    public void testMultipleStatements() {
        String multipleStatements = "SELECT a FROM b### SELECT c FROM d###";
        List<LineageNode> nodeList = LineageExtractor.extractLineageWithAnonymousTables(multipleStatements)
                .getNodeList();

        // Source table (first statement).
        LineageNode firstSource = new LineageNode(Constants.Node.TYPE_TABLE, "b");
        firstSource.addColumn(new Column("a"));

        // Anonymous table (first statement).
        LineageNode firstAnonymous = new LineageNode(Constants.Node.TYPE_ANON, Constants.Node.TYPE_ANON.concat("0"));
        firstAnonymous.addColumn(new Column("a", "b::a"));

        // Source table (second statement).
        LineageNode secondSource = new LineageNode(Constants.Node.TYPE_TABLE, "d");
        secondSource.addColumn(new Column("c"));

        // Anonymous table (second statement).
        LineageNode secondAnonymous = new LineageNode(Constants.Node.TYPE_ANON, Constants.Node.TYPE_ANON.concat("1"));
        secondAnonymous.addColumn(new Column("c", "d::c"));

        LineageNode.testNodeListEquivalency(Arrays.asList(firstSource, firstAnonymous, secondSource, secondAnonymous), nodeList);
    }

    @Test
    @DisplayName("testMultipleReferences")
    public void testMultipleReferences() {
        String multipleReferences = "SELECT a FROM b### SELECT c FROM b###";
        List<LineageNode> nodeList = LineageExtractor.extractLineageWithAnonymousTables(multipleReferences)
                .getNodeList();

        // Source table (both statements).
        LineageNode source = new LineageNode(Constants.Node.TYPE_TABLE, "b");
        source.addListOfColumns(Column.arrayToColumns(Arrays.asList("a", "c")));

        // Anonymous table (first statement).
        LineageNode firstAnonymous = new LineageNode(Constants.Node.TYPE_ANON, Constants.Node.TYPE_ANON.concat("0"));
        firstAnonymous.addColumn(new Column("a", "b::a"));

        // Anonymous table (second statement).
        LineageNode secondAnonymous = new LineageNode(Constants.Node.TYPE_ANON, Constants.Node.TYPE_ANON.concat("1"));
        secondAnonymous.addColumn(new Column("c", "b::c"));

        LineageNode.testNodeListEquivalency(Arrays.asList(source, firstAnonymous, secondAnonymous), nodeList);
    }

    @Test
    @DisplayName("testMultipleSources")
    public void testMultipleSources() {
        String sql = "SELECT table1.a, table2.b " + "FROM table1 INNER JOIN table2 ON 1 = 1### ";
        List<LineageNode> nodeList = LineageExtractor.extractLineageWithAnonymousTables(sql).getNodeList();

        // Source table1.
        LineageNode table1 = new LineageNode(Constants.Node.TYPE_TABLE, "table1");
       table1.addColumn(new Column("a"));

        // Source table2.
        LineageNode table2 = new LineageNode(Constants.Node.TYPE_TABLE, "table2");
        table2.addColumn(new Column("b"));

        // Anonymous table.
        LineageNode anonymous = new LineageNode(Constants.Node.TYPE_ANON, Constants.Node.TYPE_ANON.concat("0"));
        anonymous.addListOfColumns(Column.arrayToColumns(Arrays.asList("a", "b"), Arrays.asList("table1::a", "table2::b")));

        LineageNode.testNodeListEquivalency(Arrays.asList(table1, table2, anonymous), nodeList);
    }
}
