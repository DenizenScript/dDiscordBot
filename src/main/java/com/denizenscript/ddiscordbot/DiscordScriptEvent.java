package com.denizenscript.ddiscordbot;

import net.aufdemrand.denizencore.events.ScriptEvent;
import net.aufdemrand.denizencore.objects.Element;
import net.aufdemrand.denizencore.objects.dList;
import net.aufdemrand.denizencore.objects.dObject;
import sx.blah.discord.api.events.Event;
import sx.blah.discord.handle.impl.events.guild.GuildEvent;
import sx.blah.discord.handle.impl.events.guild.channel.ChannelEvent;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageEvent;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageUpdateEvent;
import sx.blah.discord.handle.impl.events.guild.member.GuildMemberEvent;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IMessage;
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
            else if (name.equals("is_direct")) {
                return new Element(((ChannelEvent) event).getChannel().isPrivate());
            }
        }
        if (event instanceof GuildEvent) {
            if (name.equals("group")) {
                IGuild guild = ((GuildEvent) event).getGuild();
                if (guild != null) {
                    return new Element(guild.getLongID());
                }
            }
            else if (name.equals("group_name")) {
                IGuild guild = ((GuildEvent) event).getGuild();
                if (guild != null) {
                    return new Element(guild.getName());
                }
            }
        }
        if (event instanceof MessageEvent) {
            if (name.equals("new_message_valid")) {
                return new Element(((MessageEvent) event).getMessage() != null);
            }
            if (name.equals("message")) {
                IMessage message = ((MessageEvent) event).getMessage();
                if (message != null) {
                    return new Element(message.getContent());
                }
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
                IMessage message = ((MessageEvent) event).getMessage();
                if (message != null) {
                    return new Element(message.getFormattedContent());
                }
            }
            else if (name.equals("mentions")) {
                IMessage message = ((MessageEvent) event).getMessage();
                if (message != null) {
                    dList list = new dList();
                    for (IUser user : message.getMentions()) {
                        list.add(String.valueOf(user.getLongID()));
                    }
                    return list;
                }
            }
            else if (name.equals("mention_names")) {
                IMessage message = ((MessageEvent) event).getMessage();
                if (message != null) {
                    dList list = new dList();
                    for (IUser user : message.getMentions()) {
                        list.add(String.valueOf(user.getName()));
                    }
                    return list;
                }
            }
            else if (name.equals("author_id")) {
                return new Element(((MessageEvent) event).getAuthor().getLongID());
            }
            else if (name.equals("author_name")) {
                return new Element(((MessageEvent) event).getAuthor().getName());
            }
        }
        if (event instanceof MessageUpdateEvent) {
            if (name.equals("old_message_valid")) {
                return new Element(((MessageUpdateEvent) event).getOldMessage() != null);
            }
            if (name.equals("old_message")) {
                IMessage message = ((MessageUpdateEvent) event).getOldMessage();
                if (message != null) {
                    return new Element(message.getContent());
                }
            }
            if (name.equals("old_no_mention_message")) {
                IMessage message = ((MessageUpdateEvent) event).getOldMessage();
                if (message != null) {
                    String res = message.getContent();
                    for (IUser user : message.getMentions()) {
                        res = res.replace(user.mention(true), "")
                                .replace(user.mention(false), "");
                    }
                    return new Element(res);
                }
            }
            else if (name.equals("old_formatted_message")) {
                IMessage message = ((MessageUpdateEvent) event).getOldMessage();
                if (message != null) {
                    return new Element(message.getFormattedContent());
                }
            }
            else if (name.equals("old_mentions")) {
                IMessage message = ((MessageUpdateEvent) event).getOldMessage();
                if (message != null) {
                    dList list = new dList();
                    for (IUser user : message.getMentions()) {
                        list.add(String.valueOf(user.getLongID()));
                    }
                    return list;
                }
            }
            else if (name.equals("old_mention_names")) {
                IMessage message = ((MessageUpdateEvent) event).getOldMessage();
                if (message != null) {
                    dList list = new dList();
                    for (IUser user : message.getMentions()) {
                        list.add(String.valueOf(user.getName()));
                    }
                    return list;
                }
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
