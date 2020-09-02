import java.util.ArrayList;

public class PrettyPrinter {
    private static void horizontalLine(int maxWidth) {
        System.out.print("+");
        for (int i = 0; i < maxWidth + 2; ++i) System.out.print("-");
        System.out.print("+\n");
    }

    private static void printLineContent(int maxWidth, String content) {
        System.out.print("| " + content);
        int remainingSpaces = maxWidth - content.length();
        for (int i = 0; i < remainingSpaces; ++i) System.out.print(" ");
        System.out.print(" |\n");
    }

    public static void print(LineageNode lineageNode) {
        ArrayList<String> rows = new ArrayList<>();
        int maxWidth = 0;

        String header = lineageNode.getName();
        if (lineageNode.hasAlias()) header += " AS " + lineageNode.getAlias();
        if (header.length() > maxWidth) maxWidth = header.length();

        for (Column column : lineageNode.getColumns()) {
            String row = column.getName();
            if (!column.getAlias().equals("")) row += " AS " + column.getAlias();
            if (!column.getSources().isEmpty()) row += " SOURCES " + column.getSources();
            if (row.length() > maxWidth) maxWidth = row.length();
            rows.add(row);
        }

        System.out.println("\n\n");
        horizontalLine(maxWidth);
        printLineContent(maxWidth, header);
        horizontalLine(maxWidth);
        for (String row : rows) printLineContent(maxWidth, row);
        horizontalLine(maxWidth);
    }
}
