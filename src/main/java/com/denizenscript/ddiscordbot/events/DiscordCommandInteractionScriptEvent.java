package com.denizenscript.ddiscordbot.events;

import com.denizenscript.ddiscordbot.DiscordScriptEvent;
import com.denizenscript.ddiscordbot.objects.*;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.MapTag;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import net.dv8tion.jda.api.events.interaction.GenericInteractionCreateEvent;
import net.dv8tion.jda.api.interactions.commands.CommandInteractionPayload;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

public abstract class DiscordCommandInteractionScriptEvent extends DiscordScriptEvent {

    public DiscordCommandInteractionScriptEvent() {
        registerSwitches("channel", "group", "name");
    }

    public GenericInteractionCreateEvent getEvent() {
        return (GenericInteractionCreateEvent) event;
    }

    // Least verbose abstraction of command/autocomplete event data
    public abstract CommandInteractionPayload getPayload();

    @Override
    public boolean matches(ScriptPath path) {
        if (!tryChannel(path, getEvent().getChannel())) {
            return false;
        }
        if (!tryGuild(path, getEvent().isFromGuild() ? getEvent().getGuild() : null)) {
            return false;
        }
        if (!runGenericSwitchCheck(path, "name", CoreUtilities.replace(getPayload().getName(), " ", "_"))) {
            return false;
        }
        return super.matches(path);
    }

    public ObjectTag getOptionAsObject(OptionMapping option) {
        switch (option.getType()) {
            case STRING:
            case SUB_COMMAND:
            case SUB_COMMAND_GROUP:
                return new ElementTag(option.getAsString());
            case BOOLEAN: return new ElementTag(option.getAsBoolean());
            case INTEGER: return new ElementTag(option.getAsLong());
            case NUMBER: return new ElementTag(option.getAsDouble());
            case ATTACHMENT: return new ElementTag(option.getAsAttachment().getUrl());
            case CHANNEL: return new DiscordChannelTag(botID, option.getAsChannel());
            case MENTIONABLE: {
                String mention = option.getAsMentionable().getAsMention();
                if (mention.startsWith("<@&")) {
                    return new DiscordRoleTag(botID, getEvent().getGuild().getIdLong(), option.getAsMentionable().getIdLong());
                }
                else {
                    return new DiscordUserTag(botID, option.getAsMentionable().getIdLong());
                }
            }
            case ROLE: return new DiscordRoleTag(botID, option.getAsRole());
            case USER: return new DiscordUserTag(botID, option.getAsUser());
            default: return new ElementTag(botID, null);
        }
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
                return new DiscordCommandTag(botID, getEvent().isFromGuild() ? getEvent().getGuild().getIdLong() : 0, getPayload().getCommandIdLong());
            case "options": {
                MapTag options = new MapTag();
                for (OptionMapping mapping : getPayload().getOptions()) {
                    options.putObject(mapping.getName(), getOptionAsObject(mapping));
                }
                return options;
            }
        }
        return super.getContext(name);
    }
}
