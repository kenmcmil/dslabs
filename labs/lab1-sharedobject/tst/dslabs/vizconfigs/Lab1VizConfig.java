package dslabs.vizconfigs;

import dslabs.framework.Command;
import dslabs.framework.testing.StateGenerator;
import dslabs.framework.testing.StateGenerator.StateGeneratorBuilder;
import dslabs.framework.testing.Workload;
import dslabs.framework.testing.search.SearchState;
import dslabs.framework.testing.LocalAddress;
//import dslabs.framework.testing.visualization.VizConfig;
import dslabs.kvstore.KVStore.Append;
import dslabs.kvstore.KVStore.Get;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

import static dslabs.sharedobject.ChainRepAppendTest.builder;

public class Lab1VizConfig extends dslabs.framework.testing.visualization.VizConfig {
    @Override
    public SearchState getInitialState(int numServers, int numClients,
                                       List<String> commands) {
        SearchState searchState =
                super.getInitialState(0, numClients, commands);
        return searchState;
    }

    @Override
    protected StateGenerator stateGenerator(List<String> workload) {
        StateGeneratorBuilder builder = builder();
        builder.workloadSupplier(a -> {
                List<Command> cs = new ArrayList<Command>();
                for (String s : workload) {
                    List<String> fs = Arrays.asList(s.split(":"));
                    if (fs.size() == 1) {
                        cs.add(new Append("k",fs.get(0)));
                    } else if (fs.size() == 2 && fs.get(1).equals("Get")) {
                        System.out.println("fs: " + fs + " a: " + a);
                        if (a.equals(new LocalAddress(fs.get(0)))) {
                            cs.add(new Get("k"));
                        } 
                    } else if (fs.size() == 3 && fs.get(1).equals("Append")) {
                        System.out.println("fs: " + fs + " a: " + a);
                        if (a.equals(new LocalAddress(fs.get(0)))) {
                            cs.add(new Append("k",fs.get(2)));
                        } 
                    } else {
                        throw new IllegalArgumentException();
                    }
                }
                return Workload.workload(cs);
            });
        //        builder.workloadSupplier(__ -> Workload.workload(
        //        workload.stream().map(Append::new).collect(Collectors.toList())));
        return builder.build();
    }
}
