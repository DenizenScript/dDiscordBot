package com.denizenscript.ddiscordbot.events;

import com.denizenscript.ddiscordbot.DiscordScriptEvent;
import com.denizenscript.ddiscordbot.objects.DiscordGroupTag;
import com.denizenscript.ddiscordbot.objects.DiscordRoleTag;
import com.denizenscript.ddiscordbot.objects.DiscordUserTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.guild.member.GenericGuildMemberEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleAddEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleRemoveEvent;

import java.util.List;

public class DiscordUserRoleChangeScriptEvent extends DiscordScriptEvent {

    public static DiscordUserRoleChangeScriptEvent instance;

    // <--[event]
    // @Events
    // discord user role changes
    //
    // @Regex ^on discord user role changes$
    //
    // @Switch for:<bot> to only process the event for a specified Discord bot.
    // @Switch group:<group_id> to only process the event for a specified Discord group.
    //
    // @Triggers when a Discord user's roles change.
    //
    // @Plugin dDiscordBot
    //
    // @Group Discord
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

    public boolean isAdding() {
        return event instanceof GuildMemberRoleAddEvent;
    }

    public GuildMemberRoleAddEvent getAddEvent() {
        return (GuildMemberRoleAddEvent) event;
    }

    public GuildMemberRoleRemoveEvent getRemoveEvent() {
        return (GuildMemberRoleRemoveEvent) event;
    }

    public GenericGuildMemberEvent getGenericEvent() {
        return (GenericGuildMemberEvent) event;
    }

    @Override
    public boolean couldMatch(ScriptPath path) {
        return path.eventLower.startsWith("discord user role changes");
    }

    @Override
    public boolean matches(ScriptPath path) {
        if (!path.checkSwitch("group", getGenericEvent().getGuild().getId())) {
            return false;
        }
        return super.matches(path);
    }

    @Override
    public ObjectTag getContext(String name) {
        switch (name) {
            case "group":
                return new DiscordGroupTag(botID, getGenericEvent().getGuild().getIdLong());
            case "user":
                return new DiscordUserTag(botID, getGenericEvent().getUser().getIdLong());
            case "old_roles": {
                List<Role> existing = getGenericEvent().getMember().getRoles();
                ListTag oldRoles = new ListTag();
                if (isAdding()) {
                    for (Role role : existing) {
                        if (!getAddEvent().getRoles().contains(role)) {
                            oldRoles.addObject(new DiscordRoleTag(botID, role));
                        }
                    }
                }
                else {
                    for (Role role : existing) {
                        oldRoles.addObject(new DiscordRoleTag(botID, role));
                    }
                    for (Role role : getRemoveEvent().getRoles()) {
                        if (!existing.contains(role)) {
                            oldRoles.addObject(new DiscordRoleTag(botID, role));
                        }
                    }
                }
                return oldRoles;
            }
            case "new_roles": {
                List<Role> existing = getGenericEvent().getMember().getRoles();
                ListTag newRoles = new ListTag();
                if (!isAdding()) {
                    for (Role role : existing) {
                        if (!getRemoveEvent().getRoles().contains(role)) {
                            newRoles.addObject(new DiscordRoleTag(botID, role));
                        }
                    }
                }
                else {
                    for (Role role : existing) {
                        newRoles.addObject(new DiscordRoleTag(botID, role));
                    }
                    for (Role role : getAddEvent().getRoles()) {
                        if (!existing.contains(role)) {
                            newRoles.addObject(new DiscordRoleTag(botID, role));
                        }
                    }
                }
                return newRoles;
            }
            case "added_roles":
                ListTag addedRoles = new ListTag();
                if (!isAdding()) {
                    return addedRoles;
                }
                for (Role role : getAddEvent().getRoles()) {
                    addedRoles.addObject(new DiscordRoleTag(botID, role));
                }
                return addedRoles;
            case "removed_roles":
                ListTag removedRoles = new ListTag();
                if (isAdding()) {
                    return removedRoles;
                }
                for (Role role : getRemoveEvent().getRoles()) {
                    removedRoles.addObject(new DiscordRoleTag(botID, role));
                }
                return removedRoles;
        }
        return super.getContext(name);
    }

    @Override
    public String getName() {
        return "DiscordUserRoleChange";
    }
}
