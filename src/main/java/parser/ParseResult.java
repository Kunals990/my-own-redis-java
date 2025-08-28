package parser;

import java.util.List;

public class ParseResult {

    public static class CommandData {
        private final List<String> commandParts;
        private final int commandSize;

        public CommandData(List<String> commandParts, int commandSize) {
            this.commandParts = commandParts;
            this.commandSize = commandSize;
        }

        public List<String> getCommandParts() {
            return commandParts;
        }

        public int getCommandSize() {
            return commandSize;
        }
    }

    private final List<CommandData> commandDataList;
    private final int consumedBytes;

    public ParseResult(List<CommandData> commandDataList, int consumedBytes) {
        this.commandDataList = commandDataList;
        this.consumedBytes = consumedBytes;
    }

    public List<CommandData> getCommandDataList() {
        return commandDataList;
    }

    public int getConsumedBytes() {
        return consumedBytes;
    }
}