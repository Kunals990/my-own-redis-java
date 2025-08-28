package handler.commands.lists;

import handler.Command;
import handler.CommandContext;
import store.ListStore;

import java.io.IOException;
import java.util.List;

public class LLENcommand implements Command {

    ListStore listStore = ListStore.getInstance();

    @Override
    public String execute(CommandContext commandContext) throws IOException {
        if(commandContext.args.size()<2) return "-ERR wrong number of arguments for 'LLEN'\r\n";

        String key = commandContext.args.get(1);

        List<String> list = listStore.getList(key);

        if(list==null || list.isEmpty()) return ":0\r\n";

        return ":"+list.size()+"\r\n";
    }
}
