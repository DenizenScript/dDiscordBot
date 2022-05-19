package com.denizenscript.ddiscordbot.events;

import com.denizenscript.ddiscordbot.DenizenDiscordBot;
import com.denizenscript.ddiscordbot.DiscordScriptEvent;
import com.denizenscript.ddiscordbot.objects.DiscordChannelTag;
import com.denizenscript.ddiscordbot.objects.DiscordGroupTag;
import com.denizenscript.ddiscordbot.objects.DiscordMessageTag;
import com.denizenscript.ddiscordbot.objects.DiscordUserTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.tags.TagContext;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.PrivateChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageUpdateEvent;

public class DiscordMessageModifiedScriptEvent extends DiscordScriptEvent {

    // <--[event]
    // @Events
    // discord message modified
    //
    // @Switch for:<bot> to only process the event for a specified Discord bot.
    // @Switch channel:<channel_id> to only process the event when it occurs in a specified Discord channel.
    // @Switch group:<group_id> to only process the event for a specified Discord group.
    //
    // @Triggers when a Discord user modified a message.
    //
    // @Plugin dDiscordBot
    //
    // @Group Discord
    //
    // @Context
    // <context.bot> returns the relevant DiscordBotTag.
    // <context.channel> returns the DiscordChannelTag.
    // <context.group> returns the DiscordGroupTag.
    // <context.new_message> returns the message as it now exists, as a DiscordMessageTag.
    // <context.old_message_valid> returns whether the old message is available (it may be lost due to caching).
    // <context.old_message> returns the original DiscordMessageTag (data may be missing if not cached).
    //
    // -->

    public static DiscordMessageModifiedScriptEvent instance;

    public Message oldMessage;

    public DiscordMessageModifiedScriptEvent() {
        instance = this;
        registerCouldMatcher("discord message modified");
        registerSwitches("channel", "group");
    }

    public MessageUpdateEvent getEvent() {
        return (MessageUpdateEvent) event;
    }

    @Override
    public boolean matches(ScriptPath path) {
        if (!tryChannel(path, getEvent().getChannel())) {
            return false;
        }
        if (!tryGuild(path, getEvent().isFromGuild() ? getEvent().getGuild() : null)) {
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
            case "new_message":
                return new DiscordMessageTag(botID, getEvent().getMessage());
            case "old_message_valid":
                return new ElementTag(oldMessage != null);
            case "old_message":
                return oldMessage == null ? null : new DiscordMessageTag(botID, oldMessage);
            case "old_no_mention_message":
            case "old_formatted_message":
                DenizenDiscordBot.oldMessageContexts.warn((TagContext) null);
                return null;
            case "message":
                DenizenDiscordBot.oldMessageContexts.warn((TagContext) null);
                return new ElementTag(getEvent().getMessage().getContentRaw());
            case "message_id":
                DenizenDiscordBot.oldMessageContexts.warn((TagContext) null);
                return new ElementTag(getEvent().getMessage().getId());
            case "no_mention_message":
                DenizenDiscordBot.oldMessageContexts.warn((TagContext) null);
                return new ElementTag(DiscordMessageTag.stripMentions(getEvent().getMessage().getContentRaw()));
            case "formatted_message":
                DenizenDiscordBot.oldMessageContexts.warn((TagContext) null);
                return new ElementTag(getEvent().getMessage().getContentDisplay());
            case "author":
                DenizenDiscordBot.oldMessageContexts.warn((TagContext) null);
                return new DiscordUserTag(botID, getEvent().getMessage().getAuthor());
            case "mentions":
                DenizenDiscordBot.oldMessageContexts.warn((TagContext) null);
                ListTag list = new ListTag();
                for (User user : getEvent().getMessage().getMentions().getUsers()) {
                    list.addObject(new DiscordUserTag(botID, user));
                }
                return list;
            case "is_direct":
                DenizenDiscordBot.oldMessageContexts.warn((TagContext) null);
                return new ElementTag(getEvent().getChannel() instanceof PrivateChannel);
        }
        return super.getContext(name);
    }

    @Override
    public String getName() {
        return "DiscordModifiedMessage";
    }
}
