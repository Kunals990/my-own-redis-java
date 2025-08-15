package handler.commands;

import config.ReplicationInfo;
import handler.Command;
import handler.CommandContext;

import java.io.IOException;
import java.nio.ByteBuffer;

public class PSYNCcommand implements Command {

    ReplicationInfo replicationInfo = ReplicationInfo.getInstance();
    private static final String RDB_HEX = "524544495330303131fa0972656469732d76657205372e322e30fa0a72656469732d62697473c040fa056374696d65c26d08bc65fa08757365642d6d656dc2b0c41000fa08616f662d62617365c000fff06e3bfec0ff5aa2";

    @Override
    public String execute(CommandContext commandContext) throws IOException {
        if (commandContext.args.size() < 3) {
            return "-ERR wrong number of arguments for 'psync' command\r\n";
        }
        if (commandContext.args.get(1).equals("?") && commandContext.args.get(2).equals("-1")) {
            String fullResyncResponse = "+FULLRESYNC " + replicationInfo.getMasterReplid() + " 0\r\n";
            byte[] rdbFileBytes = hexStringToByteArray(RDB_HEX);
            String rdbHeader = "$" + rdbFileBytes.length + "\r\n";

            commandContext.clientChannel.write(ByteBuffer.wrap(fullResyncResponse.getBytes()));
            commandContext.clientChannel.write(ByteBuffer.wrap(rdbHeader.getBytes()));
            commandContext.clientChannel.write(ByteBuffer.wrap(rdbFileBytes));
        }

        return null;
    }
    private static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }
}
