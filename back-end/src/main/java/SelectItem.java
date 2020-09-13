import java.util.List;
import java.util.ArrayList;

/**
 * Represents a select item within an SQL statement.
 * Any given select item may be composed of multiple identifiers and an optional alias. Eg.
 * SELECT a, someFunction(b, c) as d FROM d
 * There are 2 distinct select items here.
 * The first contains a single identifier (a) and no alias.
 * The second contains two identifiers (b and c) and an alias (d).
 */
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
