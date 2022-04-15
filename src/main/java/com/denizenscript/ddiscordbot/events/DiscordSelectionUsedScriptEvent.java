package com.denizenscript.ddiscordbot.events;

import com.denizenscript.ddiscordbot.DiscordScriptEvent;
import com.denizenscript.ddiscordbot.objects.DiscordChannelTag;
import com.denizenscript.ddiscordbot.objects.DiscordGroupTag;
import com.denizenscript.ddiscordbot.objects.DiscordInteractionTag;
import com.denizenscript.ddiscordbot.objects.DiscordSelectionTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import net.dv8tion.jda.api.events.interaction.component.SelectMenuInteractionEvent;

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
    //
    // -->

    public static DiscordSelectionUsedScriptEvent instance;

    public DiscordSelectionUsedScriptEvent() {
        instance = this;
        registerCouldMatcher("discord selection used");
        registerSwitches("channel", "group", "id");
    }

    public SelectMenuInteractionEvent getEvent() {
        return (SelectMenuInteractionEvent) event;
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
                return DiscordSelectionTag.getSelectionOption(getEvent().getSelectedOptions().get(0));
        }
        return super.getContext(name);
    }

    @Override
    public String getName() {
        return "DiscordSelectionUsed";
    }
}
