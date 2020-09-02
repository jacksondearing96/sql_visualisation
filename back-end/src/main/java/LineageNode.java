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

    public LineageNode(String type) { this(type, "", ""); }
    public LineageNode(String type, String name) { this(type, name, ""); }

    /**
     * Create a lineage node.
     * @param type Either "TABLE", "VIEW" or "ANONYMOUS"
     * @param name Table or view name
     * @param alias Table, view or subquery alias
     */
    public LineageNode(String type, String name, String alias) {
        this.type = type;
        this.name = name;
        this.alias = alias;
    }

    /**
     * Determine if this LineageNode is represented in a given list of sources.
     * @param sources A list of sources that may or may not contain this LineageNode.
     * @return True if this LineageNode is a source within sources. False otherwise.
     */
    public boolean isSourceOf(List<String> sources) {
        for (String source : sources) {
            if (source == alias || source == name) return true;
        }
        return false;
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

    public boolean hasAlias() {
        return !this.alias.equals("");
    }
}
