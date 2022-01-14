package dslabs.framework;

import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public abstract class StandardClient extends Node implements Client {
    Result result = null;

    public StandardClient(Address address) {
        super(address);
    }

    @Override
    public synchronized boolean hasResult() {
        return result != null;
    }

    @Override
    public synchronized Result getResult() throws InterruptedException {
        while (result == null) {
            wait();
        }
        Result r = result;
        result = null;
        return r;
    }

    /* -------------------------------------------------------------------------
        Subclass calls this to return result to user
       -----------------------------------------------------------------------*/
    protected void recvResult(Result res, Address sender) {
        result = res;
        notify();
    }

}
