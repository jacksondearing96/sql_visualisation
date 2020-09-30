import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

public class TestLineage {

    @Test
    @DisplayName("lineageNodeNamingConvention")
    void lineageNodeSetName() {
        // Basic name.
        LineageNode node = new LineageNode(Constants.Node.TYPE_TABLE, "name");
        Assertions.assertEquals("name", node.getName());

        // Name with a base prefix.
        node.setName("base.field");
        Assertions.assertEquals("field", node.getName());

        // Name with multiple base parts.
        node.setName("base0.base1.base2.field");
        Assertions.assertEquals("field", node.getName());

        // Empty name. Make sure this doesn't throw an error.
        node.setName("");
    }

    @Test
    @DisplayName("testLineageNodes")
    void testLineageNodes() {
        String simpleSelect = "SELECT a FROM b###";
        List<LineageNode> nodeList = LineageExtractor.extractLineageWithAnonymousTables(simpleSelect).getNodeList();

        // Source table.
        LineageNode sourceNode = new LineageNode(Constants.Node.TYPE_TABLE, "b");
        Column a = new Column("a");
        sourceNode.addColumn(a);

        // Anonymous table.
        LineageNode anonymousNode = new LineageNode(Constants.Node.TYPE_ANON, Constants.Node.TYPE_ANON.concat("0"));
        a.addSource("b::a");
        anonymousNode.addColumn(a);

        Assertions.assertEquals(2, nodeList.size());
        Assertions.assertTrue(anonymousNode.equals(nodeList.get(1)));
        Assertions.assertTrue(sourceNode.equals(nodeList.get(0)));
    }

}
