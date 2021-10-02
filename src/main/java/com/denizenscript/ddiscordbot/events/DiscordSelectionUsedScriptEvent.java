package com.denizenscript.ddiscordbot.events;

import com.denizenscript.ddiscordbot.DiscordScriptEvent;
import com.denizenscript.ddiscordbot.objects.DiscordChannelTag;
import com.denizenscript.ddiscordbot.objects.DiscordGroupTag;
import com.denizenscript.ddiscordbot.objects.DiscordInteractionTag;
import com.denizenscript.ddiscordbot.objects.DiscordSelectionTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import net.dv8tion.jda.api.events.interaction.SelectionMenuEvent;

public class DiscordSelectionUsedScriptEvent extends DiscordScriptEvent {

    public static DiscordSelectionUsedScriptEvent instance;

    // <--[event]
    // @Events
    // discord selection used
    //
    // @Regex ^on discord selection used$
    //
    // @Switch for:<bot> to only process the event for a specified Discord bot.
    // @Switch channel:<channel_id> to only process the event when it occurs in a specified Discord channel.
    // @Switch group:<group_id> to only process the event for a specified Discord group.
    // @Switch id:<menu_id> to only process the event for a specified Discord selection menu.
    //
    // @Triggers when a Discord user uses a selection menu.
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
    // <context.menu> returns the selection menu.
    // <context.options> returns the selected options.
    //
    // -->

    public SelectionMenuEvent getEvent() {
        return (SelectionMenuEvent) event;
    }

    @Override
    public boolean couldMatch(ScriptPath path) {
        return path.eventLower.startsWith("discord selection used");
    }

    @Override
    public boolean matches(ScriptPath path) {
        if (!path.checkSwitch("channel", getEvent().getChannel().getId())) {
            return false;
        }
        if (path.switches.containsKey("group")) {
            if (!getEvent().isFromGuild()) {
                return false;
            }
            if (!path.checkSwitch("group", getEvent().getGuild().getId())) {
                return false;
            }
        }
        if (!path.checkSwitch("id", getEvent().getSelectionMenu().getId())) {
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
            case "menu":
                return new DiscordSelectionTag(getEvent().getSelectionMenu());
            case "options":
                return DiscordSelectionTag.getSelectionOptions(getEvent().getSelectedOptions());
        }
        return super.getContext(name);
    }

    @Override
    public String getName() {
        return "DiscordSelectionUsed";
    }
}
