package dslabs.lamportdme;

import dslabs.framework.Message;
import lombok.Data;

@Data
class AcquireRequest implements Message {
}

@Data
class AcquireReply implements Message {
}

@Data
class ReleaseRequest implements Message {
}

@Data
class ReleaseReply implements Message {
}

@Data
class ServerRequest implements Message {
    private final int time;
}

@Data
class ServerReply implements Message {
    private final int time;
}

@Data
class ServerRelease implements Message {
    private final int time;
}
