package dslabs.helloworld;

import dslabs.framework.Application;
import dslabs.framework.Address;
import dslabs.framework.StandardClient;
import dslabs.framework.Command;
import dslabs.framework.Node;
import dslabs.framework.Result;
import dslabs.framework.Message;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.ToString;


@ToString
@EqualsAndHashCode
class HelloApplication implements Application {
    @Data
    public static final class Hello implements Command {
        @NonNull private final String value;
    }

    @Data
    public static final class HelloResult implements Result {
        @NonNull private final String value;
    }

    private int counter = 0;

    @Override
    public Result execute(Command command) {
        if (command instanceof Hello) {
            Hello c = (Hello) command;
            return new HelloResult(String.format("Hello, %s. You are number %d.",c.value(),counter++));
        }
        throw new IllegalArgumentException();
    }
}

@Data
class Request implements Message {
    private final Command command;
}

@Data
class Reply implements Message {
    private final Result result;
}


@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
class HelloServer extends Node {
    private final Application app = new HelloApplication();
    
    public HelloServer(Address address) {
        super(address);
    }

    private void handleRequest(Request m, Address sender) {
        Reply reply = new Reply(app.execute(m.command()));
        send(reply,sender);
    }
}


@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
class HelloClient extends StandardClient {
    private final Address serverAddress;

    public HelloClient(Address address, Address serverAddress) {
        super(address);
        this.serverAddress = serverAddress;
    }

    @Override
    public synchronized void sendCommand(Command command) {
        send(new Request(command), serverAddress);
    }

    private synchronized void handleReply(Reply m, Address sender) {
        recvResult(m.result(),sender);
    }

}
