import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

public class TestMisc {
    /**
     *  Todo: Organise below methods
     */

    @Test
    @DisplayName("testNumericSelectValues")
    public void testNumericSelectValues() {
        String numericSelectValues = "SELECT 1 as one FROM a###";
        List<LineageNode> nodeList = LineageExtractor.extractLineageWithAnonymousTables(numericSelectValues)
                .getNodeList();

        // Source table (no columns).
        LineageNode sourceTable = new LineageNode(Constants.Node.TYPE_TABLE, "a");

        // Anonymous table.
        LineageNode anonymousTable = new LineageNode(Constants.Node.TYPE_ANON, Constants.Node.TYPE_ANON.concat("0"));
        anonymousTable.addColumn(new Column("one"));

        LineageNode.testNodeListEquivalency(Arrays.asList(sourceTable, anonymousTable), nodeList);
    }

    @Test
    @DisplayName("testFunctionCall")
    public void testFunctionCall() {
        String sql = "SELECT someFunction(a) AS b FROM c###";
        List<LineageNode> nodeList = LineageExtractor.extractLineageWithAnonymousTables(sql).getNodeList();

        // Source table.
        LineageNode source = new LineageNode(Constants.Node.TYPE_TABLE, "c");
        source.addColumn(new Column("a"));

        // Anonymous table.
        LineageNode anonymous = new LineageNode(Constants.Node.TYPE_ANON, Constants.Node.TYPE_ANON.concat("0"));
        Column b = new Column("b");
        b.addSource("c::a");
        anonymous.addColumn(b);

        LineageNode.testNodeListEquivalency(Arrays.asList(source, anonymous), nodeList);
    }

    @Test
    @DisplayName("testSubquery")
    public void testSubquery() {
        String sql = "SELECT a FROM (\n" + "SELECT b FROM c\n" + ")###\n";
        List<LineageNode> nodeList = LineageExtractor.extractLineageWithAnonymousTables(sql).getNodeList();

        // Table c.
        LineageNode tableC = new LineageNode(Constants.Node.TYPE_TABLE, "c");
        Column b = new Column("b");
        tableC.addColumn(b);

        // Inner-most anonymous table.
        LineageNode anonymous0 = new LineageNode(Constants.Node.TYPE_ANON, Constants.Node.TYPE_ANON.concat("0"));
        b.addSource("c::b");
        anonymous0.addColumn(b);

        // Outer-most anonymous table.
        LineageNode anonymous1 = new LineageNode(Constants.Node.TYPE_ANON, Constants.Node.TYPE_ANON.concat("1"));
        Column a = new Column("a");
        anonymous0.addColumn(a);
        a.addSource("ANONYMOUS0::a");
        anonymous1.addColumn(a);

        LineageNode.testNodeListEquivalency(Arrays.asList(tableC, anonymous0, anonymous1), nodeList);
    }

    @Test
    @DisplayName("testRenameTable")
    public void testRenameTable() {
        String sql = "ALTER TABLE mytable RENAME TO newname###";
        List<LineageNode> nodeList = LineageExtractor.extractLineage(sql).getNodeList();

        LineageNode table = new LineageNode(Constants.Node.TYPE_TABLE, "newname");

        LineageNode.testNodeListEquivalency(Arrays.asList(table), nodeList);
    }

    @Test
    @DisplayName("testTopLevelNodeIds")
    public void testTopLevelNodeIds() {
        LineageNode node = new LineageNode(Constants.Node.TYPE_TABLE, "node");
        Assertions.assertEquals("node::", node.getID());

        node.setName("newname");
        Assertions.assertEquals("newname::", node.getID());
    }
}
