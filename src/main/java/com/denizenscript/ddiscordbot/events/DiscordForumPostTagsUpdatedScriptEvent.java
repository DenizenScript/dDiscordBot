package com.denizenscript.ddiscordbot.events;

import com.denizenscript.ddiscordbot.DiscordScriptEvent;
import com.denizenscript.ddiscordbot.objects.DiscordChannelTag;
import com.denizenscript.ddiscordbot.objects.DiscordGroupTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import net.dv8tion.jda.api.entities.channel.forums.ForumTag;
import net.dv8tion.jda.api.events.channel.update.ChannelUpdateAppliedTagsEvent;

import java.util.List;

public class DiscordForumPostTagsUpdatedScriptEvent extends DiscordScriptEvent {

    // <--[event]
    // @Events
    // discord forum post tags updated
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
    // <context.added_tags> returns a ListTag(MapTag) of all added tags in the same format as <@link tag DiscordChannelTag.applied_tags>.
    // <context.removed_tags> returns a ListTag(MapTag) of all removed tags in the same format as <@link tag DiscordChannelTag.applied_tags>.
    // <context.old_tags> returns a ListTag(MapTag) of the post's old tags in the same format as <@link tag DiscordChannelTag.applied_tags>.
    // <context.new_tags> returns a ListTag(MapTag) of the post's new tags in the same format as <@link tag DiscordChannelTag.applied_tags>.
    // <context.post> returns a DiscordChannelTag of the post whose tags were updated.
    // -->

    public static DiscordForumPostTagsUpdatedScriptEvent instance;

    public DiscordForumPostTagsUpdatedScriptEvent() {
        registerCouldMatcher("discord forum post tags updated");
        registerSwitches("added", "removed", "post", "forum", "group");
        instance = this;
    }

    public ChannelUpdateAppliedTagsEvent getEvent() {
        return (ChannelUpdateAppliedTagsEvent) event;
    }

    @Override
    public boolean matches(ScriptPath path) {
        if (!matchTagsList(path, getEvent().getAddedTags(), "added")) {
            return false;
        }
        if (!matchTagsList(path, getEvent().getRemovedTags(), "removed")) {
            return false;
        }
        if (!tryChannel(path, getEvent().getChannel(), "post")) {
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
            case "added_tags": return DiscordChannelTag.getListForForumTags(getEvent().getAddedTags(), getConnection());
            case "removed_tags": return DiscordChannelTag.getListForForumTags(getEvent().getRemovedTags(), getConnection());
            case "old_tags": return DiscordChannelTag.getListForForumTags(getEvent().getOldTags(), getConnection());
            case "new_tags": return DiscordChannelTag.getListForForumTags(getEvent().getNewTags(), getConnection());
            case "post": return new DiscordChannelTag(botID, getEvent().getChannel());
            case "group": return new DiscordGroupTag(botID, getEvent().getGuild());
        }
        return super.getContext(name);
    }

    public boolean matchTagsList(ScriptPath path, List<ForumTag> tags, String switchName) {
        if (tags.isEmpty() && !path.switches.containsKey(switchName)) {
            return true;
        }
        for (ForumTag tag : tags) {
            if (tryForumsTag(path, tag, switchName)) {
                return true;
            }
        }
        return false;
    }
}
