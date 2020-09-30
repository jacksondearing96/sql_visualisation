import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

public class TestAnonymous {

    @Test
    @DisplayName("testBasicAnonymousTableGeneration")
    public void testBasicAnonymousTableGeneration() {
        String statement = "select column1, column2, cast(someDate as date) as columnA from \"tablename\"###";

        List<LineageNode> nodeList = LineageExtractor.extractLineageWithAnonymousTables(statement).getNodeList();

        // Source table.
        LineageNode table = new LineageNode(Constants.Node.TYPE_TABLE, "tablename");
        Column column1 = new Column("column1");
        Column column2 = new Column("column2");
        Column dateColumn = new Column("someDate");
        table.addListOfColumns(Arrays.asList(column1, column2, dateColumn));

        // Anonymous table.
        LineageNode anonymousTable = new LineageNode(Constants.Node.TYPE_ANON, Constants.Node.TYPE_ANON.concat("0"));
        Column column1a = new Column("column1");
        Column column2a = new Column("column2");
        Column columnA = new Column("columnA");
        column1a.addSource("tablename::column1");
        column2a.addSource("tablename::column2");
        columnA.addSource("tablename::someDate");
        anonymousTable.addListOfColumns(Arrays.asList(column1a, column2a, columnA));

        Assertions.assertEquals(2, nodeList.size());
        Assertions.assertTrue(table.equals(nodeList.get(0)));
        Assertions.assertTrue(anonymousTable.equals(nodeList.get(1)));
    }

    @Test
    @DisplayName("testBypassAnonymousTables")
    public void testBypassAnonymousTables() {
        String sql = "CREATE VIEW view AS " + "SELECT b " + "FROM (" + "SELECT b " + "FROM B" + ") AS A" + "###";

        // Source table.
        LineageNode source = new LineageNode(Constants.Node.TYPE_TABLE, "b");
        Column b = new Column("b");
        source.addColumn(b);

        // Anonymous table.
        LineageNode anonymous = new LineageNode(Constants.Node.TYPE_ANON, Constants.Node.TYPE_ANON.concat("0"));
        anonymous.setAlias("A");
        b.addSource(DataLineage.makeId(source.getName(), b.getName()));
        anonymous.addColumn(b);

        // View.
        LineageNode view = new LineageNode(Constants.Node.TYPE_VIEW, "view");
        b = new Column("b");
        b.addSource(DataLineage.makeId(anonymous.getName(), b.getName()));
        view.addColumn(b);

        // First, verify that the anonymous table is produced correctly as the
        // intermediate table.
        List<LineageNode> nodeList = LineageExtractor.extractLineageWithAnonymousTables(sql).getNodeList();

        Assertions.assertEquals(3, nodeList.size());
        Assertions.assertTrue(source.equals(nodeList.get(0)));
        Assertions.assertTrue(anonymous.equals(nodeList.get(1)));
        Assertions.assertTrue(view.equals(nodeList.get(2)));

        // Now extract the lineage, including the step where the anonymous tables are
        // bypassed.
        nodeList = LineageExtractor.extractLineage(sql).getNodeList();

        // Adjust the view, it's column's sources have now bypassed the anonymous table.
        view = new LineageNode(Constants.Node.TYPE_VIEW, "view");
        b = new Column("b");
        b.addSource(DataLineage.makeId(source.getName(), b.getName()));
        view.addColumn(b);

        // Check the resultant lineage is as expected.
        Assertions.assertEquals(2, nodeList.size());
        Assertions.assertTrue(source.equals(nodeList.get(0)));
        Assertions.assertTrue(view.equals(nodeList.get(1)));
    }

}
