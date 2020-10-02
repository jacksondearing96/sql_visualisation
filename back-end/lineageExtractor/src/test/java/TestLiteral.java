import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

public class TestLiteral {

    @Test
    @DisplayName("testStandAloneLiteralTable")
    public void testStandAloneLiteralTable() {
        String sql = "VALUES " + "(1, 'a')," + "(2, 'b')," + "(3, 'c')###";

        List<LineageNode> nodeList = LineageExtractor.extractLineageWithAnonymousTables(sql).getNodeList();

        Assertions.assertEquals(0, nodeList.size());
    }

    @Test
    @DisplayName("testLiteralInlineTable")
    public void testLiteralInlineTables() {
        String sql = "SELECT b FROM ( " + "VALUES " + "(1, 'a')," + "(2, 'b')," + "(3, 'c')" + ")###";

        List<LineageNode> nodeList = LineageExtractor.extractLineageWithAnonymousTables(sql).getNodeList();

        // Inline literal table.
        LineageNode inlineLiteral = new LineageNode(Constants.Node.TYPE_ANON, Constants.Node.TYPE_ANON.concat("0"));
        inlineLiteral.addColumn(new Column("b"));

        // Anonymous table (from select statement).
        LineageNode anonymous = new LineageNode(Constants.Node.TYPE_ANON, Constants.Node.TYPE_ANON.concat("1"));
        anonymous.addColumn(new Column("b", Constants.Node.TYPE_ANON.concat("0::b")));

        LineageNode.testNodeListEquivalency(Arrays.asList(inlineLiteral, anonymous), nodeList);
    }

    @Test
    @DisplayName("testLiteralInlineTableWithAlias")
    public void testLiteralInlineTablesWithAlias() {
        String sql = "SELECT b FROM ( " + "VALUES " + "(1, 'a')," + "(2, 'b')," + "(3, 'c')" + ") AS a###";

        List<LineageNode> nodeList = LineageExtractor.extractLineageWithAnonymousTables(sql).getNodeList();

        // Inline literal table.
        LineageNode inlineLiteral = new LineageNode(Constants.Node.TYPE_ANON, Constants.Node.TYPE_ANON.concat("0"), "a");
        inlineLiteral.addColumn(new Column("b"));

        // Anonymous table (from select statement).
        LineageNode anonymous = new LineageNode(Constants.Node.TYPE_ANON, Constants.Node.TYPE_ANON.concat("1"));
        anonymous.addColumn(new Column("b", Constants.Node.TYPE_ANON.concat("0::b")));

        LineageNode.testNodeListEquivalency(Arrays.asList(inlineLiteral, anonymous), nodeList);
    }

    @Test
    @DisplayName("testLiteralInlineTableWithAliasAndColumnLabels")
    public void testLiteralInlineTablesWithAliasAndColumnLabels() {
        String sql = "SELECT b FROM ( " + "VALUES " + "(1, 'a')," + "(2, 'b')," + "(3, 'c')" + ") AS a (b, c)###";

        List<LineageNode> nodeList = LineageExtractor.extractLineageWithAnonymousTables(sql).getNodeList();

        // Inline literal table.
        LineageNode inlineLiteral = new LineageNode(Constants.Node.TYPE_ANON, Constants.Node.TYPE_ANON.concat("0"), "a");
        inlineLiteral.addListOfColumns(Column.arrayToColumns(Arrays.asList("b", "c")));

        // Anonymous table (from select statement).
        LineageNode anonymous = new LineageNode(Constants.Node.TYPE_ANON, Constants.Node.TYPE_ANON.concat("1"));
        anonymous.addColumn(new Column("b", Constants.Node.TYPE_ANON.concat("0::b")));

        LineageNode.testNodeListEquivalency(Arrays.asList(inlineLiteral, anonymous), nodeList);
    }

}
