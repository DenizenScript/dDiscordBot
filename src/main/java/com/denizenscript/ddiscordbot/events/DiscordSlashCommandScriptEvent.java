package com.denizenscript.ddiscordbot.events;

import java.util.HashMap;
import java.util.Map;

import com.denizenscript.ddiscordbot.DiscordScriptEvent;
import com.denizenscript.ddiscordbot.objects.DiscordChannelTag;
import com.denizenscript.ddiscordbot.objects.DiscordCommandTag;
import com.denizenscript.ddiscordbot.objects.DiscordGroupTag;
import com.denizenscript.ddiscordbot.objects.DiscordInteractionTag;
import com.denizenscript.ddiscordbot.objects.DiscordRoleTag;
import com.denizenscript.ddiscordbot.objects.DiscordUserTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.MapTag;
import com.denizenscript.denizencore.utilities.text.StringHolder;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

public class DiscordSlashCommandScriptEvent extends DiscordScriptEvent {

    // <--[event]
    // @Events
    // discord slash command
    //
    // @Switch for:<bot> to only process the event for a specified Discord bot.
    // @Switch channel:<channel_id> to only process the event when it occurs in a specified Discord channel.
    // @Switch group:<group_id> to only process the event for a specified Discord group.
    // @Switch name:<command_name> to only process the event for a specified Discord slash command.
    //
    // @Triggers when a Discord user uses a slash command.
    //
    // @Plugin dDiscordBot
    //
    // @Group Discord
    //
    // @Context
    // <context.bot> returns the relevant Discord bot object.
    // <context.channel> returns the channel.
    // <context.group> returns the group.
    // <context.interaction> returns the interaction.
    // <context.command> returns the command.
    // <context.options> returns the supplied options.
    //
    // -->

    public static DiscordSlashCommandScriptEvent instance;

    public DiscordSlashCommandScriptEvent() {
        instance = this;
        registerCouldMatcher("discord slash command");
        registerSwitches("channel", "group", "name");
    }

    public SlashCommandEvent getEvent() {
        return (SlashCommandEvent) event;
    }

    @Override
    public boolean matches(ScriptPath path) {
        if (!tryChannel(path, getEvent().getChannel())) {
            return false;
        }
        if (!tryGuild(path, getEvent().isFromGuild() ? getEvent().getGuild() : null)) {
            return false;
        }
        if (!runGenericSwitchCheck(path, "name", getEvent().getName())) {
            return false;
        }
        return super.matches(path);
    }

    @Override
    public ObjectTag getContext(String name) {
        switch (name) {
            case "channel":
                return new DiscordChannelTag(botID, getEvent().getChannel());
            case "group":
                if (getEvent().isFromGuild()) {
                    return new DiscordGroupTag(botID, getEvent().getGuild());
                }
                break;
            case "interaction":
                return DiscordInteractionTag.getOrCreate(botID, getEvent().getInteraction());
            case "command":
                return new DiscordCommandTag(botID, getEvent().isFromGuild() ? getEvent().getGuild().getIdLong() : 0, getEvent().getCommandIdLong());
            case "options": {
                Map<StringHolder, ObjectTag> options = new HashMap<>();
                for (OptionMapping mapping : getEvent().getOptions()) {
                    ObjectTag result;
                    switch (mapping.getType()) {
                        case STRING:
                        case SUB_COMMAND:
                        case SUB_COMMAND_GROUP:
                            result = new ElementTag(mapping.getAsString()); break;
                        case BOOLEAN: result = new ElementTag(mapping.getAsBoolean()); break;
                        case INTEGER: result = new ElementTag(mapping.getAsLong()); break;
                        case CHANNEL: result = new DiscordChannelTag(botID, mapping.getAsMessageChannel()); break;
                        case MENTIONABLE: {
                            String mention = mapping.getAsMentionable().getAsMention();
                            if (mention.startsWith("<@&")) {
                                result = new DiscordRoleTag(botID, getEvent().getGuild().getIdLong(), mapping.getAsMentionable().getIdLong());
                            }
                            else {
                                result = new DiscordUserTag(botID, mapping.getAsMentionable().getIdLong());
                            }
                            break;
                        }
                        case ROLE: result = new DiscordRoleTag(botID, mapping.getAsRole()); break;
                        case USER: result = new DiscordUserTag(botID, mapping.getAsUser()); break;
                        default: result = new ElementTag(botID, null);
                    }
                    options.put(new StringHolder(mapping.getName()), result);
                }
                return new MapTag(options);
            }
        }

        return super.getContext(name);
    }

    @Override
    public String getName() {
        return "DiscordSlashCommand";
    }
}
