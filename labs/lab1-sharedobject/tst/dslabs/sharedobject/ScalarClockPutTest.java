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
import dslabs.framework.testing.junit.TestPointValue;
import dslabs.framework.testing.runner.RunState;
import dslabs.framework.testing.search.SearchState;
import dslabs.framework.testing.utils.SerializableFunction;
import dslabs.framework.testing.StatePredicate;
import dslabs.framework.testing.MessageEnvelope;
import static dslabs.framework.testing.StatePredicate.statePredicate;
import dslabs.kvstore.KVStore.Put;
import dslabs.kvstore.KVStore.PutOk;
import dslabs.kvstore.KVStore.Get;
import dslabs.kvstore.KVStore.GetResult;
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
public final class ScalarClockPutTest extends BaseJUnitTest {

    public static final int numServers = 2;
    public static final ArrayList<Address> servers, clients;
    public static final HashMap<Address,Address> clientServer;

    static {
        servers = new ArrayList<Address>(); 
        clients = new ArrayList<Address>(); 
        clientServer = new HashMap<Address,Address>();
        for (int i = 0; i < numServers; i++) {
            servers.add(server(i+1));
            clients.add(client(i+1));
            clientServer.put(client(i+1),server(i+1));
        }
    }
    
    private static final class PutParser implements
            SerializableFunction<Pair<String, String>, Pair<Command, Result>> {
        @Override
        public Pair<Command, Result> apply(
                @NonNull Pair<String, String> commandAndResultString) {
            return new ImmutablePair<>(
                    new Put("key",commandAndResultString.getValue()),
                    new PutOk());
        }
    }

    static Workload repeatedPuts(int numPuts) {
        return Workload.builder().parser(new PutParser())
                       .commandStrings("append-%i").resultStrings("result-%i")
                       .numTimes(numPuts).build();
    }

    public static StateGeneratorBuilder builder() {
        StateGeneratorBuilder builder = StateGenerator.builder();
        builder.serverSupplier(a -> new ScalarClockPutServer(a,a.equals(server(1)) ? server(2) : server(1)));
        builder.clientSupplier(a -> new AppendClient(a,clientServer.get(a)));
        builder.workloadSupplier(Workload.emptyWorkload());
        return builder;
    }

    @Override
    protected void setupRunTest() {
        runState = new RunState(builder().build());
        for (int i = 0; i < numServers; i++)
            runState.addServer(servers.get(i));
    }

    @Override
    protected void setupSearchTest() {
        initSearchState = new SearchState(builder().build(),true);
        for (int i = 0; i < numServers; i++)
            initSearchState.addServer(servers.get(i));
    }

