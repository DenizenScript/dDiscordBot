package com.denizenscript.ddiscordbot;

import net.aufdemrand.denizencore.events.ScriptEvent;
import net.aufdemrand.denizencore.objects.Element;
import net.aufdemrand.denizencore.objects.dList;
import net.aufdemrand.denizencore.objects.dObject;
import net.aufdemrand.denizencore.scripts.containers.ScriptContainer;
import net.aufdemrand.denizencore.utilities.CoreUtilities;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IUser;

public class DiscordMessageReceivedScriptEvent extends ScriptEvent {

    public static DiscordMessageReceivedScriptEvent instance;

    // <--[event]
    // @Events
    // discord message received (by <bot>)
    //
    // @Regex ^on discord message received( by [^\s]+)?$
    //
    // @Triggers when a Discord bot receives a message.
    //
    // @Plugin dDiscordBot
    //
    // @Context
    // <context.bot> returns the Denizen ID of the bot.
    // <context.channel> returns the channel ID.
    // <context.channel_name> returns the channel name.
    // <context.group> returns the group ID.
    // <context.group_name> returns the group name.
    // <context.message> returns the message sent (raw).
    // <context.no_mention_message> returns the message with all user mentions stripped.
    // <context.formatted_message> returns the formatted message sent (mentions/etc. are written cleanly).
    // <context.author_id> returns the author's internal ID.
    // <context.author_name> returns the author's name.
    // <context.mentions> returns a list of all mentioned user IDs.
    // <context.mention_names> returns a list of all mentioned user names.
    // <context.self> returns the bots own Discord user ID.
    // <context.is_direct> returns whether the message was sent directly to the bot (if false, the message was sent to a public channel).
    //
    // -->

    @Override
    public boolean couldMatch(ScriptContainer scriptContainer, String s) {
        return CoreUtilities.toLowerCase(s).startsWith("discord message received");
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

    public MessageReceivedEvent mre;

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
        else if (name.equals("is_direct")) {
            return new Element(false); // TODO
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
        return super.getContext(name);
    }

    @Override
    public String getName() {
        return "DiscordMessageReceived";
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
