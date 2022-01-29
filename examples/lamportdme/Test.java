package lamportdme;

import com.google.common.collect.Iterables;
import dslabs.framework.Address;
import dslabs.framework.Command;
import dslabs.framework.Result;
import dslabs.framework.testing.LocalAddress;
import dslabs.framework.testing.StateGenerator;
import dslabs.framework.testing.StateGenerator.StateGeneratorBuilder;
import dslabs.framework.testing.Workload;
import dslabs.framework.testing.junit.BaseJUnitTest;
import dslabs.framework.testing.junit.PrettyTestName;
import dslabs.framework.testing.junit.RunTests;
import dslabs.framework.testing.junit.SearchTests;
import dslabs.framework.testing.junit.UnreliableTests;
import dslabs.framework.testing.runner.RunState;
import dslabs.framework.testing.search.SearchState;
import dslabs.framework.testing.utils.SerializableFunction;
import dslabs.framework.testing.StatePredicate;
import static dslabs.framework.testing.StatePredicate.statePredicate;
import java.util.Objects;
import lombok.NonNull;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.FixMethodOrder;
//import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runners.MethodSorters;
import java.util.Arrays;
import java.util.List;

import static dslabs.framework.testing.StatePredicate.CLIENTS_DONE;
import static dslabs.framework.testing.StatePredicate.RESULTS_OK;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public final class Test extends BaseJUnitTest {
    static final List<Address> sas = Arrays.asList(server(1),server(2),server(3));

    
    private static final class LamportDMEParser implements
            SerializableFunction<Pair<String, String>, Pair<Command, Result>> {
        @Override
        public Pair<Command, Result> apply(
                @NonNull Pair<String, String> commandAndResultString) {
            return new ImmutablePair<>(
                                       new MutexCommand(),
                                       new MutexResult());
        }
    }

    static StateGeneratorBuilder builder() {
        StateGeneratorBuilder builder = StateGenerator.builder();
        builder.serverSupplier(a -> new LamportDMEServer(a,sas));
        builder.clientSupplier(a -> new DMEClient(a,
                                                    a.equals(client(1)) ? server(1) :
                                                    a.equals(client(2)) ? server(2) :
                                                    server(3)));
        builder.workloadSupplier(Workload.emptyWorkload());
        return builder;
    }

    @Override
    protected void setupRunTest() {
        runState = new RunState(builder().build());
        runState.addServer(server(1));
        runState.addServer(server(2));
        runState.addServer(server(3));
    }

    @Override
    protected void setupSearchTest() {
        initSearchState = new SearchState(builder().build(),true);
        initSearchState.addServer(server(1));
        initSearchState.addServer(server(2));
        initSearchState.addServer(server(3));
    }

    @org.junit.Test(timeout = 20 * 1000)
    @PrettyTestName("Single client ping test")
    @Category({RunTests.class})
    public void test01BasicLamportDME() throws InterruptedException {

        StatePredicate mutualExclusion =
            statePredicate("Mutual Exclusion",
                           s -> {
                               for (int i = 1; i < 4; i++)
                                   for (int j = i+1; j < 4; j++) {
                                       if (((DMEClient)s.clientWorker(client(i)).clientNode()).acquiredState
                                           && ((DMEClient)s.clientWorker(client(j)).clientNode()).acquiredState)
                                           return false;
                                   }
                               return true;
                           });
        
        runState.addClientWorker(client(1), Workload.emptyWorkload(), true);
        runState.addClientWorker(client(2), Workload.emptyWorkload(), true);
        runState.addClientWorker(client(3), Workload.emptyWorkload(), true);
        for (int i = 0; i < 2; i++) {
            runState.clientWorker(client(1)).addCommand(new MutexCommand(),new MutexResult());
            runState.clientWorker(client(2)).addCommand(new MutexCommand(),new MutexResult());
            runState.clientWorker(client(3)).addCommand(new MutexCommand(),new MutexResult());
        }
        runSettings.addInvariant(mutualExclusion);
        runState.run(runSettings);
    }

    @org.junit.Test(timeout = 500 * 1000)
    @PrettyTestName("Check eventual consistency")
    @Category(SearchTests.class)
    public void test02BasicLamportDME() throws InterruptedException {
        
        StatePredicate mutualExclusion =
            statePredicate("Mutual Exclusion",
                           s -> {
                               for (int i = 1; i < 4; i++)
                                   for (int j = i+1; j < 4; j++)
                                       if (((DMEClient)s.clientWorker(client(i)).clientNode()).acquiredState
                                           && ((DMEClient)s.clientWorker(client(j)).clientNode()).acquiredState)
                                           return false;
                               return true;
                           });
        
        initSearchState.addClientWorker(client(1), Workload.emptyWorkload());
        initSearchState.addClientWorker(client(2), Workload.emptyWorkload());
        initSearchState.addClientWorker(client(3), Workload.emptyWorkload());
        for (int i = 0; i < 2; i++) {
            initSearchState.clientWorker(client(1)).addCommand(new MutexCommand(),new MutexResult());
            initSearchState.clientWorker(client(2)).addCommand(new MutexCommand(),new MutexResult());
            //            initSearchState.clientWorker(client(3)).addCommand(new MutexCommand(),new MutexResult());
        }
        searchSettings.maxTimeSecs(500);
        searchSettings.addInvariant(mutualExclusion);
        bfs(initSearchState);
        assertSpaceExhausted();
    }



}
