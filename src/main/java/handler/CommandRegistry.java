package handler;

import handler.commands.*;

import java.util.Map;

public class CommandRegistry {
    private static final Map<String, Command> commandMap = Map.ofEntries(
            Map.entry("PING", new PINGcommand()),
            Map.entry("ECHO",new ECHOcommand()),
            Map.entry("SET",new SETcommand()),
            Map.entry("GET",new GETcommand()),
            Map.entry("RPUSH",new RPUSHcommand()),
            Map.entry("LRANGE",new LRANGEcommand()),
            Map.entry("LPUSH",new LPUSHcommand()),
            Map.entry("LLEN",new LLENcommand()),
            Map.entry("LPOP",new LPOPcommand()),
            Map.entry("BLPOP",new BLPOPcommand()),
            Map.entry("TYPE",new TYPEcommand()),
            Map.entry("XADD",new XADDcommand()),
            Map.entry("XRANGE",new XRANGEcommand()),
            Map.entry("XREAD",new XREADcommand()),
            Map.entry("INCR",new INCRcommand()),
            Map.entry("MULTI",new MULTIcommand()),
            Map.entry("EXEC",new EXECcommand()),
            Map.entry("INFO",new INFOcommand())
    );

    public static Command getCommand(String name) {
        return commandMap.get(name.toUpperCase());
    }
}