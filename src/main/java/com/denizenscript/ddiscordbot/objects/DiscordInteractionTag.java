package com.denizenscript.ddiscordbot.objects;

import com.denizenscript.ddiscordbot.DenizenDiscordBot;
import com.denizenscript.ddiscordbot.DiscordConnection;
import com.denizenscript.denizencore.DenizenCore;
import com.denizenscript.denizencore.flags.AbstractFlagTracker;
import com.denizenscript.denizencore.flags.FlaggableObject;
import com.denizenscript.denizencore.flags.SavableMapFlagTracker;
import com.denizenscript.denizencore.objects.ArgumentHelper;
import com.denizenscript.denizencore.objects.Fetchable;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.tags.Attribute;
import com.denizenscript.denizencore.tags.ObjectTagProcessor;
import com.denizenscript.denizencore.tags.TagContext;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import com.denizenscript.denizencore.utilities.scheduling.OneTimeSchedulable;
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel;
import net.dv8tion.jda.api.interactions.Interaction;
import net.dv8tion.jda.api.interactions.commands.context.MessageContextInteraction;
import net.dv8tion.jda.api.interactions.commands.context.UserContextInteraction;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DiscordInteractionTag implements ObjectTag, FlaggableObject {

    // <--[ObjectType]
    // @name DiscordInteractionTag
    // @prefix discordinteraction
    // @base ElementTag
    // @implements FlaggableObject
    // @format
    // The identity format for Discord interactions is the bot ID, followed by the interaction ID.
    // For example: mybot,5678
    //
    // @plugin dDiscordBot
    // @description
    // A DiscordInteractionTag is an object that represents an interaction on Discord, either as a generic reference, or as a bot-specific reference.
    // Interactions are temporary - they only exist for 15 minutes.
    //
    // This object type is flaggable.
    // Flags on this object type will be stored in temporary memory.
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
        if (commaSplit.size() != 2) {
            if (context == null || context.showErrors()) {
                Debug.echoError("DiscordInteractionTag input is not valid.");
            }
            return null;
        }
        String intIdText = commaSplit.get(1);
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
        String bot = commaSplit.get(0);
        DiscordInteractionTag result = interactionCache.get(bot + "_" + intId);
        if (result != null) {
            return result;
        }
        return null;
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

    public static Map<String, DiscordInteractionTag> interactionCache = new HashMap<String, DiscordInteractionTag>() {
        @Override
        public DiscordInteractionTag put(String key, DiscordInteractionTag value) {
            DenizenCore.schedule(new OneTimeSchedulable(() -> {
                value.interaction = null;
                remove(key);
            }, 15 * 60));
            return super.put(key, value);
        }
    };

    public static DiscordInteractionTag getOrCreate(String bot, Interaction interaction) {
        DiscordInteractionTag result = interactionCache.get(bot + "_" + interaction.getIdLong());
        if (result != null) {
            return result;
        }
        return new DiscordInteractionTag(bot, interaction);
    }

    public String cacheId() {
        return bot + "_" + interaction_id;
    }

    public DiscordInteractionTag(String bot, Interaction interaction) {
        this.bot = bot;
        this.interaction = interaction;
        this.interaction_id = interaction.getIdLong();
        DiscordInteractionTag.interactionCache.put(cacheId(), this);
    }

    public DiscordConnection getBot() {
        return DenizenDiscordBot.instance.connections.get(bot);
    }

    public String bot;

    public Interaction interaction;

    public long interaction_id;

    public AbstractFlagTracker tracker;

    @Override
    public AbstractFlagTracker getFlagTracker() {
        if (tracker == null) {
            tracker = new SavableMapFlagTracker();
        }
        return tracker;
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
        tagProcessor.registerTag(ElementTag.class, "id", (attribute, object) -> {
            return new ElementTag(object.interaction_id);
        });

        // <--[tag]
        // @attribute <DiscordInteractionTag.channel>
        // @returns DiscordChannelTag
        // @plugin dDiscordBot
        // @description
        // Returns the channel that the interaction was created in.
        // -->
        tagProcessor.registerTag(DiscordChannelTag.class, "channel", (attribute, object) -> {
            return new DiscordChannelTag(object.bot, object.interaction.getMessageChannel());
        });

        // <--[tag]
        // @attribute <DiscordInteractionTag.is_direct>
        // @returns ElementTag(Boolean)
        // @plugin dDiscordBot
        // @description
        // Returns true if the interaction was sent in a direct (private) channel, or false if in a public channel.
        // -->
        tagProcessor.registerTag(ElementTag.class, "is_direct", (attribute, object) -> {
            return new ElementTag(object.interaction.getMessageChannel() instanceof PrivateChannel);
        });


        // <--[tag]
        // @attribute <DiscordInteractionTag.user>
        // @returns DiscordUserTag
        // @plugin dDiscordBot
        // @description
        // Returns the user of the interaction.
        // -->
        tagProcessor.registerTag(DiscordUserTag.class, "user", (attribute, object) -> {
            return new DiscordUserTag(object.bot, object.interaction.getUser());
        });

        // <--[tag]
        // @attribute <DiscordInteractionTag.target_user>
        // @returns DiscordUserTag
        // @plugin dDiscordBot
        // @description
        // Returns the user being targeted by a USER application interaction.
        // -->
        tagProcessor.registerTag(DiscordUserTag.class, "target_user", (attribute, object) -> {
            if (object.interaction instanceof UserContextInteraction) {
                return new DiscordUserTag(object.bot, ((UserContextInteraction) object.interaction).getTarget());
            }
            return null;
        });

        // <--[tag]
        // @attribute <DiscordInteractionTag.target_message>
        // @returns DiscordMessageTag
        // @plugin dDiscordBot
        // @description
        // Returns the message being targeted by a MESSAGE application interaction.
        // -->
        tagProcessor.registerTag(DiscordMessageTag.class, "target_message", (attribute, object) -> {
            if (object.interaction instanceof MessageContextInteraction) {
                return new DiscordMessageTag(object.bot, ((MessageContextInteraction) object.interaction).getTarget());
            }
            return null;
        });
    }

    public static ObjectTagProcessor<DiscordInteractionTag> tagProcessor = new ObjectTagProcessor<>();

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
    public String identify() {
        return "discordinteraction@" + bot + "," + interaction_id;
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
