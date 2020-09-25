import java.util.ArrayList;
import java.util.List;

/**
 * Maintains the labelling information for table subqueries. This includes the alias for the
 * table as well as the names of the columns (should they be provided, eg. in the case of inline literals).
 * Eg.
 * (
 *      VALUES
 *          (1, 3),
 *          (4, 2)
 * ) AS tableName (columnA, columnB)
 *
 * This example represents an anonymous table from a subquery that would have labelling information of
 * alias = tableName, columns = [ columnA, columnB ]
 */
public class LabellingInformation {
    private String alias;
    private ArrayList<Column> columns;

    public LabellingInformation(String alias, List<com.facebook.presto.sql.tree.Identifier> columnNames) {
        this.alias = alias;
        this.columns = new ArrayList<>();

        if (columnNames == null) return;

        for (com.facebook.presto.sql.tree.Identifier columnName : columnNames) {
            this.columns.add(new Column(columnName.getValue()));
        }
    }

    public String getAlias() {
        return alias;
    }

    public ArrayList<Column> getColumns() {
        return columns;
    }
}
