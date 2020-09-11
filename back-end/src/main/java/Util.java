public class Util {
    /**
     * Counter variable used to generate the unique IDs for anonymous tables.
     */
    private static int anonymousTableCount = -1;

    /**
     * Returns the next ID used for allocating unique names to anonymous tables.
     * @return The next unique ID.
     */
    static public int getNextAnonymousTableId() {
        return ++anonymousTableCount;
    }
}
