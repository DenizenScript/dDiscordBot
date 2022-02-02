package com.denizenscript.ddiscordbot.commands;

import com.denizenscript.ddiscordbot.DenizenDiscordBot;
import com.denizenscript.ddiscordbot.objects.DiscordChannelTag;
import com.denizenscript.ddiscordbot.objects.DiscordGroupTag;
import com.denizenscript.denizen.utilities.debugging.Debug;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsException;
import com.denizenscript.denizencore.objects.Argument;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;
import com.denizenscript.denizencore.scripts.commands.Holdable;
import net.dv8tion.jda.api.entities.Category;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.requests.restaction.ChannelAction;
import org.bukkit.Bukkit;

public class DiscordChannelCommand extends AbstractCommand implements Holdable {

    public DiscordChannelCommand() {
        setName("discordchannel");
        setSyntax("discordchannel [id:<id>] (group:<group_id>) (channel:<channel_id>) [create/delete] (name:<name>) (description:<description>) (category:<category_id>)");
        setRequiredArguments(3, 6);
    }
    // <--[command]
    // @Name discordchannel
    // @Syntax discordchannel [id:<id>] (group:<group_id>) (channel:<channel_id>) [create/delete] (name:<name>) (description:<description>) (category:<category_id>)
    // @Required 3
    // @Maximum 6
    // @Short Manages text channels on Discord.
    // @Plugin dDiscordBot
    // @Guide https://guide.denizenscript.com/guides/expanding/ddiscordbot.html
    // @Group external
    //
    // @Description
    // Manages text channels on Discord.
    //
    // Use the "create" instruction to create a channel.
    // This requires a group and a name to be specified.
    //
    // You can specify the channel "topic" (description) with the "description" argument.
    //
    // You can specify the channel's parent category with the "category" argument.
    // By default, the channel will not be attached to any category.
    //
    // Use the "delete" instruction to delete an existing channel.
    // This requires a channel to be specified.
    //
    // The command should usually be ~waited for. See <@link language ~waitable>.
    //
    // @Tags
    // <entry[saveName].channel> returns the DiscordChannelTag of a channel upon creation when the command is ~waited for.
    //
    // @Usage
    // Use to create a channel in a category.
    // - ~discordchannel id:mybot group:1234 create name:my-channel category:5678
    //
    // @Usage
    // Use to create a channel and log its name upon creation.
    // - ~discordchannel id:mybot group:1234 create name:very-important-channel save:stuff
    // - debug log "Created channel '<entry[stuff].channel.name>'"
    //
    // @Usage
    // Use to delete a channel.
    // - ~discordchannel id:mybot channel:9999 delete
    //
    // -->

    public enum DiscordChannelInstruction { CREATE, DELETE }

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {
        for (Argument arg : scriptEntry) {
            if (!scriptEntry.hasObject("instruction")
                    && arg.matchesEnum(DiscordChannelInstruction.values())) {
                scriptEntry.addObject("instruction", arg.asElement());
            }
        }
        if (!scriptEntry.hasObject("instruction")) {
            throw new InvalidArgumentsException("Must have an instruction!");
        }
    }

    @Override
    public void execute(ScriptEntry scriptEntry) {
        ElementTag id = scriptEntry.requiredArgForPrefixAsElement("id");
        ElementTag instruction = scriptEntry.getElement("instruction");
        DiscordGroupTag group = scriptEntry.argForPrefix("group", DiscordGroupTag.class, true);
        DiscordChannelTag channel = scriptEntry.argForPrefix("channel", DiscordChannelTag.class, true);
        ElementTag name = scriptEntry.argForPrefixAsElement("name", null);
        ElementTag description = scriptEntry.argForPrefixAsElement("description", null);
        ElementTag category = scriptEntry.argForPrefixAsElement("category", null);
        if (scriptEntry.dbCallShouldDebug()) {
            Debug.report(scriptEntry, getName(), id, instruction, group, channel, name, description);
        }
        if (group != null && group.bot == null) {
            group.bot = id.asString();
        }
        if (channel != null && channel.bot == null) {
            channel.bot = id.asString();
        }
        Bukkit.getScheduler().runTaskAsynchronously(DenizenDiscordBot.instance, () -> {
            try {
                DiscordChannelInstruction instructionEnum = DiscordChannelInstruction.valueOf(instruction.asString().toUpperCase());
                switch (instructionEnum) {
                    case CREATE: {
                        if (group == null) {
                            Debug.echoError("Must specify a group!");
                            scriptEntry.setFinished(true);
                            return;
                        }
                        else if (name == null) {
                            Debug.echoError("Must specify a channel name!");
                            scriptEntry.setFinished(true);
                            return;
                        }
                        ChannelAction<TextChannel> action = group.getGuild().createTextChannel(name.asString());
                        if (description != null) {
                            action = action.setTopic(description.asString());
                        }
                        if (category != null) {
                            Category resultCategory = group.getGuild().getCategoryById(category.asString());
                            if (resultCategory == null) {
                                Debug.echoError("Invalid category!");
                                scriptEntry.setFinished(true);
                                return;
                            }
                            action = action.setParent(resultCategory);
                        }
                        TextChannel resultChannel = action.complete();
                        scriptEntry.addObject("channel", new DiscordChannelTag(id.asString(), resultChannel));
                        break;
                    }
                    case DELETE: {
                        if (channel == null) {
                            return;
                        }
                        channel.getChannel().delete().complete();
                        break;
                    }
                }
            }
            catch (Exception e) {
                Debug.echoError(e);
            }
            scriptEntry.setFinished(true);
        });
    }
}
