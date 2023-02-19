package com.denizenscript.ddiscordbot.events;

import com.denizenscript.ddiscordbot.DiscordScriptEvent;
import com.denizenscript.ddiscordbot.objects.*;
import com.denizenscript.denizencore.objects.ObjectTag;
import net.dv8tion.jda.api.events.interaction.component.GenericSelectMenuInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;

public class DiscordSelectionUsedScriptEvent extends DiscordScriptEvent {

    // <--[event]
    // @Events
    // discord selection used
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
    // <context.bot> returns the relevant DiscordBotTag.
    // <context.channel> returns the DiscordChannelTag.
    // <context.group> returns the DiscordGroupTag.
    // <context.interaction> returns the DiscordInteractionTag.
    // <context.menu> returns the selection menu as a DiscordSelectionTag.
    // <context.option> returns the selected option as a MapTag.
    // <context.message> returns the relevant message the selection was on.
    //
    // -->

    public static DiscordSelectionUsedScriptEvent instance;

    public DiscordSelectionUsedScriptEvent() {
        instance = this;
        registerCouldMatcher("discord selection used");
        registerSwitches("channel", "group", "id");
    }

    public GenericSelectMenuInteractionEvent getEvent() {
        return (GenericSelectMenuInteractionEvent) event;
    }

    @Override
    public boolean matches(ScriptPath path) {
        if (!tryChannel(path, getEvent().getChannel())) {
            return false;
        }
        if (!tryGuild(path, getEvent().isFromGuild() ? getEvent().getGuild() : null)) {
            return false;
        }
        if (!runGenericSwitchCheck(path, "id", getEvent().getSelectMenu().getId())) {
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
                return new DiscordSelectionTag(getEvent().getSelectMenu());
            case "option":
                if (getEvent() instanceof StringSelectInteractionEvent stringEvent) {
                    return DiscordSelectionTag.getSelectionOption(stringEvent.getSelectedOptions().get(0));
                }
                else {
                    // TODO: ? EntitySelectInteractionEvent and generic?
                    return null;
                }
            case "message":
                return new DiscordMessageTag(botID, getEvent().getMessage());
        }
        return super.getContext(name);
    }
}
