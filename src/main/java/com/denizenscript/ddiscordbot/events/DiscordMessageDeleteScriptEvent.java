package com.denizenscript.ddiscordbot.events;

import net.aufdemrand.denizencore.events.ScriptEvent;
import net.aufdemrand.denizencore.objects.Element;
import net.aufdemrand.denizencore.objects.dObject;
import net.aufdemrand.denizencore.scripts.containers.ScriptContainer;
import net.aufdemrand.denizencore.utilities.CoreUtilities;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageDeleteEvent;

public class DiscordMessageDeleteScriptEvent extends ScriptEvent {
    public static DiscordMessageDeleteScriptEvent instance;

    // <--[event]
    // @Events
    // discord user joined (by <bot>)
    //
    // @Regex ^on discord user join(by [^\s]+)?$
    //
    // @Triggers when a Discord user joins a guild.
    //
    // @Plugin dDiscordBot
    //
    // @Context
    // <context.bot> returns the ID of the bot.
    // <context.channel> returns the channel ID.
    // <context.channel_name> returns the channel name.
    // <context.group> returns the group ID.
    // <context.group_name> returns the group name.
    // <context.user_id> returns the author's internal ID.
    // <context.user_name> return's the author's name.
    //
    // -->

    @Override
    public boolean couldMatch(ScriptContainer scriptContainer, String s) {
        return CoreUtilities.toLowerCase(s).startsWith("discord message deleted");
    }

    @Override
    public boolean matches(ScriptContainer scriptContainer, String s) {
        String lower = CoreUtilities.toLowerCase(s);
        if (lower.equals("discord message deleted")) {
            return true;
        }
        else return CoreUtilities.xthArgEquals(4, lower, botID);
    }

    public String botID;

    public MessageDeleteEvent mre;

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
        else if (name.equals("is_private")) {
            return new Element(mre.getChannel().isPrivate());
        }
        return super.getContext(name);
    }

    @Override
    public String getName() {
        return "MessageDeleteEvent";
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
