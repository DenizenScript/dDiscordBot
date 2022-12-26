package com.denizenscript.ddiscordbot.events;

import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.objects.core.MapTag;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.CommandInteractionPayload;

import java.util.ArrayList;
import java.util.List;

public class DiscordCommandAutocompleteScriptEvent extends DiscordCommandInteractionScriptEvent {

    // <--[event]
    // @Events
    // discord command autocomplete
    //
    // @Switch for:<bot> to only process the event for a specified Discord bot.
    // @Switch channel:<channel_id> to only process the event when it occurs in a specified Discord channel.
    // @Switch group:<group_id> to only process the event for a specified Discord group.
    // @Switch name:<command_name> to only process the event for a specified Discord application command. Spaces are replaced with underscores.
    // @Switch option:<option_name> to only process the event for a specified autocompletable option.
    //
    // @Triggers when a Discord user queries a slash command option that can be autocompleted.
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
    // <context.focused_option> returns the name of the focused option.
    //
    // @Determine
    // "CHOICES:" + ListTag to suggest values to the Discord client. Up to 25 suggestions are allowed to be sent. Each entry can be an ElementTag which controls both the value and display of the choice or a MapTag with "name" and "value" keys to control both separately.
    //
    // @Example
    // # Suggests fruits that are only longer in length than the current input.
    // on discord command autocomplete name:eat option:fruit:
    // - define length <context.options.get[fruit].length>
    // - define fruits <list[lime|apple|orange|blueberry|dragonfruit]>
    // - determine choices:<[fruits].filter_tag[<[filter_value].length.is_more_than[<[length]>]>]>
    //
    // @Example
    // # Suggests the 25 best-matching selections from some dataset against the current input.
    // on discord command autocomplete:
    // - define value <context.options.get[<context.focused_option>]>
    // - determine choices:<server.flag[dataset].sort_by_number[difference[<[value]>]].first[25].if_null[<list>]>
    //
    // -->

    public static DiscordCommandAutocompleteScriptEvent instance;

    public DiscordCommandAutocompleteScriptEvent() {
        instance = this;
        registerCouldMatcher("discord command autocomplete");
        registerSwitches("option");
    }

    public CommandAutoCompleteInteractionEvent getAutocompleteEvent() {
        return (CommandAutoCompleteInteractionEvent) event;
    }

    @Override
    public CommandInteractionPayload getPayload() {
        return getAutocompleteEvent().getInteraction();
    }

    @Override
    public boolean matches(ScriptPath path) {
        if (!runGenericSwitchCheck(path, "option", getAutocompleteEvent().getFocusedOption().getName())) {
            return false;
        }
        return super.matches(path);
    }

    @Override
    public ObjectTag getContext(String name) {
        switch (name) {
            case "focused_option":
                return new ElementTag(getAutocompleteEvent().getFocusedOption().getName(), true);
        }
        return super.getContext(name);
    }

    public Command.Choice getChoiceSuggestion(ObjectTag objectTag, ScriptPath path) {
        if (objectTag.canBeType(MapTag.class)) {
            MapTag map = objectTag.asType(MapTag.class, getTagContext(path));
            String name = map.getElement("name").asString();
            String value = map.getElement("value").asString();
            return new Command.Choice(name, value);
        }
        String value = objectTag.toString();
        return new Command.Choice(value, value);
    }

    @Override
    public boolean applyDetermination(ScriptPath path, ObjectTag determinationObj) {
        if (determinationObj instanceof ElementTag) {
            String determination = ((ElementTag) determinationObj).asString();
            if (CoreUtilities.toLowerCase(determination).startsWith("choices:")) {
                ListTag list = ListTag.valueOf(determination.substring("choices:".length()), getTagContext(path));
                if (list.size() > 25) {
                    Debug.echoError("Cannot suggest more than 25 choices!");
                    return false;
                }
                List<Command.Choice> choices = new ArrayList<>();
                for (ObjectTag objectTag : list.objectForms) {
                    choices.add(getChoiceSuggestion(objectTag, path));
                }
                getAutocompleteEvent().replyChoices(choices).queue();
                return true;
            }
        }
        return super.applyDetermination(path, determinationObj);
    }
}
