import java.util.ArrayList;
import java.util.List;

public class JsonNode {
    private String type = "";
    private String name = "";
    private String alias = "";
    private final ArrayList<Column> columns = new ArrayList<Column>();

    public JsonNode(String type, String name, String alias) {
        this.type = type;
        this.name = name;
        this.alias = alias;
    }

    // Setters
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

    // Getters
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
