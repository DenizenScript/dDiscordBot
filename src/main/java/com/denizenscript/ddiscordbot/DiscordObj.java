package com.denizenscript.ddiscordbot;

import net.aufdemrand.denizencore.objects.Element;
import net.aufdemrand.denizencore.objects.Fetchable;
import net.aufdemrand.denizencore.objects.dObject;
import net.aufdemrand.denizencore.tags.Attribute;
import net.aufdemrand.denizencore.tags.TagContext;
import sx.blah.discord.api.IDiscordClient;

public class DiscordObj implements dObject {

    String prefix = "discord";
    IDiscordClient discordClient = null;

    public DiscordObj(IDiscordClient client) {
        this.discordClient = client;
    }

    public static DiscordObj valueOf(String id) {
        return valueOf(id, null);
    }

    @Fetchable("discord")
    public static DiscordObj valueOf(String id, TagContext context) {
        if (id == null) {
            return null;
        }
        if (context == null) {
            return null;
        }
        // Match Discord Bot ID
        IDiscordClient discordClient = dDiscordBot.instance.connections.get(context.toString()).client;
        if (discordClient == null) {
            return null;
        }
        return new DiscordObj(discordClient);
    }

    @Override
    public String getPrefix() {
        return prefix;
    }

    @Override
    public String debug() {
        return null;
    }

    @Override
    public boolean isUnique() {
        return true;
    }

    @Override
    public String getObjectType() {
        return "Discord";
    }

    @Override
    public String identify() {
        return "Discord@" + discordClient.getApplicationClientID();
    }

    @Override
    public String identifySimple() {
        return identify();
    }

    @Override
    public dObject setPrefix(String prefix) {
        this.prefix = prefix;
        return this;
    }

    public IDiscordClient getClient() {
        return discordClient;
    }

    @Override
    public String getAttribute(Attribute attribute) {
        if (attribute == null) {
            return null;
        }
        // <--[tag]
        // @attribute <discord@discord.name>
        // @returns Element
        // @description
        // Returns the name of the Discord Bot.
        // @Plugin dDiscordBot
        // -->
        if (attribute.startsWith("name")) {
            return new Element(discordClient.getApplicationName()).getAttribute(attribute.fulfill(1));
        }
        // <--[tag]
        // @attribute <discord@discord.name>
        // @returns Element
        // @description
        // Returns the id of the Discord Bot.
        // @Plugin dDiscordBot
        // -->
        if (attribute.startsWith("id")) {
            return new Element(discordClient.getApplicationClientID()).getAttribute(attribute.fulfill(1));
        }
        return new Element(identify()).getAttribute(attribute);
    }
}
