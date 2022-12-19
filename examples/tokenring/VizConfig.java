package tokenring;

import dslabs.framework.Command;
import dslabs.framework.Address;
import dslabs.framework.testing.StateGenerator;
import dslabs.framework.testing.StateGenerator.StateGeneratorBuilder;
import dslabs.framework.testing.Workload;
import dslabs.framework.testing.search.SearchState;
import dslabs.framework.testing.LocalAddress;
import dslabs.framework.testing.junit.Lab;
//import dslabs.framework.testing.visualization.VizConfig;
import append1.AppendApplication.Append;
import append1.AppendApplication.Show;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

import static tokenring.Test.builder;
//import static append1.Test.sas;

@Lab("tokenring")
public class VizConfig extends dslabs.framework.testing.visualization.VizConfig {
    @Override
    public SearchState getInitialState(int numServers, int numClients,
                                       List<List<String>> commands) {
        SearchState searchState =
                super.getInitialState(0, numClients, commands);
        for (Address sa : Test.sas) {
            searchState.addServer(sa);
        }
        return searchState;
    }

    @Override
    protected StateGenerator stateGenerator(List<Address> servers,
                                            List<Address> clients,
                                            List<List<String>> workload) {
        StateGeneratorBuilder builder = builder();
        //        builder.workloadSupplier(__ -> Workload.workload(
        //        workload.stream().map(Append::new).collect(Collectors.toList())));
        builder.workloadSupplier(a -> {
                List<Command> cs = new ArrayList<Command>();
                for (String s : workload.get(workload.size()==1?0:clients.indexOf(a))) {
                    List<String> fs = Arrays.asList(s.split(":"));
                    if (fs.size() == 1) {
                        cs.add(new Append(fs.get(0)));
                    } else if (fs.size() == 2 && fs.get(1).equals("Show")) {
                        System.out.println("fs: " + fs + " a: " + a);
                        if (a.equals(new LocalAddress(fs.get(0)))) {
                            cs.add(new Show());
                        } 
                    } else if (fs.size() == 3 && fs.get(1).equals("Append")) {
                        System.out.println("fs: " + fs + " a: " + a);
                        if (a.equals(new LocalAddress(fs.get(0)))) {
                            cs.add(new Append(fs.get(2)));
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
