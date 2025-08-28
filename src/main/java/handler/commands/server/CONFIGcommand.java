package handler.commands.server;

import config.ServerConfig;
import handler.Command;
import handler.CommandContext;

import java.io.IOException;

public class CONFIGcommand implements Command {

    @Override
    public String execute(CommandContext commandContext) throws IOException {
        if (commandContext.args.size() < 3 || !commandContext.args.get(1).equalsIgnoreCase("GET")) {
            return "-ERR Syntax error\r\n";
        }

        String configKey = commandContext.args.get(2);
        String configValue = null;

        if (configKey.equalsIgnoreCase("dir")) {
            configValue = ServerConfig.getInstance().getDir();
        } else if (configKey.equalsIgnoreCase("dbfilename")) {
            configValue = ServerConfig.getInstance().getDbfilename();
        }

        if (configValue == null) {
            return "*0\r\n";
        }

        String keyResp = "$" + configKey.length() + "\r\n" + configKey + "\r\n";
        String valueResp = "$" + configValue.length() + "\r\n" + configValue + "\r\n";
        return "*2\r\n" + keyResp + valueResp;
    }
}