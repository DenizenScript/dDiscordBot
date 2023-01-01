package com.denizenscript.ddiscordbot;

import com.denizenscript.ddiscordbot.objects.DiscordBotTag;
import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizencore.objects.ObjectTag;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.forums.ForumTag;
import net.dv8tion.jda.api.events.Event;

import java.util.Collection;

public abstract class DiscordScriptEvent extends BukkitScriptEvent {

    public DiscordScriptEvent() {
        registerSwitches("for");
    }

    public String botID;

    public Event event;

    public DiscordBotTag getBot() {
        return new DiscordBotTag(botID);
    }

    public DiscordConnection getConnection() {
        return DenizenDiscordBot.instance.connections.get(botID);
    }

    @Override
    public boolean matches(ScriptPath path) {
        if (!runGenericSwitchCheck(path, "for", botID)) {
            return false;
        }
        return super.matches(path);
    }

    @Override
    public ObjectTag getContext(String name) {
        if (name.equals("bot")) {
            return getBot();
        }
        return super.getContext(name);
    }

    public boolean enabled = false;

    @Override
    public void init() {
        enabled = true;
    }

    @Override
    public void destroy() {
        enabled = false;
    }

    public static boolean tryChannel(ScriptPath path, Channel channel) {
        return tryChannel(path, channel, "channel");
    }

    public static boolean tryChannel(ScriptPath path, Channel channel, String switchName) {
        String text = path.switches.get(switchName);
        if (text == null) {
            return true;
        }
        if (channel == null) {
            return false;
        }
        MatchHelper matcher = createMatcher(text);
        if (matcher.doesMatch(channel.getId())) {
            return true;
        }
        if (matcher.doesMatch(channel.getName())) {
            return true;
        }
        return false;
    }

    public static boolean tryGuild(ScriptPath path, Guild guild) {
        String text = path.switches.get("group");
        if (text == null) {
            return true;
        }
        if (guild == null) {
            return false;
        }
        MatchHelper matcher = createMatcher(text);
        if (matcher.doesMatch(guild.getId())) {
            return true;
        }
        if (matcher.doesMatch(guild.getName())) {
            return true;
        }
        return false;
    }

    public static boolean tryForumTag(ScriptPath path, ForumTag tag, String switchName) {
        String rawMatcher = path.switches.get(switchName);
        if (rawMatcher == null) {
            return true;
        }
        if (tag == null) {
            return false;
        }
        MatchHelper matcher = createMatcher(rawMatcher);
        if (matcher.doesMatch(tag.getId())) {
            return true;
        }
        return matcher.doesMatch(tag.getName());
    }

    public static boolean tryForumTags(ScriptPath path, Collection<ForumTag> tags, String switchName) {
        if (!path.switches.containsKey(switchName)) {
            return true;
        }
        if (tags == null || tags.isEmpty()) {
            return false;
        }
        for (ForumTag tag : tags) {
            if (tryForumTag(path, tag, switchName)) {
                return true;
            }
        }
        return false;
    }
}
