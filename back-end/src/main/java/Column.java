import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Columns within a particular table or view.
 */
public class Column implements Cloneable {
    // Create a logger for logging - each class should have this.
    final static Logger LOGGING = LoggerFactory.getLogger(Column.class);

    private String name = "";
    private String alias = "";
    private String id = "";
    private ArrayList<String> sources = new ArrayList<String>();

    public Column() { this("", "", ""); }
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
        // There are different levels of logging, pick the appropriate log level for the function.
        LOGGING.info("Column created.");
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

    public String getName() {
        return this.name;
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
