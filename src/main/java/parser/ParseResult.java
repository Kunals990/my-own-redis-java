package parser;

import java.util.List;

public class ParseResult {
    private final List<List<String>> commands;
    private final int consumedBytes;

    public ParseResult(List<List<String>> commands, int consumedBytes) {
        this.commands = commands;
        this.consumedBytes = consumedBytes;
    }

    public List<List<String>> getCommands() {
        return commands;
    }

    public int getConsumedBytes() {
        return consumedBytes;
    }
}