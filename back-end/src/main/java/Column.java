import java.util.ArrayList;
import java.util.List;

/**
 * Columns within a particular table or view.
 */
public class Column {
    private String name = "";
    private String alias = "";
    private String id = "";
    private ArrayList<String> sources = new ArrayList<String>();

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

    /**
     * Copy a column.
     * @return The copied column.
     */
    public Column getCopy() {
        Column copy = new Column(name, alias, id);
        copy.sources = new ArrayList<String>(sources);
        return copy;
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

    public void setSources(List<String> sources) { setSources(new ArrayList<String>(sources)); }

    public void setSources(ArrayList<String> sources) { this.sources = sources; }

    public void addSource(String source) {
        this.sources.add(source);
    }

    public void addListOfSources(ArrayList<String> sources) {
        addListOfSources((List<String>)sources);
    }

    public void addListOfSources(List<String> sources) {
        for (String source : sources) {
            addSource(source);
        }
    }

    public String getName() {
        return this.name;
    }

    public String getAlias() {
        return this.alias;
    }

    public String getID() {
        return this.id;
    }

    public List<String> getSources() {
        return this.sources;
    }
}
