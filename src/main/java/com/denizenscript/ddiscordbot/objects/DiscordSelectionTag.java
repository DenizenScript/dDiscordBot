package com.denizenscript.ddiscordbot.objects;

import com.denizenscript.denizencore.objects.Fetchable;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.objects.core.MapTag;
import com.denizenscript.denizencore.tags.Attribute;
import com.denizenscript.denizencore.tags.ObjectTagProcessor;
import com.denizenscript.denizencore.tags.TagContext;
import com.denizenscript.denizencore.tags.TagRunnable;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import com.denizenscript.denizencore.utilities.text.StringHolder;
import net.dv8tion.jda.api.entities.Emoji;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.api.interactions.components.selections.SelectionMenu;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;

public class DiscordSelectionTag implements ObjectTag {

    // <--[ObjectType]
    // @name DiscordSelectionTag
    // @prefix discordselection
    // @base ElementTag
    // @format
    // The identity format for Discord selection menu is a map of menu data. Do not alter raw menu data, use the with.as tag instead.
    //
    // @plugin dDiscordBot
    // @description
    // A DiscordSelectionTag is an object that represents a Discord selection menu for use with dDiscordBot.
    //
    // -->

    @Fetchable("discordselection")
    public static DiscordSelectionTag valueOf(String string, TagContext context) {
        if (string.startsWith("discordselection@")) {
            string = string.substring("discordselection@".length());
        }
        MapTag map = MapTag.valueOf(string, context);
        if (map == null) {
            return null;
        }
        return new DiscordSelectionTag(map);
    }

    @Override
    public DiscordSelectionTag duplicate() {
        return new DiscordSelectionTag(menuData.duplicate());
    }

    public static boolean matches(String arg) {
        if (arg.startsWith("discordselection@")) {
            return true;
        }
        return MapTag.matches(arg);
    }

    public DiscordSelectionTag() {
        menuData = new MapTag();
    }

    public DiscordSelectionTag(MapTag map) {
        menuData = map;
    }

    public static MapTag getSelectionOption(SelectOption selectOption) {
        MapTag options = new MapTag();
        options.putObject("label", new ElementTag(selectOption.getLabel()));
        options.putObject("value", new ElementTag(selectOption.getValue()));
        if (selectOption.getDescription() != null) {
            options.putObject("description", new ElementTag(selectOption.getDescription()));
        }
        if (selectOption.getEmoji() != null) {
            options.putObject("emoji", new ElementTag(selectOption.getEmoji().getName()));
        }
        return options;
    }

    public DiscordSelectionTag(SelectionMenu menu) {
        menuData = new MapTag();
        if (menu.getId() != null) {
            menuData.putObject("id", new ElementTag(menu.getId()));
        }
        ListTag options = new ListTag();
        for (SelectOption option : menu.getOptions()) {
            options.addObject(DiscordSelectionTag.getSelectionOption(option));
        }
        menuData.putObject("options", options);
    }

    public SelectionMenu.Builder build(TagContext context) {
        ObjectTag id = menuData.getObject("id");
        if (id == null) {
            return null;
        }
        SelectionMenu.Builder menu = SelectionMenu.create(id.toString());
        MapTag options = (MapTag) menuData.getObject("options");
        if (options != null) {
            for (ObjectTag optionObj : options.map.values()) {
                MapTag option = optionObj.asType(MapTag.class, context);
                ElementTag label = (ElementTag) option.getObject("label");
                ElementTag value = (ElementTag) option.getObject("value");
                ElementTag description = (ElementTag) option.getObject("description");
                ElementTag emoji = (ElementTag) option.getObject("emoji");
                Emoji emojiData = null;
                if (emoji != null) {
                    emojiData = Emoji.fromUnicode(emoji.toString());
                }
                if (label == null || value == null) {
                    return null;
                }
                if (description == null && emoji == null) {
                    menu.addOption(label.toString(), value.toString());
                }
                else if (emoji == null) {
                    menu.addOption(label.toString(), value.toString(), description.toString());
                }
                else if (description == null) {
                    menu.addOption(label.toString(), value.toString(), emojiData);
                }
                else {
                    menu.addOption(label.toString(), value.toString(), description.toString(), emojiData);
                }
            }
        }
        return menu;
    }

