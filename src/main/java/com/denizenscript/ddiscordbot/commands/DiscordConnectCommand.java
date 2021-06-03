package com.denizenscript.ddiscordbot.commands;

import com.denizenscript.ddiscordbot.DenizenDiscordBot;
import com.denizenscript.ddiscordbot.DiscordConnection;
import com.denizenscript.denizen.Denizen;
import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsException;
import com.denizenscript.denizencore.flags.SavableMapFlagTracker;
import com.denizenscript.denizencore.objects.Argument;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;
import com.denizenscript.denizencore.scripts.commands.Holdable;
import com.denizenscript.denizencore.scripts.queues.ScriptQueue;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.bukkit.Bukkit;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.stream.Collectors;

public class DiscordConnectCommand extends AbstractCommand implements Holdable {

    public DiscordConnectCommand() {
        setName("discordconnect");
        setSyntax("discordconnect [id:<id>] [tokenfile:<file>] (intents:<intent>|...)");
        setRequiredArguments(2, 3);
    }

    // <--[command]
    // @Name discordconnect
    // @Syntax discordconnect [id:<id>] [tokenfile:<file>] (intents:<intent>|...)
    // @Required 2
    // @Maximum 3
    // @Short Connects to Discord.
    // @Plugin dDiscordBot
    // @Group external
    //
    // @Description
    // Connects to Discord.
    //
    // The connection will automatically specify the following gateway intents:
    // GUILD_MEMBERS, GUILD_EMOJIS, GUILD_MESSAGES, GUILD_MESSAGE_REACTIONS, DIRECT_MESSAGES, DIRECT_MESSAGE_REACTIONS
    // Optionally specify additional Gateway Intents to use as a list of any of:
    // GUILD_BANS, GUILD_WEBHOOKS, GUILD_INVITES, GUILD_VOICE_STATES, GUILD_PRESENCES, GUILD_MESSAGE_TYPING, DIRECT_MESSAGE_TYPING
    //
    // Note that you need to enable the 'members' intent on your bot in Discord bot settings https://discord.com/developers/applications
    // And also may need to manually enable other intents if you specify any.
    // If the members intent is not enabled, a significant amount of dDiscordBot's functionality will not work.
    //
    // Store your Discord bot token in a file, like "plugins/Denizen/data/discord_token.txt", with the token as the only content of the file, which you can then use like "tokenfile:data/discord_token.txt"
    //
    // The command should always be ~waited for. See <@link language ~waitable>.
    //
    // @Tags
    // <discord[<bot_id>]>
    //
    // @Usage
    // Use to connect to Discord with a token stored in text file 'plugins/Denizen/data/discord_token.txt'.
    // - ~discordconnect id:mybot tokenfile:data/discord_token.txt
    //
    // -->

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {
        for (Argument arg : scriptEntry.getProcessedArgs()) {
            if (!scriptEntry.hasObject("id")
                    && arg.matchesPrefix("id")) {
                scriptEntry.addObject("id", new ElementTag(CoreUtilities.toLowerCase(arg.getValue())));
            }
            else if (!scriptEntry.hasObject("tokenfile")
                    && arg.matchesPrefix("tokenfile")) {
                scriptEntry.addObject("tokenfile", arg.asElement());
            }
            else if (!scriptEntry.hasObject("intents")
                    && arg.matchesPrefix("intents")
                    && arg.matchesEnum(GatewayIntent.values())) {
                scriptEntry.addObject("intents", arg.asType(ListTag.class));
            }
            else {
                arg.reportUnhandled();
            }
        }
        if (!scriptEntry.hasObject("id")) {
            throw new InvalidArgumentsException("Must have an ID!");
        }
        if (!scriptEntry.hasObject("tokenfile")) {
            throw new InvalidArgumentsException("Must have a tokenfile!");
        }
    }

    public static class DiscordConnectThread extends Thread {

        public String code;

        public DiscordConnection conn;

        public Runnable ender;

        public ScriptQueue queue;

        public HashSet<GatewayIntent> intents = new HashSet<>(Arrays.asList(GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_EMOJIS, GatewayIntent.GUILD_MESSAGE_REACTIONS,
                GatewayIntent.GUILD_MESSAGES, GatewayIntent.DIRECT_MESSAGE_REACTIONS, GatewayIntent.DIRECT_MESSAGES));

