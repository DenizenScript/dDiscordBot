package com.denizenscript.ddiscordbot.objects;

import com.denizenscript.denizen.objects.ColorTag;
import com.denizenscript.denizencore.objects.Fetchable;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.objects.core.MapTag;
import com.denizenscript.denizencore.objects.core.TimeTag;
import com.denizenscript.denizencore.tags.Attribute;
import com.denizenscript.denizencore.tags.ObjectTagProcessor;
import com.denizenscript.denizencore.tags.TagContext;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import com.denizenscript.denizencore.utilities.text.StringHolder;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.bukkit.Color;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;

public class DiscordEmbedTag implements ObjectTag {

    // <--[ObjectType]
    // @name DiscordEmbedTag
    // @prefix discordembed
    // @base ElementTag
    // @format
    // The identity format for Discord embeds is a map of embed data.
    // The map matches the key set documented at <@link tag DiscordEmbedTag.with.as>, along with a "fields" key as a ListTag of MapTags with keys "title", "value", and "inline".
    //
    // @plugin dDiscordBot
    // @description
    // A DiscordEmbedTag is an object that represents a Discord embed for use with dDiscordBot.
    //
    // -->

    @Fetchable("discordembed")
    public static DiscordEmbedTag valueOf(String string, TagContext context) {
        if (string.startsWith("discordembed@")) {
            string = string.substring("discordembed@".length());
        }
        MapTag map = MapTag.valueOf(string, context);
        if (map == null) {
            return null;
        }
        return new DiscordEmbedTag(map);
    }

    @Override
    public DiscordEmbedTag duplicate() {
        return new DiscordEmbedTag(embedData.duplicate());
    }

    public static boolean matches(String arg) {
        if (arg.startsWith("discordembed@")) {
            return true;
        }
        return MapTag.matches(arg);
    }

    public DiscordEmbedTag() {
        embedData = new MapTag();
    }

    public DiscordEmbedTag(MapTag map) {
        embedData = map;
    }

    public DiscordEmbedTag(MessageEmbed embed) {
        embedData = new MapTag();
        if (embed.getAuthor() != null && embed.getAuthor().getName() != null) {
            embedData.putObject("author_name", new ElementTag(embed.getAuthor().getName()));
        }
        if (embed.getAuthor() != null && embed.getAuthor().getUrl() != null) {
            embedData.putObject("author_url", new ElementTag(embed.getAuthor().getUrl()));
        }
        if (embed.getAuthor() != null && embed.getAuthor().getIconUrl() != null) {
            embedData.putObject("author_icon_url", new ElementTag(embed.getAuthor().getIconUrl()));
        }
        if (embed.getColor() != null) {
            embedData.putObject("color", new ColorTag(Color.fromRGB(embed.getColorRaw())));
        }
        if (embed.getDescription() != null) {
            embedData.putObject("description", new ElementTag(embed.getDescription()));
        }
        if (embed.getFooter() != null && embed.getFooter().getText() != null) {
            embedData.putObject("footer", new ElementTag(embed.getFooter().getText()));
        }
        if (embed.getFooter() != null && embed.getFooter().getIconUrl() != null) {
            embedData.putObject("footer_icon", new ElementTag(embed.getFooter().getIconUrl()));
        }
        if (embed.getImage() != null && embed.getImage().getUrl() != null) {
            embedData.putObject("image", new ElementTag(embed.getImage().getUrl()));
        }
        if (embed.getThumbnail() != null && embed.getThumbnail().getUrl() != null) {
            embedData.putObject("thumbnail", new ElementTag(embed.getThumbnail().getUrl()));
        }
        if (embed.getTimestamp() != null) {
            embedData.putObject("timestamp", new TimeTag(embed.getTimestamp().toZonedDateTime()));
        }
        if (embed.getTitle() != null) {
            embedData.putObject("title", new ElementTag(embed.getTitle()));
        }
        if (embed.getUrl() != null) {
            embedData.putObject("title_url", new ElementTag(embed.getUrl()));
        }
        if (!embed.getFields().isEmpty()) {
            ListTag fields = new ListTag();
            for (MessageEmbed.Field field : embed.getFields()) {
                MapTag fieldMap = new MapTag();
                if (field.getName() != null) {
                    fieldMap.putObject("title", new ElementTag(field.getName()));
                }
                if (field.getValue() != null) {
                    fieldMap.putObject("value", new ElementTag(field.getValue()));
                }
                fieldMap.putObject("inline", new ElementTag(field.isInline()));
                fields.addObject(fieldMap);
            }
            embedData.putObject("fields", fields);
        }
    }

