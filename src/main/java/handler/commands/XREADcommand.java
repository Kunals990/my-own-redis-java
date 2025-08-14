package handler.commands;

import handler.Command;
import protocols.RESPBuilder;
import store.StreamEntry;
import store.StreamStore;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class XREADcommand implements Command {
    private final StreamStore streamStore = StreamStore.getInstance();
    @Override
    public String execute(List<String> args, SocketChannel clientChannel) throws IOException {
        try{
            boolean isBlocking = false;
            long timeout = 0;
            int streamsIndex = -1;


            // Parse XREAD [BLOCK timeout] STREAMS key [key2 ...] ID [ID2 ...]
            for (int i = 1; i < args.size(); i++) {
                if (args.get(i).equalsIgnoreCase("BLOCK")) {
                    isBlocking = true;
                    timeout = Long.parseLong(args.get(i + 1));
                    i++; // skip timeout value
                }
                else if (args.get(i).equalsIgnoreCase("STREAMS")) {
                    streamsIndex = i;
                    break;
                }
            }

            if (streamsIndex == -1 || streamsIndex + 1 >= args.size()) {
                return RESPBuilder.error("ERR syntax error: missing STREAMS section");
            }

            List<String> keys = new ArrayList<>();
            List<String> ids = new ArrayList<>();

            int numKeys = (args.size() - streamsIndex - 1) / 2;
            for (int i = streamsIndex + 1; i <= streamsIndex + numKeys; i++) {
                keys.add(args.get(i));
            }
            for (int i = 0; i < keys.size(); i++) {
                String rawId = args.get(streamsIndex + 1 + numKeys + i);
                if (rawId.equals("$")) {
                    List<StreamEntry> stream = streamStore.getStream(keys.get(i));
                    if (!stream.isEmpty()) {
                        String lastId = stream.get(stream.size() - 1).getId();
                        ids.add(lastId);
                    } else {
                        ids.add("0-0");
                    }
                } else {
                    ids.add(rawId);
                }
            }

            Map<String, List<String>> availableEntries = new LinkedHashMap<>();
            for (int i = 0; i < keys.size(); i++) {
                String streamKey = keys.get(i);
                String id = ids.get(i);
                List<String> entries = streamStore.readRangeAfter(streamKey, id);
                if (!entries.isEmpty()) {
                    availableEntries.put(streamKey, entries);
                }
            }

            if (!availableEntries.isEmpty()) {
                return RESPBuilder.streamEntries(availableEntries);
            }

            return "$-1\r\n";

        }
        catch (Exception e){
            return RESPBuilder.error("ERR " + e.getMessage());
        }
    }
}
