package com.denizenscript.ddiscordbot.events;

import com.denizenscript.ddiscordbot.DiscordScriptEvent;
import com.denizenscript.ddiscordbot.objects.*;
import com.denizenscript.denizencore.objects.ObjectTag;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;

public class DiscordButtonClickedScriptEvent extends DiscordScriptEvent {

    // <--[event]
    // @Events
    // discord button clicked
    //
    // @Switch for:<bot> to only process the event for a specified Discord bot.
    // @Switch channel:<channel_id> to only process the event when it occurs in a specified Discord channel.
    // @Switch group:<group_id> to only process the event for a specified Discord group.
    // @Switch id:<button_id> to only process the event for a specified Discord button.
    //
    // @Triggers when a Discord user clicks a button.
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
    // <context.button> returns the DiscordButtonTag.
    // <context.message> returns the relevant message the button was on.
    //
    // -->

    public static DiscordButtonClickedScriptEvent instance;

    public DiscordButtonClickedScriptEvent() {
        instance = this;
        registerCouldMatcher("discord button clicked");
        registerSwitches("channel", "group", "id");
    }

    public ButtonInteractionEvent getEvent() {
        return (ButtonInteractionEvent) event;
    }

    @Override
    public boolean matches(ScriptPath path) {
        if (!tryChannel(path, getEvent().getChannel())) {
            return false;
        }
        if (!tryGuild(path, getEvent().isFromGuild() ? getEvent().getGuild() : null)) {
            return false;
        }
        if (!runGenericSwitchCheck(path, "id", getEvent().getButton().getId())) {
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
            case "button":
                return new DiscordButtonTag(getEvent().getButton());
            case "message":
                return new DiscordMessageTag(botID, getEvent().getMessage());
        }

        return super.getContext(name);
    }

    @Override
    public String getName() {
        return "DiscordButtonClicked";
    }
}
