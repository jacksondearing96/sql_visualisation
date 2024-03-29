import org.junit.jupiter.api.DisplayName;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

public class TestAlias {

    @Test
    @DisplayName("testAliasForColumn")
    public void testAliasForColumn() {
        String statement = "SELECT a AS b from c###";

        // Output
        List<LineageNode> nodeList = LineageExtractor.extractLineageWithAnonymousTables(statement).getNodeList();

        // Expected tables.
        LineageNode table = new LineageNode(Constants.Node.TYPE_TABLE, "c");
        table.addColumn(new Column("a"));

        LineageNode anonymousTable = new LineageNode(Constants.Node.TYPE_ANON, Constants.Node.TYPE_ANON.concat("0"));
        anonymousTable.addColumn(new Column("b", "c::a"));

        LineageNode.testNodeListEquivalency(Arrays.asList(table, anonymousTable), nodeList);
    }

    @Test
    @DisplayName("testAliasForTable")
    public void testAliasForTable() {
        String statement = "SELECT a FROM b AS c###";

        // Output
        List<LineageNode> nodeList = LineageExtractor.extractLineageWithAnonymousTables(statement).getNodeList();

        // Expected tables.
        LineageNode table = new LineageNode(Constants.Node.TYPE_TABLE, "b", "c");
        table.addColumn(new Column("a"));

        LineageNode anonymousTable = new LineageNode(Constants.Node.TYPE_ANON, Constants.Node.TYPE_ANON.concat("0"));
        anonymousTable.addColumn(new Column("a", "b::a"));

        LineageNode.testNodeListEquivalency(Arrays.asList(table, anonymousTable), nodeList);
    }

    @Test
    @DisplayName("testMultipleAliasesWithinSelectItem")
    public void testMultipleAliasesWithinSelectItem() {
        String sql = "SELECT cast(a AS date) AS b FROM c###";
        List<LineageNode> nodeList = LineageExtractor.extractLineageWithAnonymousTables(sql).getNodeList();

        // Source table.
        LineageNode source = new LineageNode(Constants.Node.TYPE_TABLE, "c");
        source.addColumn(new Column("a"));

        // Anonymous table.
        LineageNode anonymous = new LineageNode(Constants.Node.TYPE_ANON, Constants.Node.TYPE_ANON.concat("0"));
        anonymous.addColumn(new Column("b", "c::a"));

        LineageNode.testNodeListEquivalency(Arrays.asList(source, anonymous), nodeList);
    }

}
