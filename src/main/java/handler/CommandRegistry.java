package handler;

import handler.commands.*;

import java.util.Map;

public class CommandRegistry {
    private static final Map<String, Command> commandMap = Map.ofEntries(
            Map.entry("PING", new PINGcommand())
    );

    public static Command getCommand(String name) {
        return commandMap.get(name.toUpperCase());
    }
}