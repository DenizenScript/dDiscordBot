package com.denizenscript.ddiscordbot.events;

import com.denizenscript.ddiscordbot.DiscordScriptEvent;
import com.denizenscript.ddiscordbot.objects.DiscordChannelTag;
import com.denizenscript.ddiscordbot.objects.DiscordGroupTag;
import com.denizenscript.ddiscordbot.objects.DiscordInteractionTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.MapTag;
import com.denizenscript.denizencore.utilities.text.StringHolder;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;

import java.util.Map;
import java.util.stream.Collectors;

public class DiscordModalSubmittedScriptEvent extends DiscordScriptEvent {

    // <--[event]
    // @Events
    // discord modal submitted
    //
    // @Switch for:<bot> to only process the event for a specified Discord bot.
    // @Switch channel:<channel_id> to only process the event when it occurs in a specified Discord channel.
    // @Switch group:<group_id> to only process the event for a specified Discord group.
    // @Switch name:<modal_name> to only process the event for a specified Discord modal.
    //
    // @Triggers when a Discord user submits a modal.
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
    // <context.name> returns the name of the modal.
    // <context.values> returns a MapTag of the values submitted by the user.
    //
    // -->

    public static DiscordModalSubmittedScriptEvent instance;

    public DiscordModalSubmittedScriptEvent() {
        instance = this;
        registerCouldMatcher("discord modal submitted");
        registerSwitches("channel", "group", "name");
    }

    public ModalInteractionEvent getEvent() {
        return (ModalInteractionEvent) event;
    }

    @Override
    public boolean matches(ScriptPath path) {
        if (!tryChannel(path, getEvent().getChannel())) {
            return false;
        }
        if (!tryGuild(path, getEvent().isFromGuild() ? getEvent().getGuild() : null)) {
            return false;
        }
        if (!runGenericSwitchCheck(path, "name", getEvent().getModalId())) {
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
            case "name":
                return new ElementTag(getEvent().getModalId());
            case "values":
                Map<StringHolder, ObjectTag> map = getEvent().getValues().stream()
                        .collect(Collectors.toMap(key -> new StringHolder(key.getId()), value -> new ElementTag(value.getAsString(), true)));
                return new MapTag(map);
        }
        return super.getContext(name);
    }
}
