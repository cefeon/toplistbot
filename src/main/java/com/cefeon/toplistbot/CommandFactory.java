package com.cefeon.toplistbot;
import java.util.HashMap;
import java.util.Map;

public class CommandFactory {
    private final Map<String, Command> commandMap = new HashMap<>();
    public CommandFactory() {
        commandMap.put("!toplist", new ToplistCommand());
        commandMap.put("!blacklist", new BlacklistCommand());
        commandMap.put("!test", new TestCommand());
    }

    public Map<String, Command> getCommandMap() {
        return commandMap;
    }

    public Command getCommand(String command) {
        return commandMap.get(command);
    }
}
