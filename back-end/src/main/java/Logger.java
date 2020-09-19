/**
 * Logging class for documenting warnings and errors.
 */
public class Logger {

    static private boolean isLogging = true;

    static void setIsLogging(boolean isLogging) {
        Logger.isLogging = isLogging;
    }

    /**
     * Ordinary logging messages.
     * @param message Message to be logged.
     */
    static void log(String message) {
        if (isLogging) System.out.println(message);
    }

    /**
     * Print a warning message.
     * @param message The warning message to be printed.
     */
    static void warning(String message) {
        System.out.println("\n(!) WARNING - unexpected behaviour:");
        System.out.println("\t" + message + "\n");
    }
}