    public MapTag menuData;

    public static HashSet<String> acceptedWithKeys = new HashSet<>(Arrays.asList(
        "id", "options"
    ));

    public static void registerTags() {

        // <--[tag]
        // @attribute <DiscordSelectionTag.with_map[<map>]>
        // @returns DiscordSelectionTag
        // @plugin dDiscordBot
        // @description
        // Returns a copy of this Selection tag, with the map of keys to values applied.
        // Refer to <@link tag DiscordSelectionTag.with.as>.
        // -->
        registerTag("with_map", (attribute, object) -> {
            DiscordSelectionTag menu = object.duplicate();
            if (!attribute.hasContext(1)) {
                attribute.echoError("Invalid selection.with_map[...] tag: must have an input value.");
                return null;
            }
            MapTag map = MapTag.getMapFor(attribute.getContextObject(1), attribute.context);
            for (Map.Entry<StringHolder, ObjectTag> entry : map.map.entrySet()) {
                String key = entry.getKey().low;
                if (!acceptedWithKeys.contains(key)) {
                    attribute.echoError("Invalid selection.with_map[...] tag: unknown key '" + key + "' given.");
                    return null;
                }
                ObjectTag val = entry.getValue();
                if (val == null) {
                    attribute.echoError("selection.with_map[...] value is invalid.");
                    return null;
                }
                menu.menuData.putObject(key, val);
            }
            return menu;
        });

        // <--[tag]
        // @attribute <DiscordSelectionTag.with[<key>].as[<value>]>
        // @returns DiscordSelectionTag
        // @plugin dDiscordBot
        // @description
        // Returns a copy of this Selection tag, with the specified data key set to the specified value.
        // The following keys are accepted, with values of the listed type:
        // id: ElementTag
        // options: MapTag where the values are also a MapTag consisting of:
        // - label: ElementTag
        // - value: ElementTag
        // - description: ElementTag
        // - emoji: ElementTag
        // -->
        registerTag("with", (attribute, object) -> {
            DiscordSelectionTag menu = object.duplicate();
            if (!attribute.hasContext(1)) {
                attribute.echoError("Invalid selection.with[...] tag: must have an input value.");
                return null;
            }
            String key = CoreUtilities.toLowerCase(attribute.getContext(1));
            if (!acceptedWithKeys.contains(key)) {
                attribute.echoError("Invalid selection.with[...] tag: unknown key '" + key + "' given.");
                return null;
            }
            attribute.fulfill(1);
            if (!attribute.startsWith("as") || !attribute.hasContext(1)) {
                attribute.echoError("selection.with[...] must be followed by as[...].");
            }
            ObjectTag val = attribute.getContextObject(1);
            if (val == null) {
                attribute.echoError("selection.with[...].as[...] value is invalid.");
                return null;
            }
            menu.menuData.putObject(key, val);
            return menu;
        });

        // <--[tag]
        // @attribute <DiscordSelectionTag.map>
        // @returns MapTag
        // @plugin dDiscordBot
        // @description
        // Returns the MapTag internally backing this selection tag.
        // -->
        registerTag("map", (attribute, object) -> {
            return object.menuData.duplicate();
        });
    }

    public static ObjectTagProcessor<DiscordSelectionTag> tagProcessor = new ObjectTagProcessor<>();

    public static void registerTag(String name, TagRunnable.ObjectInterface<DiscordSelectionTag> runnable, String... variants) {
        tagProcessor.registerTag(name, runnable, variants);
    }

    @Override
    public ObjectTag getObjectAttribute(Attribute attribute) {
        return tagProcessor.getObjectAttribute(this, attribute);
    }

    String prefix = "discordselection";

    @Override
    public String getPrefix() {
        return prefix;
    }

    @Override
    public String debuggable() {
        return "discordselection@" + menuData.debuggable();
    }

    @Override
    public boolean isUnique() {
        return false;
    }

    @Override
    public String getObjectType() {
        return "DiscordSelection";
    }

    @Override
    public String identify() {
        return "discordselection@" + menuData.identify();
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
