package lamportdme;

import dslabs.framework.Address;
import dslabs.framework.Node;
import dslabs.framework.Command;
import dslabs.framework.Result;
import dslabs.framework.Application;
import dslabs.framework.Message;
import dslabs.kvstore.KVStore;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Data;
import java.util.List;
import java.util.HashMap;

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
public class LamportDMEServer extends Node {
    private List<Address> servers;

    public enum State {Idle, Waiting,Critical}; 

    private int time = 0;
    public State state = State.Idle; 
    private HashMap<Address,ClockTuple> requestTime = new HashMap<Address,ClockTuple>();
    private HashMap<Address,ClockTuple> replyTime = new HashMap<Address,ClockTuple>();
    private Address client;
    
    public LamportDMEServer(Address address, List<Address> servers) {
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
    private void handleAcquireRequest(AcquireRequest m, Address sender) {
        time++;
        broadcast(new ServerRequest(time));
        requestTime.put(address(),new ClockTuple(time,address()));
        state = State.Waiting;
        client = sender;
    }

    /* When we receive a `request` message from another server, we put
       it on our request queue, and return a reply message to the
       sender. */

    private void handleServerRequest(ServerRequest m, Address sender) {
        updateTime(m.time());
        requestTime.put(sender,new ClockTuple(m.time(),sender));
        send(new ServerReply(time), sender);
        tryCritical();
    }

    /* On a reply from another server, we update the highest timestamp
       received from this sender. Because of in-order delivery, the
       timestamps are received in increasing order, so the incoming
       one must be the greatest so far. */
        
    private void handleServerReply(ServerReply m, Address sender) {
        updateTime(m.time());
        replyTime.put(sender,new ClockTuple(m.time(),sender));
        tryCritical();
    }

    /* When we receive a `release` message from another server, the
       sender's request must be at the head of our queue.  We dequeue
       it. */

    private void handleServerRelease(ServerRelease m, Address sender) {
        updateTime(m.time());
        requestTime.remove(sender);
        tryCritical();
    }

    /* When the client releases, we remove our request from the queue,
       broadcast a release to other servers, set our state to idle
       and reply to the client. */
       

    private void handleReleaseRequest(ReleaseRequest m, Address sender) {
        time++;
        requestTime.remove(address());
        broadcast(new ServerRelease(time));
        state = State.Idle;
        send(new ReleaseReply(),sender);
    }
    

    /* 
       Having proceesed an incoming message, we might now be able
       to enter our critical section. We do this if:
       
       - We are in the waiting state
       - Our request message has the least timestamp in lexicographic order
       - Every host has sent a reply later than our request
       
    */


    private void tryCritical() {
        if (state == State.Waiting) {
            for (Address other : servers) {
                if (!other.equals(address())) {

                    if (requestTime.containsKey(other)
                        && requestTime.get(other).lessThan(requestTime.get(address())))
                        return;

                    if (!(replyTime.containsKey(other)
                          && replyTime.get(other).time() > requestTime.get(address()).time()))
                        return;
                }
            }
            state = State.Critical;
            send(new AcquireReply(), client);
        }
    }

    /* Broadcast a message to all servers (except self) */

    private void broadcast(Message m) {
        for (Address addr : servers) {
            if (!addr.equals(address()))
                send(m,addr);
        }
    }

    /* Make a clock tuple from local time and self address */

    private ClockTuple myTime(int time) {
        return new ClockTuple(time,address());
    }

    /* Make a clock tuple from local time and other address */

    private ClockTuple otherTime(int time, Address addr) {
        return new ClockTuple(time,addr);
    }
    

    /* Update our logical time to reflect the incoming
       message. */

    private void updateTime(int t) {
        time = Math.max(time,t) + 1;
    }



}
