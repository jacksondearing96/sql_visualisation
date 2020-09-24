import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Top-Level node. This can represent either a Table or a View.
 */
public class LineageNode {
    private String type = "";
    private String name = "";
    private String alias = "";
    private final ArrayList<Column> columns = new ArrayList<Column>();

    private Optional<String> stagedRename = Optional.empty();

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
        setName(name);
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
            String.format("Node names are not equal ('%s' and '%s')", name, lineage.name);
        assert alias.equals(lineage.alias) :
            String.format("Node aliases are not equal ('%s' and '%s')", alias, lineage.alias);
        assert type.equals(lineage.type) :
            String.format("Node types are not equal ('%s' and '%s')", type, lineage.type);
        assert columns.size() == lineage.columns.size()
                : String.format("Node number of columns are not equal ('%s' and '%s')", columns.size(), lineage.columns.size());

        // Generic catch-all to catch any unaccounted for attributes.
        for (int i = 0; i < columns.size(); i++) {
            assert columns.get(i).equals(lineage.columns.get(i)) : String.format(
                "Node columns are not equal", columns.get(i), lineage.columns.get(i));
        }

        return true;
    }

    public void setType(String type) {
        this.type = type;
    }

    /**
     * The name may be composed of multiple parts (dot delimited), eg. "%(db)s.customer_insights", just take the field part
     * and ignore the base prefix, therefore for this case, name = "customer_insights"
     * @param name The new name of the LineageNode.
     */
    public void setName(String name) {
        this.name = Util.removeDatabasePrefix(name);
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    /**
     * Add a column to the list of columns. Ensures columns are unique (by name).
     * If a duplicate column is found, the sources from the new column are copied over.
     * @param column The column to be added.
     */
    public void addColumn(Column column) {
        // Check if this column already exists.
        for (Column existingColumn : columns) {
            if (existingColumn.getName().equals(column.getName())) {
                existingColumn.addListOfSources(column.getSources());
                return;
            }
        }

        // This is a branch new column, clone it.
        try {
            column.setID(DataLineage.makeId(name, column.getName()));
            this.columns.add((Column)column.clone());
        } catch (CloneNotSupportedException c) {}
    }

    public void addListOfColumns(List<Column> columns) {
        addListOfColumns(new ArrayList<Column>(columns));
    }

    public void addListOfColumns(ArrayList<Column> columns) {
        for (Column column : columns) {
            try {
                addColumn((Column)column.clone());
            } catch (CloneNotSupportedException c) {}
        }
    }

    public void stageRenameTo(String rename) {
        this.stagedRename = Optional.of(rename);
    }

    public void rename(String rename) {
        setName(rename);
    }

    public Optional<String> getRename() {
        return this.stagedRename;
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
