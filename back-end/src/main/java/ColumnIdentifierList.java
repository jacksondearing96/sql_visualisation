import java.util.ArrayList;

final class ColumnIdentifier {
    String base = "";
    String field;

    ColumnIdentifier(String field) {
        this.field = field;
    }

    ColumnIdentifier(String base, String field) {
        this.base = base;
        this.field = field;
    }
}

public class ColumnIdentifierList extends ArrayList<ColumnIdentifier> {
    String alias = "";
};