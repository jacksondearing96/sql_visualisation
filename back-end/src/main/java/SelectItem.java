import java.util.List;
import java.util.ArrayList;

public class SelectItem {
    private final ArrayList<Identifier> identifiers = new ArrayList<>();
    private String alias = "";

    public List<Identifier> getIdentifiers()  {
        return identifiers;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public void addIdentifier(String field) {
        this.identifiers.add(new Identifier(field));
    }

    public void addIdentifier(String base, String field) {
        this.identifiers.add(new Identifier(base, field));
    }
}
