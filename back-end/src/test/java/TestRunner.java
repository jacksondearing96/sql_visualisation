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

    @Tag("TAG")
    @Test
    void thisTest(){
        System.out.println("This is a Test!");
    }


}
