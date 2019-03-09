package com.denizenscript.ddiscordbot.events;

import com.denizenscript.ddiscordbot.DiscordScriptEvent;
import net.aufdemrand.denizencore.scripts.containers.ScriptContainer;
import net.aufdemrand.denizencore.utilities.CoreUtilities;

public class DiscordMessageDeletedScriptEvent extends DiscordScriptEvent {

    public static DiscordMessageDeletedScriptEvent instance;

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
    // <context.author_id> returns the author's internal ID.
    // <context.author_name> returns the author's name.
    // <context.mentions> returns a list of all mentioned user IDs.
    // <context.mention_names> returns a list of all mentioned user names.
    // <context.self> returns the bots own Discord user ID.
    // <context.is_direct> returns whether the message was sent directly to the bot (if false, the message was sent to a public channel).
    // <context.old_message_valid> returns whether the old message is available (it may be lost due to caching).
    // <context.old_message> returns the previous message (raw).
    // <context.old_no_mention_message> returns the previous message with all user mentions stripped.
    // <context.old_formatted_message> returns the formatted previous message (mentions/etc. are written cleanly).
    //
    // -->

    @Override
    public boolean couldMatch(ScriptContainer scriptContainer, String s) {
        return CoreUtilities.toLowerCase(s).startsWith("discord message deleted");
    }

    @Override
    public String getName() {
        return "MessageDeleteEvent";
    }
}
