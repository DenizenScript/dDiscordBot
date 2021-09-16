package com.denizenscript.ddiscordbot.objects;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.denizenscript.ddiscordbot.DenizenDiscordBot;
import com.denizenscript.ddiscordbot.DiscordConnection;
import com.denizenscript.denizen.utilities.debugging.Debug;
import com.denizenscript.denizencore.DenizenCore;
import com.denizenscript.denizencore.flags.AbstractFlagTracker;
import com.denizenscript.denizencore.flags.FlaggableObject;
import com.denizenscript.denizencore.flags.RedirectionFlagTracker;
import com.denizenscript.denizencore.objects.ArgumentHelper;
import com.denizenscript.denizencore.objects.Fetchable;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.tags.Attribute;
import com.denizenscript.denizencore.tags.ObjectTagProcessor;
import com.denizenscript.denizencore.tags.TagContext;
import com.denizenscript.denizencore.tags.TagRunnable;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import com.denizenscript.denizencore.utilities.scheduling.OneTimeSchedulable;

import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.PrivateChannel;
import net.dv8tion.jda.api.interactions.Interaction;

public class DiscordInteractionTag implements ObjectTag, FlaggableObject {

    // <--[ObjectType]
    // @name DiscordInteractionTag
    // @prefix discordinteraction
    // @base ElementTag
    // @implements FlaggableObject
    // @format
    // The identity format for Discord interactions is the bot ID (optional), followed by the channel ID (required), followed by the interaction ID (required).
    // For example: 1234,5678
    // Or: mybot,1234,5678
    //
    // @plugin dDiscordBot
    // @description
    // A DiscordInteractionTag is an object that represents an interaction on Discord, either as a generic reference,
    // or as a bot-specific reference.
    //
    // This object type is flaggable.
    // Flags on this object type will be stored in: plugins/dDiscordBot/flags/bot_(botname).dat, under special sub-key "__interactions"
    //
    // -->

    @Fetchable("discordinteraction")
    public static DiscordInteractionTag valueOf(String string, TagContext context) {
        if (string.startsWith("discordinteraction@")) {
            string = string.substring("discordinteraction@".length());
        }
        if (string.contains("@")) {
            return null;
        }
        List<String> commaSplit = CoreUtilities.split(string, ',');
        if (commaSplit.size() == 0 || commaSplit.size() > 4) {
            if (context == null || context.showErrors()) {
                Debug.echoError("DiscordInteractionTag input is not valid.");
            }
            return null;
        }
        String intIdText = commaSplit.get(commaSplit.size() - 1);
        if (!ArgumentHelper.matchesInteger(intIdText)) {
            if (context == null || context.showErrors()) {
                Debug.echoError("DiscordInteractionTag input is not a number.");
            }
            return null;
        }
        long intId = Long.parseLong(intIdText);
        if (intId == 0) {
            return null;
        }
        if (commaSplit.size() == 1) {
            return new DiscordInteractionTag(null, 0, intId);
        }
        String chanIdText = commaSplit.get(commaSplit.size() - 2);
        if (!ArgumentHelper.matchesInteger(chanIdText)) {
            if (context == null || context.showErrors()) {
                Debug.echoError("DiscordInteractionTag channel ID input is not a number.");
            }
            return null;
        }
        long chanId = Long.parseLong(chanIdText);
        if (chanId == 0) {
            return null;
        }
        return new DiscordInteractionTag(commaSplit.size() == 3 ? commaSplit.get(0) : null, chanId, intId);
    }

    public static boolean matches(String arg) {
        if (arg.startsWith("discordinteraction@")) {
            return true;
        }
        if (arg.contains("@")) {
            return false;
        }
        int comma = arg.lastIndexOf(',');
        if (comma == -1) {
            return ArgumentHelper.matchesInteger(arg);
        }
        if (comma == arg.length() - 1) {
            return false;
        }
        return ArgumentHelper.matchesInteger(arg.substring(comma + 1));
    }

    public static Map<Long, Interaction> interactionCache = new HashMap<Long, Interaction>() {
        @Override
        public Interaction put(Long key, Interaction value) {
            DenizenCore.schedule(new OneTimeSchedulable(() -> {
                remove(key);
            }, 15 * 60));
            return super.put(key, value);
        }
    };

