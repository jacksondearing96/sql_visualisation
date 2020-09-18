/**
 * Utility class to contain methods that need to be used throughout the system.
 * Abstract these commonly used methods into a dedicated util class to avoid circular dependencies.
 */
public class Util {
    /**
     * Counter variable used to generate the unique IDs for anonymous tables.
     * Start this at a value of -1 to ensure the first ID returned is 0 (after the auto-increment).
     */
    private static int anonymousTableCount = -1;

    /**
     * Returns the next ID used for allocating unique names to anonymous tables.
     * @return The next unique ID.
     */
    public static int getNextAnonymousTableId() {
        return ++anonymousTableCount;
    }

    public static void resetAnonymousTableCount() { anonymousTableCount = -1; }

    /**
     * Removes the database name prefix from a given name. Eg.
     * Input of "%(db)s.table_name" would return "table_name"
     * @param name The name to have the prefix removed.
     * @return The table name without the database name prefix.
     */
    public static String removeDatabasePrefix(String name) {
        String[] nameParts = name.split("[.]");
        return nameParts[nameParts.length - 1];
    }
}
