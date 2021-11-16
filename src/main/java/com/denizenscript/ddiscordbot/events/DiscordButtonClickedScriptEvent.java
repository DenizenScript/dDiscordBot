package com.denizenscript.ddiscordbot.events;

import com.denizenscript.ddiscordbot.DiscordScriptEvent;
import com.denizenscript.ddiscordbot.objects.DiscordButtonTag;
import com.denizenscript.ddiscordbot.objects.DiscordChannelTag;
import com.denizenscript.ddiscordbot.objects.DiscordGroupTag;
import com.denizenscript.ddiscordbot.objects.DiscordInteractionTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;

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
    // <context.bot> returns the relevant Discord bot object.
    // <context.channel> returns the channel.
    // <context.group> returns the group.
    // <context.interaction> returns the interaction.
    // <context.button> returns the button.
    //
    // -->

    public static DiscordButtonClickedScriptEvent instance;

    public DiscordButtonClickedScriptEvent() {
        instance = this;
        registerCouldMatcher("discord button clicked");
        registerSwitches("for", "channel", "group", "id");
    }

    public ButtonClickEvent getEvent() {
        return (ButtonClickEvent) event;
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
        }

        return super.getContext(name);
    }

    @Override
    public String getName() {
        return "DiscordButtonClicked";
    }
}
