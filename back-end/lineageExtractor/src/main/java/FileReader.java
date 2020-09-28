import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

/**
 * Class to handle the reading of files.
 * This is intended to be a temporary utility for development and potentially for testing.
 */
class FileReader {
    /**
     * @param filePath The path to the file that is to be read.
     * @return The contents of the given file in string format.
     */
    public static String ReadFile(String filePath) {
        String data = "";

        try {
            File myObj = new File(filePath);
            Scanner myReader = new Scanner(myObj);

            // Read every line of the file.
            while (myReader.hasNextLine()) {
                // Replace newlines with spaces in the SQL scripts for compactness.
                data += " " + myReader.nextLine();
            }
            myReader.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            System.out.println("Error reading file: " + filePath);
            System.exit(1);
        }

        return data;
    }
}