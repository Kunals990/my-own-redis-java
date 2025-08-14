package handler;

import java.nio.channels.SocketChannel;
import java.util.List;

public class CommandContext {
    public final List<String> args;
    public final SocketChannel clientChannel;
    public final ClientState clientState;

    public CommandContext(List<String> args, SocketChannel clientChannel, ClientState clientState) {
        this.args = args;
        this.clientChannel = clientChannel;
        this.clientState = clientState;
    }
}