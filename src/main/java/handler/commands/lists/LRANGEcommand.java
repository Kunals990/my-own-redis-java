package handler.commands.lists;

import handler.Command;
import handler.CommandContext;
import store.ListStore;

import java.io.IOException;
import java.util.List;

public class LRANGEcommand implements Command {

    ListStore listStore = ListStore.getInstance();

    @Override
    public String execute(CommandContext commandContext) throws IOException {
        if(commandContext.args.size()<4) return "-ERR wrong number of arguments for 'LRANGE'\r\n";

        String key = commandContext.args.get(1);
        List<String> list = listStore.getList(key);

        if (list == null || list.isEmpty()) return "*0\r\n";

        int size = list.size();
        int start = Integer.parseInt(commandContext.args.get(2));
        int stop = Integer.parseInt(commandContext.args.get(3));

        // Handle negative indices
        if (start < 0) start = size + start;
        if (stop < 0) stop = size + stop;

        // Clamp indices to bounds
        start = Math.max(0, start);
        stop = Math.min(size - 1, stop);

        if (start > stop) return "*0\r\n";

        StringBuilder result = new StringBuilder("*" + (stop - start + 1) + "\r\n");

        for (int i = start; i <= stop; i++) {
            String element = list.get(i);
            result.append("$").append(element.length()).append("\r\n");
            result.append(element).append("\r\n");
        }

        return result.toString();
    }
}
