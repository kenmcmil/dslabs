package dslabs.tokenring;

import dslabs.framework.Address;
import dslabs.framework.StandardClient;
import dslabs.framework.Command;
import dslabs.framework.Node;
import dslabs.framework.Result;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Data;


@Data
class MutexCommand implements Command {
}

@Data
class MutexResult implements Result {
}


@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
class TokenClient extends StandardClient {
    private final Address serverAddress;

    boolean acquiredState = false;

    /* -------------------------------------------------------------------------
        Construction and Initialization
       -----------------------------------------------------------------------*/
    public TokenClient(Address address, Address serverAddress) {
        super(address);
        this.serverAddress = serverAddress;
    }

    @Override
    public synchronized void init() {
        // No initialization necessary
    }

    /* -------------------------------------------------------------------------
        Client Methods
       -----------------------------------------------------------------------*/
    @Override
    public synchronized void sendCommand(Command command) {
        if (!acquiredState)
            send(new AcquireRequest(), serverAddress);
        else {
            acquiredState = false;
            send(new ReleaseRequest(), serverAddress);
        }
    }

    private synchronized void handleAcquireReply(AcquireReply m, Address sender) {
        acquiredState = true;
        recvResult(new MutexResult(),sender);
    }

    private synchronized void handleReleaseReply(ReleaseReply m, Address sender) {
        recvResult(new MutexResult(),sender);
    }

    /* 
       Invariant:

       There is exactly one of:
       - A Token, AcquireReply or ReleaseRequest in the network
       - A client with acquiredState == true

       
     */

}
