import org.junit.jupiter.api.Assertions;
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

        LineageNode mytable = new LineageNode(Constants.Node.TYPE_TABLE, "mytable");
        mytable.addListOfColumns(Column.arrayToColumns(Arrays.asList("a", "b", "c", "d")));

        Assertions.assertEquals(1, nodeList.size());
        mytable.equals(nodeList.get(0));
    }

    @Test
    @DisplayName("testDereferenceConditionalSelectItems")
    public void testDereferenceConditionalSelectItems() {
        String sql = "SELECT CASE " + "WHEN lefttable.a = righttable.b " + "THEN lefttable.c " + "ELSE righttable.d "
                + "END FROM lefttable INNER JOIN righttable ON 1 = 1###";
        List<LineageNode> nodeList = LineageExtractor.extractLineage(sql).getNodeList();

        LineageNode leftTable = new LineageNode(Constants.Node.TYPE_TABLE, "lefttable");
        leftTable.addListOfColumns(Column.arrayToColumns(Arrays.asList("a", "c")));

        LineageNode rightTable = new LineageNode(Constants.Node.TYPE_TABLE, "righttable");
        rightTable.addListOfColumns(Column.arrayToColumns(Arrays.asList("b", "d")));

        Assertions.assertEquals(2, nodeList.size());
        leftTable.equals(nodeList.get(0));
        rightTable.equals(nodeList.get(1));
    }

}
