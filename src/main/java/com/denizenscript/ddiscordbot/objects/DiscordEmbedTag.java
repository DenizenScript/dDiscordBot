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
import com.denizenscript.denizencore.tags.TagRunnable;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import com.denizenscript.denizencore.utilities.text.StringHolder;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.bukkit.Color;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;

public class DiscordEmbedTag implements ObjectTag {

    // <--[language]
    // @name DiscordEmbedTag Objects
    // @group Object System
    // @plugin dDiscordBot
    // @description
    // A DiscordEmbedTag is an object that represents a Discord embed for use with dDiscordBot.
    //
    // These use the object notation "discordembed@".
    // The identity format for Discord embeds is a map of embed data. Do not alter raw embed data, use the with.as tag instead.
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
            embedData.putObject("author_icon_url", new ColorTag(Color.fromRGB(embed.getColorRaw())));
        }
        if (embed.getDescription() != null) {
            embedData.putObject("getDescription", new ElementTag(embed.getDescription()));
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
                    fieldMap.putObject("title", new ElementTag(field.getValue()));
                }
                fieldMap.putObject("inline", new ElementTag(field.isInline()));
                fields.addObject(fieldMap);
            }
            embedData.putObject("fields", fields);
        }
    }

    public EmbedBuilder build(TagContext context) {
        EmbedBuilder builder = new EmbedBuilder();
        ObjectTag author_name = embedData.getObject("author_name");
        ObjectTag author_url = embedData.getObject("author_url");
        ObjectTag author_icon_url = embedData.getObject("author_icon_url");
        ObjectTag color = embedData.getObject("color");
        ObjectTag description = embedData.getObject("description");
        ObjectTag footer = embedData.getObject("footer");
        ObjectTag footer_icon = embedData.getObject("footer_icon");
        ObjectTag image = embedData.getObject("image");
        ObjectTag thumbnail = embedData.getObject("thumbnail");
        ObjectTag timestamp = embedData.getObject("timestamp");
        ObjectTag title = embedData.getObject("title");
        ObjectTag title_url = embedData.getObject("title_url");
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
            builder.setColor(ColorTag.valueOf(color.toString(), context).getColor().asRGB());
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
            builder.setTimestamp(TimeTag.valueOf(timestamp.toString(), context).instant);
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
            ListTag fieldList = ListTag.getListFor(fields, context);
            for (ObjectTag field : fieldList.objectForms) {
                MapTag fieldMap = MapTag.getMapFor(field, context);
                if (fieldMap != null) {
                    ObjectTag fieldTitle = fieldMap.getObject("title");
                    ObjectTag fieldValue = fieldMap.getObject("value");
                    ObjectTag fieldInline = fieldMap.getObject("inline");
                    builder.addField(fieldTitle.toString(), fieldValue.toString(), fieldInline != null && fieldInline.toString().equals("true"));
                }
            }
        }
        return builder;
    }

    public MapTag embedData;

    public static HashSet<String> acceptedWithKeys = new HashSet<>(Arrays.asList(
            "author_name", "author_url", "author_icon_url", "color", "description",
            "footer", "footer_icon", "image", "thumbnail", "timestamp", "title", "title_url"
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
        registerTag("with_map", (attribute, object) -> {
            DiscordEmbedTag embed = object.duplicate();
            if (!attribute.hasContext(1)) {
                attribute.echoError("Invalid embed.with_map[...] tag: must have an input value.");
                return null;
            }
            MapTag map = MapTag.getMapFor(attribute.getContextObject(1), attribute.context);
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
        // -->
        registerTag("with", (attribute, object) -> {
            DiscordEmbedTag embed = object.duplicate();
            if (!attribute.hasContext(1)) {
                attribute.echoError("Invalid embed.with[...] tag: must have an input value.");
                return null;
            }
            String key = CoreUtilities.toLowerCase(attribute.getContext(1));
            if (!acceptedWithKeys.contains(key)) {
                attribute.echoError("Invalid embed.with[...] tag: unknown key '" + key + "' given.");
                return null;
            }
            attribute.fulfill(1);
            if (!attribute.startsWith("as") || !attribute.hasContext(1)) {
                attribute.echoError("embed.with[...] must be followed by as[...].");
            }
            ObjectTag val = attribute.getContextObject(1);
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
        registerTag("map", (attribute, object) -> {
            return object.embedData.duplicate();
        });

        // <--[tag]
        // @attribute <DiscordEmbedTag.add_field[<title>].value[<value>]>
        // @returns DiscordEmbedTag
        // @plugin dDiscordBot
        // @description
        // Returns a copy of this Embed tag, with a field added with the given title and value.
        // -->
        registerTag("add_field", (attribute, object) -> {
            DiscordEmbedTag embed = object.duplicate();
            if (!attribute.hasContext(1)) {
                attribute.echoError("Invalid embed.add_field[...] tag: must have an input title.");
                return null;
            }
            String title = attribute.getContext(1);
            attribute.fulfill(1);
            if (!attribute.startsWith("value") || !attribute.hasContext(1)) {
                attribute.echoError("embed.add_field[...] must be followed by value[...].");
            }
            String value = attribute.getContext(1);
            MapTag fieldMap = new MapTag();
            fieldMap.putObject("title", new ElementTag(title));
            fieldMap.putObject("value", new ElementTag(value));
            ObjectTag fieldsData = embed.embedData.getObject("fields");
            ListTag fieldList;
            if (fieldsData == null) {
                fieldList = new ListTag();
                embed.embedData.putObject("fields", fieldList);
            }
            else {
                fieldList = ListTag.getListFor(fieldsData, attribute.context);
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
        registerTag("add_inline_field", (attribute, object) -> {
            DiscordEmbedTag embed = object.duplicate();
            if (!attribute.hasContext(1)) {
                attribute.echoError("Invalid embed.add_inline_field[...] tag: must have an input title.");
                return null;
            }
            String title = attribute.getContext(1);
            attribute.fulfill(1);
            if (!attribute.startsWith("value") || !attribute.hasContext(1)) {
                attribute.echoError("embed.add_inline_field[...] must be followed by value[...].");
            }
            String value = attribute.getContext(1);
            MapTag fieldMap = new MapTag();
            fieldMap.putObject("title", new ElementTag(title));
            fieldMap.putObject("value", new ElementTag(value));
            fieldMap.putObject("inline", new ElementTag("true"));
            ObjectTag fieldsData = embed.embedData.getObject("fields");
            ListTag fieldList;
            if (fieldsData == null) {
                fieldList = new ListTag();
                embed.embedData.putObject("fields", fieldList);
            }
            else {
                fieldList = ListTag.getListFor(fieldsData, attribute.context);
            }
            fieldList.addObject(fieldMap);
            return embed;
        });
    }

    public static ObjectTagProcessor<DiscordEmbedTag> tagProcessor = new ObjectTagProcessor<>();

    public static void registerTag(String name, TagRunnable.ObjectInterface<DiscordEmbedTag> runnable, String... variants) {
        tagProcessor.registerTag(name, runnable, variants);
    }

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
    public String debug() {
        return (prefix + "='<A>" + identify() + "<G>'  ");
    }

    @Override
    public boolean isUnique() {
        return false;
    }

    @Override
    public String getObjectType() {
        return "DiscordEmbed";
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
