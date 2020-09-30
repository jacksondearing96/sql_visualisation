import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

public class TestLiteral {

    @Test
    @DisplayName("testStandAloneLiteralTable")
    void testStandAloneLiteralTable() {
        String sql = "VALUES " + "(1, 'a')," + "(2, 'b')," + "(3, 'c')###";

        List<LineageNode> nodeList = LineageExtractor.extractLineageWithAnonymousTables(sql).getNodeList();

        Assertions.assertEquals(0, nodeList.size());
    }

    @Test
    @DisplayName("testLiteralInlineTable")
    void testLiteralInlineTables() {

        String sql = "SELECT b FROM ( " + "VALUES " + "(1, 'a')," + "(2, 'b')," + "(3, 'c')" + ")###";

        List<LineageNode> nodeList = LineageExtractor.extractLineageWithAnonymousTables(sql).getNodeList();

        // Inline literal table.
        LineageNode inlineLiteral = new LineageNode(Constants.Node.TYPE_ANON, Constants.Node.TYPE_ANON.concat("0"));
        Column b = new Column("b");
        inlineLiteral.addColumn(b);

        // Anonymous table (from select statement).
        LineageNode anonymous = new LineageNode(Constants.Node.TYPE_ANON, Constants.Node.TYPE_ANON.concat("1"));
        b.addSource("ANONYMOUS0::b");
        anonymous.addColumn(b);

        Assertions.assertEquals(2, nodeList.size());
        inlineLiteral.equals(nodeList.get(0));
    }

    @Test
    @DisplayName("testLiteralInlineTableWithAlias")
    void testLiteralInlineTablesWithAlias() {

        String sql = "SELECT b FROM ( " + "VALUES " + "(1, 'a')," + "(2, 'b')," + "(3, 'c')" + ") AS a###";

        List<LineageNode> nodeList = LineageExtractor.extractLineageWithAnonymousTables(sql).getNodeList();

        // Inline literal table.
        LineageNode inlineLiteral = new LineageNode(Constants.Node.TYPE_ANON, Constants.Node.TYPE_ANON.concat("0"), "a");
        Column b = new Column("b");
        inlineLiteral.addColumn(b);

        // Anonymous table (from select statement).
        LineageNode anonymous = new LineageNode(Constants.Node.TYPE_ANON, Constants.Node.TYPE_ANON.concat("1"));
        b.addSource("ANONYMOUS0::b");
        anonymous.addColumn(b);

        Assertions.assertEquals(2, nodeList.size());
        inlineLiteral.equals(nodeList.get(0));
    }

    @Test
    @DisplayName("testLiteralInlineTableWithAliasAndColumnLabels")
    void testLiteralInlineTablesWithAliasAndColumnLabels() {

        String sql = "SELECT b FROM ( " + "VALUES " + "(1, 'a')," + "(2, 'b')," + "(3, 'c')" + ") AS a (b, c)###";

        List<LineageNode> nodeList = LineageExtractor.extractLineageWithAnonymousTables(sql).getNodeList();

        // Inline literal table.
        LineageNode inlineLiteral = new LineageNode(Constants.Node.TYPE_ANON, Constants.Node.TYPE_ANON.concat("0"), "a");
        Column b = new Column("b");
        inlineLiteral.addListOfColumns(Arrays.asList(b, new Column("c")));

        // Anonymous table (from select statement).
        LineageNode anonymous = new LineageNode(Constants.Node.TYPE_ANON, Constants.Node.TYPE_ANON.concat("1"));
        b.addSource("ANONYMOUS0::b");
        anonymous.addColumn(b);

        Assertions.assertEquals(2, nodeList.size());
        inlineLiteral.equals(nodeList.get(0));
        anonymous.equals(nodeList.get(1));
    }

}
