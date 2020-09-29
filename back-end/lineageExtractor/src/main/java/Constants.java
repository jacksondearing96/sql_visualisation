public final class Constants {
    public static final String WILDCARD = "*";

    public final class PrestoSQLSyntax {
        public static final String STATEMENT_DELIM = "###";
        public static final String DEREFERENCE_DELIM_REGEX = "[.]";

        private PrestoSQLSyntax() {}
    }

    public final class LineageExtractor {
        public static final String OUTPUT_JSON_FILE = "src/gen/java/lineage_output.json";

        private LineageExtractor() {}
    }

    public final class Node {
        public static final String TYPE_TABLE = "TABLE";
        public static final String TYPE_VIEW = "VIEW";
        public static final String TYPE_ANON = "ANONYMOUS";
        public static final String SEPARATOR = "::";

        private Node() {}
    }

    private Constants() {}
}
