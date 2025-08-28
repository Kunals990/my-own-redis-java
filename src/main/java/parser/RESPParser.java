package parser;

import java.util.ArrayList;
import java.util.List;

public class RESPParser {

    public static ParseResult parse(String input) throws IncompleteCommandException {
        // This now stores our new, more detailed CommandData objects
        List<ParseResult.CommandData> commandDataList = new ArrayList<>();
        int consumed = 0;

        while (consumed < input.length()) {
            int startOfCommand = consumed; // Mark the beginning of the command

            if (input.charAt(startOfCommand) != '*') {
                throw new IllegalArgumentException("Invalid command format: Must start with '*'");
            }

            int endOfLine = input.indexOf("\r\n", startOfCommand);
            if (endOfLine == -1) {
                throw new IncompleteCommandException();
            }

            int numArgs = Integer.parseInt(input.substring(startOfCommand + 1, endOfLine));
            consumed = endOfLine + 2;

            List<String> commandParts = new ArrayList<>();
            for (int i = 0; i < numArgs; i++) {
                if (consumed >= input.length() || input.charAt(consumed) != '$') {
                    throw new IllegalArgumentException("Invalid bulk string format: Must start with '$'");
                }

                endOfLine = input.indexOf("\r\n", consumed);
                if (endOfLine == -1) {
                    throw new IncompleteCommandException();
                }

                int stringLength = Integer.parseInt(input.substring(consumed + 1, endOfLine));
                consumed = endOfLine + 2;

                if (consumed + stringLength + 2 > input.length()) {
                    throw new IncompleteCommandException();
                }

                String argument = input.substring(consumed, consumed + stringLength);
                commandParts.add(argument);
                consumed += stringLength + 2;
            }
            int commandSize = consumed - startOfCommand;
            commandDataList.add(new ParseResult.CommandData(commandParts, commandSize));
        }

        return new ParseResult(commandDataList, consumed);
    }
}