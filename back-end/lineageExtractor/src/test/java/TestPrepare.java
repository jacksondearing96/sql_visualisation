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
        Column columnA = new Column("a");
        Column columnB = new Column("b");
        tableC.addListOfColumns(Arrays.asList(columnA, columnB));

        LineageNode prepareNode = new LineageNode(Constants.Node.TYPE_TABLE, "mytable");
        columnA.addSource(DataLineage.makeId(tableC.getName(), columnA.getName()));
        columnB.addSource(DataLineage.makeId(tableC.getName(), columnB.getName()));
        prepareNode.addListOfColumns(Arrays.asList(columnA, columnB));

        LineageNode.testNodeListEquivalency(Arrays.asList(tableC, prepareNode), nodeList);
    }

}
