package handler.commands;

import handler.Command;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.List;

public class BLPOPcommand implements Command {
    @Override
    public String execute(List<String> args, SocketChannel clientChannel) throws IOException {
        return "";
    }
}
