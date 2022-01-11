package dslabs.sharedobject;

import dslabs.framework.Address;
import dslabs.framework.Node;
import dslabs.framework.Command;
import dslabs.framework.Result;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import java.util.ArrayList;

/* -------------------------------------------------------------------------
   
In this lab, we will performs operations on a shared object. That is,
two or more users who are far apart want to edit a shared
document. All of the users would like to see the results of their own
updates right away, despite delays in the network. It's OK for users
to temporarily see inconsistent states of the document, owing to
network delay, so long as the inconsistencies are fairly quickly
corrected.

Our specification is as follows:

- Assumption: Ordered, reliable network channels (for example using TCP)
- Guarantee: Eventual consistency

We will use a strategy called "chain replication" to maintain a
consistent ordering of operations on a shared text file.

Our application interface has the following commands:

- `Append(s)`: append string s to the file, returning void
- `Show()`: return the contents of the file

Server 0 is the "primary" replica. All the servers forward client
update requests to the primary server. After performing an update
(`Append`) each server passes the request on to the next host in the
chain. On the other hand, to distribute the load and reduce
communication time, read-only queries (`Show`) are performed directly
on the client's local server.

Fill in the "TODO" regions below.

To test the implementation, use this command (in the main `dslabs`
directory:

   $ ./run-tests --lab 1 --part 1


Questions to think about:

- What determines the update order in this algorithm?
- When is it safe to reply to an append request? 
- Does this algorithm have disadvantages from a user point of view? 
- What assumptions on the network are needed? Why?

   -----------------------------------------------------------------------*/

@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class ChainRepAppendServer extends Node {
    private final AppendApplication app = new AppendApplication();
    private ArrayList<Address> servers;
    private Address client;
    private Reply reply;
    
    /* -------------------------------------------------------------------------
        Construction and Initialization.

        The 'self' parameter gives the number of the server in the
        chain (starting with zero). The address of server number n in the
        chain can be obtained as server(n). 
        
       -----------------------------------------------------------------------*/
    public ChainRepAppendServer(Address address, ArrayList<Address> servers) {
        super(address);
        this.servers = servers;
    }

    @Override
    public void init() {
        // No initialization necessary
    }

    /* -------------------------------------------------------------------------
        Message Handlers
       -----------------------------------------------------------------------*/
    private void handleRequest(Request m, Address sender) {
        /* TODO: handle request from a client here */
        if (m.command().readOnly()) {
            /* TODO: Handle read-only queries (i.e., Show) here. */
        } else {
            /* TODO: Handle updates (i.e., Append) here. */
        }
    }

    /* TODO: handle messages from other servers here. */
}
