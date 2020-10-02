import org.junit.jupiter.api.*;

import java.util.Arrays;
import java.util.List;
import org.junit.Test;

public class TestCreate {

    @Test
    @DisplayName("testCreateTable")
    public void testCreateTable() {
        String sql = "CREATE TABLE createdTable(" +
                "col1 varchar," +
                "col2 double" +
                ")###";
        List<LineageNode> nodeList = LineageExtractor.extractLineageWithAnonymousTables(sql).getNodeList();

        LineageNode createdTable = new LineageNode("TABLE", "createdtable");
        createdTable.addListOfColumns(Column.arrayToColumns(Arrays.asList("col1", "col2")));

        LineageNode.testNodeListEquivalency(Arrays.asList(createdTable), nodeList);
    }

    @Test
    @DisplayName("testCreateTableAsSelect")
    public void testCreateTableAsSelect() {
        String sql = "CREATE TABLE createdtable AS SELECT a, b FROM existingtable###";
        List<LineageNode> nodeList = LineageExtractor.extractLineage(sql).getNodeList();

        LineageNode existingTable = new LineageNode("TABLE", "existingtable");
        existingTable.addListOfColumns(Column.arrayToColumns(Arrays.asList("a", "b")));

        LineageNode createdTable = new LineageNode("TABLE", "createdtable");
        createdTable.addListOfColumns(Column.arrayToColumns(
            Arrays.asList("a", "b"), Arrays.asList("existingtable::a", "existingtable::b")));

        LineageNode.testNodeListEquivalency(Arrays.asList(existingTable, createdTable), nodeList);
    }

    @Test
    @DisplayName("testCreateView")
    public void testCreateView() {
        String statement = "CREATE VIEW a AS SELECT b from c###";

        List<LineageNode> nodeList = LineageExtractor.extractLineageWithAnonymousTables(statement).getNodeList();

        // Source table.
        LineageNode table = new LineageNode(Constants.Node.TYPE_TABLE, "c");
        table.addColumn(new Column("b"));

        // View.
        LineageNode view = new LineageNode(Constants.Node.TYPE_VIEW, "a");
        view.addColumn(new Column("b", "c::b"));

        LineageNode.testNodeListEquivalency(Arrays.asList(table, view), nodeList);
    }
}
