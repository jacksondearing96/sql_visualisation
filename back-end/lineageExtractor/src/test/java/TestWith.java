import checkers.units.quals.A;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

public class TestWith {

    @Test
    @DisplayName("testWithClause")
    public void testWithClause() {
        String sql = "WITH withtable AS (" +
                "SELECT a, b FROM existingtable" +
                ")" +
                "SELECT a FROM withtable###";
        List<LineageNode> nodeList = LineageExtractor.extractLineageWithAnonymousTables(sql).getNodeList();

        LineageNode existingTable = new LineageNode(Constants.Node.TYPE_TABLE, "existingtable");
        existingTable.addListOfColumns(Column.arrayToColumns(Arrays.asList("a", "b")));

        LineageNode withTable = new LineageNode(Constants.Node.TYPE_ANON, Constants.Node.TYPE_ANON.concat("0"), "withtable");
        withTable.addListOfColumns(Column.arrayToColumns(Arrays.asList("a", "b"), Arrays.asList("existingtable::a", "existingtable::b")));

        LineageNode resultantTable = new LineageNode(Constants.Node.TYPE_ANON, Constants.Node.TYPE_ANON.concat("1"));
        resultantTable.addColumn(new Column("a", Constants.Node.TYPE_ANON.concat("0::a")));

        LineageNode.testNodeListEquivalency(Arrays.asList(existingTable, withTable, resultantTable), nodeList);
    }

    @Test
    @DisplayName("testMultipleWithClause")
    public void testMultipleWithClause() {
        String sql = "WITH withtable1 AS (" +
                "SELECT a, b FROM existingtable1" +
                "), " +
                "withtable2 AS (" +
                "SELECT c, d FROM existingtable2" +
                ") " +
                "SELECT withtable1.a, withtable2.c " +
                "FROM withtable1 INNER JOIN withtable2 ON 1 = 1###";
        List<LineageNode> nodeList = LineageExtractor.extractLineageWithAnonymousTables(sql).getNodeList();

        LineageNode existingTable1 = new LineageNode(Constants.Node.TYPE_TABLE, "existingtable1");
        existingTable1.addListOfColumns(Column.arrayToColumns(Arrays.asList("a", "b")));

        LineageNode existingTable2 = new LineageNode(Constants.Node.TYPE_TABLE, "existingtable2");
        existingTable2.addListOfColumns(Column.arrayToColumns(Arrays.asList("c", "d")));

        LineageNode withTable1 = new LineageNode(Constants.Node.TYPE_ANON, Constants.Node.TYPE_ANON.concat("0"), "withtable1");
        withTable1.addListOfColumns(Column.arrayToColumns(Arrays.asList("a", "b"),
                Arrays.asList("existingtable1::a", "existingtable1::b")));

        LineageNode withTable2 = new LineageNode(Constants.Node.TYPE_ANON, Constants.Node.TYPE_ANON.concat("1"), "withtable2");
        withTable2.addListOfColumns(Column.arrayToColumns(Arrays.asList("c", "d"),
                Arrays.asList("existingtable2::c", "existingtable2::d")));

        LineageNode resultantTable = new LineageNode(Constants.Node.TYPE_ANON, Constants.Node.TYPE_ANON.concat("2"));
        resultantTable.addListOfColumns(Column.arrayToColumns(Arrays.asList("a", "c"),
            Arrays.asList(Constants.Node.TYPE_ANON.concat("0::a"), Constants.Node.TYPE_ANON.concat("1::c"))));


        LineageNode.testNodeListEquivalency(Arrays.asList(existingTable1, existingTable2, withTable1, withTable2, resultantTable), nodeList);
    }

    @Test
    @DisplayName("testMultipleWithClauseEarlyReference")
    public void testMultipleWithClauseEarlyReference() {
        String sql = "CREATE VIEW myview AS " +
                "WITH withtable1 AS (" +
                "SELECT a, b FROM existingtable1" +
                "), " +
                "withtable2 AS (" +
                "SELECT b AS c FROM withtable1" +
                ") " +
                "SELECT withtable1.a, withtable2.c " +
                "FROM withtable1 INNER JOIN withtable2 ON 1 = 1###";
        List<LineageNode> nodeList = LineageExtractor.extractLineage(sql).getNodeList();

        LineageNode existingTable1 = new LineageNode(Constants.Node.TYPE_TABLE, "existingtable1");
        existingTable1.addListOfColumns(Column.arrayToColumns(Arrays.asList("a", "b")));

        LineageNode view = new LineageNode(Constants.Node.TYPE_VIEW, "myview");
        view.addListOfColumns(Column.arrayToColumns(Arrays.asList("a", "c"), Arrays.asList("existingtable1::a", "existingtable1::b")));

        LineageNode.testNodeListEquivalency(Arrays.asList(existingTable1, view), nodeList);
    }

    @Test
    @DisplayName("testBypassAnonymousWithTable")
    public void testBypassAnonymousWithTable() {
        String sql = "CREATE OR REPLACE VIEW view1 AS " +
                "WITH withtable AS (" +
                "SELECT a, b FROM existingtable" +
                ")" +
                "SELECT a FROM withtable###";
        List<LineageNode> nodeList = LineageExtractor.extractLineage(sql).getNodeList();

        LineageNode existingTable = new LineageNode(Constants.Node.TYPE_TABLE, "existingtable");
        existingTable.addListOfColumns(Column.arrayToColumns(Arrays.asList("a", "b")));

        LineageNode view = new LineageNode(Constants.Node.TYPE_VIEW, "view1");
        view.addColumn(new Column("a", "existingtable::a"));

        LineageNode.testNodeListEquivalency(Arrays.asList(existingTable, view), nodeList);
    }

}
