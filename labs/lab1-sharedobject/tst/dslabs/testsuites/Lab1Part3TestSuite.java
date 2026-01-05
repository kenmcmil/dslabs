package dslabs.testsuites;


import org.junit.FixMethodOrder;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses(dslabs.sharedobject.ScalarClockAppendTest.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public interface Lab1Part3TestSuite {
}
