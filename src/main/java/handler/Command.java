package handler;
import java.io.IOException;

public interface Command {
    String execute(CommandContext commandContext) throws IOException;
}