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
            Map.entry("LRANGE",new LRANGEcommand())
    );

    public static Command getCommand(String name) {
        return commandMap.get(name.toUpperCase());
    }
}