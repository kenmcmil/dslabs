package dslabs.vizconfigs;

import dslabs.framework.testing.StateGenerator;
import dslabs.framework.testing.StateGenerator.StateGeneratorBuilder;
import dslabs.framework.testing.Workload;
import dslabs.framework.testing.search.SearchState;
//import dslabs.framework.testing.visualization.VizConfig;
import dslabs.sharedobject.AppendApplication.Append;
import java.util.List;
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
        builder.workloadSupplier(__ -> Workload.workload(
                workload.stream().map(Append::new).collect(Collectors.toList())));
        return builder.build();
    }
}
