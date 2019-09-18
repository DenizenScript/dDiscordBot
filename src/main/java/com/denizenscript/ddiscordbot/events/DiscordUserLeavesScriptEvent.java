package com.denizenscript.ddiscordbot.events;

import com.denizenscript.ddiscordbot.DiscordScriptEvent;
import com.denizenscript.ddiscordbot.dDiscordBot;
import com.denizenscript.ddiscordbot.objects.DiscordGroupTag;
import com.denizenscript.ddiscordbot.objects.DiscordUserTag;
import discord4j.core.event.domain.guild.MemberLeaveEvent;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.scripts.containers.ScriptContainer;
import com.denizenscript.denizencore.utilities.CoreUtilities;

public class DiscordUserLeavesScriptEvent extends DiscordScriptEvent {

    public static DiscordUserLeavesScriptEvent instance;

    // <--[event]
    // @Events
    // discord user leaves
    //
    // @Regex ^on discord user leaves$
    //
    // @Switch for <bot>
    // @Switch group <group_id>
    //
    // @Triggers when a Discord user leaves a guild.
    //
    // @Plugin dDiscordBot
    //
    // @Context
    // <context.bot> returns the Denizen ID of the bot.
    // <context.self> returns the bots own Discord user ID.
    // <context.group> returns the group.
    // <context.user> returns the user.
    // -->

    public MemberLeaveEvent getEvent() {
        return (MemberLeaveEvent) event;
    }

    @Override
    public boolean matches(ScriptPath path) {
        if (!path.checkSwitch("group", String.valueOf(getEvent().getGuildId().asLong()))) {
            return false;
        }
        return super.matches(path);
    }

    @Override
    public boolean couldMatch(ScriptContainer scriptContainer, String s) {
        return CoreUtilities.toLowerCase(s).startsWith("discord user leaves");
    }

    @Override
    public ObjectTag getContext(String name) {
        if (name.equals("group")) {
            return new DiscordGroupTag(botID, getEvent().getGuildId().asLong());
        }
        else if (name.equals("user")) {
            return new DiscordUserTag(botID, getEvent().getMember().get());
        }
        else if (name.equals("group_name")) {
            dDiscordBot.userContextDeprecation.warn();
            return new ElementTag(getEvent().getGuild().block().getName());
        }
        else if (name.equals("user_id")) {
            dDiscordBot.userContextDeprecation.warn();
            return new ElementTag(getEvent().getMember().get().getId().asLong());
        }
        else if (name.equals("user_name")) {
            dDiscordBot.userContextDeprecation.warn();
            return new ElementTag(getEvent().getMember().get().getUsername());
        }
        return super.getContext(name);
    }

    @Override
    public String getName() {
        return "DiscordUserLeaves";
    }
}
