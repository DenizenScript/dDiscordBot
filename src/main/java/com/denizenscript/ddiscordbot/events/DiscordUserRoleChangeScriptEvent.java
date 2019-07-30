package com.denizenscript.ddiscordbot.events;

import com.denizenscript.ddiscordbot.DiscordScriptEvent;
import com.denizenscript.ddiscordbot.objects.DiscordUserTag;
import discord4j.core.event.domain.guild.MemberUpdateEvent;
import discord4j.core.object.util.Snowflake;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.scripts.containers.ScriptContainer;
import com.denizenscript.denizencore.utilities.CoreUtilities;

import java.util.ArrayList;

public class DiscordUserRoleChangeScriptEvent extends DiscordScriptEvent {

    public static DiscordUserRoleChangeScriptEvent instance;

    // <--[event]
    // @Events
    // discord user role changes
    //
    // @Regex ^on discord role changes$
    //
    // @Switch for <bot>
    // @Switch group <group_id>
    //
    // @Triggers when a Discord user's roles change.
    //
    // @Plugin dDiscordBot
    //
    // @Context
    // <context.bot> returns the Denizen ID of the bot.
    // <context.self> returns the bots own Discord user ID.
    // <context.group> returns the group ID.
    // <context.group_name> returns the group name.
    // <context.user> returns the user.
    // <context.old_roles_ids> returns a list of the user's previous role set.
    // <context.new_role_ids> returns a list of the user's new role set.
    // <context.added_role_ids> returns a list of the user's added role set.
    // <context.removed_role_ids> returns a list of the user's removed role set.
    // -->

    public MemberUpdateEvent getEvent() {
        return (MemberUpdateEvent) event;
    }

    @Override
    public boolean couldMatch(ScriptContainer scriptContainer, String s) {
        return CoreUtilities.toLowerCase(s).startsWith("discord user role changes");
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

    public ListTag getAddedRoles() {
        ListTag addedRoles = new ListTag();
        ArrayList<Long> oldRoles = getOldRoles();
        for (Long role : getNewRoles()) {
            if (!oldRoles.contains(role)) {
                addedRoles.addObject(new ElementTag(role));
            }
        }
        return addedRoles;
    }

    public ListTag getRemovedRoles() {
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
            return new ElementTag(getEvent().getGuildId().asLong());
        }
        else if (name.equals("group_name")) {
            return new ElementTag(getEvent().getGuild().block().getName());
        }
        else if (name.equals("user")) {
            return new DiscordUserTag(botID, getEvent().getMember().block());
        }
        else if (name.equals("user_id")) { // Deprecated
            return new ElementTag(getEvent().getMember().block().getId().asLong());
        }
        else if (name.equals("user_name")) { // Deprecated
            return new ElementTag(getEvent().getMember().block().getUsername());
        }
        if (name.equals("old_role_ids")) {
            ListTag oldRoles = new ListTag();
            for (Long role : getOldRoles()) {
                oldRoles.addObject(new ElementTag(role));
            }
            return oldRoles;
        }
        else if (name.equals("new_role_ids")) {
            ListTag newRoles = new ListTag();
            for (Long role : getNewRoles()) {
                newRoles.addObject(new ElementTag(role));
            }
            return newRoles;
        }
        else if (name.equals("added_role_ids")) {
            return getAddedRoles();
        }
        else if (name.equals("removed_role_ids")) {
            return getRemovedRoles();
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
        return getAddedRoles().size() > 0 || getRemovedRoles().size() > 0;
    }
}
