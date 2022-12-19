package append1;

import com.google.common.collect.Iterables;
import dslabs.framework.Address;
import dslabs.framework.Command;
import dslabs.framework.Result;
import dslabs.framework.testing.LocalAddress;
import dslabs.framework.testing.StateGenerator;
import dslabs.framework.testing.StateGenerator.StateGeneratorBuilder;
import dslabs.framework.testing.Workload;
import dslabs.framework.testing.junit.BaseJUnitTest;
import dslabs.framework.testing.junit.TestDescription;
import dslabs.framework.testing.junit.RunTests;
import dslabs.framework.testing.junit.SearchTests;
import dslabs.framework.testing.junit.UnreliableTests;
import dslabs.framework.testing.runner.RunState;
import dslabs.framework.testing.search.SearchState;
import dslabs.framework.testing.utils.SerializableFunction;
import dslabs.framework.testing.StatePredicate;
import static dslabs.framework.testing.StatePredicate.statePredicate;
import append1.AppendApplication.Append;
import append1.AppendApplication.AppendResult;
import append1.AppendApplication.Show;
import append1.AppendApplication.ShowResult;
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
    static final List<Address> sas = Arrays.asList(server(1),server(2));

    
    private static final class AppendParser implements
            SerializableFunction<Pair<String, String>, Pair<Command, Result>> {
        @Override
        public Pair<Command, Result> apply(
                @NonNull Pair<String, String> commandAndResultString) {
            return new ImmutablePair<>(
                    new Append(commandAndResultString.getValue()),
                    new AppendResult());
        }
    }

    static Workload repeatedAppends(int numAppends) {
        return Workload.builder().parser(new AppendParser())
                       .commandStrings("append-%i").resultStrings("result-%i")
                       .numTimes(numAppends).build();
    }

    static StateGeneratorBuilder builder() {
        StateGeneratorBuilder builder = StateGenerator.builder();
        builder.serverSupplier(a -> new AppendServer(a,a.equals(server(1)) ? server(2) : server(1)));
        builder.clientSupplier(a -> new AppendClient(a,a.equals(client(1)) ? server(1) : server(2)));
        builder.workloadSupplier(Workload.emptyWorkload());
        return builder;
    }

    @Override
    protected void setupRunTest() {
        runState = new RunState(builder().build());
        runState.addServer(server(1));
        runState.addServer(server(2));
    }

    @Override
    protected void setupSearchTest() {
        initSearchState = new SearchState(builder().build(),true);
        initSearchState.addServer(server(1));
        initSearchState.addServer(server(2));
    }

    @org.junit.Test(timeout = 20 * 1000)
    @TestDescription("Single client ping test")
    @Category({RunTests.class})
    public void test01BasicAppend() throws InterruptedException {

        StatePredicate showResultsMatch =
            statePredicate("After updates, server contents match",
                           s -> {
                               Result r0 = Iterables.getLast(s.clientWorker(client(1)).results());
                               Result r1 = Iterables.getLast(s.clientWorker(client(2)).results());
                               return !(r0 instanceof ShowResult) || !(r0 instanceof ShowResult) || r0.equals(r1);
                           });
        
        runState.addClientWorker(client(1), Workload.emptyWorkload(), true);
        runState.addClientWorker(client(2), Workload.emptyWorkload(), true);
        for (int i = 0; i < 2; i++) {
            runState.clientWorker(client(1)).addCommand(new Append("0"),new AppendResult());
            runState.clientWorker(client(2)).addCommand(new Append("1"),new AppendResult());
        }
        runState.run(runSettings);
        runState.clientWorker(client(1)).addCommand(new Show(), new ShowResult("0101"));
        runState.clientWorker(client(2)).addCommand(new Show(), new ShowResult("1010"));
        runSettings.addInvariant(showResultsMatch);
        runState.run(runSettings);
    }

    @org.junit.Test(timeout = 20 * 1000)
    @TestDescription("Check eventual consistency")
    @Category(SearchTests.class)
    public void test02BasicAppend() throws InterruptedException {

        StatePredicate showResultsMatch =
            statePredicate("Eventual consisency",
                           s -> {
                               if (s.clientWorker(client(1)).results().size() == 0 ||
                                   s.clientWorker(client(2)).results().size() == 0)
                                   return true;
                               Result r0 = Iterables.getLast(s.clientWorker(client(1)).results());
                               Result r1 = Iterables.getLast(s.clientWorker(client(2)).results());
                               return !(r0 instanceof ShowResult) || !(r1 instanceof ShowResult)
                                   || ((ShowResult)r0).value().length() < 2 || ((ShowResult)r0).value().length() < 2 ||
                                   r0.equals(r1);
                           });
        
        initSearchState.addClientWorker(client(1), Workload.emptyWorkload(), true);
        initSearchState.addClientWorker(client(2), Workload.emptyWorkload(), true);
        for (int i = 0; i < 1; i++) {
            initSearchState.clientWorker(client(1)).addCommand(new Append("0"),new AppendResult());
            initSearchState.clientWorker(client(2)).addCommand(new Append("1"),new AppendResult());
        }
        initSearchState.clientWorker(client(1)).addCommand(new Show(), new ShowResult("01"));
        initSearchState.clientWorker(client(2)).addCommand(new Show(), new ShowResult("10"));
        searchSettings.addInvariant(showResultsMatch);
        bfs(initSearchState);
        assertSpaceExhausted();
    }



    
    /*    @Test(timeout = 5 * 1000)
    @TestDescription("Multiple clients can append simultaneously")
    @Category({RunTests.class})
    public void test02MultipleClientsAppend() throws InterruptedException {
        Workload workload = Workload.builder().parser(new AppendParser())
                                    .commandStrings("hello from %a")
                                    .resultStrings("hello from %a").build();

        for (int i = 1; i <= 2; i++) {
            runState.addClientWorker(client(i), workload);
        }

        runSettings.addInvariant(RESULTS_OK);
        runState.run(runSettings);
    }

    @Test(timeout = 5 * 1000)
    @TestDescription("Client can still ping if some messages are dropped")
    @Category({RunTests.class, UnreliableTests.class})
    public void test03MessagesDropped() throws InterruptedException {
        runState.addClientWorker(client(1), repeatedAppends(100));

        runSettings.networkUnreliable(true);

        runSettings.addInvariant(RESULTS_OK);
        runState.run(runSettings);
    }

    @Test
    @TestDescription("Single client repeatedly pings")
    @Category(SearchTests.class)
    public void test04AppendSearch() throws InterruptedException {
        initSearchState.addClientWorker(client(1), repeatedAppends(10));

        System.out.println("Checking that the client can finish all pings");
        searchSettings.addInvariant(RESULTS_OK).addGoal(CLIENTS_DONE)
                      .maxTimeSecs(10);
        bfs(initSearchState);
        assertGoalFound();

        System.out
                .println("Checking that all of the returned pongs match pings");
        searchSettings.clearGoals().addPrune(CLIENTS_DONE);
        bfs(initSearchState);
        assertSpaceExhausted();
        } */
}
