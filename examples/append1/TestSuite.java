package dslabs.append1;


import dslabs.append1.Test;
import org.junit.FixMethodOrder;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses(Test.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public interface TestSuite {
}
