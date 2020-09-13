import org.junit.jupiter.api.*;

import java.util.List;

public class LineageNodeCompare {
    static void assertColumnEquals(Column left, Column right) {
        Assertions.assertEquals(left.getName(), right.getName(), "Column names");
        Assertions.assertEquals(left.getAlias(), right.getAlias(),  "Column aliases");
        Assertions.assertEquals(left.getID(), right.getID(), "Column IDs");
        List<String> leftSources = left.getSources();
        List<String> rightSources = right.getSources();
        Assertions.assertEquals(leftSources.size(), rightSources.size(), "Column source list sizes");
        for (int i = 0; i < leftSources.size(); ++i) {
            Assertions.assertEquals(leftSources.get(i), rightSources.get(i), "Column sources");
        }
    }

    static void assertColumnListEquals(List<Column> left, List<Column> right) {
        Assertions.assertEquals(left.size(), right.size(), "Column list sizes");
        for (int i = 0; i < left.size(); ++i) {
            assertColumnEquals(left.get(i), right.get(i));
        }
    }

    static void assertNodesEquals(LineageNode left, LineageNode right) {
        Assertions.assertEquals(left.getName(), right.getName(), "Node names");
        Assertions.assertEquals(left.getAlias(), right.getAlias(), "Node aliases");
        Assertions.assertEquals(left.getType(), right.getType(), "Node types");
        assertColumnListEquals(left.getColumns(), right.getColumns());
    }
}
