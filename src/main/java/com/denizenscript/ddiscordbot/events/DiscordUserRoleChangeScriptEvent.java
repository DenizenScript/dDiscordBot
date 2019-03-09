package com.denizenscript.ddiscordbot.events;

import com.denizenscript.ddiscordbot.DiscordScriptEvent;
import net.aufdemrand.denizencore.objects.Element;
import net.aufdemrand.denizencore.objects.dList;
import net.aufdemrand.denizencore.objects.dObject;
import net.aufdemrand.denizencore.scripts.containers.ScriptContainer;
import net.aufdemrand.denizencore.utilities.CoreUtilities;
import sx.blah.discord.handle.impl.events.guild.member.UserRoleUpdateEvent;
import sx.blah.discord.handle.obj.IRole;

import java.util.List;

public class DiscordUserRoleChangeScriptEvent extends DiscordScriptEvent {

    public static DiscordUserRoleChangeScriptEvent instance;

    // <--[event]
    // @Events
    // discord user role changes
    //
    // @Regex ^on discord role changes$
    // @Switch for <bot>
    //
    // @Triggers when a Discord user's roles change.
    //
    // @Plugin dDiscordBot
    //
    // @Context
    // <context.bot> returns the Denizen ID of the bot.
    // <context.group> returns the group ID.
    // <context.group_name> returns the group name.
    // <context.user_id> returns the user's internal ID.
    // <context.user_name> returns the user's name.
    // <context.self> returns the bots own Discord user ID.
    // <context.old_roles_ids> returns a list of the user's previous role set.
    // <context.new_role_ids> returns a list of the user's new role set.
    // <context.added_role_ids> returns a list of the user's added role set.
    // <context.removed_role_ids> returns a list of the user's removed role set.
    // -->

    @Override
    public boolean couldMatch(ScriptContainer scriptContainer, String s) {
        return CoreUtilities.toLowerCase(s).startsWith("discord user role changes");
    }

    @Override
    public dObject getContext(String name) {
        if (name.equals("old_role_ids")) {
            dList oldRoles = new dList();
            List<IRole> oldRoleList = ((UserRoleUpdateEvent) event).getOldRoles();
            if (oldRoleList == null) {
                return oldRoles;
            }
            for (IRole role : oldRoleList) {
                oldRoles.addObject(new Element(role.getLongID()));
            }
            return oldRoles;
        }
        else if (name.equals("new_role_ids")) {
            dList newRoles = new dList();
            for (IRole role : ((UserRoleUpdateEvent) event).getNewRoles()) {
                newRoles.addObject(new Element(role.getLongID()));
            }
            return newRoles;
        }
        else if (name.equals("added_role_ids")) {
            dList addedRoles = new dList();
            List<IRole> oldRoles = ((UserRoleUpdateEvent) event).getOldRoles();
            if (oldRoles == null) {
                return addedRoles;
            }
            for (IRole role : ((UserRoleUpdateEvent) event).getNewRoles()) {
                if (!oldRoles.contains(role)) {
                    addedRoles.addObject(new Element(role.getLongID()));
                }
            }
            return addedRoles;
        }
        else if (name.equals("removed_role_ids")) {
            dList removedRoles = new dList();
            List<IRole> newRoles = ((UserRoleUpdateEvent) event).getNewRoles();
            if (newRoles == null) {
                return removedRoles;
            }
            for (IRole role : ((UserRoleUpdateEvent) event).getOldRoles()) {
                if (!newRoles.contains(role)) {
                    removedRoles.addObject(new Element(role.getLongID()));
                }
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
