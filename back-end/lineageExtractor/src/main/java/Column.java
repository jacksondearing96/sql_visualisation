import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Columns within a particular table or view.
 */
public class Column implements Cloneable {

    private final String type = Constants.Node.TYPE_COLUMN;
    private String name = "";
    private String alias = "";
    private String id = "";
    private ArrayList<String> sources = new ArrayList<String>();

    /**
     * Maintains an (optional) new name that the node will be renamed to when it is added
     * to the DataLineage object it is eventually added to.
     * This has to be staged as opposed to updating the name immediately so that the node is
     * still able to be matched to potential duplicates that exist in the DatLineage it is added to.
     */
    private Optional<String> stagedRename = Optional.empty();

    public Column() { this("", "", ""); }

    /**
     * Create a column with a comma separated list of sources.
     * @param name Column name.
     * @param sources Comma separated list of sources.
     */
    public Column(String name, String sources) {
        this(name, "", "");
        this.addListOfSources(Arrays.asList(sources.split(",")));
    }

    public Column(String name) {
        this(name, "", "");
    }

    /**
     * Create a column.
     * @param name Column name.
     * @param alias Column alias name.
     * @param id Each column has a unique ID as a combination of
     *           <tableName>::<columnName>.
     */
    public Column(String name, String alias, String id){
        this.name = name;
        this.alias = alias;
        this.id = id;
    }

    public static ArrayList<Column> arrayToColumns(List<String> names) {
        ArrayList<Column> columns = new ArrayList<>();
        for (int i = 0; i < names.size(); i++) {
            columns.add(new Column(names.get(i), "", ""));
        }
        return columns;
    }

    public static ArrayList<Column> arrayToColumns(List<String> names, List<String> sources) {
        ArrayList<Column> columns = new ArrayList<>();
        for (int i = 0; i < names.size(); i++) {
            columns.add(new Column(names.get(i), sources.get(i)));
        }
        return columns;
    }

    public Object clone() throws CloneNotSupportedException {
        Column clone = (Column)super.clone();
        clone.sources = new ArrayList<String>(sources);
        return clone;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Column column = (Column) o;

        assert this.name.equals(column.name) :
                String.format("Column names are not equal ('%s' and '%s')", name, column.name);
        assert alias.equals(column.alias) :
                String.format("Column aliases are not equal ('%s' and '%s')", alias, column.alias);
        assert id.equals(column.id) :
                String.format("Column IDs are not equal ('%s' and '%s')", id, column.id);
        assert type.equals(column.type) :
                String.format("Column types are not equal ('%s' and '%s')", type, column.type);
        assert sources.equals(column.sources) :
                String.format("Column sources are not equal ('%s' and '%s')", sources, column.sources);

        return true;
    }

    private boolean hasSource(String source) {
        for (String existingSource : sources) {
            if (existingSource.equals(source)) return true;
        }
        return false;
    }

    public boolean isWildcard() {
        return name.equals(Constants.WILDCARD);
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public void setID(String id) {
        this.id = id;
    }

    public void setSources(ArrayList<String> sources) { this.sources = new ArrayList<>(sources); }

    /**
     * Add only unique sources.
     * @param source The new source to be added.
     */
    public void addSource(String source) {
        if (!hasSource(source)) this.sources.add(source);
    }

    public void addListOfSources(ArrayList<String> sources) {
        addListOfSources((List<String>)sources);
    }

    public void addListOfSources(List<String> sources) {
        for (String source : sources) {
            addSource(source);
        }
    }

    public void stageRenameTo(String rename) {
        this.stagedRename = Optional.of(rename);
    }

    public void renameAndUpdateId(String rename) {
        setName(rename);
        if (!id.isEmpty()) {
            String[] idParts = id.split(Constants.Node.SEPARATOR);
            setID(DataLineage.makeId(idParts[0], getName()));
        }
    }

    public Optional<String> getRename() {
        return this.stagedRename;
    }

    public String getName() {
        return this.name;
    }

    public String getType() {
        return this.type;
    }

    public String getAlias() {
        return this.alias;
    }

    public String getID() {
        return this.id;
    }

    public ArrayList<String> getSources() {
        return this.sources;
    }
}
