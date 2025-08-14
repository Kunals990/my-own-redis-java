package handler.commands;

import handler.BlockingClientManager;
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
    private final BlockingClientManager blockingManager = BlockingClientManager.getInstance();
    @Override
    public String execute(List<String> args, SocketChannel clientChannel) throws IOException {
        try{
            boolean isBlocking = false;
            long timeout = 0;
            int streamsIndex = -1;

            for (int i = 1; i < args.size(); i++) {
                if (args.get(i).equalsIgnoreCase("BLOCK")) {
                    isBlocking = true;
                    timeout = Long.parseLong(args.get(i + 1));
                    i++;
                } else if (args.get(i).equalsIgnoreCase("STREAMS")) {
                    streamsIndex = i;
                    break;
                }
            }

            if (streamsIndex == -1) {
                return RESPBuilder.error("ERR syntax error: missing STREAMS section");
            }

            int numKeys = (args.size() - streamsIndex - 1) / 2;
            if (numKeys <= 0) {
                return RESPBuilder.error("ERR syntax error: must specify at least one stream");
            }

            List<String> keys = new ArrayList<>();
            for (int i = 0; i < numKeys; i++) {
                keys.add(args.get(streamsIndex + 1 + i));
            }

            List<String> ids = new ArrayList<>();
            for (int i = 0; i < numKeys; i++) {
                String rawId = args.get(streamsIndex + 1 + numKeys + i);
                if (rawId.equals("$")) {
                    StreamEntry lastEntry = streamStore.getLastEntry(keys.get(i));
                    ids.add(lastEntry != null ? lastEntry.getId() : "0-0");
                } else {
                    ids.add(rawId);
                }
            }

            Map<String, String> keysAndStartIds = new LinkedHashMap<>();
            for (int i = 0; i < keys.size(); i++) {
                keysAndStartIds.put(keys.get(i), ids.get(i));
            }


            Map<String, List<String>> availableEntries = new LinkedHashMap<>();
            for (Map.Entry<String, String> entry : keysAndStartIds.entrySet()) {
                String streamKey = entry.getKey();
                String startId = entry.getValue();
                List<String> entries = streamStore.readRangeAfter(streamKey, startId);
                if (!entries.isEmpty()) {
                    availableEntries.put(streamKey, entries);
                }
            }

            if (!availableEntries.isEmpty()) {
                return RESPBuilder.streamEntries(availableEntries);
            }

            if (isBlocking) {
                blockingManager.addBlockedStreamClient(keysAndStartIds, clientChannel, timeout);
                return null;
            }

            return "$-1\r\n";

        }
        catch (Exception e){
            return RESPBuilder.error("ERR " + e.getMessage());
        }
    }
}