    public EmbedBuilder build(TagContext context) {
        EmbedBuilder builder = new EmbedBuilder();
        ElementTag author_name = embedData.getElement("author_name");
        ElementTag author_url = embedData.getElement("author_url");
        ElementTag author_icon_url = embedData.getElement("author_icon_url");
        ColorTag color = embedData.getObjectAs("color", ColorTag.class, context);
        ElementTag description = embedData.getElement("description");
        ElementTag footer = embedData.getElement("footer");
        ElementTag footer_icon = embedData.getElement("footer_icon");
        ElementTag image = embedData.getElement("image");
        ElementTag thumbnail = embedData.getElement("thumbnail");
        TimeTag timestamp = embedData.getObjectAs("timestamp", TimeTag.class, context);
        ElementTag title = embedData.getElement("title");
        ElementTag title_url = embedData.getElement("title_url");
        ObjectTag fields = embedData.getObject("fields");
        if (author_name != null) {
            if (author_url != null) {
                if (author_icon_url != null) {
                    builder.setAuthor(author_name.toString(), author_url.toString(), author_icon_url.toString());
                }
                else {
                    builder.setAuthor(author_name.toString(), author_url.toString());
                }
            }
            else {
                if (author_icon_url != null) {
                    builder.setAuthor(author_name.toString(), null, author_icon_url.toString());
                }
                else {
                    builder.setAuthor(author_name.toString());
                }
            }
        }
        if (color != null) {
            builder.setColor(color.getColor().asRGB());
        }
        if (description != null) {
            builder.setDescription(description.toString());
        }
        if (footer != null) {
            if (footer_icon != null) {
                builder.setFooter(footer.toString(), footer_icon.toString());
            }
            else {
                builder.setFooter(footer.toString());
            }
        }
        if (image != null) {
            builder.setImage(image.toString());
        }
        if (thumbnail != null) {
            builder.setThumbnail(thumbnail.toString());
        }
        if (timestamp != null) {
            builder.setTimestamp(timestamp.instant);
        }
        if (title != null) {
            if (title_url != null) {
                builder.setTitle(title.toString(), title_url.toString());
            }
            else {
                builder.setTitle(title.toString());
            }
        }
        if (fields != null) {
            Collection<ObjectTag> fieldList = CoreUtilities.objectToList(fields, context);
            for (ObjectTag field : fieldList) {
                MapTag fieldMap = MapTag.getMapFor(field, context);
                if (fieldMap != null) {
                    ElementTag fieldTitle = fieldMap.getElement("title");
                    ElementTag fieldValue = fieldMap.getElement("value");
                    ElementTag fieldInline = fieldMap.getElement("inline");
                    builder.addField(fieldTitle.toString(), fieldValue.toString(), fieldInline != null && fieldInline.asBoolean());
                }
            }
        }
        return builder;
    }

    public MapTag embedData;

    public static HashSet<String> acceptedWithKeys = new HashSet<>(Arrays.asList(
            "author_name", "author_url", "author_icon_url", "color", "description",
            "footer", "footer_icon", "image", "thumbnail", "timestamp", "title", "title_url", "fields"
    ));

