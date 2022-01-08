package append1;

import dslabs.framework.Message;
import dslabs.framework.Command;
import dslabs.framework.Result;
import lombok.Data;

@Data
class Request implements Message {
    private final Command command;
}

@Data
class Reply implements Message {
    private final Result result;
}

