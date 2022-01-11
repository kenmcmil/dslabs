package dslabs.kvstore;

import dslabs.framework.Application;
import dslabs.framework.Command;
import dslabs.framework.Result;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.ToString;
import java.util.HashMap;

@ToString
@EqualsAndHashCode
public class KVStore implements Application {

    public interface KVStoreCommand extends Command {
    }

    public interface SingleKeyCommand extends KVStoreCommand {
        String key();
    }

    @Data
    public static final class Get implements SingleKeyCommand {
        @NonNull private final String key;

        @Override
        public boolean readOnly() {
            return true;
        }
    }

    @Data
    public static final class Put implements SingleKeyCommand {
        @NonNull private final String key, value;
    }

    @Data
    public static final class Append implements SingleKeyCommand {
        @NonNull private final String key, value;
    }

    public interface KVStoreResult extends Result {
    }

    @Data
    public static final class GetResult implements KVStoreResult {
        @NonNull private final String value;
    }

    @Data
    public static final class KeyNotFound implements KVStoreResult {
    }

    @Data
    public static final class PutOk implements KVStoreResult {
    }

    @Data
    public static final class AppendResult implements KVStoreResult {
        @NonNull private final String value;
    }

    HashMap<String, String> mem = new HashMap<String,String>();

    @Override
    public KVStoreResult execute(Command command) {
        if (command instanceof Get) {
            Get g = (Get) command;
            if (mem.containsKey(g.key)) {
                return new GetResult(mem.get(g.key));
            } else {
                return new KeyNotFound();
            }
        }

        if (command instanceof Put) {
            Put p = (Put) command;
            mem.put(p.key,p.value);
            return new PutOk();
        }

        if (command instanceof Append) {
            Append a = (Append) command;
            String val = "";
            if (mem.containsKey(a.key)) {
                val = mem.get(a.key);
            }
            val += a.value;
            mem.put(a.key,val);
            return new AppendResult(val);
        }

        throw new IllegalArgumentException();
    }
}
