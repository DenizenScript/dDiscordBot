package com.denizenscript.ddiscordbot.events;

import net.aufdemrand.denizencore.events.ScriptEvent;
import net.aufdemrand.denizencore.objects.Element;
import net.aufdemrand.denizencore.objects.dObject;
import net.aufdemrand.denizencore.scripts.containers.ScriptContainer;
import net.aufdemrand.denizencore.utilities.CoreUtilities;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageEditEvent;

public class DiscordMessageModifiedScriptEvent extends ScriptEvent {
    public static DiscordMessageModifiedScriptEvent instance;

    // <--[event]
    // @Events
    // discord message modified (for <bot>)
    //
    // @Regex ^on discord message modified( for [^\s]+)?$
    //
    // @Triggers when a Discord user modified a message.
    //
    // @Plugin dDiscordBot
    //
    // @Context
    // <context.bot> returns the Denizen ID of the bot.
    // <context.channel> returns the channel ID.
    // <context.channel_name> returns the channel name.
    // <context.group> returns the group ID.
    // <context.group_name> returns the group name.
    // <context.author_id> returns the author's internal ID.
    // <context.author_name> returns the author's name.
    // <context.self> returns the bots own Discord user ID.
    // <context.is_private> returns true if the message was received in a private channel.
    // <context.new_message> returns the new message.
    // <context.old_message> returns the old message.
    //
    // -->

    @Override
    public boolean couldMatch(ScriptContainer scriptContainer, String s) {
        return CoreUtilities.toLowerCase(s).startsWith("discord message modified");
    }

    @Override
    public boolean matches(ScriptPath path) {
        if (!CoreUtilities.xthArgEquals(3, path.eventLower, "for")) {
            return true;
        }
        if (CoreUtilities.xthArgEquals(4, path.eventLower, botID)) {
            return true;
        }
        return false;
    }

    public String botID;

    public MessageEditEvent mre;

    @Override
    public dObject getContext(String name) {
        if (name.equals("bot")) {
            return new Element(botID);
        }
        else if (name.equals("self")) {
            return new Element(mre.getClient().getOurUser().getLongID());
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
        else if (name.equals("new_message")) {
            return new Element(mre.getNewMessage().toString());
        }
        else if (name.equals("old_message")) {
            return new Element(mre.getOldMessage().toString());
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
        return "DiscordModifiedMessage";
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

}
