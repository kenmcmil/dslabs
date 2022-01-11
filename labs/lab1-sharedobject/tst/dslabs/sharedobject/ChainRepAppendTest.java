package dslabs.sharedobject;

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
import dslabs.sharedobject.AppendApplication.Append;
import dslabs.sharedobject.AppendApplication.AppendResult;
import dslabs.sharedobject.AppendApplication.Show;
import dslabs.sharedobject.AppendApplication.ShowResult;
import java.util.Objects;
import lombok.NonNull;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.FixMethodOrder;
//import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runners.MethodSorters;
import java.util.ArrayList;
import java.util.HashMap;

import static dslabs.framework.testing.StatePredicate.CLIENTS_DONE;
import static dslabs.framework.testing.StatePredicate.RESULTS_OK;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public final class ChainRepAppendTest extends BaseJUnitTest {

    private static final int numServers = 2;
    private static final ArrayList<Address> servers, clients;
    private static final HashMap<Address,Address> clientServer;

    static {
        servers = new ArrayList<Address>(); 
        clients = new ArrayList<Address>(); 
        clientServer = new HashMap<Address,Address>();
        for (int i = 0; i < numServers; i++) {
            servers.add(server(i));
            clients.add(client(i));
            clientServer.put(client(i),server(i));
        }
    }
    
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

    public static StateGeneratorBuilder builder() {
        StateGeneratorBuilder builder = StateGenerator.builder();
        builder.serverSupplier(a -> new ChainRepAppendServer(a,servers));
        builder.clientSupplier(a -> new AppendClient(a,clientServer.get(a)));
        builder.workloadSupplier(Workload.emptyWorkload());
        return builder;
    }

    @Override
    protected void setupRunTest() {
        runState = new RunState(builder().build());
        for (int i = 0; i < numServers; i++)
            runState.addServer(server(i));
    }

    @Override
    protected void setupSearchTest() {
        initSearchState = new SearchState(builder().build(),true);
        for (int i = 0; i < numServers; i++)
            initSearchState.addServer(server(i));
    }

    @org.junit.Test(timeout = 20 * 1000)
    @PrettyTestName("Short test sequence.")
    @Category({RunTests.class})
    public void test01BasicAppend() throws InterruptedException {

        StatePredicate showResultsMatch =
            statePredicate("After updates, server contents match",
                           s -> {
                               Result r0 = Iterables.getLast(s.clientWorker(client(0)).results());
                               Result r1 = Iterables.getLast(s.clientWorker(client(1)).results());
                               return !(r0 instanceof ShowResult) || !(r0 instanceof ShowResult) || r0.equals(r1);
                           });
        
        for (int i = 0; i < numServers; i++) 
            runState.addClientWorker(client(i), Workload.emptyWorkload(), true);

        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < numServers; j++) {
                runState.clientWorker(client(j)).addCommand(new Append(String.valueOf(j)),new AppendResult());
            }
        }
        runState.run(runSettings);
        for (int i = 0; i < numServers; i++) {
            runState.clientWorker(client(i)).addCommand(new Show(), new ShowResult(""));
        }
        runSettings.addInvariant(showResultsMatch);
        runState.run(runSettings);
    }

    @org.junit.Test(timeout = 180 * 1000)
    @PrettyTestName("Check eventual consistency")
    @Category(SearchTests.class)
    public void test02BasicAppend() throws InterruptedException {

        StatePredicate showResultsMatch =
            statePredicate("Eventual consisency",
                           s -> {
                               for (int i = 0; i < numServers; i++) {
                                   if (s.clientWorker(client(i)).results().size() == 0)
                                       continue;
                                   for (int j = i+1; j < numServers; j++) {
                                       if (s.clientWorker(client(j)).results().size() == 0)
                                           continue;
                                       Result r0 = Iterables.getLast(s.clientWorker(client(i)).results());
                                       Result r1 = Iterables.getLast(s.clientWorker(client(j)).results());
                                       if (!( !(r0 instanceof ShowResult) || !(r1 instanceof ShowResult)
                                             || ((ShowResult)r0).value().length() < numServers*2 || ((ShowResult)r1).value().length() < numServers*2 ||
                                              r0.equals(r1)))
                                           return false;
                                   }
                               }
                               return true;
                           });
        
        for (int i = 0; i < numServers; i++) 
            initSearchState.addClientWorker(client(i), Workload.emptyWorkload(), true);
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < numServers; j++) {
                initSearchState.clientWorker(client(j)).addCommand(new Append(String.valueOf(j)),new AppendResult());
            }
        }
        for (int i = 0; i < numServers; i++) 
            initSearchState.clientWorker(client(i)).addCommand(new Show(), new ShowResult(""));
        searchSettings.addInvariant(showResultsMatch).maxTimeSecs(120);
        bfs(initSearchState);
        assertSpaceExhausted();
    }

    @org.junit.Test(timeout = 20 * 1000)
    @PrettyTestName("Do not send too many messages for read-only commands.")
    @Category({RunTests.class})
    public void test03OneServerMessagePerShow() throws InterruptedException {

        StatePredicate oneServerMessagePerShow =
            statePredicate("At most one message to server per read-only command",
                           s -> {
                               int ms = 0;
                               for (int i = 0; i < numServers; i++)
                                   ms += ((RunState)s).numMessagesSentTo(servers.get(i));
                               return ms <= 2 * servers.size();
                           });
        
        for (int i = 0; i < numServers; i++) 
            runState.addClientWorker(client(i), Workload.emptyWorkload(), true);

        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < numServers; j++) {
                runState.clientWorker(client(j)).addCommand(new Show(),new ShowResult(""));
            }
        }
        runSettings.addInvariant(oneServerMessagePerShow);
        runState.run(runSettings);
    }

    @org.junit.Test(timeout = 20 * 1000)
    @PrettyTestName("Do not send multiple replies to clients.")
    @Category({RunTests.class})
    public void test04OneReplyPerRequest() throws InterruptedException {

        StatePredicate oneReplyPerRequest =
            statePredicate("At most one reply to client per command",
                           s -> {
                               int ms = 0;
                               for (int i = 0; i < numServers; i++)
                                   ms += ((RunState)s).numMessagesSentTo(client(i));
                               return ms <= 2 * numServers;
                           });
        
        for (int i = 0; i < numServers; i++) 
            runState.addClientWorker(client(i), Workload.emptyWorkload(), true);

        for (int j = 0; j < numServers; j++) {
            runState.clientWorker(client(j)).addCommand(new Append(String.valueOf(j)),new AppendResult());
        }
        for (int j = 0; j < numServers; j++) {
            runState.clientWorker(client(j)).addCommand(new Show(),new ShowResult(""));
        }
        runSettings.addInvariant(oneReplyPerRequest);
        runState.run(runSettings);
    }

    
    /*    @Test(timeout = 5 * 1000)
    @PrettyTestName("Multiple clients can append simultaneously")
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
    @PrettyTestName("Client can still ping if some messages are dropped")
    @Category({RunTests.class, UnreliableTests.class})
    public void test03MessagesDropped() throws InterruptedException {
        runState.addClientWorker(client(1), repeatedAppends(100));

        runSettings.networkUnreliable(true);

        runSettings.addInvariant(RESULTS_OK);
        runState.run(runSettings);
    }

    @Test
    @PrettyTestName("Single client repeatedly pings")
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