        @Override
        public void run() {
            try {
                try {
                    // Try with intents
                    JDA jda = JDABuilder.createDefault(code)
                            .enableCache(Arrays.stream(CacheFlag.values()).filter(f -> f.getRequiredIntent() == null || intents.contains(f.getRequiredIntent())).collect(Collectors.toList()))
                            .enableIntents(intents)
                            .setMemberCachePolicy(MemberCachePolicy.ALL)
                            .setAutoReconnect(true)
                            .setLargeThreshold(100000)
                            .setChunkingFilter(ChunkingFilter.ALL)
                            .build();
                    conn.client = jda;
                    jda.awaitReady();
                }
                catch (Exception ex) {
                    if (Debug.verbose) {
                        Debug.echoError(ex);
                    }
                    DiscordCommand.errorMessage(queue, "Discord full connection attempt failed.");
                    Debug.log("Discord using fallback connection path - connecting with intents disabled. Enable the members intent in your bot's settings (at https://discord.com/developers/applications ) to fix this.");
                    // If startup failure, try without intents
                    JDA jda = JDABuilder.createDefault(code).build();
                    conn.client = jda;
                    jda.awaitReady();
                }
                conn.registerHandlers();
            }
            catch (Exception ex) {
                Bukkit.getScheduler().runTask(DenizenDiscordBot.instance, () -> {
                    DenizenDiscordBot.instance.connections.remove(conn.botID);
                });
                Debug.echoError(ex);
            }
            Bukkit.getScheduler().runTask(DenizenDiscordBot.instance, ender);
        }
    }


    public static String flagFilePathFor(String bot) {
        return DenizenDiscordBot.instance.getDataFolder().getAbsolutePath() + "/flags/bot_" + Argument.prefixCharsAllowed.trimToMatches(CoreUtilities.toLowerCase(bot)) + ".dat";
    }

    @Override
    public void execute(ScriptEntry scriptEntry) {
        ElementTag id = scriptEntry.getElement("id");
        ElementTag tokenFile = scriptEntry.getElement("tokenfile");
        ListTag intents = scriptEntry.getObjectTag("intents");
        if (scriptEntry.dbCallShouldDebug()) {
            Debug.report(scriptEntry, getName(), id, tokenFile, intents);
        }
        if (DenizenDiscordBot.instance.connections.containsKey(id.asString())) {
            DiscordCommand.errorMessage(scriptEntry.getResidingQueue(), "Failed to connect: duplicate ID!");
            return;
        }
        DiscordConnection dc = new DiscordConnection();
        dc.botID = id.asString();
        DenizenDiscordBot.instance.connections.put(id.asString(), dc);
        Bukkit.getScheduler().runTaskAsynchronously(DenizenDiscordBot.instance, () -> {
            File f = new File(Denizen.getInstance().getDataFolder(), tokenFile.asString());
            if (!Utilities.canReadFile(f)) {
                DiscordCommand.errorMessage(scriptEntry.getResidingQueue(), "Cannot read from that token file path due to security settings in Denizen/config.yml.");
                scriptEntry.setFinished(true);
                DenizenDiscordBot.instance.connections.remove(id.asString());
                return;
            }
            if (!f.exists()) {
                DiscordCommand.errorMessage(scriptEntry.getResidingQueue(), "Invalid tokenfile specified. File does not exist.");
                scriptEntry.setFinished(true);
                DenizenDiscordBot.instance.connections.remove(id.asString());
                return;
            }
            String codeRaw = CoreUtilities.journallingLoadFile(f.getAbsolutePath());
            if (codeRaw == null || codeRaw.length() < 5 || codeRaw.length() > 200) {
                DiscordCommand.errorMessage(scriptEntry.getResidingQueue(), "Invalid tokenfile specified. File content doesn't look like a bot token.");
                scriptEntry.setFinished(true);
                DenizenDiscordBot.instance.connections.remove(id.asString());
                return;
            }
            codeRaw = codeRaw.trim();
            dc.flags = SavableMapFlagTracker.loadFlagFile(DenizenDiscordBot.instance.getDataFolder().getPath() + flagFilePathFor(id.asString()));
            DiscordConnectThread dct = new DiscordConnectThread();
            dct.queue = scriptEntry.getResidingQueue();
            dct.code = codeRaw;
            dct.conn = dc;
            dct.ender = () -> scriptEntry.setFinished(true);
            if (intents != null) {
                for (String intent : intents) {
                    dct.intents.add(GatewayIntent.valueOf(intent.toUpperCase()));
                }
            }
            dct.start();
        });
    }
}
