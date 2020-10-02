import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

public class TestWildcard {

    @Test
    @DisplayName("testWildCardOperatorWithoutColumns")
    public void testWildCardOperatorWithoutColumns() {
        String statement = "SELECT * from b###";

        // Output
        List<LineageNode> nodeList = LineageExtractor.extractLineageWithAnonymousTables(statement).getNodeList();

        // Source table.
        LineageNode table = new LineageNode(Constants.Node.TYPE_TABLE, "b");

        // Anonymous table.
        LineageNode anonymousTable = new LineageNode(Constants.Node.TYPE_ANON, Constants.Node.TYPE_ANON.concat("0"));

        LineageNode.testNodeListEquivalency(Arrays.asList(table, anonymousTable), nodeList);
    }

    @Test
    @DisplayName("testWildCardOperatorWithColumns")
    public void testWildCardOperatorWithColumns() {
        String statement = "SELECT a, b FROM mytable### SELECT * from mytable###";
        List<LineageNode> nodeList = LineageExtractor.extractLineageWithAnonymousTables(statement).getNodeList();

        LineageNode mytable = new LineageNode(Constants.Node.TYPE_TABLE, "mytable");
        Column a = new Column("a");
        Column b = new Column("b");
        mytable.addListOfColumns(Arrays.asList(a, b));

        LineageNode anonymous1 = new LineageNode(Constants.Node.TYPE_ANON, Constants.Node.TYPE_ANON.concat("1"));
        a.addSource(DataLineage.makeId(mytable.getName(), a.getName()));
        b.addSource(DataLineage.makeId(mytable.getName(), b.getName()));
        anonymous1.addListOfColumns(Arrays.asList(a, b));

        Assertions.assertEquals(3, nodeList.size());
        Assertions.assertTrue(mytable.equals(nodeList.get(0)));
        Assertions.assertTrue(anonymous1.equals(nodeList.get(2)));
    }

    @Test
    @DisplayName("testDereferencedWildcard")
    public void testDereferencedWildcard() {
        String sql = "SELECT a.*, b.c FROM a INNER JOIN b ON 1 = 1###";
        List<LineageNode> nodeList = LineageExtractor.extractLineageWithAnonymousTables(sql).getNodeList();

        // Source table a.
        LineageNode sourceA = new LineageNode(Constants.Node.TYPE_TABLE, "a");

        // Source table b.
        LineageNode sourceB = new LineageNode(Constants.Node.TYPE_TABLE, "b");
        sourceB.addColumn(new Column("c"));

        // Anonymous table.
        LineageNode anonymous = new LineageNode(Constants.Node.TYPE_ANON, Constants.Node.TYPE_ANON.concat("0"));
        anonymous.addColumn(new Column("c", "b::c"));

        LineageNode.testNodeListEquivalency(Arrays.asList(sourceA, sourceB, anonymous), nodeList);
    }
}
