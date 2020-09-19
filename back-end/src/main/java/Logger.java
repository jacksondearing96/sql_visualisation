/**
 * Logging class for documenting warnings and errors.
 */
public class Logger {

    /**
     * Print a warning message.
     * @param message The warning message to be printed.
     */
    static void warning(String message) {
        System.out.println("\n(!) WARNING - unexpected behaviour:");
        System.out.println("\t" + message + "\n");
    }
}
