package com.denizenscript.ddiscordbot;

import net.aufdemrand.denizencore.events.ScriptEvent;
import net.aufdemrand.denizencore.objects.Element;
import net.aufdemrand.denizencore.objects.dList;
import net.aufdemrand.denizencore.objects.dObject;
import net.aufdemrand.denizencore.utilities.CoreUtilities;
import sx.blah.discord.api.events.Event;
import sx.blah.discord.handle.impl.events.guild.GuildEvent;
import sx.blah.discord.handle.impl.events.guild.channel.ChannelEvent;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageEvent;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageUpdateEvent;
import sx.blah.discord.handle.impl.events.guild.member.GuildMemberEvent;
import sx.blah.discord.handle.obj.IUser;

public abstract class DiscordScriptEvent extends ScriptEvent {

    public String botID;

    public Event event;

    @Override
    public boolean matches(ScriptPath path) {
        return runBotIDCheck(path, "for");
    }

    @Override
    public dObject getContext(String name) {
        if (name.equals("bot")) {
            return new Element(botID);
        }
        else if (name.equals("self")) {
            return new Element(event.getClient().getOurUser().getLongID());
        }
        if (event instanceof ChannelEvent) {
            if (name.equals("channel")) {
                return new Element(((ChannelEvent) event).getChannel().getLongID());
            }
            else if (name.equals("channel_name")) {
                return new Element(((ChannelEvent) event).getChannel().getName());
            }
            else if (name.equals("is_private")) {
                return new Element(((ChannelEvent) event).getChannel().isPrivate());
            }
        }
        if (event instanceof GuildEvent) {
            if (name.equals("group")) {
                return new Element(((GuildEvent) event).getGuild().getLongID());
            }
            else if (name.equals("group_name")) {
                return new Element(((GuildEvent) event).getGuild().getName());
            }
        }
        if (event instanceof MessageEvent) {
            if (name.equals("message")) {
                return new Element(((MessageEvent) event).getMessage().getContent());
            }
            if (name.equals("new_message_valid")) {
                return new Element(((MessageEvent) event).getMessage() != null);
            }
            if (name.equals("no_mention_message")) {
                String res = ((MessageEvent) event).getMessage().getContent();
                for (IUser user : ((MessageEvent) event).getMessage().getMentions()) {
                    res = res.replace(user.mention(true), "")
                            .replace(user.mention(false), "");
                }
                return new Element(res);
            }
            else if (name.equals("formatted_message")) {
                return new Element(((MessageEvent) event).getMessage().getFormattedContent());
            }
            else if (name.equals("author_id")) {
                return new Element(((MessageEvent) event).getAuthor().getLongID());
            }
            else if (name.equals("author_name")) {
                return new Element(((MessageEvent) event).getAuthor().getName());
            }
            else if (name.equals("mentions")) {
                dList list = new dList();
                for (IUser user : ((MessageEvent) event).getMessage().getMentions()) {
                    list.add(String.valueOf(user.getLongID()));
                }
                return list;
            }
            else if (name.equals("mention_names")) {
                dList list = new dList();
                for (IUser user : ((MessageEvent) event).getMessage().getMentions()) {
                    list.add(String.valueOf(user.getName()));
                }
                return list;
            }
        }
        if (event instanceof MessageUpdateEvent) {
            if (name.equals("old_message_valid")) {
                return new Element(((MessageUpdateEvent) event).getOldMessage() != null);
            }
            if (name.equals("old_message")) {
                return new Element(((MessageUpdateEvent) event).getOldMessage().getContent());
            }
            if (name.equals("old_no_mention_message")) {
                String res = ((MessageUpdateEvent) event).getOldMessage().getContent();
                for (IUser user : ((MessageUpdateEvent) event).getOldMessage().getMentions()) {
                    res = res.replace(user.mention(true), "")
                            .replace(user.mention(false), "");
                }
                return new Element(res);
            }
            else if (name.equals("old_formatted_message")) {
                return new Element(((MessageUpdateEvent) event).getOldMessage().getFormattedContent());
            }
            else if (name.equals("old_mentions")) {
                dList list = new dList();
                for (IUser user : ((MessageUpdateEvent) event).getOldMessage().getMentions()) {
                    list.add(String.valueOf(user.getLongID()));
                }
                return list;
            }
            else if (name.equals("old_mention_names")) {
                dList list = new dList();
                for (IUser user : ((MessageEvent) event).getMessage().getMentions()) {
                    list.add(String.valueOf(user.getName()));
                }
                return list;
            }
        }
        if (event instanceof GuildMemberEvent) {
            if (name.equals("user_id")) {
                return new Element(((GuildMemberEvent) event).getUser().getLongID());
            }
            else if (name.equals("user_name")) {
                return new Element(((GuildMemberEvent) event).getUser().getName());
            }
        }
        return super.getContext(name);
    }

    public boolean runBotIDCheck(ScriptPath path, String label) {
        String botLabel = path.switches.get(label);
        if (botLabel == null) {
            return true;
        }
        return botLabel.equalsIgnoreCase(botID);
    }

    public boolean enabled = false;

    @Override
    public void init() {
        enabled = true;
    }

    @Override
    public void destroy() {
        enabled = false;
    }
}
