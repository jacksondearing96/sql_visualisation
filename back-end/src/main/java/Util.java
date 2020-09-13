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
}
