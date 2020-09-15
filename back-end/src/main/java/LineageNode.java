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
            if (source.contains(alias + "::") || source.contains(name + "::")
            || source.equals(alias) || source.equals(name)) return true;
        }
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        LineageNode lineage = (LineageNode) o;

        assert name.equals(lineage.name) :
            String.format("Node names are node equal ('%s' and '%s')", name, lineage.name);
        assert alias.equals(lineage.alias) :
            String.format("Node aliases are not equal ('%s' and '%s')", alias, lineage.alias);
        assert type.equals(lineage.type) :
            String.format("Node types are not equal ('%s' and '%s')", type, lineage.type);
        assert columns.size() == lineage.columns.size() : String.format("Nodes have dissimilar number of columns");

        // Generic catch-all to catch any unaccounted for attributes.
        for (int i = 0; i < columns.size(); i++) {
            assert columns.get(i).equals(lineage.columns.get(i)) : String.format(
                "Node columns are not equivalent", columns.get(i), lineage.columns.get(i));
        }

        return true;
    }


    private boolean hasColumnWithName(String name) {
        for (Column column : columns) {
            if (column.getName().equals(name)) return true;
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

    /**
     * Add a column to the list of columns. Ensures columns are unique (by name).
     * @param column The column to be added.
     */
    public void addColumn(Column column) {
        if (!hasColumnWithName(column.getName())) {
            column.setID(DataLineage.makeId(name, column.getName()));
            this.columns.add(column);
        }
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
