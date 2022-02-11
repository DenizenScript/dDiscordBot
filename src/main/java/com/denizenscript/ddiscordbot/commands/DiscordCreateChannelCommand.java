package com.denizenscript.ddiscordbot.commands;

import com.denizenscript.ddiscordbot.DenizenDiscordBot;
import com.denizenscript.ddiscordbot.objects.*;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.Holdable;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Category;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.requests.restaction.ChannelAction;
import org.bukkit.Bukkit;

import java.util.Collections;
import java.util.List;
import java.util.Set;

public class DiscordCreateChannelCommand extends AbstractDiscordCommand implements Holdable {

    public DiscordCreateChannelCommand() {
        setName("discordcreatechannel");
        setSyntax("discordcreatechannel [id:<id>] [group:<group>] [name:<name>] (description:<description>) (type:<type>) (category:<category_id>) (position:<#>) (roles:<list>) (users:<list>)");
        setRequiredArguments(3, 9);
        isProcedural = false;
        setPrefixesHandled("id", "group", "name", "description", "type", "category", "position", "roles", "users");
    }
    // <--[command]
    // @Name discordcreatechannel
    // @Syntax discordcreatechannel [id:<id>] [group:<group>] [name:<name>] (description:<description>) (type:<type>) (category:<category_id>) (position:<#>) (roles:<list>) (users:<list>)
    // @Required 3
    // @Maximum 9
    // @Short Creates text channels on Discord.
    // @Plugin dDiscordBot
    // @Group external
    //
    // @Description
    // Creates text channels on Discord.
    //
    // This functionality requires the Manage Channels permission.
    //
    // You can optionally specify the channel description (aka "topic") with the "description" argument.
    //
    // You can optionally specify the channel type. Valid types are TEXT, NEWS, CATEGORY, and VOICE.
    // Only text and news channels can have a description.
    // Categories cannot have a parent category.
    //
    // You can optionally specify the channel's parent category with the "category" argument.
    // By default, the channel will not be attached to any category.
    //
    // You can optionally specify the channel's position in the list as an integer with the "position" argument.
    //
    // You can optionally specify the roles or users that are able to view the channel.
    // The "roles" argument takes a list of DiscordRoleTags, and the "users" argument takes a list of DiscordUserTags.
    // Specifying either of these arguments will create a private channel (hidden for anyone not in the lists).
    //
    // The command should usually be ~waited for. See <@link language ~waitable>.
    //
    // @Tags
    // <entry[saveName].channel> returns the DiscordChannelTag of a channel upon creation when the command is ~waited for.
    //
    // @Usage
    // Use to create a channel in a category.
    // - ~discordcreatechannel id:mybot group:1234 name:my-channel category:5678
    //
    // @Usage
    // Use to create a voice channel and log its name upon creation.
    // - ~discordcreatechannel id:mybot group:1234 name:very-important-channel type:voice save:stuff
    // - debug log "Created channel '<entry[stuff].channel.name>'"
    //
    // -->

    @Override
    public void execute(ScriptEntry scriptEntry) {
        DiscordBotTag bot = scriptEntry.requiredArgForPrefix("id", DiscordBotTag.class);
        DiscordGroupTag group = scriptEntry.requiredArgForPrefix("group", DiscordGroupTag.class);
        ElementTag name = scriptEntry.requiredArgForPrefixAsElement("name");
        ElementTag description = scriptEntry.argForPrefixAsElement("description", null);
        ElementTag type = scriptEntry.argForPrefixAsElement("type", null);
        ElementTag category = scriptEntry.argForPrefixAsElement("category", null);
        ElementTag position = scriptEntry.argForPrefixAsElement("position", null);
        List<DiscordRoleTag> roles = scriptEntry.argForPrefixList("roles", DiscordRoleTag.class, true);
        List<DiscordUserTag> users = scriptEntry.argForPrefixList("users", DiscordUserTag.class, true);
        if (scriptEntry.dbCallShouldDebug()) {
            Debug.report(scriptEntry, getName(), bot, group, name, description, category);
        }
        if (group != null && group.bot == null) {
            group.bot = bot.bot;
        }
        Runnable runner = () -> {
            try {
                ChannelAction<? extends GuildChannel> action;
                ChannelType typeEnum = type != null ? ChannelType.valueOf(type.asString().toUpperCase()) : ChannelType.TEXT;
                switch (typeEnum) {
                    case NEWS:
                    case TEXT: {
                        action = group.getGuild().createTextChannel(name.asString()).setType(typeEnum);
                        break;
                    }
                    case CATEGORY: {
                        action = group.getGuild().createCategory(name.asString());
                        break;
                    }
                    case VOICE: {
                        action = group.getGuild().createVoiceChannel(name.asString());
                        break;
                    }
                    default: {
                        handleError(scriptEntry, "Invalid channel type!");
                        return;
                    }
                }
                if (description != null) {
                    if (typeEnum != ChannelType.TEXT && typeEnum != ChannelType.NEWS) {
                        handleError(scriptEntry, "Only text and news channels can have descriptions!");
                        return;
                    }
                    action = action.setTopic(description.asString());
                }
                if (category != null) {
                    if (typeEnum == ChannelType.CATEGORY) {
                        handleError(scriptEntry, "A category cannot have a parent category!");
                        return;
                    }
                    Category resultCategory = group.getGuild().getCategoryById(category.asString());
                    if (resultCategory == null) {
                        handleError(scriptEntry, "Invalid category!");
                        return;
                    }
                    action = action.setParent(resultCategory);
                }
                if (position != null) {
                    action = action.setPosition(position.asInt());
                }
                Set<Permission> permissions = Collections.singleton(Permission.VIEW_CHANNEL);
                if (roles != null || users != null) {
                    action = action.addRolePermissionOverride(group.guild_id, null, permissions);
                }
                if (roles != null) {
                    for (DiscordRoleTag role : roles) {
                        action = action.addRolePermissionOverride(role.role_id, permissions, null);
                    }
                }
                if (users != null) {
                    for (DiscordUserTag user : users) {
                        action = action.addMemberPermissionOverride(user.user_id, permissions, null);
                    }
                }
                GuildChannel resultChannel = action.complete();
                scriptEntry.addObject("channel", new DiscordChannelTag(bot.bot, resultChannel));
            }
            catch (Exception ex) {
                handleError(scriptEntry, ex);
            }
        };
        Bukkit.getScheduler().runTaskAsynchronously(DenizenDiscordBot.instance, () -> {
            runner.run();
            scriptEntry.setFinished(true);
        });
    }
}
