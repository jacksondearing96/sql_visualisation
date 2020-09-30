import org.junit.jupiter.api.*;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.List;

public class TestCreate {

    @Test
    @DisplayName("testCreateTable")
    public void testCreateTable() {
        System.out.println("first test create");
        String sql = "CREATE TABLE createdTable(" +
                "col1 varchar," +
                "col2 double" +
                ")###";
        List<LineageNode> nodeList = LineageExtractor.extractLineageWithAnonymousTables(sql).getNodeList();

        LineageNode createdTable = new LineageNode("TABLE", "createdtable");
        createdTable.addListOfColumns(Arrays.asList(new Column("col1"), new Column("col2")));

        Assertions.assertEquals(1, nodeList.size());
        createdTable.equals(nodeList.get(0));
    }

    @Test
    @DisplayName("testCreateTableAsSelect")
    void testCreateTableAsSelect() {
        String sql = "CREATE TABLE createdtable AS SELECT a, b FROM existingtable###";
        List<LineageNode> nodeList = LineageExtractor.extractLineage(sql).getNodeList();

        LineageNode existingTable = new LineageNode("TABLE", "existingtable");
        Column a = new Column("a");
        Column b = new Column("b");
        existingTable.addListOfColumns(Arrays.asList(a, b));

        LineageNode createdTable = new LineageNode("TABLE", "createdtable");
        a.addSource(DataLineage.makeId(existingTable.getName(), a.getName()));
        b.addSource(DataLineage.makeId(existingTable.getName(), b.getName()));
        createdTable.addListOfColumns(Arrays.asList(a, b));

        Assertions.assertEquals(2, nodeList.size());
        existingTable.equals(nodeList.get(0));
        createdTable.equals(nodeList.get(1));
    }

    @Test
    @DisplayName("testCreateView")
    void testCreateView() {
        String statement = "CREATE VIEW a AS SELECT b from c###";

        List<LineageNode> nodeList = LineageExtractor.extractLineageWithAnonymousTables(statement).getNodeList();

        // Source table.
        LineageNode table = new LineageNode(Constants.Node.TYPE_TABLE, "c");
        table.addColumn(new Column("b"));

        // View.
        LineageNode view = new LineageNode(Constants.Node.TYPE_VIEW, "a");
        Column columnA = new Column("b");
        columnA.addSource("c::b");
        view.addColumn(columnA);

        Assertions.assertEquals(2, nodeList.size());
        Assertions.assertTrue(table.equals(nodeList.get(0)));
        Assertions.assertTrue(view.equals(nodeList.get(1)));
    }
}
