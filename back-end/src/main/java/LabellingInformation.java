import java.util.ArrayList;
import java.util.List;

public class LabellingInformation {
    private String alias;
    private ArrayList<Column> columns;

    public LabellingInformation(String alias) {
        this(alias, new ArrayList<com.facebook.presto.sql.tree.Identifier>());
    }

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
