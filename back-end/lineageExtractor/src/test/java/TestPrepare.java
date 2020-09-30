import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

public class TestPrepare {

    @Test
    @DisplayName("testPrepareStatement")
    void testPrepareStatement() {
        String sql = "PREPARE mytable FROM SELECT a, b FROM c###";
        List<LineageNode> nodeList = LineageExtractor.extractLineage(sql).getNodeList();

        LineageNode tableC = new LineageNode(Constants.Node.TYPE_TABLE, "c");
        Column columnA = new Column("a");
        Column columnB = new Column("b");
        tableC.addListOfColumns(Arrays.asList(columnA, columnB));

        LineageNode prepareNode = new LineageNode(Constants.Node.TYPE_TABLE, "mytable");
        columnA.addSource(DataLineage.makeId(tableC.getName(), columnA.getName()));
        columnB.addSource(DataLineage.makeId(tableC.getName(), columnB.getName()));
        prepareNode.addListOfColumns(Arrays.asList(columnA, columnB));

        Assertions.assertEquals(2, nodeList.size());
        tableC.equals(nodeList.get(0));
        prepareNode.equals(nodeList.get(1));
    }

}
