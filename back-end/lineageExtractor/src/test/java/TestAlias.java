import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

public class TestAlias {

    @Test
    @DisplayName("testAliasForColumn")
    void testAliasForColumn() {
        String statement = "SELECT a AS b from c###";

        // Output
        List<LineageNode> nodeList = LineageExtractor.extractLineageWithAnonymousTables(statement).getNodeList();

        // Expected tables.
        LineageNode table = new LineageNode(Constants.Node.TYPE_TABLE, "c");
        table.addColumn(new Column("a"));

        LineageNode anonymousTable = new LineageNode(Constants.Node.TYPE_ANON, Constants.Node.TYPE_ANON.concat("0"));
        Column aliasedColumn = new Column("b");
        aliasedColumn.addSource("c::a");
        anonymousTable.addColumn(aliasedColumn);

        Assertions.assertEquals(2, nodeList.size());
        Assertions.assertTrue(table.equals(nodeList.get(0)));
        Assertions.assertTrue(anonymousTable.equals(nodeList.get(1)));
    }

    @Test
    @DisplayName("testAliasForTable")
    void testAliasForTable() {
        String statement = "SELECT a FROM b AS c###";

        // Output
        List<LineageNode> nodeList = LineageExtractor.extractLineageWithAnonymousTables(statement).getNodeList();

        // Expected tables.
        LineageNode table = new LineageNode(Constants.Node.TYPE_TABLE, "b", "c");
        table.addColumn(new Column("a"));

        LineageNode anonymousTable = new LineageNode(Constants.Node.TYPE_ANON, Constants.Node.TYPE_ANON.concat("0"));
        Column aliasedColumn = new Column("a");
        aliasedColumn.addSource("b::a");
        anonymousTable.addColumn(aliasedColumn);

        Assertions.assertEquals(2, nodeList.size());
        Assertions.assertTrue(table.equals(nodeList.get(0)));
        Assertions.assertTrue(anonymousTable.equals(nodeList.get(1)));
    }

    @Test
    @DisplayName("testMultipleAliasesWithinSelectItem")
    void testMultipleAliasesWithinSelectItem() {
        String sql = "SELECT cast(a AS date) AS b FROM c###";
        List<LineageNode> nodeList = LineageExtractor.extractLineageWithAnonymousTables(sql).getNodeList();

        // Source table.
        LineageNode source = new LineageNode(Constants.Node.TYPE_TABLE, "c");
        source.addColumn(new Column("a"));

        // Anonymous table.
        LineageNode anonymous = new LineageNode(Constants.Node.TYPE_ANON, Constants.Node.TYPE_ANON.concat("0"));
        Column b = new Column("b");
        b.addSource("c::a");
        anonymous.addColumn(b);

        Assertions.assertEquals(2, nodeList.size());
        source.equals(nodeList.get(0));
        anonymous.equals(nodeList.get(1));
    }

}
