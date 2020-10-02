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
        table.addListOfColumns(Column.arrayToColumns(Arrays.asList("column1", "column2", "someDate")));

        // Anonymous table.
        LineageNode anonymousTable = new LineageNode(Constants.Node.TYPE_ANON, Constants.Node.TYPE_ANON.concat("0"));
        anonymousTable.addListOfColumns(
            Column.arrayToColumns(
                Arrays.asList("column1", "column2", "columnA"),
                Arrays.asList("tablename::column1", "tablename::column2", "tablename::someDate")
            )
        );

        LineageNode.testNodeListEquivalency(Arrays.asList(table, anonymousTable), nodeList);
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

        // First, verify that the anonymous table is produced correctly as the intermediate table.
        List<LineageNode> nodeList = LineageExtractor.extractLineageWithAnonymousTables(sql).getNodeList();
        LineageNode.testNodeListEquivalency(Arrays.asList(source, anonymous, view), nodeList);


        // Now extract the lineage, including the step where the anonymous tables are bypassed.
        nodeList = LineageExtractor.extractLineage(sql).getNodeList();

        // Adjust the view, it's column's sources have now bypassed the anonymous table.
        view = new LineageNode(Constants.Node.TYPE_VIEW, "view");
        view.addColumn(new Column("b", DataLineage.makeId(source.getName(), b.getName())));

        LineageNode.testNodeListEquivalency(Arrays.asList(view, source), nodeList);
    }

}
