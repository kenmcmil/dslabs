package dslabs.vizconfigs;

import dslabs.framework.testing.StateGenerator.StateGeneratorBuilder;
import dslabs.framework.testing.junit.Lab;
import dslabs.framework.testing.junit.Part;

@Lab("1")
@Part(3)
public class Lab1Part3VizConfig extends Lab1VizConfig {
    StateGeneratorBuilder builder() {
        return dslabs.sharedobject.ScalarClockPutTest.builder();
    }
}
