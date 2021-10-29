package com.denizenscript.ddiscordbot.properties;

import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.TimeTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.objects.properties.PropertyParser;

public class DiscordTimeTagProperties implements Property {

    public static boolean describes(ObjectTag time) {
        return time instanceof TimeTag;
    }

    public static DiscordTimeTagProperties getFrom(ObjectTag entity) {
        if (!describes(entity)) {
            return null;
        }
        return new DiscordTimeTagProperties((TimeTag) entity);
    }

    public static final String[] handledMechs = new String[] {
    }; // None

    private DiscordTimeTagProperties(TimeTag time) {
        this.time = time;
    }

    TimeTag time;

    @Override
    public String getPropertyString() {
        return null;
    }

    @Override
    public String getPropertyId() {
        return "DiscordTimeTagProperties";
    }

    public static void registerTags() {

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
        PropertyParser.<DiscordTimeTagProperties, ElementTag>registerTag(ElementTag.class, "format_discord", (attribute, object) -> {
            long stamp = object.time.millis() / 1000;
            if (attribute.hasParam()) {
                return new ElementTag("<t:" + stamp + ":" + attribute.getParam() + ">");
            }
            return new ElementTag("<t:" + stamp + ">");
        });
    }

    @Override
    public void adjust(Mechanism mechanism) {
    }
}
