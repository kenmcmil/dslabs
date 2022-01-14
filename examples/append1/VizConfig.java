package append1;

import dslabs.framework.Address;
import dslabs.framework.testing.StateGenerator;
import dslabs.framework.testing.StateGenerator.StateGeneratorBuilder;
import dslabs.framework.testing.Workload;
import dslabs.framework.testing.search.SearchState;
//import dslabs.framework.testing.visualization.VizConfig;
import append1.AppendApplication.Append;
import java.util.List;
import java.util.stream.Collectors;

import static append1.Test.builder;
//import static append1.Test.sas;

public class VizConfig extends dslabs.framework.testing.visualization.VizConfig {
    @Override
    public SearchState getInitialState(int numServers, int numClients,
                                       List<String> commands) {
        SearchState searchState =
                super.getInitialState(0, numClients, commands);
        for (Address sa : Test.sas) {
            searchState.addServer(sa);
        }
        return searchState;
    }

    @Override
    protected StateGenerator stateGenerator(List<String> workload) {
        StateGeneratorBuilder builder = builder();
        builder.workloadSupplier(__ -> Workload.workload(
                workload.stream().map(Append::new).collect(Collectors.toList())));
        return builder.build();
    }
}
