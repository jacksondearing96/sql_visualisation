import org.junit.jupiter.api.DisplayName;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

public class TestConditional {

    @Test
    @DisplayName("testConditionalSelectItems")
    public void testConditionalSelectItems() {
        String sql = "SELECT CASE WHEN a = b THEN c ELSE d END FROM mytable###";
        List<LineageNode> nodeList = LineageExtractor.extractLineage(sql).getNodeList();

        LineageNode myTable = new LineageNode(Constants.Node.TYPE_TABLE, "mytable");
        myTable.addListOfColumns(Arrays.asList(
                new Column("a"),
                new Column("b"),
                new Column("c"),
                new Column("d")
        ));

        LineageNode.testNodeListEquivalency(Arrays.asList(myTable), nodeList);
    }

    @Test
    @DisplayName("testDereferenceConditionalSelectItems")
    public void testDereferenceConditionalSelectItems() {
        String sql = "SELECT CASE " + "WHEN lefttable.a = righttable.b " + "THEN lefttable.c " + "ELSE righttable.d "
                + "END FROM lefttable INNER JOIN righttable ON 1 = 1###";
        List<LineageNode> nodeList = LineageExtractor.extractLineage(sql).getNodeList();

        LineageNode leftTable = new LineageNode(Constants.Node.TYPE_TABLE, "lefttable");
        leftTable.addListOfColumns(Arrays.asList(
                new Column("a"),
                new Column("c")
        ));

        LineageNode rightTable = new LineageNode(Constants.Node.TYPE_TABLE, "righttable");
        rightTable.addListOfColumns(Arrays.asList(
                new Column("b"),
                new Column("d")
        ));

        LineageNode.testNodeListEquivalency(Arrays.asList(leftTable, rightTable), nodeList);
    }

}
