package dslabs.sharedobject;

import dslabs.framework.Message;
import dslabs.framework.Command;
import dslabs.framework.Result;
import dslabs.framework.Address;
import lombok.Data;

@Data
class Request implements Message {
    private final Command command;
}

@Data
class Reply implements Message {
    private final Result result;
}

@Data
class ServerRequest implements Message {
    private final Address client;
    private final Command command;
}
