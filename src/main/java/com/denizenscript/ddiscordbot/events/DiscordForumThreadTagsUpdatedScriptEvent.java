package com.denizenscript.ddiscordbot.events;

import com.denizenscript.ddiscordbot.DiscordScriptEvent;
import com.denizenscript.ddiscordbot.objects.DiscordChannelTag;
import com.denizenscript.ddiscordbot.objects.DiscordGroupTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import net.dv8tion.jda.api.events.channel.update.ChannelUpdateAppliedTagsEvent;

public class DiscordForumThreadTagsUpdatedScriptEvent extends DiscordScriptEvent {

    // <--[event]
    // @Events
    // discord forum thread tags updated
    //
    // @Switch for:<bot> to only process the event for a specified Discord bot.
    // @Switch group:<group> to only process the event for a specified Discord group.
    // @Switch added:<tag> to only process the event if at least one of the tags added matches the specified tag.
    // @Switch removed:<tag> to only process the event if at least one of the tags removed matches the specified tag.
    // @Switch post:<channel> to only process the event if the post whose tags were updated matches the specified channel.
    // @Switch forum:<channel> to only process the event if the forum the post is in matches the specified channel.
    //
    // @Triggers when a Discord forum post's tags change.
    //
    // @Plugin dDiscordBot
    //
    // @Group Discord
    //
    // @Context
    // <context.bot> returns the relevant DiscordBotTag.
    // <context.group> returns the DiscordGroupTag.
    // <context.added_tags> returns a ListTag(MapTag) of all added tags in the same format as <@link tag DiscordChannelTag.available_tags>.
    // <context.removed_tags> returns a ListTag(MapTag) of all removed tags in the same format as <@link tag DiscordChannelTag.available_tags>.
    // <context.old_tags> returns a ListTag(MapTag) of the post's old tags in the same format as <@link tag DiscordChannelTag.available_tags>.
    // <context.new_tags> returns a ListTag(MapTag) of the post's new tags in the same format as <@link tag DiscordChannelTag.available_tags>.
    // <context.thread> returns a DiscordChannelTag of the post whose tags were updated.
    //
    // @Example
    // This example fires the event when the forum tag added is called 'MyTag'.
    // on discord forum thread tags updated added:MyTag
    // - debug log "<[text]>The forum tag <context.added_tags.first.get[name].custom_color[emphasis]> was added to a forum post."
    //
    // -->

    public static DiscordForumThreadTagsUpdatedScriptEvent instance;

    public DiscordForumThreadTagsUpdatedScriptEvent() {
        instance = this;
        registerCouldMatcher("discord forum thread tags updated");
        registerSwitches("added", "removed", "thread", "forum", "group");
    }

    public ChannelUpdateAppliedTagsEvent getEvent() {
        return (ChannelUpdateAppliedTagsEvent) event;
    }

    @Override
    public boolean matches(ScriptPath path) {
        if (!tryForumTags(path, getEvent().getAddedTags(), "added")) {
            return false;
        }
        if (!tryForumTags(path, getEvent().getRemovedTags(), "removed")) {
            return false;
        }
        if (!tryChannel(path, getEvent().getChannel(), "thread")) {
            return false;
        }
        if (!tryChannel(path, getEvent().getChannel().asThreadChannel().getParentChannel(), "forum")) {
            return false;
        }
        if (!tryGuild(path, getEvent().getGuild())) {
            return false;
        }
        return super.matches(path);
    }

    @Override
    public ObjectTag getContext(String name) {
        switch (name) {
            case "added_tags": return DiscordChannelTag.getForumTags(getEvent().getAddedTags(), getConnection());
            case "removed_tags": return DiscordChannelTag.getForumTags(getEvent().getRemovedTags(), getConnection());
            case "old_tags": return DiscordChannelTag.getForumTags(getEvent().getOldTags(), getConnection());
            case "new_tags": return DiscordChannelTag.getForumTags(getEvent().getNewTags(), getConnection());
            case "thread": return new DiscordChannelTag(botID, getEvent().getChannel());
            case "group": return new DiscordGroupTag(botID, getEvent().getGuild());
        }
        return super.getContext(name);
    }
}
