package com.denizenscript.ddiscordbot.objects;

import com.denizenscript.denizencore.objects.Fetchable;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.MapTag;
import com.denizenscript.denizencore.tags.Attribute;
import com.denizenscript.denizencore.tags.ObjectTagProcessor;
import com.denizenscript.denizencore.tags.TagContext;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import com.denizenscript.denizencore.utilities.text.StringHolder;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;

public class DiscordTextInputTag implements ObjectTag {

    // <--[ObjectType]
    // @name DiscordTextInputTag
    // @prefix discordtextinput
    // @base ElementTag
    // @format
    // The identity format for Discord text input is a map of relevant data. Do not alter raw text input data, use the with.as tag instead.
    //
    // @plugin dDiscordBot
    // @description
    // A DiscordTextInputTag is an object that represents a Discord text input for use with dDiscordBot.
    //
    // -->

    @Fetchable("discordtextinput")
    public static DiscordTextInputTag valueOf(String string, TagContext context) {
        if (string.startsWith("discordtextinput@")) {
            string = string.substring("discordtextinput@".length());
        }
        MapTag map = MapTag.valueOf(string, context);
        if (map == null) {
            return null;
        }
        return new DiscordTextInputTag(map);
    }

    @Override
    public DiscordTextInputTag duplicate() {
        return new DiscordTextInputTag(textInputData.duplicate());
    }

    public static boolean matches(String arg) {
        if (arg.startsWith("discordtextinput@")) {
            return true;
        }
        return MapTag.matches(arg);
    }

    public DiscordTextInputTag() {
        textInputData = new MapTag();
    }

    public DiscordTextInputTag(MapTag map) {
        textInputData = map;
    }

    public DiscordTextInputTag(TextInput textInput) {
        textInputData = new MapTag();
        textInputData.putObject("style", new ElementTag(textInput.getStyle().name()));
        textInputData.putObject("label", new ElementTag(textInput.getLabel()));
        if (textInput.getId() != null) {
            textInputData.putObject("id", new ElementTag(textInput.getId()));
        }
        if(textInput.getMinLength() != -1) {
            textInputData.putObject("min_length", new ElementTag(textInput.getMinLength()));
        }
        if(textInput.getMaxLength() != -1) {
            textInputData.putObject("max_length", new ElementTag(textInput.getMaxLength()));
        }
        textInputData.putObject("is_required", new ElementTag(textInput.isRequired()));
        if(textInput.getValue() != null) {
            textInputData.putObject("value", new ElementTag(textInput.getValue()));
        }
        if(textInput.getPlaceHolder() != null) {
            textInputData.putObject("placeholder", new ElementTag(textInput.getPlaceHolder()));
        }
    }

    public TextInput build() {
        ObjectTag id = textInputData.getObject("id");
        if (id == null) {
            return null;
        }
        ObjectTag label = textInputData.getObject("label");
        ObjectTag style = textInputData.getObject("style");
        TextInputStyle textInputStyle = TextInputStyle.SHORT;
        if(style != null) {
            textInputStyle = TextInputStyle.valueOf(style.toString().toUpperCase());
        }

        TextInput.Builder textInput = TextInput.create(id.toString(), label.toString(), textInputStyle);

        ObjectTag minLength = textInputData.getObject("min_length");
        if(minLength != null) {
            textInput.setMinLength(Integer.parseInt(minLength.toString()));
        }
        ObjectTag maxLength = textInputData.getObject("max_length");
        if(maxLength != null) {
            textInput.setMaxLength(Integer.parseInt(maxLength.toString()));
        }
        ObjectTag isRequired = textInputData.getObject("is_required");
        if(isRequired != null) {
            textInput.setRequired(Boolean.parseBoolean(isRequired.toString()));
        }
        ObjectTag value = textInputData.getObject("value");
        textInput.setValue(value == null ? null : value.toString());
        ObjectTag placeholder = textInputData.getObject("placeholder");
        textInput.setPlaceholder(placeholder == null ? null : placeholder.toString());

        return textInput.build();
    }

    public MapTag textInputData;

