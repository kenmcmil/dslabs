package dslabs.sharedobject;

import dslabs.framework.Address;
import dslabs.framework.Node;
import dslabs.framework.Command;
import dslabs.framework.Result;
import dslabs.framework.Application;
import dslabs.kvstore.KVStore;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Data;
import java.util.ArrayList;


@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class ScalarClockAppendServer extends Node {
    public final Application app = new KVStore();
    private Address otherServer;

    // TODO: add some state here
    
    /* -------------------------------------------------------------------------
        Construction and Initialization.

        The 'servers' parameter gives the address of all of the
        servers in the chain (starting with zero). The address of
        server number n in the chain can be obtained as
        servers.get(n). Our own index in the chain is stored in
        'myIndex'.
        
       -----------------------------------------------------------------------*/
    public ScalarClockAppendServer(Address address, Address otherServer) {
        super(address);
        this.otherServer = otherServer;
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
