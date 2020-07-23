package com.denizenscript.ddiscordbot;

import com.denizenscript.ddiscordbot.events.*;
import com.denizenscript.ddiscordbot.objects.*;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import com.denizenscript.denizencore.DenizenCore;
import com.denizenscript.denizencore.events.ScriptEvent;
import com.denizenscript.denizencore.objects.ObjectFetcher;
import com.denizenscript.denizencore.tags.TagManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;

public class DenizenDiscordBot extends JavaPlugin {

    public static DenizenDiscordBot instance;

    public HashMap<String, DiscordConnection> connections = new HashMap<>();

    @Override
    public void onEnable() {
        Debug.log("dDiscordBot loaded!");
        instance = this;
        try {
            DenizenCore.getCommandRegistry().registerCommand(DiscordCommand.class);
            ScriptEvent.registerScriptEvent(DiscordMessageModifiedScriptEvent.instance = new DiscordMessageModifiedScriptEvent());
            ScriptEvent.registerScriptEvent(DiscordMessageDeletedScriptEvent.instance = new DiscordMessageDeletedScriptEvent());
            ScriptEvent.registerScriptEvent(DiscordMessageReceivedScriptEvent.instance = new DiscordMessageReceivedScriptEvent());
            ScriptEvent.registerScriptEvent(DiscordUserJoinsScriptEvent.instance = new DiscordUserJoinsScriptEvent());
            ScriptEvent.registerScriptEvent(DiscordUserLeavesScriptEvent.instance = new DiscordUserLeavesScriptEvent());
            ScriptEvent.registerScriptEvent(DiscordUserRoleChangeScriptEvent.instance = new DiscordUserRoleChangeScriptEvent());
            ObjectFetcher.registerWithObjectFetcher(DiscordBotTag.class, DiscordBotTag.tagProcessor);
            ObjectFetcher.registerWithObjectFetcher(DiscordChannelTag.class, DiscordChannelTag.tagProcessor);
            ObjectFetcher.registerWithObjectFetcher(DiscordEmbedTag.class, DiscordEmbedTag.tagProcessor);
            ObjectFetcher.registerWithObjectFetcher(DiscordGroupTag.class, DiscordGroupTag.tagProcessor);
            ObjectFetcher.registerWithObjectFetcher(DiscordRoleTag.class, DiscordRoleTag.tagProcessor);
            ObjectFetcher.registerWithObjectFetcher(DiscordUserTag.class, DiscordUserTag.tagProcessor);
            // <--[tag]
            // @attribute <discord[<bot-id>]>
            // @returns DiscordBotTag
            // @plugin dDiscordBot
            // @description
            // Returns the Discord bot for the given bot ID.
            // -->
            TagManager.registerTagHandler("discord", (attribute) -> {
                if (!attribute.hasContext(1)) {
                    attribute.echoError("Discord tag base must have input.");
                    return null;
                }
                return DiscordBotTag.valueOf(attribute.getContext(1), attribute.context);
            });
            // <--[tag]
            // @attribute <discord_embed>
            // @returns DiscordEmbedTag
            // @plugin dDiscordBot
            // @description
            // Returns a blank DiscordEmbedTag instance, to be filled with data via the with.as tag.
            // -->
            TagManager.registerTagHandler("discord_embed", (attribute) -> {
                if (!attribute.hasContext(1)) {
                    return new DiscordEmbedTag();
                }
                return DiscordEmbedTag.valueOf(attribute.getContext(1), attribute.context);
            });
        }
        catch (Throwable ex) {
            Debug.echoError(ex);
        }
    }
}
