package com.denizenscript.ddiscordbot.objects;

import com.denizenscript.denizencore.objects.Fetchable;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.MapTag;
import com.denizenscript.denizencore.tags.Attribute;
import com.denizenscript.denizencore.tags.ObjectTagProcessor;
import com.denizenscript.denizencore.tags.TagContext;
import com.denizenscript.denizencore.tags.TagRunnable;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import com.denizenscript.denizencore.utilities.text.StringHolder;
import net.dv8tion.jda.api.entities.Emoji;
import net.dv8tion.jda.api.interactions.components.Button;
import net.dv8tion.jda.api.interactions.components.ButtonStyle;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;

public class DiscordButtonTag implements ObjectTag {

    // <--[ObjectType]
    // @name DiscordButtonTag
    // @prefix discordbutton
    // @base ElementTag
    // @format
    // The identity format for Discord button is a map of button data. Do not alter raw button data, use the with.as tag instead.
    //
    // @plugin dDiscordBot
    // @description
    // A DiscordButtonTag is an object that represents a Discord button for use with dDiscordBot.
    //
    // -->

    @Fetchable("discordbutton")
    public static DiscordButtonTag valueOf(String string, TagContext context) {
        if (string.startsWith("discordbutton@")) {
            string = string.substring("discordbutton@".length());
        }
        MapTag map = MapTag.valueOf(string, context);
        if (map == null) {
            return null;
        }
        return new DiscordButtonTag(map);
    }

    @Override
    public DiscordButtonTag duplicate() {
        return new DiscordButtonTag(buttonData.duplicate());
    }

    public static boolean matches(String arg) {
        if (arg.startsWith("discordembed@")) {
            return true;
        }
        return MapTag.matches(arg);
    }

    public DiscordButtonTag() {
        buttonData = new MapTag();
    }

    public DiscordButtonTag(MapTag map) {
        buttonData = map;
    }

    public DiscordButtonTag(Button button) {
        buttonData = new MapTag();
        buttonData.putObject("style", new ElementTag(button.getStyle().name()));
        buttonData.putObject("label", new ElementTag(button.getLabel()));
        if (button.getId() != null) {
            buttonData.putObject("id", new ElementTag(button.getId()));
        }
        if (button.getEmoji() != null) {
            buttonData.putObject("emoji", new ElementTag(button.getEmoji().getName()));
        }
    }

    public Button build() {
        ObjectTag style = buttonData.getObject("style");
        ObjectTag id = buttonData.getObject("id");
        ObjectTag label = buttonData.getObject("label");
        ObjectTag emoji = buttonData.getObject("emoji");
        ButtonStyle styleData;
        Button button = null;
        boolean isValidType = false;
        for (ButtonStyle val : ButtonStyle.values()) {
            if (val.name().toUpperCase().equals(
                style.toString().toUpperCase()
            )) {
                isValidType = true;
                break;
            }
        }
        if (!isValidType) {
            return null;
        }
        styleData = ButtonStyle.valueOf(style.toString().toUpperCase());
        if (style != null) {
            styleData = ButtonStyle.valueOf(style.toString().toUpperCase());
        }
        if (id != null) {
            if (emoji != null) {
                Emoji emojiData = Emoji.fromUnicode(emoji.toString());
                if (emojiData != null) {
                    button = Button.of(styleData, id.toString(), emojiData);
                }
            } else {
                if (label != null) {
                    button = Button.of(styleData, id.toString(), label.toString());
                }
            }
        }
        return button;
    }

    public MapTag buttonData;

    public static HashSet<String> acceptedWithKeys = new HashSet<>(Arrays.asList(
        "style", "id", "label", "emoji"
    ));

    public static void registerTags() {

        // <--[tag]
        // @attribute <DiscordButtonTag.with_map[<map>]>
        // @returns DiscordButtonTag
        // @plugin dDiscordBot
        // @description
        // Returns a copy of this Button tag, with the map of keys to values applied.
        // Refer to <@link tag DiscordButtonTag.with.as>.
        // -->
        registerTag("with_map", (attribute, object) -> {
            DiscordButtonTag button = object.duplicate();
            if (!attribute.hasContext(1)) {
                attribute.echoError("Invalid button.with_map[...] tag: must have an input value.");
                return null;
            }
            MapTag map = MapTag.getMapFor(attribute.getContextObject(1), attribute.context);
            for (Map.Entry<StringHolder, ObjectTag> entry : map.map.entrySet()) {
                String key = entry.getKey().low;
                if (!acceptedWithKeys.contains(key)) {
                    attribute.echoError("Invalid button.with_map[...] tag: unknown key '" + key + "' given.");
                    return null;
                }
                ObjectTag val = entry.getValue();
                if (val == null) {
                    attribute.echoError("button.with_map[...] value is invalid.");
                    return null;
                }
                button.buttonData.putObject(key, val);
            }
            return button;
        });

        // <--[tag]
        // @attribute <DiscordButtonTag.with[<key>].as[<value>]>
        // @returns DiscordButtonTag
        // @plugin dDiscordBot
        // @description
        // Returns a copy of this Button tag, with the specified data key set to the specified value.
        // The following keys are accepted, with values of the listed type:
        // style: ElementTag of either primary, secondary, success, danger, or link
        // id: ElementTag, can be a URL
        // label: ElementTag
        // emoji: ElementTag
        // -->
        registerTag("with", (attribute, object) -> {
            DiscordButtonTag button = object.duplicate();
            if (!attribute.hasContext(1)) {
                attribute.echoError("Invalid button.with[...] tag: must have an input value.");
                return null;
            }
            String key = CoreUtilities.toLowerCase(attribute.getContext(1));
            if (!acceptedWithKeys.contains(key)) {
                attribute.echoError("Invalid button.with[...] tag: unknown key '" + key + "' given.");
                return null;
            }
            attribute.fulfill(1);
            if (!attribute.startsWith("as") || !attribute.hasContext(1)) {
                attribute.echoError("button.with[...] must be followed by as[...].");
            }
            ObjectTag val = attribute.getContextObject(1);
            if (val == null) {
                attribute.echoError("button.with[...].as[...] value is invalid.");
                return null;
            }
            button.buttonData.putObject(key, val);
            return button;
        });

        // <--[tag]
        // @attribute <DiscordButtonTag.map>
        // @returns MapTag
        // @plugin dDiscordBot
        // @description
        // Returns the MapTag internally backing this embed tag.
        // -->
        registerTag("map", (attribute, object) -> {
            return object.buttonData.duplicate();
        });
    }

    public static ObjectTagProcessor<DiscordButtonTag> tagProcessor = new ObjectTagProcessor<>();

    public static void registerTag(String name, TagRunnable.ObjectInterface<DiscordButtonTag> runnable, String... variants) {
        tagProcessor.registerTag(name, runnable, variants);
    }

    @Override
    public ObjectTag getObjectAttribute(Attribute attribute) {
        return tagProcessor.getObjectAttribute(this, attribute);
    }

    String prefix = "discordbutton";

    @Override
    public String getPrefix() {
        return prefix;
    }

    @Override
    public String debuggable() {
        return "discordbutton@" + buttonData.debuggable();
    }

    @Override
    public boolean isUnique() {
        return false;
    }

    @Override
    public String getObjectType() {
        return "DiscordButton";
    }

    @Override
    public String identify() {
        return "discordbutton@" + buttonData.identify();
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
