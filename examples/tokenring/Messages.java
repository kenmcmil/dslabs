package tokenring;

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
class Token implements Message {
}

