import java.util.ArrayList;
import java.util.List;

public class Column {
    private String name = "";
    private String alias = "";
    private String id = "";
    private ArrayList<String> sources = new ArrayList<String>();

    public Column(String name, String alias, String id){
        this.name = name;
        this.alias = alias;
        this.id = id;
    }

    // Setters
    public void setName(String name) {
        this.name = name;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public void setID(String id) {
        this.id = id;
    }

    public void addSource(String source) {
        this.sources.add(source);
    }

    // Getters
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
