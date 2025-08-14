package parser;

import java.util.ArrayList;
import java.util.List;

public class RESPParser {

    public static ParseResult parse(String input) throws IncompleteCommandException {
        List<List<String>> commands = new ArrayList<>();
        int consumed = 0;

        while (consumed < input.length()) {
            int startOfCommand = consumed;

            if (input.charAt(startOfCommand) != '*') {
                throw new IllegalArgumentException("Invalid command format: Must start with '*'");
            }

            int endOfLine = input.indexOf("\r\n", startOfCommand);
            if (endOfLine == -1) {
                throw new IncompleteCommandException();
            }

            // Parse the number of arguments in the array
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

                // Parse the length of the bulk string
                int stringLength = Integer.parseInt(input.substring(consumed + 1, endOfLine));
                consumed = endOfLine + 2;

                // Check if the entire bulk string has arrived
                if (consumed + stringLength + 2 > input.length()) {
                    throw new IncompleteCommandException();
                }

                // Extract the bulk string
                String argument = input.substring(consumed, consumed + stringLength);
                commandParts.add(argument);
                consumed += stringLength + 2; // Add 2 for the final \r\n
            }
            commands.add(commandParts);
        }

        return new ParseResult(commands, consumed);
    }
}