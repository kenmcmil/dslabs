package dslabs.sharedobject;

import dslabs.framework.Address;
import dslabs.framework.Node;
import dslabs.framework.Command;
import dslabs.framework.Result;
import dslabs.framework.Application;
import dslabs.kvstore.KVStore;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import java.util.ArrayList;


@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class ChainRepAppendServer extends Node {
    private final Application app = new KVStore();
    private ArrayList<Address> servers;
    private int myIndex;
    
    /* -------------------------------------------------------------------------
        Construction and Initialization.

        The 'servers' parameter gives the address of all of the
        servers in the chain (starting with zero). The address of
        server number n in the chain can be obtained as
        servers.get(n). Our own index in the chain is stored in
        'myIndex'.
        
       -----------------------------------------------------------------------*/
    public ChainRepAppendServer(Address address, ArrayList<Address> servers) {
        super(address);
        this.servers = servers;
        for (int i = 0; i < servers.size(); i++)
            if (address().equals(servers.get(i)))
                this.myIndex = i;
    }

    @Override
    public void init() {
        // No initialization necessary
    }

    /* -------------------------------------------------------------------------
        Message Handlers
       -----------------------------------------------------------------------*/
    private void handleRequest(Request m, Address sender) {
        if (m.command().readOnly()) {
            /* TODO: Handle read-only queries locally. */
        } else {
            /* TODO: Handle updates here by forwarding to primary server */
        }
    }

    private void handleServerRequest(ServerRequest m, Address sender) {
        /* TODO: handle requests from other servers here. */
    }

}
