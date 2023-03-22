package com.denizenscript.ddiscordbot.properties;

import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.TimeTag;

public class DiscordTimeTagExtensions {

    public static void register() {

        // <--[tag]
        // @attribute <TimeTag.format_discord[(<style>)]>
        // @returns ElementTag
        // @group properties
        // @Plugin dDiscordBot
        // @description
        // Returns the time formatted for display on Discord, optionally using the specified style from <@link url https://discord.com/developers/docs/reference#message-formatting-timestamp-styles>.
        // For example: <util.time_now.format_discord> or <util.time_now.format_discord[R]>
        // Note that style input, if used, is case sensitive.
        // -->
        TimeTag.tagProcessor.registerStaticTag(ElementTag.class, "format_discord", (attribute, time) -> {
            long stamp = time.millis() / 1000;
            if (attribute.hasParam()) {
                return new ElementTag("<t:" + stamp + ":" + attribute.getParam() + ">");
            }
            return new ElementTag("<t:" + stamp + ">");
        });
    }
}
