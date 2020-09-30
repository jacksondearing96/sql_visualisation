import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TestColumns {

    /**
     * Get the data from a column in the format:
     * "alias=columnAlias,id=columnID,name=columnName,sources={source1,source2,...}"
     *
     * @param column The column which will have its data extracted and stringified.
     * @return The column data in the string form (above).
     */
    private String getColumnDataString(Column column) {
        String columnData = ReflectionToStringBuilder.reflectionToString(column);
        return columnData.substring(columnData.indexOf("[") + 1, columnData.indexOf("]"));
    }

    @Test
    @DisplayName("testColumn")
    public void testColumn() {
        // Testing "getters"
        Column column = new Column("name", "alias", "id");
        Assertions.assertEquals("name", column.getName());
        Assertions.assertEquals("alias", column.getAlias());
        Assertions.assertEquals("id", column.getID());
        Assertions.assertTrue(column.getSources().isEmpty());

        // Testing "setters"
        ArrayList<String> sources = new ArrayList<>(Arrays.asList("source1", "source2"));
        column.setName("newName");
        column.setAlias("newAlias");
        column.setID("newID");
        column.setSources(sources);
        column.addSource("source3");
        column.addListOfSources(Arrays.asList("source4", "source5"));
        column.addListOfSources(sources);
        column.addSource("source1");
        Assertions.assertEquals(
                "alias=newAlias,id=newID,name=newName,sources={source1,source2,source3,source4,source5},stagedRename=Optional.empty,type=COLUMN",
                getColumnDataString(column));

        // Test Column cloning and equals
        try {
            Column clone = (Column) column.clone();
            Assertions.assertTrue(column.equals(clone));
        } catch (CloneNotSupportedException c) {
            Assertions.fail("Cloning column failure.");
        }
    }

    @Test
    @DisplayName("testRenameColumn")
    public void testRenameColumn()  {
        String sql = "SELECT a FROM mytable### ALTER TABLE mytable RENAME COLUMN a TO b###";
        List<LineageNode> nodeList = LineageExtractor.extractLineage(sql).getNodeList();

        LineageNode tableAfterRename = new LineageNode(Constants.Node.TYPE_TABLE, "mytable");
        tableAfterRename.addColumn(new Column("b"));

        Assertions.assertEquals(1, nodeList.size());
        tableAfterRename.equals(nodeList.get(0));
    }

    @Test
    @DisplayName("testAddColumn")
    public void testAddColumn() {
        String sql = "ALTER TABLE mytable ADD COLUMN a varchar###";
        List<LineageNode> nodeList = LineageExtractor.extractLineage(sql).getNodeList();

        LineageNode mytable = new LineageNode(Constants.Node.TYPE_TABLE, "mytable");
        mytable.addColumn(new Column("a"));

        Assertions.assertEquals(1, nodeList.size());
        mytable.equals(nodeList.get(0));
    }

}
