import java.util.ArrayList;

/**
 * Printer class for printing readable tables to the terminal.
 */
public class PrettyPrinter {

    public static void printLineageNodesNames(ArrayList<LineageNode> list) {
        for (LineageNode item : list) System.out.print(item.getName().concat(", "));
        System.out.print("\n");
    }

    public static void printColumns(ArrayList<Column> list) {
        for (Column item : list) System.out.print(item.getName().concat(", "));
        System.out.print("\n");
    }

    private static void printHorizontalLine(int maxWidth) {
        System.out.print("+");
        for (int i = 0; i < maxWidth + 2; ++i) System.out.print("-");
        System.out.print("+\n");
    }

    private static void printLineContent(int maxWidth, String content) {
        // Left margin.
        System.out.print("| ".concat(content));

        int remainingSpaces = maxWidth - content.length();
        for (int i = 0; i < remainingSpaces; ++i) System.out.print(" ");

        // Right margin.
        System.out.print(" |\n");
    }

    public static void printLineageNode(LineageNode lineageNode) {
        ArrayList<String> rows = new ArrayList<>();
        int maxWidth = 0;

        String header = lineageNode.getName();
        if (lineageNode.hasAlias()) header = header.concat(" AS ").concat(lineageNode.getAlias());
        header = header.concat(" TYPE(").concat(lineageNode.getType()).concat(")");
        if (header.length() > maxWidth) maxWidth = header.length();

        for (Column column : lineageNode.getColumns()) {
            String row = column.getName();
            if (!column.getAlias().equals("")) row = row.concat(" AS ").concat(column.getAlias());
            if (!column.getSources().isEmpty()) row = row.concat(" SOURCES ").concat(column.getSources().toString());
            if (row.length() > maxWidth) maxWidth = row.length();
            rows.add(row);
        }

        // Table structure.
        System.out.println("\n");
        printHorizontalLine(maxWidth);
        printLineContent(maxWidth, header);
        printHorizontalLine(maxWidth);
        for (String row : rows) printLineContent(maxWidth, row);
        printHorizontalLine(maxWidth);
    }
}