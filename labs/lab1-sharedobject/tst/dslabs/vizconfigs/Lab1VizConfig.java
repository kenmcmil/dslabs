package dslabs.vizconfigs;

import dslabs.framework.Command;
import dslabs.framework.Address;
import dslabs.framework.testing.StateGenerator;
import dslabs.framework.testing.StateGenerator.StateGeneratorBuilder;
import dslabs.framework.testing.Workload;
import dslabs.framework.testing.search.SearchState;
import dslabs.framework.testing.LocalAddress;
import dslabs.framework.testing.junit.Lab;
import dslabs.framework.testing.junit.Part;
//import dslabs.framework.testing.visualization.VizConfig;
import dslabs.kvstore.KVStore.Append;
import dslabs.kvstore.KVStore.Get;
import dslabs.kvstore.KVStoreWorkload;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

@Lab("1")
@Part(1)
public class Lab1VizConfig extends dslabs.framework.testing.visualization.VizConfig {
    StateGeneratorBuilder builder() {
        return dslabs.sharedobject.ChainRepAppendTest.builder();
    }
    @Override
    public SearchState getInitialState(int numServers, int numClients,
                                       List<List<String>> commands) {
        SearchState searchState =
                super.getInitialState(0, numClients, commands);
        for (Address sa : dslabs.sharedobject.ChainRepAppendTest.servers) {
            searchState.addServer(sa);
        }
        return searchState;
    }
    @Override
    protected StateGenerator stateGenerator(List<Address> servers,
                                            List<Address> clients,
                                            List<List<String>> workload) {
        StateGeneratorBuilder builder = builder();
        // builder.workloadSupplier(a -> {
        //         List<Command> cs = new ArrayList<Command>();
        //         for (String s : workload.get(workload.size()==1?0:clients.indexOf(a))) {
        //             List<String> fs = Arrays.asList(s.split(":"));
        //             if (fs.size() == 2 && fs.get(0).equals("Get")) {
        //                 cs.add(new Get(fs.get(1)));
        //             } else if (fs.size() == 3 && fs.get(0).equals("Append")) {
        //                 cs.add(new Append(fs.get(1),fs.get(2)));
        //             } else { 
        //                 throw new IllegalArgumentException();
        //             }
        //         }
        //         return Workload.workload(cs);
        //     });
        //        builder.workloadSupplier(__ -> Workload.workload(
        //        workload.stream().map(Append::new).collect(Collectors.toList())));

        builder.workloadSupplier(a ->
            KVStoreWorkload.builder().commandStrings(workload.get(clients.indexOf(a))).build());
        return builder.build();
    }
}