    @org.junit.Test(timeout = 20 * 1000)
    @PrettyTestName("Short test sequence.")
    @Category({RunTests.class})
    @TestPointValue(5)
    public void test01BasicPut() throws InterruptedException {

        StatePredicate showResultsMatch =
            statePredicate("After updates, server contents match",
                           s -> {
                               Result r0 = Iterables.getLast(s.clientWorker(clients.get(0)).results());
                               Result r1 = Iterables.getLast(s.clientWorker(clients.get(1)).results());
                               return !(r0 instanceof GetResult) || !(r0 instanceof GetResult) || r0.equals(r1);
                           });
        
        for (int i = 0; i < numServers; i++) 
            runState.addClientWorker(clients.get(i), Workload.emptyWorkload(), true);

        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < numServers; j++) {
                runState.clientWorker(clients.get(j)).addCommand(new Put("key",String.valueOf(j)),new PutOk());
            }
        }
        runSettings.setTimeLimit(1);
        runSettings.waitForClients(false);
        runState.run(runSettings);
        for (int i = 0; i < numServers; i++) {
            runState.clientWorker(clients.get(i)).addCommand(new Get("key"), new GetResult(""));
        }
        runSettings.setTimeLimit(-1);
        runSettings.waitForClients(true);
        runSettings.addInvariant(showResultsMatch);
        runState.run(runSettings);
    }

    @org.junit.Test(timeout = 180 * 1000)
    @PrettyTestName("Check eventual consistency, one key")
    @Category(SearchTests.class)
    @TestPointValue(10)
    public void test02ABasicPut() throws InterruptedException {

        StatePredicate showResultsMatch =
            statePredicate("Replicas consistent when network quiescent, same key",
                           s -> {
                               Command cmd = new Get("key");
                               boolean msgs = false;
                               for (MessageEnvelope m : s.network())
                                   msgs = true;
                               if (!msgs)
                                   for (int i = 0; i < numServers; i++) {
                                       ScalarClockPutServer si = (ScalarClockPutServer) s.server(servers.get(i));
                                       Result r = si.app.execute(cmd);
                                       if (!(r instanceof GetResult))
                                           return false;
                                       String vi = ((GetResult)r).value();
                                       for (int j = i+1; j < numServers; j++) {
                                           ScalarClockPutServer sj = (ScalarClockPutServer) s.server(servers.get(j));
                                           Result rj = sj.app.execute(cmd);
                                           if (!(rj instanceof GetResult))
                                               return false;
                                           String vj = ((GetResult)rj).value();
                                           if (!vj.equals(vi))
                                               return false;
                                       }
                                   }
                               return true;
                           });
        
        for (int i = 0; i < numServers; i++) 
            initSearchState.addClientWorker(clients.get(i), Workload.emptyWorkload(), true);
        for (int i = 0; i < 1; i++) {
            for (int j = 0; j < numServers; j++) {
                initSearchState.clientWorker(clients.get(j)).addCommand(new Put("key",String.valueOf(j)),new PutOk());
            }
        }
        searchSettings.addInvariant(showResultsMatch).maxTimeSecs(120);
        bfs(initSearchState);
        assertSpaceExhausted();
    }

    @org.junit.Test(timeout = 180 * 1000)
    @PrettyTestName("Check eventual consistency, two keys")
    @Category(SearchTests.class)
    @TestPointValue(10)
    public void test02BBasicPut() throws InterruptedException {

        StatePredicate showResultsMatch =
            statePredicate("Replicas consistent when network quiescent, different keys",
                           s -> {
                               Command cmd = new Get("key0");
                               boolean msgs = false;
                               for (MessageEnvelope m : s.network())
                                   msgs = true;
                               if (!msgs)
                                   for (int i = 0; i < numServers; i++) {
                                       ScalarClockPutServer si = (ScalarClockPutServer) s.server(servers.get(i));
                                       Result r = si.app.execute(cmd);
                                       if (!(r instanceof GetResult))
                                           return false;
                                       String vi = ((GetResult)r).value();
                                       for (int j = i+1; j < numServers; j++) {
                                           ScalarClockPutServer sj = (ScalarClockPutServer) s.server(servers.get(j));
                                           Result rj = sj.app.execute(cmd);
                                           if (!(rj instanceof GetResult))
                                               return false;
                                           String vj = ((GetResult)rj).value();
                                           if (!vj.equals(vi))
                                               return false;
                                       }
                                   }
                               return true;
                           });
        
        for (int i = 0; i < numServers; i++) 
            initSearchState.addClientWorker(clients.get(i), Workload.emptyWorkload(), true);
        for (int i = 0; i < 1; i++) {
            for (int j = 0; j < numServers; j++) {
                initSearchState.clientWorker(clients.get(j)).addCommand(new Put("key"+j,String.valueOf(j)),new PutOk());
            }
        }
        searchSettings.addInvariant(showResultsMatch).maxTimeSecs(120);
        bfs(initSearchState);
        assertSpaceExhausted();
    }

    @org.junit.Test(timeout = 20 * 1000)
    @PrettyTestName("Do not send too many messages for read-only commands.")
    @TestPointValue(10)
    @Category({RunTests.class})
    public void test03OneServerMessagePerGet() throws InterruptedException {

        StatePredicate oneServerMessagePerGet =
            statePredicate("At most one message to server per read-only command",
                           s -> {
                               int ms = 0;
                               for (int i = 0; i < numServers; i++)
                                   ms += ((RunState)s).numMessagesSentTo(servers.get(i));
                               return ms <= 2 * servers.size();
                           });
        
        for (int i = 0; i < numServers; i++) 
            runState.addClientWorker(clients.get(i), Workload.emptyWorkload(), true);

        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < numServers; j++) {
                runState.clientWorker(clients.get(j)).addCommand(new Get("key"),new GetResult(""));
            }
        }
        runSettings.addInvariant(oneServerMessagePerGet);
        runState.run(runSettings);
    }

    @org.junit.Test(timeout = 20 * 1000)
    @PrettyTestName("Do not send too many messages for update commands.")
    @TestPointValue(10)
    @Category({RunTests.class})
    public void test04TwoServerMessagePerPut() throws InterruptedException {

        StatePredicate twoServerMessagePerPut =
            statePredicate("At most two message to servers per updatecommand",
                           s -> {
                               int ms = 0;
                               for (int i = 0; i < numServers; i++)
                                   ms += ((RunState)s).numMessagesSentTo(servers.get(i));
                               return ms <= 4 * servers.size();
                           });
        
        for (int i = 0; i < numServers; i++) 
            runState.addClientWorker(clients.get(i), Workload.emptyWorkload(), true);

        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < numServers; j++) {
                runState.clientWorker(clients.get(j)).addCommand(new Put("key",String.valueOf(j)),new PutOk());
            }
        }
        runSettings.addInvariant(twoServerMessagePerPut);
        runState.run(runSettings);
    }

    @org.junit.Test(timeout = 20 * 1000)
    @PrettyTestName("Do not send multiple replies to clients.")
    @Category({RunTests.class})
    @TestPointValue(5)
    public void test05OneReplyPerRequest() throws InterruptedException {

        StatePredicate oneReplyPerRequest =
            statePredicate("At most one reply to client per command",
                           s -> {
                               int ms = 0;
                               for (int i = 0; i < numServers; i++)
                                   ms += ((RunState)s).numMessagesSentTo(clients.get(i));
                               return ms <= 2 * numServers;
                           });
        
        for (int i = 0; i < numServers; i++) 
            runState.addClientWorker(clients.get(i), Workload.emptyWorkload(), true);

        for (int j = 0; j < numServers; j++) {
            runState.clientWorker(clients.get(j)).addCommand(new Put("key",String.valueOf(j)),new PutOk());
        }
        for (int j = 0; j < numServers; j++) {
            runState.clientWorker(clients.get(j)).addCommand(new Get("key"),new GetResult(""));
        }
        runSettings.addInvariant(oneReplyPerRequest);
        runState.run(runSettings);
    }

    
    @org.junit.Test(timeout = 20 * 1000)
    @PrettyTestName("Results OK for non-concurrent commands")
    @TestPointValue(10)
    @Category({RunTests.class})
    public void test06ResultsOkNonconcurrent() throws InterruptedException {

        for (int i = 0; i < numServers; i++) 
            runState.addClientWorker(clients.get(i), Workload.emptyWorkload(), true);

        runSettings.addInvariant(RESULTS_OK);
        
        for (int j = 0; j < 2; j++) {
            runState.clientWorker(clients.get(j)).addCommand(new Put("key",String.valueOf(j)),new PutOk());
            runState.clientWorker(clients.get(j)).addCommand(new Get("key"),new GetResult(String.valueOf(j)));
            runState.run(runSettings);
        }
     }

    /*    @Test(timeout = 5 * 1000)
    @PrettyTestName("Multiple clients can append simultaneously")
    @Category({RunTests.class})
    public void test02MultipleClientsPut() throws InterruptedException {
        Workload workload = Workload.builder().parser(new PutParser())
                                    .commandStrings("hello from %a")
                                    .resultStrings("hello from %a").build();

        for (int i = 1; i <= 2; i++) {
            runState.addClientWorker(clients.get(i), workload);
        }

        runSettings.addInvariant(RESULTS_OK);
        runState.run(runSettings);
    }

    @Test(timeout = 5 * 1000)
    @PrettyTestName("Client can still ping if some messages are dropped")
    @Category({RunTests.class, UnreliableTests.class})
    public void test03MessagesDropped() throws InterruptedException {
        runState.addClientWorker(clients.get(1), repeatedPuts(100));

        runSettings.networkUnreliable(true);

        runSettings.addInvariant(RESULTS_OK);
        runState.run(runSettings);
    }

    @Test
    @PrettyTestName("Single client repeatedly pings")
    @Category(SearchTests.class)
    public void test04PutSearch() throws InterruptedException {
        initSearchState.addClientWorker(clients.get(1), repeatedPuts(10));

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
