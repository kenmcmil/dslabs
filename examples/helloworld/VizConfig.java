package helloworld;

import dslabs.framework.Command;
import dslabs.framework.Address;
import dslabs.framework.testing.StateGenerator;
import dslabs.framework.testing.StateGenerator.StateGeneratorBuilder;
import dslabs.framework.testing.Workload;
import dslabs.framework.testing.search.SearchState;
import dslabs.framework.testing.LocalAddress;
import dslabs.framework.testing.junit.Lab;
//import dslabs.framework.testing.visualization.VizConfig;
import helloworld.HelloApplication.Hello;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

import static helloworld.Test.builder;

@Lab("helloworld")
public class VizConfig extends dslabs.framework.testing.visualization.VizConfig {
    @Override
    public SearchState getInitialState(int numServers, int numClients,
                                       List<List<String>> commands) {
        SearchState searchState =
                super.getInitialState(0, numClients, commands);
        searchState.addServer(Test.sa);
        return searchState;
    }

    @Override
    protected StateGenerator stateGenerator(List<Address> servers,
                                            List<Address> clients,
                                            List<List<String>> workload) {
        StateGeneratorBuilder builder = builder();
        //        builder.workloadSupplier(__ -> Workload.workload(
        //        workload.stream().map(Hello::new).collect(Collectors.toList())));
        builder.workloadSupplier(a -> {
                List<Command> cs = new ArrayList<Command>();
                for (String s : workload.get(workload.size()==1?0:clients.indexOf(a))) {
                    List<String> fs = Arrays.asList(s.split(":"));
                    if (fs.size() == 1) {
                        cs.add(new Hello(fs.get(0)));
                    } else if (fs.size() == 2) {
                        if (a.equals(new LocalAddress(fs.get(0)))) {
                            cs.add(new Hello(fs.get(1)));
                        } 
                    } else {
                        throw new IllegalArgumentException();
                    }
                }
                return Workload.workload(cs);
            });
        return builder.build();
    }
}
