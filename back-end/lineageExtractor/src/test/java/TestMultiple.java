import org.junit.jupiter.api.Assertions;
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
        Column a = new Column("a");
        Column b = new Column("b");
        Column d = new Column("d");
        myTable.addListOfColumns(Arrays.asList(a, b, d));

        // Expected anonymous table.
        LineageNode anonymousTable = new LineageNode(Constants.Node.TYPE_ANON);
        anonymousTable.setName(Constants.Node.TYPE_ANON.concat("0"));
        Column c = new Column("c");
        c.addListOfSources(Arrays.asList("mytable::a", "mytable::b"));
        d.addSource("mytable::d");
        anonymousTable.addListOfColumns(Arrays.asList(c, d));

        Assertions.assertEquals(2, nodeList.size(), "nodeList size");
        Assertions.assertTrue(myTable.equals(nodeList.get(0)));
        Assertions.assertTrue(anonymousTable.equals(nodeList.get(1)));

        // While we have the expected tables constructed, test more statements with the
        // same expected output
        // with variations to the SQL syntax.
        multipleIdentifiersSelectStatement = "select someFunction(a, b) as c, d from mytable###";
        nodeList = LineageExtractor.extractLineageWithAnonymousTables(multipleIdentifiersSelectStatement).getNodeList();

        Assertions.assertTrue(LineageNode.areNodeListsEqual(Arrays.asList(myTable, anonymousTable), nodeList));
    }

    @Test
    @DisplayName("testMultipleSelect")
    public void testMultipleSelect() {
        String statement = "SELECT a, b FROM c###";

        // Output
        List<LineageNode> nodeList = LineageExtractor.extractLineageWithAnonymousTables(statement).getNodeList();

        // Expected tables.
        LineageNode table = new LineageNode(Constants.Node.TYPE_TABLE, "c");
        table.addColumn(new Column("a"));
        table.addColumn(new Column("b"));

        LineageNode anonymousTable = new LineageNode(Constants.Node.TYPE_ANON, Constants.Node.TYPE_ANON.concat("0"));
        Column columnA = new Column("a");
        columnA.addSource("c::a");
        Column columnB = new Column("b");
        columnB.addSource("c::b");
        anonymousTable.addListOfColumns(Arrays.asList(columnA, columnB));

        Assertions.assertTrue(LineageNode.areNodeListsEqual(Arrays.asList(table, anonymousTable), nodeList));
    }

    @Test
    @DisplayName("testMultipleStatements")
    public void testMultipleStatements() {
        String multipleStatements = "SELECT a FROM b### SELECT c FROM d###";
        List<LineageNode> nodeList = LineageExtractor.extractLineageWithAnonymousTables(multipleStatements)
                .getNodeList();

        // Source table (first statement).
        LineageNode firstSource = new LineageNode(Constants.Node.TYPE_TABLE, "b");
        Column a = new Column("a");
        firstSource.addColumn(a);

        // Anonymous table (first statement).
        LineageNode firstAnonymous = new LineageNode(Constants.Node.TYPE_ANON, Constants.Node.TYPE_ANON.concat("0"));
        a.addSource("b::a");
        firstAnonymous.addColumn(a);

        // Source table (second statement).
        LineageNode secondSource = new LineageNode(Constants.Node.TYPE_TABLE, "d");
        Column c = new Column("c");
        secondSource.addColumn(c);

        // Anonymous table (second statement).
        LineageNode secondAnonymous = new LineageNode(Constants.Node.TYPE_ANON, Constants.Node.TYPE_ANON.concat("1"));
        c.addSource("d::c");
        secondAnonymous.addColumn(c);

        Assertions.assertTrue(LineageNode.areNodeListsEqual(
                Arrays.asList(firstSource, firstAnonymous, secondSource, secondAnonymous), nodeList));
    }

    @Test
    @DisplayName("testMultipleReferences")
    public void testMultipleReferences() {
        String multipleReferences = "SELECT a FROM b### SELECT c FROM b###";
        List<LineageNode> nodeList = LineageExtractor.extractLineageWithAnonymousTables(multipleReferences)
                .getNodeList();

        // Source table (both statements).
        LineageNode source = new LineageNode(Constants.Node.TYPE_TABLE, "b");
        Column a = new Column("a");
        Column c = new Column("c");
        source.addListOfColumns(Arrays.asList(a, c));

        // Anonymous table (first statement).
        LineageNode firstAnonymous = new LineageNode(Constants.Node.TYPE_ANON, Constants.Node.TYPE_ANON.concat("0"));
        a.addSource("b::a");
        firstAnonymous.addColumn(a);

        // Anonymous table (second statement).
        LineageNode secondAnonymous = new LineageNode(Constants.Node.TYPE_ANON, Constants.Node.TYPE_ANON.concat("1"));
        c.addSource("b::c");
        secondAnonymous.addColumn(c);

        Assertions.assertTrue(LineageNode.areNodeListsEqual(Arrays.asList(source, firstAnonymous, secondAnonymous), nodeList));
    }

    @Test
    @DisplayName("testMultipleSources")
    public void testMultipleSources() {
        String sql = "SELECT table1.a, table2.b " + "FROM table1 INNER JOIN table2 ON 1 = 1### ";
        List<LineageNode> nodeList = LineageExtractor.extractLineageWithAnonymousTables(sql).getNodeList();

        // Source table1.
        LineageNode table1 = new LineageNode(Constants.Node.TYPE_TABLE, "table1");
        Column a = new Column("a");
        table1.addColumn(a);

        // Source table2.
        LineageNode table2 = new LineageNode(Constants.Node.TYPE_TABLE, "table2");
        Column b = new Column("b");
        table2.addColumn(b);

        // Anonymous table.
        LineageNode anonymous = new LineageNode(Constants.Node.TYPE_ANON, Constants.Node.TYPE_ANON.concat("0"));
        a.addSource("table1::a");
        b.addSource("table2::b");
        anonymous.addListOfColumns(Arrays.asList(a, b));

        Assertions.assertTrue(LineageNode.areNodeListsEqual(Arrays.asList(table1, table2, anonymous), nodeList));
    }
}
