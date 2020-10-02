import org.junit.jupiter.api.DisplayName;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

public class TestInsert {

    @Test
    @DisplayName("testInsertStatement")
    public void testInsertStatement() {
        String sql = "INSERT INTO existingTable VALUES a###";
        List<LineageNode> nodeList = LineageExtractor.extractLineageWithAnonymousTables(sql).getNodeList();

        // The only table that will be derived from this.
        LineageNode existingTable = new LineageNode("TABLE", "existingtable");

        LineageNode.testNodeListEquivalency(Arrays.asList(existingTable), nodeList);
    }

    @Test
    @DisplayName("testInsertFromSelect")
    public void testInsertFromSelect() {
        String sql = "INSERT INTO existingTable SELECT col FROM a###";
        List<LineageNode> nodeList = LineageExtractor.extractLineageWithAnonymousTables(sql).getNodeList();

        // Source table a.
        LineageNode sourceA = new LineageNode("TABLE", "a");
        Column col = new Column("col");
        sourceA.addColumn(col);

        // Anonymous table from select statement.
        LineageNode anonymous = new LineageNode(Constants.Node.TYPE_ANON, Constants.Node.TYPE_ANON.concat("0"));
        anonymous.addColumn(new Column("*", "a::*"));

        // The existing table that is having values inserted.
        LineageNode existingTable = new LineageNode("TABLE", "existingtable");
        existingTable.addColumn(new Column("*", Constants.Node.TYPE_ANON.concat("0::*")));

        LineageNode.testNodeListEquivalency(Arrays.asList(sourceA, anonymous, existingTable), nodeList);
    }

    @Test
    @DisplayName("testInsertWithListedColumnsAndInlineLiteral")
    public void testInsertWithListedColumnsAndInlineLiteral() {
        String sql = "INSERT INTO existingTable (a, b, c) VALUES d, e, f###";
        List<LineageNode> nodeList = LineageExtractor.extractLineageWithAnonymousTables(sql).getNodeList();

        // The existing table that is having values inserted.
        LineageNode existingTable = new LineageNode("TABLE", "existingtable");
        existingTable.addListOfColumns(Arrays.asList(new Column("a"), new Column("b"), new Column("c")));

        LineageNode.testNodeListEquivalency(Arrays.asList(existingTable), nodeList);
    }

    @Test
    @DisplayName("testInsertWithListedColumnsAndSelect")
    public void testInsertWithListedColumnsAndSelect() {
        String sql = "INSERT INTO existingTable (a, b, c) SELECT d, e, f FROM sourceTable###";
        List<LineageNode> nodeList = LineageExtractor.extractLineageWithAnonymousTables(sql).getNodeList();

        // Source table.
        LineageNode sourceTable = new LineageNode("TABLE", "sourcetable");
        sourceTable.addListOfColumns(Column.arrayToColumns(Arrays.asList("d", "e", "f")));

        // Anonymous table from select statement.
        LineageNode anonymous = new LineageNode(Constants.Node.TYPE_ANON, Constants.Node.TYPE_ANON.concat("0"));
        anonymous.addListOfColumns(Column.arrayToColumns(
            Arrays.asList("d", "e", "f"), Arrays.asList("sourcetable::d", "sourcetable::e", "sourcetable::f")));

        // The existing table that is having values inserted.
        LineageNode existingTable = new LineageNode("TABLE", "existingtable");
        existingTable.addListOfColumns(Column.arrayToColumns(
            Arrays.asList("a", "b", "c"),
            Arrays.asList(
                Constants.Node.TYPE_ANON.concat("0::d"),
                Constants.Node.TYPE_ANON.concat("0::e"),
                Constants.Node.TYPE_ANON.concat("0::f"))
        ));

        LineageNode.testNodeListEquivalency(Arrays.asList(sourceTable, anonymous, existingTable), nodeList);
    }

}
