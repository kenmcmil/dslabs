package tokenring;

import dslabs.framework.Address;
import dslabs.framework.Node;
import dslabs.framework.Command;
import dslabs.framework.Result;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class TokenServer extends Node {
    private Address nextServer;
    private boolean initToken;
    private Address requester = null;

    
    /* -------------------------------------------------------------------------
        Construction and Initialization
        -----------------------------------------------------------------------*/
    public TokenServer(Address address, boolean initToken, Address nextServer) {
        super(address);
        this.nextServer = nextServer;
        this.initToken = initToken;
    }

    @Override
    public void init() {
        if (initToken)
            send(new Token(),nextServer);
    }
    
    /* -------------------------------------------------------------------------
       Message Handlers
       -----------------------------------------------------------------------*/
    private void handleAcquireRequest(AcquireRequest m, Address sender) {
        // Assume: requester == null
        requester = sender;
    }
    
    private void handleReleaseRequest(ReleaseRequest m, Address sender) {
        // Assume: no tokens and requester == null
        send(new ReleaseReply(), sender);
        send(new Token(), nextServer);
    }
    
    private void handleToken(Token m, Address sender) {
        // Assume: not acquired
        if (requester != null) {
            send(new AcquireReply(), requester);
            requester = null;
        } else {
            send(new Token(), nextServer);
        }
    }
}
