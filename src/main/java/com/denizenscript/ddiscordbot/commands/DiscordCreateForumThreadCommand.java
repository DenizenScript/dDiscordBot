package com.denizenscript.ddiscordbot.commands;

import com.denizenscript.ddiscordbot.DenizenDiscordBot;
import com.denizenscript.ddiscordbot.objects.DiscordBotTag;
import com.denizenscript.ddiscordbot.objects.DiscordChannelTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;
import com.denizenscript.denizencore.scripts.commands.Holdable;
import com.denizenscript.denizencore.scripts.commands.generator.ArgDefaultNull;
import com.denizenscript.denizencore.scripts.commands.generator.ArgName;
import com.denizenscript.denizencore.scripts.commands.generator.ArgPrefixed;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.entities.channel.concrete.ForumChannel;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import net.dv8tion.jda.api.entities.channel.forums.ForumTagSnowflake;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import org.bukkit.Bukkit;

import java.util.ArrayList;
import java.util.Collection;

public class DiscordCreateForumThreadCommand extends AbstractCommand implements Holdable {

    public DiscordCreateForumThreadCommand() {
        setName("discordcreateforumthread");
        setSyntax("discordcreateforumthread [id:<id>] [name:<name>] [message:<text>] [channel:<channel>] (tags:<ForumTagID>|...)");
        setRequiredArguments(4, 5);
        isProcedural = false;
        autoCompile();
    }

    // <--[command]
    // @Name discordcreateforumthread
    // @Syntax discordcreateforumthread [id:<id>] [name:<name>] [message:<text>] [channel:<channel>] (tags:<ForumTagID>|...)
    // @Required 4
    // @Maximum 5
    // @Short Creates a new Discord forum thread.
    // @Plugin dDiscordBot
    // @Guide https://guide.denizenscript.com/guides/expanding/ddiscordbot.html
    // @Group external
    // @Synonyms CreatePost, ForumPost, CreateThread, MakePost
    //
    // @Description
    // Creates a new Discord forum thread.
    //
    // You must specify the bot object ID, the thread name, the initial message and the channel ID.
    //
    // Optionally specify a list of ForumTagIDs to apply forum tags.
    // Note: You can't have more than 5 forum tags in a forum post. Adding more will result in an error.
    //
    // @Tags
    // <entry[saveName].created_thread> returns the newly created thread.
    // <DiscordChannelTag.available_tags> Returns all available forum tags from a discord forum channel.
    //
    // @Usage
    // Use to create a new forum thread.
    // - ~discordcreateforumthread id:mybot "name:The bot's awesome thread" "message:Hello, this is my forum post now!" channel:<[channel]>
    //
    // Use to create a new forum thread and apply a forum tag.
    // - ~discordcreateforumthread id:mybot "name:The bot's awesome thread" "message:Hello, this is my forum post now!" channel:<[channel]> tags:<[channel].available_tags.get[1].get[id]>
    //
    // -->

    public static void autoExecute(ScriptEntry scriptEntry,
                                   @ArgPrefixed @ArgName("id") DiscordBotTag bot,
                                   @ArgPrefixed @ArgName("name") String name,
                                   @ArgPrefixed @ArgName("message") String message,
                                   @ArgPrefixed @ArgName("channel") DiscordChannelTag channel,
                                   @ArgPrefixed @ArgName("tags") @ArgDefaultNull ListTag tags) {
        Runnable runner = () -> {
            Collection<ForumTagSnowflake> forumTags = new ArrayList<>();
            Channel actualChannel = channel.getChannel();
            if (tags != null) {
                for (String tag : tags) {
                    if (((ForumChannel) actualChannel).getAvailableTagById(tag) != null) {
                        forumTags.add(ForumTagSnowflake.fromId(tag));
                    }
                }
            }
            if (!(actualChannel instanceof ForumChannel)) {
                Debug.echoError(scriptEntry, "You can not create a forum thread in non forum channels.");
                return;
            }
            try {
                ThreadChannel created;
                if (forumTags.isEmpty()) {
                    created = ((ForumChannel) actualChannel).createForumPost(name, MessageCreateData.fromContent(message)).complete().getThreadChannel();
                } else {
                    created = ((ForumChannel) actualChannel).createForumPost(name, MessageCreateData.fromContent(message)).setTags(forumTags).complete().getThreadChannel();
                }
                scriptEntry.addObject("created_thread", new DiscordChannelTag(bot.bot, created));
            } catch (Throwable ex) {
                Debug.echoError(scriptEntry, ex);
            }
        };
        Bukkit.getScheduler().runTaskAsynchronously(DenizenDiscordBot.instance, () -> {
            runner.run();
            scriptEntry.setFinished(true);
        });
    }
}
