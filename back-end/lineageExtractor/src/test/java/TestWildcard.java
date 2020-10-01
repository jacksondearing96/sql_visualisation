import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

public class TestWildcard {

    @Test
    @DisplayName("testWildCardOperator")
    public void testWildCardOperator() {
        String statement = "SELECT * from b###";

        // Output
        List<LineageNode> nodeList = LineageExtractor.extractLineageWithAnonymousTables(statement).getNodeList();

        // Source table.
        LineageNode table = new LineageNode(Constants.Node.TYPE_TABLE, "b");

        // Anonymous table.
        LineageNode anonymousTable = new LineageNode(Constants.Node.TYPE_ANON, Constants.Node.TYPE_ANON.concat("0"));
        anonymousTable.addColumn(new Column("*", "b::*"));

        Assertions.assertEquals(2, nodeList.size());
        Assertions.assertTrue(table.equals(nodeList.get(0)));
        Assertions.assertTrue(anonymousTable.equals(nodeList.get(1)));
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
        anonymous.addListOfColumns(Column.arrayToColumns(Arrays.asList("*", "c"), Arrays.asList("a::*", "b::c")));

        Assertions.assertEquals(3, nodeList.size());
        sourceA.equals(nodeList.get(0));
        sourceB.equals(nodeList.get(1));
    }
}
