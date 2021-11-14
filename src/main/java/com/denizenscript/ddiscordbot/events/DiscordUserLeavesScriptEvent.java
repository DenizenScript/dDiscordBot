package com.denizenscript.ddiscordbot.events;

import com.denizenscript.ddiscordbot.DiscordScriptEvent;
import com.denizenscript.ddiscordbot.objects.DiscordGroupTag;
import com.denizenscript.ddiscordbot.objects.DiscordUserTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;

public class DiscordUserLeavesScriptEvent extends DiscordScriptEvent {

    // <--[event]
    // @Events
    // discord user leaves
    //
    // @Switch for:<bot> to only process the event for a specified Discord bot.
    // @Switch group:<group_id> to only process the event for a specified Discord group.
    //
    // @Triggers when a Discord user leaves a guild.
    //
    // @Plugin dDiscordBot
    //
    // @Group Discord
    //
    // @Context
    // <context.bot> returns the relevant Discord bot object.
    // <context.group> returns the group.
    // <context.user> returns the user.
    // -->

    public static DiscordUserLeavesScriptEvent instance;

    public DiscordUserLeavesScriptEvent() {
        instance = this;
        registerCouldMatcher("discord user leaves");
        registerSwitches("for", "group");
    }

    public GuildMemberRemoveEvent getEvent() {
        return (GuildMemberRemoveEvent) event;
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
        switch (name) {
            case "group":
                return new DiscordGroupTag(botID, getEvent().getGuild());
            case "user":
                return new DiscordUserTag(botID, getEvent().getUser());
        }
        return super.getContext(name);
    }

    @Override
    public String getName() {
        return "DiscordUserLeaves";
    }
}
