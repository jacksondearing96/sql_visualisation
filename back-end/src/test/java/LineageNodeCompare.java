import org.junit.jupiter.api.*;

import java.util.List;

public class LineageNodeCompare {
    static void assertEquals(Column left, Column right) {
        Assertions.assertEquals(left.getName(), right.getName(), "Column name");
        Assertions.assertEquals(left.getAlias(), right.getAlias());
        Assertions.assertEquals(left.getID(), right.getID());
        List<String> leftSources = left.getSources();
        List<String> rightSources = right.getSources();
        Assertions.assertEquals(leftSources.size(), rightSources.size());
        for (int i = 0; i < leftSources.size(); ++i) {
            Assertions.assertEquals(leftSources.get(i), rightSources.get(i));
        }
    }

    static void assertEquals(List<Column> left, List<Column> right) {
        Assertions.assertEquals(left.size(), right.size());
        for (int i = 0; i < left.size(); ++i) {
            assertEquals(left.get(i), right.get(i));
        }
    }

    static void assertEquals(LineageNode left, LineageNode right) {
        Assertions.assertEquals(left.getName(), right.getName());
        Assertions.assertEquals(left.getAlias(), right.getAlias());
        Assertions.assertEquals(left.getType(), right.getType());
        assertEquals(left.getColumns(), right.getColumns());
    }
}
