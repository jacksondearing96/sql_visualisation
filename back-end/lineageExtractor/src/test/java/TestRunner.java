import org.junit.jupiter.api.*;
import org.junit.runner.JUnitCore;

public class TestRunner {

    @BeforeAll
    static void setup() {
        System.out.println("Testing for SIVT Back-end:");
    }

    @AfterAll
    static void afterAll() {
        System.out.println("All tests complete.");
    }

    @BeforeEach
    void beforeEach(TestInfo testInfo) {
        System.out.println("Testing: " + testInfo.getDisplayName() + " - Started");
    }

    @AfterEach
    void afterEach(TestInfo testInfo) {
        System.out.println("Testing: " + testInfo.getDisplayName() + " - Complete");
    }

    @Test
    void testCreates(){
        JUnitCore.runClasses(TestCreate.class);
    }

    @Test
    @DisplayName("testInserts")
    void testInserts(){
        JUnitCore.runClasses(TestInsert.class);
    }

    @Test
    @DisplayName("testAlias")
    void testAlias(){
        JUnitCore.runClasses(TestAlias.class);
    }

    @Test
    @DisplayName("testConditionals")
    void testConditionals(){
        JUnitCore.runClasses(TestConditional.class);
    }

    @Test
    @DisplayName("testFiles")
    void testFiles(){
        JUnitCore.runClasses(TestFiles.class);
    }

    @Test
    @DisplayName("testLiterals")
    void testLiterals(){
        JUnitCore.runClasses(TestLiteral.class);
    }

    @Test
    @DisplayName("testColumns")
    void testColumns(){
        JUnitCore.runClasses(TestColumns.class);
    }

    @Test
    @DisplayName("testWildcards")
    void testWildcards(){
        JUnitCore.runClasses(TestWildcard.class);
    }

    @Test
    @DisplayName("testPrepares")
    void testPrepares(){
        JUnitCore.runClasses(TestPrepare.class);
    }

    @Test
    @DisplayName("testMultiple")
    void testMultiples(){
        JUnitCore.runClasses(TestMultiple.class);
    }

    @Test
    @DisplayName("testAnonymous")
    void testAnonymous(){
        JUnitCore.runClasses(TestAnonymous.class);
    }

    @Test
    @DisplayName("testLineage")
    void testLineage(){
        JUnitCore.runClasses(TestLineage.class);
    }

    @Test
    @DisplayName("testMisc")
    void testMisc(){
        JUnitCore.runClasses(TestMisc.class);
    }


}
