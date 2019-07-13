package com.denizenscript.ddiscordbot.objects;

import com.denizenscript.ddiscordbot.DiscordConnection;
import com.denizenscript.ddiscordbot.dDiscordBot;
import com.denizenscript.denizencore.objects.*;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import discord4j.core.object.entity.*;
import discord4j.core.object.util.Snowflake;
import com.denizenscript.denizencore.tags.Attribute;
import com.denizenscript.denizencore.tags.TagContext;
import com.denizenscript.denizencore.utilities.CoreUtilities;

import java.util.HashMap;

public class dDiscordChannel implements dObject {

    @Fetchable("discordchannel")
    public static dDiscordChannel valueOf(String string, TagContext context) {
        if (string.startsWith("discordchannel@")) {
            string = string.substring("discordchannel@".length());
        }
        if (string.contains("@")) {
            return null;
        }
        int comma = string.indexOf(',');
        String bot = null;
        if (comma > 0) {
            bot = CoreUtilities.toLowerCase(string.substring(0, comma));
            string = string.substring(comma + 1);
        }
        long chanID = ArgumentHelper.getLongFrom(string);
        if (chanID == 0) {
            return null;
        }
        return new dDiscordChannel(bot, chanID);
    }

    public static boolean matches(String arg) {
        if (arg.startsWith("discordchannel@")) {
            return true;
        }
        if (arg.contains("@")) {
            return false;
        }
        int comma = arg.indexOf(',');
        if (comma == -1) {
            return ArgumentHelper.matchesInteger(arg);
        }
        if (comma == arg.length() - 1) {
            return false;
        }
        return ArgumentHelper.matchesInteger(arg.substring(comma + 1));
    }

    public dDiscordChannel(String bot, long channelId) {
        this.bot = bot;
        this.channel_id = channelId;
        if (bot != null) {
            DiscordConnection conn = dDiscordBot.instance.connections.get(bot);
            if (conn != null) {
                channel = conn.client.getChannelById(Snowflake.of(channel_id)).block();
            }
        }
    }

    public dDiscordChannel(String bot, Channel channel) {
        this.bot = bot;
        this.channel = channel;
        channel_id = channel.getId().asLong();
    }

    public Channel channel;

    public String bot;

    public long channel_id;

    public static void registerTags() {

        // <--[tag]
        // @attribute <discordchannel@channel.name>
        // @returns Element
        // @plugin dDiscordBot
        // @description
        // Returns the name of the channel.
        // -->
        registerTag("name", new TagRunnable() {
            @Override
            public String run(Attribute attribute, dObject object) {
                Channel chan = ((dDiscordChannel) object).channel;
                String name;
                if (chan instanceof GuildChannel) {
                    name = ((GuildChannel) chan).getName();
                }
                else if (chan instanceof PrivateChannel) {
                    name = "private";
                }
                else {
                    name = "unknown";
                }
                return new Element(name)
                        .getAttribute(attribute.fulfill(1));
            }
        });

        // <--[tag]
        // @attribute <discordchannel@channel.type>
        // @returns Element
        // @plugin dDiscordBot
        // @description
        // Returns the type of the channel.
        // Will be any of: GUILD_TEXT, DM, GUILD_VOICE, GROUP_DM, GUILD_CATEGORY
        // -->
        registerTag("type", new TagRunnable() {
            @Override
            public String run(Attribute attribute, dObject object) {
                return new Element(((dDiscordChannel) object).channel.getType().name())
                        .getAttribute(attribute.fulfill(1));
            }
        });

        // <--[tag]
        // @attribute <discordchannel@channel.id>
        // @returns Element(Number)
        // @plugin dDiscordBot
        // @description
        // Returns the ID number of the channel.
        // -->
        registerTag("id", new TagRunnable() {
            @Override
            public String run(Attribute attribute, dObject object) {
                return new Element(((dDiscordChannel) object).channel_id)
                        .getAttribute(attribute.fulfill(1));
            }
        });

        // <--[tag]
        // @attribute <discordchannel@channel.mention>
        // @returns Element
        // @plugin dDiscordBot
        // @description
        // Returns the raw mention string for the channel.
        // -->
        registerTag("mention", new TagRunnable() {
            @Override
            public String run(Attribute attribute, dObject object) {
                return new Element(((dDiscordChannel) object).channel.getMention())
                        .getAttribute(attribute.fulfill(1));
            }
        });

        // <--[tag]
        // @attribute <discordchannel@channel.group>
        // @returns DiscordGroup
        // @plugin dDiscordBot
        // @description
        // Returns the group that owns this channel.
        // -->
        registerTag("group", new TagRunnable() {
            @Override
            public String run(Attribute attribute, dObject object) {
                Channel chan = ((dDiscordChannel) object).channel;
                Guild guild;
                if (chan instanceof GuildChannel) {
                    guild = ((GuildChannel) chan).getGuild().block();
                }
                else {
                    return null;
                }
                return new dDiscordGroup(((dDiscordChannel) object).bot, guild)
                        .getAttribute(attribute.fulfill(1));
            }
        });
    }

        public static HashMap<String, TagRunnable> registeredTags = new HashMap<>();

    public static void registerTag(String name, TagRunnable runnable) {
        if (runnable.name == null) {
            runnable.name = name;
        }
        registeredTags.put(name, runnable);
    }

    @Override
    public String getAttribute(Attribute attribute) {
        if (attribute == null) {
            return null;
        }

        // TODO: Scrap getAttribute, make this functionality a core system
        String attrLow = CoreUtilities.toLowerCase(attribute.getAttributeWithoutContext(1));
        TagRunnable tr = registeredTags.get(attrLow);
        if (tr != null) {
            if (!tr.name.equals(attrLow)) {
                Debug.echoError(attribute.getScriptEntry() != null ? attribute.getScriptEntry().getResidingQueue() : null,
                        "Using deprecated form of tag '" + tr.name + "': '" + attrLow + "'.");
            }
            return tr.run(attribute, this);
        }

        return new Element(identify()).getAttribute(attribute);
    }

    String prefix = "discordchannel";

    @Override
    public String getPrefix() {
        return prefix;
    }

    @Override
    public String debug() {
        return (prefix + "='<A>" + identify() + "<G>'  ");
    }

    @Override
    public boolean isUnique() {
        return false;
    }

    @Override
    public String getObjectType() {
        return "DiscordChannel";
    }

    @Override
    public String identify() {
        if (bot != null) {
            return "discordchannel@" + bot + "," + channel_id;
        }
        return "discordchannel@" + channel_id;
    }

    @Override
    public String identifySimple() {
        return identify();
    }

    @Override
    public String toString() {
        return identify();
    }

    @Override
    public dObject setPrefix(String prefix) {
        if (prefix != null) {
            this.prefix = prefix;
        }
        return this;
    }
}
