package com.denizenscript.ddiscordbot.events;

import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.CommandInteractionPayload;

public class DiscordApplicationCommandScriptEvent extends DiscordCommandInteractionScriptEvent {

    // <--[event]
    // @Events
    // discord application|slash|message|user command
    //
    // @Switch for:<bot> to only process the event for a specified Discord bot.
    // @Switch channel:<channel_id> to only process the event when it occurs in a specified Discord channel.
    // @Switch group:<group_id> to only process the event for a specified Discord group.
    // @Switch name:<command_name> to only process the event for a specified Discord application command. Spaces are replaced with underscores.
    //
    // @Triggers when a Discord user uses an application command.
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
    // <context.command> returns the DiscordCommandTag.
    // <context.options> returns the supplied options as a MapTag.
    //
    // -->

    public static DiscordApplicationCommandScriptEvent instance;

    public DiscordApplicationCommandScriptEvent() {
        instance = this;
        registerCouldMatcher("discord application|slash|message|user command");
    }

    @Override
    public CommandInteractionPayload getPayload() {
        return ((GenericCommandInteractionEvent) event).getInteraction();
    }

    @Override
    public boolean matches(ScriptPath path) {
        String type = path.eventArgLowerAt(1);
        if (!type.equals("application") && !runGenericCheck(type, getPayload().getCommandType().name())) {
            return false;
        }
        return super.matches(path);
    }
}
