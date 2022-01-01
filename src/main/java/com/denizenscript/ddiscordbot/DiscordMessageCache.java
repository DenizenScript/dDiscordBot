package com.denizenscript.ddiscordbot;

import net.dv8tion.jda.api.entities.Message;

import java.util.LinkedHashMap;

public class DiscordMessageCache {

    public LinkedHashMap<Long, Message> cacheMap = new LinkedHashMap<>();

    public void add(Message message) {
        if (DenizenDiscordBot.messageCacheSize < 1) {
            return;
        }
        long id = message.getIdLong();
        cacheMap.put(id, message);
        if (cacheMap.size() > DenizenDiscordBot.messageCacheSize) {
            cacheMap.remove(cacheMap.keySet().stream().findFirst().get());
        }
    }

    public Message get(long id) {
        return cacheMap.get(id);
    }
}
