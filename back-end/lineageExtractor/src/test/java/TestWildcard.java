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
        Column columnA = new Column("*");
        columnA.addSource("b::*");
        anonymousTable.addColumn(columnA);

        Assertions.assertTrue(LineageNode.areNodeListsEqual(Arrays.asList(table, anonymousTable), nodeList));
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
        Column c = new Column("c");
        sourceB.addColumn(c);

        // Anonymous table.
        LineageNode anonymous = new LineageNode(Constants.Node.TYPE_ANON, Constants.Node.TYPE_ANON.concat("0"));
        Column wildcard = new Column("*");
        wildcard.addSource("a::*");
        c.addSource("b::c");
        anonymous.addListOfColumns(Arrays.asList(wildcard, c));

        Assertions.assertTrue(LineageNode.areNodeListsEqual(Arrays.asList(sourceA, sourceB, anonymous), nodeList));
    }
}
