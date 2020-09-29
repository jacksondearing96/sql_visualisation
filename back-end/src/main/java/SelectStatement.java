import java.util.ArrayList;
import java.util.Stack;
import java.util.function.Predicate;

/**
 * Represents a select statement from an SQL statement.
 * A select statement contains potentially multiple distinct select items and these items
 * are sourced from potentially multiple source tables.
 */
public class SelectStatement {
    private Stack<SelectItem> selectItems = new Stack<>();
    private ArrayList<LineageNode> sourceTables = new ArrayList<>();
    private LineageNode anonymousTable;
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

    /**
     * Make sure the select items have been reconciled with the source tables before returning
     * the source tables.
     * @return The (completed) source tables.
     */
    public ArrayList<LineageNode> getSourceTables() {
        if (!isReconciled) reconcileSelectItemsWithSourceTables();
        return sourceTables;
    }

    /**
     * Make sure the select items have been reconciled with the source tables before returning
     * the anonymous table.
     * @return The anonymous table.
     */
    public LineageNode getAnonymousTable() {
        if (!isReconciled) reconcileSelectItemsWithSourceTables();
        return anonymousTable;
    }

    /**
     * Takes the select items and the source tables for the select statement and reconciles them into
     * completed LineageNodes. This finalises the sourceTables and anonymousTable members.
     */
    private void reconcileSelectItemsWithSourceTables() {
        anonymousTable = new LineageNode(Constants.Node.TYPE_ANON, Util.getNextAnonymousTableName());

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

                        // Add the source column to the source table.
                        // Skip wildcard columns.
                        if (!column.getName().equals(Constants.WILDCARD)) sourceTable.addColumn(column);

                        // Add this as a source of the column. This will be for the anonymous table.
                        anonymousColumn.addSource(DataLineage.makeId(sourceTable.getName(), column.getName()));

                    }

                    // In the event that the anonymous column derives from a single column in the source table,
                    // it will share the name of that source column.
                    if (selectItem.getIdentifiers().size() == 1) anonymousColumn.setName(column.getName());
                }

                // If an alias exists for this column, use it as the name for the anonymous table.
                if (!selectItem.getAlias().isEmpty()) anonymousColumn.setName(selectItem.getAlias());
                // Every selected item is added to the anonymous table.
                anonymousTable.addColumn(anonymousColumn);
            }
        }
        isReconciled = true;
    }
}
