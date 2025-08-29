package handler;

import handler.commands.keys.KEYScommand;
import handler.commands.lists.*;
import handler.commands.pubsub.PUBLISHcommand;
import handler.commands.pubsub.SUBSCRIBEcommand;
import handler.commands.pubsub.UNSUBSCRIBEcommand;
import handler.commands.server.*;
import handler.commands.sets.ZADDcommand;
import handler.commands.streams.XADDcommand;
import handler.commands.streams.XRANGEcommand;
import handler.commands.streams.XREADcommand;
import handler.commands.strings.GETcommand;
import handler.commands.strings.INCRcommand;
import handler.commands.strings.SETcommand;
import handler.commands.strings.TYPEcommand;
import handler.commands.transactions.EXECcommand;
import handler.commands.transactions.MULTIcommand;

import java.util.Map;

public class CommandRegistry {
    private static final Map<String, Command> commandMap = Map.ofEntries(
            Map.entry("PING", new PINGcommand()),
            Map.entry("ECHO",new ECHOcommand()),
            Map.entry("SET",new SETcommand()),
            Map.entry("GET",new GETcommand()),
            Map.entry("RPUSH",new RPUSHcommand()),
            Map.entry("LRANGE",new LRANGEcommand()),
            Map.entry("LPUSH",new LPUSHcommand()),
            Map.entry("LLEN",new LLENcommand()),
            Map.entry("LPOP",new LPOPcommand()),
            Map.entry("BLPOP",new BLPOPcommand()),
            Map.entry("TYPE",new TYPEcommand()),
            Map.entry("XADD",new XADDcommand()),
            Map.entry("XRANGE",new XRANGEcommand()),
            Map.entry("XREAD",new XREADcommand()),
            Map.entry("INCR",new INCRcommand()),
            Map.entry("MULTI",new MULTIcommand()),
            Map.entry("EXEC",new EXECcommand()),
            Map.entry("INFO",new INFOcommand()),
            Map.entry("REPLCONF",new REPLCONFcommand()),
            Map.entry("PSYNC",new PSYNCcommand()),
            Map.entry("WAIT",new WAITcommand()),
            Map.entry("CONFIG",new CONFIGcommand()),
            Map.entry("KEYS",new KEYScommand()),
            Map.entry("SUBSCRIBE",new SUBSCRIBEcommand()),
            Map.entry("PUBLISH",new PUBLISHcommand()),
            Map.entry("UNSUBSCRIBE",new UNSUBSCRIBEcommand()),
            Map.entry("ZADD",new ZADDcommand())
    );

    public static Command getCommand(String name) {
        return commandMap.get(name.toUpperCase());
    }
}