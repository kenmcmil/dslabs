package dslabs.append1;

import dslabs.framework.Address;
import dslabs.framework.Node;
import dslabs.framework.Command;
import dslabs.framework.Result;
// import dslabs.dslabs.append1.AppendApplication;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class AppendServer extends Node {
    private final AppendApplication app = new AppendApplication();
    private Address otherServer;
    private Address client;
    private Reply reply;
    
    /* -------------------------------------------------------------------------
        Construction and Initialization
       -----------------------------------------------------------------------*/
    public AppendServer(Address address, Address otherServer) {
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
        reply = new Reply(app.execute(m.command()));
        if (!sender.equals(otherServer) && (m.command() instanceof AppendApplication.Append)) {
            client = sender;
            send(m,otherServer);
        } else {
            send(reply,sender);
        }
    }

    private void handleReply(Reply m, Address sender) {
        send(reply,client);
    }
}
