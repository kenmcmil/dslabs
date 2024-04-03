package dslabs.sharedobject;

import dslabs.framework.Application;
import dslabs.framework.Command;
import dslabs.framework.Result;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.ToString;

@ToString
@EqualsAndHashCode
public class AppendApplication implements Application {
    @Data
    public static final class Append implements Command {
        @NonNull private final String value;
    }

    @Data
    public static final class AppendResult implements Result {
    }

    @Data
    public static final class Show implements Command {
        @Override
        public boolean readOnly() {
            return true;
        }
    }

    @Data
    public static final class ShowResult implements Result {
        @NonNull private final String value;
    }

    private String file = "";

    @Override
    public Result execute(Command command) {
        if (command instanceof Append) {
            Append a = (Append) command;
            file += a.value;
            return new AppendResult();
        }
        else if (command instanceof Show){
            Show p = (Show) command;

            return new ShowResult(file);
        } 
        throw new IllegalArgumentException();
    }
}
