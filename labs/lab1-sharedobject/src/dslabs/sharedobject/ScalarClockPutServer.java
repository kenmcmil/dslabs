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

/* -------------------------------------------------------------------------
   Lexicographically ordered clock
   
   Use this class for a clock value that is ordered primarily by
   an integer time value and secondarily by the server id.
   If x and y are ClockTuples, then x.lessThan(y) is true when
   x is lexicographically less than y.
   
   -----------------------------------------------------------------------*/

@Data
class ClockTuple {
    private final int time;
    private final Address server;
    public boolean lessThan(ClockTuple x) {
        return time < x.time || time == x.time() && server.toString().compareTo(x.server().toString()) < 0;
    }
}

@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class ScalarClockPutServer extends Node {
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
    public ScalarClockPutServer(Address address, Address otherServer) {
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

    /* TODO: Handle other message types here */

}
