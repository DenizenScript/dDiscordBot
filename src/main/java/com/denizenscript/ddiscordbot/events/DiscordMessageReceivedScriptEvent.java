package com.denizenscript.ddiscordbot.events;

import com.denizenscript.ddiscordbot.DiscordScriptEvent;
import net.aufdemrand.denizencore.scripts.containers.ScriptContainer;
import net.aufdemrand.denizencore.utilities.CoreUtilities;

public class DiscordMessageReceivedScriptEvent extends DiscordScriptEvent {

    public static DiscordMessageReceivedScriptEvent instance;

    // <--[event]
    // @Events
    // discord message received
    //
    // @Regex ^on discord message received$
    // @Switch for <bot>
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
    // <context.message> returns the message (raw).
    // <context.no_mention_message> returns the message with all user mentions stripped.
    // <context.formatted_message> returns the formatted message (mentions/etc. are written cleanly).
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
    public String getName() {
        return "DiscordMessageReceived";
    }
}
