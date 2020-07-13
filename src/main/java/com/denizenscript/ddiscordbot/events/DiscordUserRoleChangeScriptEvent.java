package com.denizenscript.ddiscordbot.events;

import com.denizenscript.ddiscordbot.DiscordScriptEvent;
import com.denizenscript.ddiscordbot.DenizenDiscordBot;
import com.denizenscript.ddiscordbot.objects.DiscordGroupTag;
import com.denizenscript.ddiscordbot.objects.DiscordRoleTag;
import com.denizenscript.ddiscordbot.objects.DiscordUserTag;
import discord4j.core.event.domain.guild.MemberUpdateEvent;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import discord4j.common.util.Snowflake;

import java.util.ArrayList;

public class DiscordUserRoleChangeScriptEvent extends DiscordScriptEvent {

    public static DiscordUserRoleChangeScriptEvent instance;

    // <--[event]
    // @Events
    // discord user role changes
    //
    // @Regex ^on discord role changes$
    //
    // @Switch for:<bot> to only process the event for a specified Discord bot.
    // @Switch group:<group_id> to only process the event for a specified Discord group.
    //
    // @Triggers when a Discord user's roles change.
    //
    // @Plugin dDiscordBot
    //
    // @Context
    // <context.bot> returns the relevant Discord bot object.
    // <context.group> returns the group.
    // <context.user> returns the user.
    // <context.old_roles> returns a list of the user's previous role set.
    // <context.new_roles> returns a list of the user's new role set.
    // <context.added_roles> returns a list of the user's added role set.
    // <context.removed_roles> returns a list of the user's removed role set.
    // -->

    public MemberUpdateEvent getEvent() {
        return (MemberUpdateEvent) event;
    }

    @Override
    public boolean couldMatch(ScriptPath path) {
        return path.eventLower.startsWith("discord user role changes");
    }

    @Override
    public boolean matches(ScriptPath path) {
        if (!path.checkSwitch("group", String.valueOf(getEvent().getGuildId().asLong()))) {
            return false;
        }
        return super.matches(path);
    }

    public ArrayList<Long> getOldRoles() {
        ArrayList<Long> oldRoles = new ArrayList<>();
        for (Snowflake role : getEvent().getOld().get().getRoleIds()) {
            oldRoles.add(role.asLong());
        }
        return oldRoles;
    }

    public ArrayList<Long> getNewRoles() {
        ArrayList<Long> newRoles = new ArrayList<>();
        for (Snowflake role : getEvent().getCurrentRoles()) {
            newRoles.add(role.asLong());
        }
        return newRoles;
    }

    public ListTag getAddedRoleIds() {
        ListTag addedRoles = new ListTag();
        ArrayList<Long> oldRoles = getOldRoles();
        for (Long role : getNewRoles()) {
            if (!oldRoles.contains(role)) {
                addedRoles.addObject(new ElementTag(role));
            }
        }
        return addedRoles;
    }

    public ListTag getRemovedRoleIds() {
        ListTag removedRoles = new ListTag();
        ArrayList<Long> newRoles = getNewRoles();
        for (Long role : getOldRoles()) {
            if (!newRoles.contains(role)) {
                removedRoles.addObject(new ElementTag(role));
            }
        }
        return removedRoles;
    }

    @Override
    public ObjectTag getContext(String name) {
        if (name.equals("group")) {
            return new DiscordGroupTag(botID, getEvent().getGuildId().asLong());
        }
        else if (name.equals("user")) {
            return new DiscordUserTag(botID, getEvent().getMember().block());
        }
        else if (name.equals("old_roles")) {
            ListTag oldRoles = new ListTag();
            for (Long role : getOldRoles()) {
                oldRoles.addObject(new DiscordRoleTag(botID, getEvent().getGuildId().asLong(), role));
            }
            return oldRoles;
        }
        else if (name.equals("new_roles")) {
            ListTag newRoles = new ListTag();
            for (Long role : getNewRoles()) {
                newRoles.addObject(new DiscordRoleTag(botID, getEvent().getGuildId().asLong(), role));
            }
            return newRoles;
        }
        else if (name.equals("added_roles")) {
            ListTag addedRoles = new ListTag();
            ArrayList<Long> oldRoles = getOldRoles();
            for (Long role : getNewRoles()) {
                if (!oldRoles.contains(role)) {
                    addedRoles.addObject(new DiscordRoleTag(botID, getEvent().getGuildId().asLong(), role));
                }
            }
            return addedRoles;
        }
        else if (name.equals("removed_roles")) {
            ListTag removedRoles = new ListTag();
            ArrayList<Long> newRoles = getNewRoles();
            for (Long role : getOldRoles()) {
                if (!newRoles.contains(role)) {
                    removedRoles.addObject(new DiscordRoleTag(botID, getEvent().getGuildId().asLong(), role));
                }
            }
            return removedRoles;
        }
        else if (name.equals("group_name")) {
            DenizenDiscordBot.userContextDeprecation.warn();
            return new ElementTag(getEvent().getGuild().block().getName());
        }
        else if (name.equals("user_id")) {
            DenizenDiscordBot.userContextDeprecation.warn();
            return new ElementTag(getEvent().getMember().block().getId().asLong());
        }
        else if (name.equals("user_name")) {
            DenizenDiscordBot.userContextDeprecation.warn();
            return new ElementTag(getEvent().getMember().block().getUsername());
        }
        else if (name.equals("old_role_ids")) {
            DenizenDiscordBot.userContextDeprecation.warn();
            ListTag oldRoles = new ListTag();
            for (Long role : getOldRoles()) {
                oldRoles.addObject(new ElementTag(role));
            }
            return oldRoles;
        }
        else if (name.equals("new_role_ids")) {
            DenizenDiscordBot.userContextDeprecation.warn();
            ListTag newRoles = new ListTag();
            for (Long role : getNewRoles()) {
                newRoles.addObject(new ElementTag(role));
            }
            return newRoles;
        }
        else if (name.equals("added_role_ids")) {
            DenizenDiscordBot.userContextDeprecation.warn();
            return getAddedRoleIds();
        }
        else if (name.equals("removed_role_ids")) {
            DenizenDiscordBot.userContextDeprecation.warn();
            return getRemovedRoleIds();
        }
        return super.getContext(name);
    }

    @Override
    public String getName() {
        return "DiscordUserRoleChange";
    }

    @Override
    public boolean isProperEvent() {
        if (!getEvent().getOld().isPresent()) {
            return false;
        }
        return getAddedRoleIds().size() > 0 || getRemovedRoleIds().size() > 0;
    }
}
