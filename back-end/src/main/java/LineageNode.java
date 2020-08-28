import java.util.ArrayList;
import java.util.List;

/**
 * Top-Level node. This can represent either a Table or a View.
 */
public class LineageNode {
    private String type = "";
    private String name = "";
    private String alias = "";
    private final ArrayList<Column> columns = new ArrayList<Column>();

    /**
     * Create a lineage node.
     * @param type Either "TABLE" or "VIEW"
     * @param name Column name
     * @param alias Column name alias
     */
    public LineageNode(String type, String name, String alias) {
        this.type = type;
        this.name = name;
        this.alias = alias;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public void addColumn(Column column) {
        this.columns.add(column);
    }

    public void addListOfColumns(ArrayList<Column> columns) {
        for (Column column : columns) {
            addColumn(column);
        }
    }

    public String getType() {
        return this.type;
    }

    public String getName() {
        return this.name;
    }

    public String getAlias() {
        return this.alias;
    }

    public List<Column> getColumns() {
        return this.columns;
    }
}
