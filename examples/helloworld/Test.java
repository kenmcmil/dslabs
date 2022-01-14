package helloworld;

import dslabs.framework.Address;
import dslabs.framework.Command;
import dslabs.framework.Result;
import dslabs.framework.testing.StateGenerator;
import dslabs.framework.testing.StateGenerator.StateGeneratorBuilder;
import dslabs.framework.testing.Workload;
import dslabs.framework.testing.junit.BaseJUnitTest;
import dslabs.framework.testing.junit.PrettyTestName;
import dslabs.framework.testing.junit.RunTests;
import dslabs.framework.testing.runner.RunState;
import helloworld.HelloApplication.Hello;
import helloworld.HelloApplication.HelloResult;
import org.junit.FixMethodOrder;
import org.junit.experimental.categories.Category;
import org.junit.runners.MethodSorters;


@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public final class Test extends BaseJUnitTest {
    static final Address sa = server(0);

    static StateGeneratorBuilder builder() {
        StateGeneratorBuilder builder = StateGenerator.builder();
        builder.serverSupplier(a -> new HelloServer(a));
        builder.clientSupplier(a -> new HelloClient(a,server(0)));
        builder.workloadSupplier(Workload.emptyWorkload());
        return builder;
    }

    @Override
    protected void setupRunTest() {
        runState = new RunState(builder().build());
        runState.addServer(server(0));
    }

    @org.junit.Test(timeout = 20 * 1000)
    @PrettyTestName("Hello test!")
    @Category({RunTests.class})
    public void test01BasicHello() throws InterruptedException {

        runState.addClientWorker(client(0), Workload.emptyWorkload(), true);
        runState.addClientWorker(client(1), Workload.emptyWorkload(), true);
        for (int i = 0; i < 2; i++) {
            runState.clientWorker(client(0)).addCommand(new Hello("Client 0"),new HelloResult(""));
            runState.clientWorker(client(1)).addCommand(new Hello("Client 1"),new HelloResult(""));
        }
        runState.run(runSettings);
    }

}
