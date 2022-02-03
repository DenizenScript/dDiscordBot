package com.denizenscript.ddiscordbot.commands;

import com.denizenscript.ddiscordbot.DenizenDiscordBot;
import com.denizenscript.ddiscordbot.objects.DiscordChannelTag;
import com.denizenscript.ddiscordbot.objects.DiscordGroupTag;
import com.denizenscript.denizen.utilities.debugging.Debug;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;
import com.denizenscript.denizencore.scripts.commands.Holdable;
import net.dv8tion.jda.api.entities.Category;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.requests.restaction.ChannelAction;
import org.bukkit.Bukkit;

public class DiscordCreateChannelCommand extends AbstractCommand implements Holdable {

    public DiscordCreateChannelCommand() {
        setName("discordcreatechannel");
        setSyntax("discordcreatechannel [id:<id>] [group:<group>] [name:<name>] (description:<description>) (category:<category_id>) (position:<#>)");
        setRequiredArguments(3, 6);
    }
    // <--[command]
    // @Name discordcreatechannel
    // @Syntax discordcreatechannel [id:<id>] [group:<group>] [name:<name>] (description:<description>) (category:<category_id>) (position:<#>)
    // @Required 3
    // @Maximum 6
    // @Short Creates text channels on Discord.
    // @Plugin dDiscordBot
    // @Group external
    //
    // @Description
    // Creates text channels on Discord.
    //
    // You can specify the channel "topic" (description) with the "description" argument.
    //
    // You can specify the channel's parent category with the "category" argument.
    // By default, the channel will not be attached to any category.
    //
    // You can specify the channel's position in the list as an integer with the "position" argument.
    //
    // The command should usually be ~waited for. See <@link language ~waitable>.
    //
    // @Tags
    // <entry[saveName].channel> returns the DiscordChannelTag of a channel upon creation when the command is ~waited for.
    //
    // @Usage
    // Use to create a channel in a category.
    // - ~discordcreatechannel id:mybot group:1234 name:my-channel category:5678
    //
    // @Usage
    // Use to create a channel and log its name upon creation.
    // - ~discordcreatechannel id:mybot group:1234 name:very-important-channel save:stuff
    // - debug log "Created channel '<entry[stuff].channel.name>'"
    //
    // -->

    @Override
    public void parseArgs(ScriptEntry scriptEntry) {
    }

    @Override
    public void execute(ScriptEntry scriptEntry) {
        ElementTag id = scriptEntry.requiredArgForPrefixAsElement("id");
        DiscordGroupTag group = scriptEntry.requiredArgForPrefix("group", DiscordGroupTag.class);
        ElementTag name = scriptEntry.requiredArgForPrefixAsElement("name");
        ElementTag description = scriptEntry.argForPrefixAsElement("description", null);
        ElementTag category = scriptEntry.argForPrefixAsElement("category", null);
        ElementTag position = scriptEntry.argForPrefixAsElement("position", null);
        if (scriptEntry.dbCallShouldDebug()) {
            Debug.report(scriptEntry, getName(), id, group, name, description, category);
        }
        if (group != null && group.bot == null) {
            group.bot = id.asString();
        }
        Runnable runner = () -> {
            try {
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
                if (position != null) {
                    action = action.setPosition(position.asInt());
                }
                TextChannel resultChannel = action.complete();
                scriptEntry.addObject("channel", new DiscordChannelTag(id.asString(), resultChannel));
            }
            catch (Exception e) {
                Debug.echoError(e);
            }
        };
        Bukkit.getScheduler().runTaskAsynchronously(DenizenDiscordBot.instance, () -> {
            runner.run();
            scriptEntry.setFinished(true);
        });
    }
}
