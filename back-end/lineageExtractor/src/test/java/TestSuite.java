import org.junit.BeforeClass;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)

@Suite.SuiteClasses(
        {
                TestAlias.class,
                TestAnonymous.class,
                TestColumns.class,
                TestConditional.class,
                TestCreate.class,
                TestInsert.class,
                TestLineage.class,
                TestLiteral.class,
                TestMisc.class,
                TestMultiple.class,
                TestPrepare.class,
                TestWildcard.class,
                TestWith.class
        }
)

public class TestSuite{
}