    public DiscordInteractionTag(String bot, long channel_id, long interaction_id) {
        this.bot = bot;
        this.channel_id = channel_id;
        this.interaction_id = interaction_id;
    }

    public DiscordInteractionTag(String bot, Interaction interaction) {
        this.bot = bot;
        this.interaction = interaction;
        this.interaction_id = interaction.getIdLong();
        this.channel = this.interaction.getMessageChannel();
        this.channel_id = this.channel.getIdLong();

        DiscordInteractionTag.interactionCache.put(this.interaction_id, this.interaction);
    }

    public DiscordConnection getBot() {
        return DenizenDiscordBot.instance.connections.get(bot);
    }

    public MessageChannel getChannel() {
        if (channel != null) {
            return channel;
        }
        if (bot == null) {
            return null;
        }
        channel = getBot().client.getTextChannelById(channel_id);
        if (channel == null) {
            channel = getBot().client.getPrivateChannelById(channel_id);
        }
        return channel;
    }

    public Interaction getInteraction() {
        if (interaction != null) {
            return interaction;
        }
        if (!DiscordInteractionTag.interactionCache.containsKey(interaction_id)) {
            return null;
        }
        interaction = DiscordInteractionTag.interactionCache.get(interaction_id);
        return interaction;
    }

    public String bot;

    public MessageChannel channel;

    public Interaction interaction;

    public long channel_id;

    public long interaction_id;

    @Override
    public AbstractFlagTracker getFlagTracker() {
        return new RedirectionFlagTracker(getBot().flags, "__interactions." + channel_id + "." + interaction_id);
    }

    @Override
    public void reapplyTracker(AbstractFlagTracker tracker) {
        // Nothing to do.
    }

    public static void registerTags() {
        
        AbstractFlagTracker.registerFlagHandlers(tagProcessor);

        // <--[tag]
        // @attribute <DiscordInteractionTag.id>
        // @returns ElementTag(Number)
        // @plugin dDiscordBot
        // @description
        // Returns the ID of the interaction.
        // -->
        registerTag("id", (attribute, object) -> {
            return new ElementTag(object.interaction_id);
        });

        // <--[tag]
        // @attribute <DiscordInteractionTag.channel>
        // @returns DiscordChannelTag
        // @plugin dDiscordBot
        // @description
        // Returns the channel that the interaction was created in.
        // -->
        registerTag("channel", (attribute, object) -> {
            return new DiscordChannelTag(object.bot, object.channel_id);
        });

        // <--[tag]
        // @attribute <DiscordInteractionTag.is_direct>
        // @returns ElementTag(Boolean)
        // @plugin dDiscordBot
        // @description
        // Returns true if the interaction was sent in a direct (private) channel, or false if in a public channel.
        // -->
        registerTag("is_direct", (attribute, object) -> {
            return new ElementTag(object.getChannel() instanceof PrivateChannel);
        });


        // <--[tag]
        // @attribute <DiscordInteractionTag.user>
        // @returns DiscordUserTag
        // @plugin dDiscordBot
        // @description
        // Returns the user of the interaction.
        // -->
        registerTag("user", (attribute, object) -> {
            return new DiscordUserTag(object.bot, object.getInteraction().getUser());
        });
    }

    public static ObjectTagProcessor<DiscordInteractionTag> tagProcessor = new ObjectTagProcessor<>();

    public static void registerTag(String name, TagRunnable.ObjectInterface<DiscordInteractionTag> runnable, String... variants) {
        tagProcessor.registerTag(name, runnable, variants);
    }

    @Override
    public ObjectTag getObjectAttribute(Attribute attribute) {
        return tagProcessor.getObjectAttribute(this, attribute);
    }

    String prefix = "discordinteraction";

    @Override
    public String getPrefix() {
        return prefix;
    }

    @Override
    public boolean isUnique() {
        return true;
    }

    @Override
    public String getObjectType() {
        return "DiscordInteraction";
    }

    @Override
    public String identify() {
        if (bot != null) {
            return "discordinteraction@" + bot + "," + channel_id + "," + interaction_id;
        }
        if (channel_id != 0) {
            return "discordinteraction@" + channel_id + "," + interaction_id;
        }
        return "discordinteraction@" + interaction_id;
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
    public ObjectTag setPrefix(String prefix) {
        if (prefix != null) {
            this.prefix = prefix;
        }
        return this;
    }
}
