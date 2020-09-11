public class Identifier {
    private final String base;
    private final String field;

    public Identifier(String field) {
        this("", field);
    }

    public Identifier(String base, String field) {
        this.base = base;
        this.field = field;
    }

    public String getBase() {
        return base;
    }

    public String getField() {
        return field;
    }
}
