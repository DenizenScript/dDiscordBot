package com.denizenscript.ddiscordbot.properties;

import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.TimeTag;
import net.dv8tion.jda.api.utils.TimeUtil;

public class DiscordElementTagExtensions {

    public static void register() {

        // <--[tag]
        // @attribute <ElementTag.discord_id_to_time>
        // @returns TimeTag
        // @group extensions
        // @Plugin dDiscordBot
        // @description
        // Returns the TimeTag converted from the given discord ID.
        // Discord IDs internally are just timestamps in a Discord-specific numeric format.
        // @example
        // # Find out when a message was sent.
        // - narrate "You wrote <[message].text> at <[message].id.discord_id_to_time.format>"
        // -->
        ElementTag.tagProcessor.registerStaticTag(TimeTag.class, "discord_id_to_time", (attribute, id) -> {
            if (!id.isInt()) {
                attribute.echoError("Element '" + id + "' is not a valid id!");
                return null;
            }
            return new TimeTag(TimeUtil.getTimeCreated(id.asLong()).toZonedDateTime());
        });
    }

}
