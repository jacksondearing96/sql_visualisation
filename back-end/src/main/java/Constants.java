public final class Constants {
    public static final String WILDCARD = "*";

    public final class PrestoSQLSyntax {
        public static final String STATEMENT_DELIM = "###";

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

        private Node() {}
    }

    public final class Source {
        public static final String TYPE_TABLE = "Table";
        public static final String TYPE_VIEW = "View";
        public static final String TYPE_ANON = "Anonymous";
        public static final String SEPARATOR = "::";


        private Source() {}
    }

    private Constants() {}
}