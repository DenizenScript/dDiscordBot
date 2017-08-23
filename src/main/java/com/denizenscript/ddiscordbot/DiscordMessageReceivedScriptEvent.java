package com.denizenscript.ddiscordbot;

import net.aufdemrand.denizencore.events.ScriptEvent;
import net.aufdemrand.denizencore.objects.Element;
import net.aufdemrand.denizencore.objects.dObject;
import net.aufdemrand.denizencore.scripts.containers.ScriptContainer;
import net.aufdemrand.denizencore.utilities.CoreUtilities;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;

public class DiscordMessageReceivedScriptEvent extends ScriptEvent {

    public static DiscordMessageReceivedScriptEvent instance;

    // <--[event]
    // @Events
    // discord message received (by <bot>)
    //
    // @Regex ^on discord message received(by [^\s]+)?$
    //
    // @Triggers when a Discord bot receives a message.
    //
    // @Plugin dDiscordBot
    //
    // @Context
    // <context.bot> returns the ID of the bot.
    // <context.channel> returns the channel ID.
    // <context.channel_name> returns the channel name.
    // <context.group> returns the group ID.
    // <context.group_name> returns the group name.
    // <context.message> returns the message sent.
    // <context.formatted_message> returns the formatted message sent.
    // <context.author_id> returns the author's internal ID.
    // <context.author_name> return's the author's name.
    //
    // -->

    @Override
    public boolean couldMatch(ScriptContainer scriptContainer, String s) {
        return CoreUtilities.toLowerCase(s).startsWith("discord message received");
    }

    @Override
    public boolean matches(ScriptContainer scriptContainer, String s) {
        String lower = CoreUtilities.toLowerCase(s);
        if (lower.equals("discord message received")) {
            return true;
        }
        else if (CoreUtilities.xthArgEquals(4, lower, botID)) {
            return true;
        }
        return false;
    }

    public String botID;

    public MessageReceivedEvent mre;

    @Override
    public dObject getContext(String name) {
        if (name.equals("bot")) {
            return new Element(botID);
        }
        else if (name.equals("channel")) {
            return new Element(mre.getChannel().getLongID());
        }
        else if (name.equals("channel_name")) {
            return new Element(mre.getChannel().getName());
        }
        else if (name.equals("group")) {
            return new Element(mre.getGuild().getLongID());
        }
        else if (name.equals("group_name")) {
            return new Element(mre.getGuild().getName());
        }
        else if (name.equals("message")) {
            return new Element(mre.getMessage().getContent());
        }
        else if (name.equals("formatted_message")) {
            return new Element(mre.getMessage().getFormattedContent());
        }
        else if (name.equals("author_id")) {
            return new Element(mre.getAuthor().getLongID());
        }
        else if (name.equals("author_name")) {
            return new Element(mre.getAuthor().getName());
        }
        return super.getContext(name);
    }

    @Override
    public String getName() {
        return "DiscordMessageReceived";
    }

    boolean enab = false;

    @Override
    public void init() {
        enab = true;
    }

    @Override
    public void destroy() {
        enab = false;
    }

}
