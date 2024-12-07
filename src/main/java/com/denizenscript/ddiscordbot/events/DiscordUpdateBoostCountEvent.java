package com.denizenscript.ddiscordbot.events;

import com.denizenscript.ddiscordbot.DiscordScriptEvent;
import com.denizenscript.ddiscordbot.objects.DiscordGroupTag;
import com.denizenscript.ddiscordbot.objects.DiscordUserTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import net.dv8tion.jda.api.events.guild.update.GuildUpdateBoostCountEvent;

public class DiscordUpdateBoostCountEvent extends DiscordScriptEvent {

    // <--[event]
    // @Events
    // discord boosts count changes
    //
    // @Switch for:<bot> to only process the event for a specified Discord bot.
    // @Switch group:<group_id> to only process the event for a specified Discord group.
    //
    // @Triggers when the boosts count of the server changes
    //
    // @Plugin dDiscordBot
    //
    // @Group Discord
    //
    // @Context
    // <context.bot> returns the relevant DiscordBotTag.
    // <context.group> returns the DiscordGroupTag.
    // <context.new_count> returns the new count of the group's boosts.
    // <context.old_count> returns the old count of the group's boosts.
    // -->

    public static DiscordUpdateBoostCountEvent instance;

    public DiscordUpdateBoostCountEvent() {
        instance = this;
        registerCouldMatcher("discord boosts count changes");
        registerSwitches("group");
    }

    public GuildUpdateBoostCountEvent getEvent() {
        return (GuildUpdateBoostCountEvent) event;
    }

    @Override
    public boolean matches(ScriptPath path) {
        if (!tryGuild(path, getEvent().getGuild())) {
            return false;
        }
        return super.matches(path);
    }

    @Override
    public ObjectTag getContext(String name) {
        return switch (name) {
            case "new_count" -> new DiscordGroupTag(botID, getEvent().getNewBoostCount());
            case "old_count" -> new DiscordUserTag(botID, getEvent().getOldBoostCount());
            default -> super.getContext(name);
        };
    }
}
