package com.denizenscript.ddiscordbot.events;

import com.denizenscript.ddiscordbot.DiscordScriptEvent;
import com.denizenscript.ddiscordbot.objects.DiscordGroupTag;
import com.denizenscript.ddiscordbot.objects.DiscordUserTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import net.dv8tion.jda.api.events.guild.member.update.GuildMemberUpdateNicknameEvent;

public class DiscordUserNicknameChangeScriptEvent extends DiscordScriptEvent {

    public static DiscordUserNicknameChangeScriptEvent instance;

    // <--[event]
    // @Events
    // discord user nickname changes
    //
    // @Regex ^on discord user nickname changes$
    //
    // @Switch for:<bot> to only process the event for a specified Discord bot.
    // @Switch group:<group_id> to only process the event for a specified Discord group.
    //
    // @Triggers when a Discord user's nickname change.
    //
    // @Plugin dDiscordBot
    //
    // @Group Discord
    //
    // @Context
    // <context.bot> returns the relevant Discord bot object.
    // <context.group> returns the group.
    // <context.user> returns the user.
    // <context.old_name> returns the user's previous nickname (if any).
    // <context.new_name> returns the user's new nickname (if any).
    // -->

    public GuildMemberUpdateNicknameEvent getEvent() {
        return (GuildMemberUpdateNicknameEvent) event;
    }

    @Override
    public boolean couldMatch(ScriptPath path) {
        return path.eventLower.startsWith("discord user nickname changes");
    }

    @Override
    public boolean matches(ScriptPath path) {
        if (!path.checkSwitch("group", getEvent().getGuild().getId())) {
            return false;
        }
        return super.matches(path);
    }

    @Override
    public ObjectTag getContext(String name) {
        switch (name) {
            case "group":
                return new DiscordGroupTag(botID, getEvent().getGuild().getIdLong());
            case "user":
                return new DiscordUserTag(botID, getEvent().getUser().getIdLong());
            case "old_name":
                if (getEvent().getOldNickname() == null) {
                    return null;
                }
                return new ElementTag(getEvent().getOldNickname());
            case "new_name":
                if (getEvent().getNewNickname() == null) {
                    return null;
                }
                return new ElementTag(getEvent().getNewNickname());
        }
        return super.getContext(name);
    }

    @Override
    public String getName() {
        return "DiscordUserNicknameChange";
    }
}
