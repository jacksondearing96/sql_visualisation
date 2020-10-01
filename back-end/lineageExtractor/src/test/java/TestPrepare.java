import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

public class TestPrepare {

    @Test
    @DisplayName("testPrepareStatement")
    public void testPrepareStatement() {
        String sql = "PREPARE mytable FROM SELECT a, b FROM c###";
        List<LineageNode> nodeList = LineageExtractor.extractLineage(sql).getNodeList();

        LineageNode tableC = new LineageNode(Constants.Node.TYPE_TABLE, "c");
        tableC.addListOfColumns(Column.arrayToColumns(Arrays.asList("a", "b")));

        LineageNode prepareNode = new LineageNode(Constants.Node.TYPE_TABLE, "mytable");
        prepareNode.addListOfColumns(Column.arrayToColumns(Arrays.asList("a", "b"), Arrays.asList("c::a", "c::b")));

        Assertions.assertEquals(2, nodeList.size());
        tableC.equals(nodeList.get(0));
        prepareNode.equals(nodeList.get(1));
    }
}
