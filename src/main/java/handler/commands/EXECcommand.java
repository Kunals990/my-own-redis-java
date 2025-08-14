package handler.commands;

import handler.Command;
import handler.CommandContext;
import handler.CommandHandler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

public class EXECcommand implements Command {
    @Override
    public String execute(CommandContext commandContext) throws IOException {

        Queue<List<String>> commands = commandContext.clientState.transactionQueue;

        if(commands==null ||commands.isEmpty()){
            commandContext.clientState.inTransaction=false;
            return "*0\r\n";
        }
        commandContext.clientState.inTransaction=false;
        List<String> results = new ArrayList<>();
        while(!commands.isEmpty()){
            List<String> commandArgs=commands.poll();
            String result = CommandHandler.handle(commandArgs,commandContext.clientState,commandContext.clientChannel);
            results.add(result.trim());
        }

        commandContext.clientState.transactionQueue.clear();

        StringBuilder resp = new StringBuilder();
        resp.append("*").append(results.size()).append("\r\n");
        for(String res:results){
            resp.append(res).append("\r\n");
        }

        return resp.toString();
    }
}
