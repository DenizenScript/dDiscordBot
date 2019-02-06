package com.denizenscript.ddiscordbot.events;

import net.aufdemrand.denizencore.events.ScriptEvent;
import net.aufdemrand.denizencore.objects.Element;
import net.aufdemrand.denizencore.objects.dList;
import net.aufdemrand.denizencore.objects.dObject;
import net.aufdemrand.denizencore.scripts.containers.ScriptContainer;
import net.aufdemrand.denizencore.utilities.CoreUtilities;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageDeleteEvent;
import sx.blah.discord.handle.obj.IUser;

public class DiscordMessageDeleteScriptEvent extends ScriptEvent {
    public static DiscordMessageDeleteScriptEvent instance;

    // <--[event]
    // @Events
    // discord message deleted (for <bot>)
    //
    // @Regex ^on discord message deleted( for [^\s]+)?$
    //
    // @Triggers when a Discord user deletes a message.
    //
    // @Plugin dDiscordBot
    //
    // @Context
    // <context.bot> returns the Denizen ID of the bot.
    // <context.channel> returns the channel ID.
    // <context.channel_name> returns the channel name.
    // <context.group> returns the group ID.
    // <context.group_name> returns the group name.
    // <context.message> returns the message deleted (raw).
    // <context.no_mention_message> returns the message with all user mentions stripped.
    // <context.formatted_message> returns the formatted message deleted (mentions/etc. are written cleanly).
    // <context.author_id> returns the author's internal ID.
    // <context.author_name> returns the author's name.
    // <context.mentions> returns a list of all mentioned user IDs.
    // <context.mention_names> returns a list of all mentioned user names.
    // <context.self> returns the bots own Discord user ID.
    // <context.is_private> returns true if the message was received in a private channel.
    //
    // -->

    @Override
    public boolean couldMatch(ScriptContainer scriptContainer, String s) {
        return CoreUtilities.toLowerCase(s).startsWith("discord message deleted");
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

    public MessageDeleteEvent mre;

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
        else if (name.equals("message")) {
            return new Element(mre.getMessage().getContent());
        }
        else if (name.equals("no_mention_message")) {
            String res = mre.getMessage().getContent();
            for (IUser user : mre.getMessage().getMentions()) {
                res = res.replace(user.mention(true), "")
                        .replace(user.mention(false), "");
            }
            return new Element(res);
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
        else if (name.equals("mentions")) {
            dList list = new dList();
            for (IUser user : mre.getMessage().getMentions()) {
                list.add(String.valueOf(user.getLongID()));
            }
            return list;
        }
        else if (name.equals("mention_names")) {
            dList list = new dList();
            for (IUser user : mre.getMessage().getMentions()) {
                list.add(String.valueOf(user.getName()));
            }
            return list;
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
