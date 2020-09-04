import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

public class TestRunner {

    @BeforeAll
    static void setup(){
        System.out.println("Testing for SIVT Back-end Parser");
    }

    @BeforeEach
    void setupEach(){
        System.out.println("This is executed before each test");
    }

    @Tag("FileReader")
    @Test
    void testFileReader(){
        System.out.println("Test: FileReader");
        Assertions.assertEquals(" SELECT * FROM hello### SELECT a FROM goodbye", FileReader.ReadFile("./src/test/java/testInput.sql"));
    }


}
