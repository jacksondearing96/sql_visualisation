import java.util.ArrayList;
import java.util.Stack;
import java.util.function.Predicate;

public class SelectStatement {
    private Stack<SelectItem> selectItems = new Stack<>();
    private ArrayList<LineageNode> sourceTables = new ArrayList<>();
    private LineageNode anonymousNode;
    private boolean isReconciled = false;

    public void addEmptySelectItem() {
        selectItems.push(new SelectItem());
    }

    public void setSourceTables(ArrayList<LineageNode> sourceTables) {
        this.sourceTables = sourceTables;
    }

    public SelectItem currentSelectItem() {
        return selectItems.peek();
    }

    public ArrayList<LineageNode> getSourceTables() {
        if (!isReconciled) reconcileSelectItemsWithSourceTables();
        return sourceTables;
    }

    public LineageNode getAnonymousTable() {
        if (!isReconciled) reconcileSelectItemsWithSourceTables();
        return anonymousNode;
    }

    private void reconcileSelectItemsWithSourceTables() {
        anonymousNode = new LineageNode("ANONYMOUS", "Anonymous" + Util.getNextAnonymousTableId());

        for (SelectItem selectItem : selectItems) {
            for (LineageNode sourceTable : sourceTables) {

                Column anonymousColumn = new Column();

                for (Identifier identifier : selectItem.getIdentifiers()) {

                    Column column = new Column(identifier.getField());
                    if (!identifier.getBase().isEmpty()) column.addSource(identifier.getBase());

                    // In the case of a single source, the columns won't already have the source recorded in their list of
                    // sources (because it will have appeared as a pure identifier in the SQL eg. columnName instead of
                    // tableName.columnName.
                    // Therefore, explicitly check for the case of a single source.
                    if (sourceTables.size() == 1 || sourceTable.isSourceOf(column.getSources())) {

                        // When this point is reached, 'sourceTable' is a known source of 'column'.
                        // Add this column to the source table.

                        // Filter out the sources of the column if they are the same as the source table's name or alias.
                        Predicate<String> isNameOrAlias = sourceName -> sourceName.equals(sourceTable.getAlias()) || sourceName.equals(sourceTable.getName());
                        column.getSources().removeIf(isNameOrAlias);

                        sourceTable.addColumn(column);

                        // Add the source column to the source table.
                        // Skip wildcard columns.
                        if (!column.getName().equals("*")) sourceTable.addColumn(column);

                        // Add this as a source of the column. This will be for the anonymous table.
                        anonymousColumn.addSource(column.getID());
                    }

                    // In the event that the anonymous column derives from a single column in the source table,
                    // it will share the name of that source column.
                    if (selectItem.getIdentifiers().size() == 1) anonymousColumn.setName(column.getName());
                }

                // If an alias exists for this column, use it as the name for the anonymous table.
                if (!selectItem.getAlias().isEmpty()) anonymousColumn.setName(selectItem.getAlias());
                // Every selected item is added to the anonymous table.
                anonymousNode.addColumn(anonymousColumn);
            }
        }
        isReconciled = true;
    }
}
