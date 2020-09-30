import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

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
        Column a = new Column("a");
        Column b = new Column("b");
        existingTable.addListOfColumns(Arrays.asList(a, b));

        LineageNode withTable = new LineageNode(Constants.Node.TYPE_ANON, Constants.Node.TYPE_ANON.concat("0"), "withtable");
        a.addSource(DataLineage.makeId(existingTable.getName(), a.getName()));
        b.addSource(DataLineage.makeId(existingTable.getName(), b.getName()));
        withTable.addListOfColumns(Arrays.asList(a, b));

        LineageNode resultantTable = new LineageNode(Constants.Node.TYPE_ANON, Constants.Node.TYPE_ANON.concat("1"));
        a = new Column("a");
        a.addSource(DataLineage.makeId(withTable.getName(), a.getName()));
        resultantTable.addColumn(a);

        Assertions.assertEquals(3, nodeList.size());
        existingTable.equals(nodeList.get(0));
        withTable.equals(nodeList.get(1));
        resultantTable.equals(nodeList.get(2));
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
        Column a = new Column("a");
        Column b = new Column("b");
        existingTable1.addListOfColumns(Arrays.asList(a, b));

        LineageNode existingTable2 = new LineageNode(Constants.Node.TYPE_TABLE, "existingtable2");
        Column c = new Column("c");
        Column d = new Column("d");
        existingTable2.addListOfColumns(Arrays.asList(c, d));

        LineageNode withTable1 = new LineageNode(Constants.Node.TYPE_ANON, Constants.Node.TYPE_ANON.concat("0"), "withtable1");
        a.addSource(DataLineage.makeId(existingTable1.getName(), a.getName()));
        b.addSource(DataLineage.makeId(existingTable1.getName(), b.getName()));
        withTable1.addListOfColumns(Arrays.asList(a, b));

        LineageNode withTable2 = new LineageNode(Constants.Node.TYPE_ANON, Constants.Node.TYPE_ANON.concat("1"), "withtable2");
        c.addSource(DataLineage.makeId(existingTable2.getName(), c.getName()));
        d.addSource(DataLineage.makeId(existingTable2.getName(), d.getName()));
        withTable2.addListOfColumns(Arrays.asList(c, d));

        LineageNode resultantTable = new LineageNode(Constants.Node.TYPE_ANON, Constants.Node.TYPE_ANON.concat("2"));
        a = new Column("a");
        c = new Column("c");
        a.addSource(DataLineage.makeId(withTable1.getName(), a.getName()));
        c.addSource(DataLineage.makeId(withTable2.getName(), c.getName()));
        resultantTable.addListOfColumns(Arrays.asList(a, c));

        Assertions.assertEquals(5, nodeList.size());
        existingTable1.equals(nodeList.get(0));
        withTable1.equals(nodeList.get(1));
        existingTable2.equals(nodeList.get(2));
        withTable2.equals(nodeList.get(3));
        resultantTable.equals(nodeList.get(4));
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
        Column a = new Column("a");
        Column b = new Column("b");
        existingTable1.addListOfColumns(Arrays.asList(a, b));

        LineageNode view = new LineageNode(Constants.Node.TYPE_VIEW, "myview");
        a = new Column("a");
        Column c = new Column("c");
        a.addSource(DataLineage.makeId(existingTable1.getName(), a.getName()));
        c.addSource(DataLineage.makeId(existingTable1.getName(), b.getName()));
        view.addListOfColumns(Arrays.asList(a, c));

        Assertions.assertEquals(2, nodeList.size());
        existingTable1.equals(nodeList.get(0));
        view.equals(nodeList.get(1));
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
        Column a = new Column("a");
        Column b = new Column("b");
        existingTable.addListOfColumns(Arrays.asList(a, b));

        LineageNode view = new LineageNode(Constants.Node.TYPE_VIEW, "view1");
        a = new Column("a");
        a.addSource(DataLineage.makeId(existingTable.getName(), a.getName()));
        view.addColumn(a);

        Assertions.assertEquals(2, nodeList.size());
        existingTable.equals(nodeList.get(0));
        view.equals(nodeList.get(1));
    }

}