    public static HashSet<String> acceptedWithKeys = new HashSet<>(Arrays.asList(
        "style", "id", "label", "min_length", "max_length", "is_required", "value", "placeholder"
    ));

    public static void registerTags() {

        // <--[tag]
        // @attribute <DiscordTextInputTag.with_map[<map>]>
        // @returns DiscordTextInputTag
        // @plugin dDiscordBot
        // @description
        // Returns a copy of this TextInput tag, with the map of keys to values applied.
        // Refer to <@link tag DiscordTextInputTag.with.as>.
        // -->
        tagProcessor.registerTag(DiscordTextInputTag.class, "with_map", (attribute, object) -> {
            DiscordTextInputTag textInput = object.duplicate();
            if (!attribute.hasParam()) {
                attribute.echoError("Invalid text_input.with_map[...] tag: must have an input value.");
                return null;
            }
            MapTag map = MapTag.getMapFor(attribute.getParamObject(), attribute.context);
            for (Map.Entry<StringHolder, ObjectTag> entry : map.map.entrySet()) {
                String key = entry.getKey().low;
                if (!acceptedWithKeys.contains(key)) {
                    attribute.echoError("Invalid text_input.with_map[...] tag: unknown key '" + key + "' given.");
                    return null;
                }
                ObjectTag val = entry.getValue();
                if (val == null) {
                    attribute.echoError("button.with_map[...] value is invalid.");
                    return null;
                }
                textInput.textInputData.putObject(key, val);
            }
            return textInput;
        });

        // <--[tag]
        // @attribute <DiscordTextInputTag.with[<key>].as[<value>]>
        // @returns DiscordTextInputTag
        // @plugin dDiscordBot
        // @description
        // Returns a copy of this TextInput tag, with the specified data key set to the specified value.
        // The following keys are accepted, with values of the listed type:
        // style: short or paragraph
        // id: ElementTag
        // label: ElementTag
        // min_length: ElementTag(number)
        // max_length: ElementTag(number)
        // is_required: ElementTag(boolean)
        // value: ElementTag
        // placeholder: ElementTag
        // -->
        tagProcessor.registerTag(DiscordTextInputTag.class, "with", (attribute, object) -> {
            DiscordTextInputTag button = object.duplicate();
            if (!attribute.hasParam()) {
                attribute.echoError("Invalid text input.with[...] tag: must have an input value.");
                return null;
            }
            String key = CoreUtilities.toLowerCase(attribute.getParam());
            if (!acceptedWithKeys.contains(key)) {
                attribute.echoError("Invalid text input.with[...] tag: unknown key '" + key + "' given.");
                return null;
            }
            attribute.fulfill(1);
            if (!attribute.startsWith("as") || !attribute.hasParam()) {
                attribute.echoError("text input.with[...] must be followed by as[...].");
            }
            ObjectTag val = attribute.getParamObject();
            if (val == null) {
                attribute.echoError("text input.with[...].as[...] value is invalid.");
                return null;
            }
            button.textInputData.putObject(key, val);
            return button;
        });

        // <--[tag]
        // @attribute <DiscordTextInputTag.map>
        // @returns MapTag
        // @plugin dDiscordBot
        // @description
        // Returns the MapTag internally backing this text input tag.
        // -->
        tagProcessor.registerTag(MapTag.class, "map", (attribute, object) -> {
            return object.textInputData.duplicate();
        });
    }

    public static ObjectTagProcessor<DiscordTextInputTag> tagProcessor = new ObjectTagProcessor<>();

    @Override
    public ObjectTag getObjectAttribute(Attribute attribute) {
        return tagProcessor.getObjectAttribute(this, attribute);
    }

    String prefix = "discordtextinput";

    @Override
    public String getPrefix() {
        return prefix;
    }

    @Override
    public String debuggable() {
        return "<LG>discordtextinput@<Y>" + textInputData.debuggable();
    }

    @Override
    public boolean isUnique() {
        return false;
    }

    @Override
    public String getObjectType() {
        return "DiscordTextInput";
    }

    @Override
    public String identify() {
        return "discordtextinput@" + textInputData.identify();
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
