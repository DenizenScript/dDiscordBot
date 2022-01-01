package com.denizenscript.ddiscordbot;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.MessageUpdateEvent;

import java.util.HashMap;

public class CacheHelper {

    public HashMap<Long, DiscordMessageCache> messageCaches = new HashMap<>();

    public Message getMessage(long channel, long message) {
        DiscordMessageCache cache = messageCaches.get(channel);
        if (cache == null) {
            return null;
        }
        return cache.get(message);
    }

    public void onMessageReceived(MessageReceivedEvent event) {
        DiscordMessageCache cache = messageCaches.computeIfAbsent(event.getChannel().getIdLong(), k -> new DiscordMessageCache());
        cache.add(event.getMessage());
    }

    public void onMessageUpdate(MessageUpdateEvent event) {
        Long id = event.getMessageIdLong();
        DiscordMessageCache cache = messageCaches.computeIfAbsent(event.getChannel().getIdLong(), k -> new DiscordMessageCache());
        if (cache.cacheMap.containsKey(id)) {
            cache.cacheMap.put(id, event.getMessage());
        }
    }
}