    public static void registerTags() {

        // <--[tag]
        // @attribute <DiscordEmbedTag.with_map[<map>]>
        // @returns DiscordEmbedTag
        // @plugin dDiscordBot
        // @description
        // Returns a copy of this Embed tag, with the map of keys to values applied.
        // Refer to <@link tag DiscordEmbedTag.with.as>.
        // -->
        tagProcessor.registerTag(DiscordEmbedTag.class, "with_map", (attribute, object) -> {
            DiscordEmbedTag embed = object.duplicate();
            if (!attribute.hasParam()) {
                attribute.echoError("Invalid embed.with_map[...] tag: must have an input value.");
                return null;
            }
            MapTag map = MapTag.getMapFor(attribute.getParamObject(), attribute.context);
            for (Map.Entry<StringHolder, ObjectTag> entry : map.map.entrySet()) {
                String key = entry.getKey().low;
                if (!acceptedWithKeys.contains(key)) {
                    attribute.echoError("Invalid embed.with_map[...] tag: unknown key '" + key + "' given.");
                    return null;
                }
                ObjectTag val = entry.getValue();
                if (key.equals("timestamp")) {
                    val = val.asType(TimeTag.class, attribute.context);
                }
                else if (key.equals("color")) {
                    val = val.asType(ColorTag.class, attribute.context);
                }
                if (val == null) {
                    attribute.echoError("embed.with_map[...] value is invalid.");
                    return null;
                }
                embed.embedData.putObject(key, val);
            }
            return embed;
        });

        // <--[tag]
        // @attribute <DiscordEmbedTag.with[<key>].as[<value>]>
        // @returns DiscordEmbedTag
        // @plugin dDiscordBot
        // @description
        // Returns a copy of this Embed tag, with the specified data key set to the specified value.
        // The following keys are accepted, with values of the listed type:
        // author_name: ElementTag
        // author_url: ElementTag of a URL (requires author_name set)
        // author_icon_url: ElementTag of a URL (requires author_name set)
        // color: ColorTag
        // description: ElementTag
        // footer: ElementTag
        // footer_icon: ElementTag of a URL (requires footer set)
        // image: ElementTag of a URL
        // thumbnail: ElementTag of a URL
        // timestamp: TimeTag
        // title: ElementTag
        // title_url: ElementTag of a URL (requires title set)
        // fields: generally don't use directly, but can be set to a list of maps wherein each sub-map has keys "title", "value", and "inline" (boolean)
        // For fields, instead prefer <@link tag DiscordEmbedTag.add_field.value> and <@link tag DiscordEmbedTag.add_inline_field.value>.
        // -->
        tagProcessor.registerTag(DiscordEmbedTag.class, "with", (attribute, object) -> {
            DiscordEmbedTag embed = object.duplicate();
            if (!attribute.hasParam()) {
                attribute.echoError("Invalid embed.with[...] tag: must have an input value.");
                return null;
            }
            String key = CoreUtilities.toLowerCase(attribute.getParam());
            if (!acceptedWithKeys.contains(key)) {
                attribute.echoError("Invalid embed.with[...] tag: unknown key '" + key + "' given.");
                return null;
            }
            attribute.fulfill(1);
            if (!attribute.startsWith("as") || !attribute.hasParam()) {
                attribute.echoError("embed.with[...] must be followed by as[...].");
            }
            ObjectTag val = attribute.getParamObject();
            if (key.equals("timestamp")) {
                val = val.asType(TimeTag.class, attribute.context);
            }
            else if (key.equals("color")) {
                val = val.asType(ColorTag.class, attribute.context);
            }
            if (val == null) {
                attribute.echoError("embed.with[...].as[...] value is invalid.");
                return null;
            }
            embed.embedData.putObject(key, val);
            return embed;
        });

        // <--[tag]
        // @attribute <DiscordEmbedTag.map>
        // @returns MapTag
        // @plugin dDiscordBot
        // @description
        // Returns the MapTag internally backing this embed tag.
        // -->
        tagProcessor.registerTag(MapTag.class, "map", (attribute, object) -> {
            return object.embedData.duplicate();
        });

        // <--[tag]
        // @attribute <DiscordEmbedTag.add_field[<title>].value[<value>]>
        // @returns DiscordEmbedTag
        // @plugin dDiscordBot
        // @description
        // Returns a copy of this Embed tag, with a field added with the given title and value.
        // -->
        tagProcessor.registerTag(DiscordEmbedTag.class, "add_field", (attribute, object) -> {
            DiscordEmbedTag embed = object.duplicate();
            if (!attribute.hasParam()) {
                attribute.echoError("Invalid embed.add_field[...] tag: must have an input title.");
                return null;
            }
            String title = attribute.getParam();
            attribute.fulfill(1);
            if (!attribute.startsWith("value") || !attribute.hasParam()) {
                attribute.echoError("embed.add_field[...] must be followed by value[...].");
            }
            String value = attribute.getParam();
            MapTag fieldMap = new MapTag();
            fieldMap.putObject("title", new ElementTag(title));
            fieldMap.putObject("value", new ElementTag(value));
            ListTag fieldList = embed.embedData.getObjectAs("fields", ListTag.class, attribute.context);
            if (fieldList == null) {
                fieldList = new ListTag();
                embed.embedData.putObject("fields", fieldList);
            }
            fieldList.addObject(fieldMap);
            return embed;
        });

        // <--[tag]
        // @attribute <DiscordEmbedTag.add_inline_field[<title>].value[<value>]>
        // @returns DiscordEmbedTag
        // @plugin dDiscordBot
        // @description
        // Returns a copy of this Embed tag, with an inline field added with the given title and value.
        // -->
        tagProcessor.registerTag(DiscordEmbedTag.class, "add_inline_field", (attribute, object) -> {
            DiscordEmbedTag embed = object.duplicate();
            if (!attribute.hasParam()) {
                attribute.echoError("Invalid embed.add_inline_field[...] tag: must have an input title.");
                return null;
            }
            String title = attribute.getParam();
            attribute.fulfill(1);
            if (!attribute.startsWith("value") || !attribute.hasParam()) {
                attribute.echoError("embed.add_inline_field[...] must be followed by value[...].");
            }
            String value = attribute.getParam();
            MapTag fieldMap = new MapTag();
            fieldMap.putObject("title", new ElementTag(title));
            fieldMap.putObject("value", new ElementTag(value));
            fieldMap.putObject("inline", new ElementTag("true"));
            ListTag fieldList = embed.embedData.getObjectAs("fields", ListTag.class, attribute.context);
            if (fieldList == null) {
                fieldList = new ListTag();
                embed.embedData.putObject("fields", fieldList);
            }
            fieldList.addObject(fieldMap);
            return embed;
        });

        // <--[tag]
        // @attribute <DiscordEmbedTag.output_length>
        // @returns ElementTag(Number)
        // @plugin dDiscordBot
        // @description
        // Returns the total number of displayed characters this embed contains.
        // Discord rejects embeds with a total character count above 6000.
        // There are other limits for embed objects, refer to <@link url https://discordjs.guide/popular-topics/embeds.html#embed-limits>
        //
        // -->
        tagProcessor.registerTag(ElementTag.class, "output_length", (attribute, object) -> {
            return new ElementTag(object.duplicate().build(attribute.context).length());
        });

        // <--[tag]
        // @attribute <DiscordEmbedTag.to_json>
        // @returns ElementTag
        // @plugin dDiscordBot
        // @description
        // Returns the raw Discord-API-compatible JSON text of this embed.
        // -->
        tagProcessor.registerTag(ElementTag.class, "to_json", (attribute, object) -> {
            return new ElementTag(object.duplicate().build(attribute.context).build().toData().toString());
        });
    }

    public static ObjectTagProcessor<DiscordEmbedTag> tagProcessor = new ObjectTagProcessor<>();

    @Override
    public ObjectTag getObjectAttribute(Attribute attribute) {
        return tagProcessor.getObjectAttribute(this, attribute);
    }

    String prefix = "discordembed";

    @Override
    public String getPrefix() {
        return prefix;
    }

    @Override
    public String debuggable() {
        return "<LG>discordembed@<Y>" + embedData.debuggable();
    }

    @Override
    public boolean isUnique() {
        return false;
    }

    @Override
    public String identify() {
        return "discordembed@" + embedData.identify();
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
