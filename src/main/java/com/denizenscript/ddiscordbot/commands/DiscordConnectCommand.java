package com.denizenscript.ddiscordbot.commands;

import com.denizenscript.ddiscordbot.DenizenDiscordBot;
import com.denizenscript.ddiscordbot.DiscordConnection;
import com.denizenscript.denizen.Denizen;
import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsRuntimeException;
import com.denizenscript.denizencore.flags.SavableMapFlagTracker;
import com.denizenscript.denizencore.objects.Argument;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.objects.core.SecretTag;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;
import com.denizenscript.denizencore.scripts.commands.Holdable;
import com.denizenscript.denizencore.scripts.commands.generator.ArgDefaultNull;
import com.denizenscript.denizencore.scripts.commands.generator.ArgName;
import com.denizenscript.denizencore.scripts.commands.generator.ArgPrefixed;
import com.denizenscript.denizencore.utilities.CoreConfiguration;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import com.denizenscript.denizencore.utilities.ReflectionHelper;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import net.dv8tion.jda.internal.utils.JDALogger;
import org.bukkit.Bukkit;
import org.slf4j.Logger;

import java.io.*;
import java.lang.invoke.MethodHandle;
import java.util.*;
import java.util.stream.Collectors;

public class DiscordConnectCommand extends AbstractCommand implements Holdable {

    public static DiscordConnectCommand instance;

    public DiscordConnectCommand() {
        instance = this;
        setName("discordconnect");
        setSyntax("discordconnect [id:<id>] [token:<secret>] (intents:<intent>|...)");
        setRequiredArguments(2, 3);
        isProcedural = false;
        autoCompile();
    }

    // <--[command]
    // @Name discordconnect
    // @Syntax discordconnect [id:<id>] [token:<secret>] (intents:<intent>|...)
    // @Required 2
    // @Maximum 3
    // @Short Connects to Discord.
    // @Plugin dDiscordBot
    // @Guide https://guide.denizenscript.com/guides/expanding/ddiscordbot.html
    // @Group external
    //
    // @Description
    // Connects to Discord.
    //
    // The connection will automatically specify the following gateway intents:
    // GUILD_MEMBERS, GUILD_EMOJIS_AND_STICKERS, GUILD_MESSAGES, GUILD_MESSAGE_REACTIONS, DIRECT_MESSAGES, DIRECT_MESSAGE_REACTIONS, MESSAGE_CONTENT
    // Optionally specify additional Gateway Intents to use as a list of any of:
    // GUILD_BANS, GUILD_WEBHOOKS, GUILD_INVITES, GUILD_VOICE_STATES, GUILD_PRESENCES, GUILD_MESSAGE_TYPING, DIRECT_MESSAGE_TYPING
    //
    // use "intents:clear|SOME_INTENT|etc" (ie the first entry as "clear") to clear out default intents and use only your manually specified choices.
    //
    // Note that you need to enable the 'members' and 'message content' intent on your bot in Discord bot settings https://discord.com/developers/applications
    // And also may need to manually enable other intents if you specify any.
    // If the members intent is not enabled, a significant amount of dDiscordBot's functionality will not work.
    //
    // Store your Discord bot token in the Denizen secrets file at 'plugins/Denizen/secrets.secret'. Refer to <@link ObjectType SecretTag> for usage info.
    //
    // The command should always be ~waited for. See <@link language ~waitable>.
    //
    // @Tags
    // <discord[<bot_id>]>
    //
    // @Usage
    // Use to connect to Discord with a token stored in secret file 'plugins/Denizen/secrets.secret'.
    // - ~discordconnect id:mybot token:<secret[discord_bot_token]>
    //
    // -->

    public static boolean loggerIsFixed = false;

    public static PrintStream altLogger = new PrintStream(new ByteArrayOutputStream()) {
        @Override
        public void println(String s) {
            Debug.log("JDA", s);
        }
    };

    /**
     * This method is a dirty hack to minimize the amount of broken output from JDA.
     */
    public static void fixJDALogger() {
        if (loggerIsFixed) {
            return;
        }
        loggerIsFixed = true;
        // Dirty hack step 1: break System.err so Paper won't complain when JDALogger's static init whines into System.err
        PrintStream currentErr = System.err;
        System.setErr(altLogger);
        Logger defaultLogger = null;
        try {
            // Force JDALogger to init now, which will do that spam, and get a SimpleLogger instance while we're at it.
            defaultLogger = JDALogger.getLog(DiscordConnectCommand.class);
        }
        finally {
            // Fix the logger back, with a try/finally to avoid breaking it.
            System.setErr(currentErr);
        }
        try {
            // Dirty hack step 2: use that SimpleLogger instance to modify the class and redirect its log path to one that won't get complained about by Paper.
            MethodHandle streamSetter = ReflectionHelper.getFinalSetter(defaultLogger.getClass(), "TARGET_STREAM");
            streamSetter.invoke(altLogger);
        }
        catch (Throwable ex) {
            Debug.echoError(ex);
        }
    }

    static {
        fixJDALogger();
    }

    public static class DiscordConnectThread extends Thread {

        public String code;

        public DiscordConnection conn;

        public Runnable ender;

        public ScriptEntry scriptEntry;

        public HashSet<GatewayIntent> intents = new HashSet<>(Arrays.asList(GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_EMOJIS_AND_STICKERS, GatewayIntent.GUILD_MESSAGE_REACTIONS,
                GatewayIntent.GUILD_MESSAGES, GatewayIntent.DIRECT_MESSAGE_REACTIONS, GatewayIntent.DIRECT_MESSAGES, GatewayIntent.MESSAGE_CONTENT));

        @Override
        public void run() {
            try {
                try {
                    // Try with intents
                    JDA jda;
                    // Hack to bypass Paper whining about JDA whining into System.err
                    PrintStream currentErr = System.err;
                    System.setErr(altLogger);
                    try {
                        jda = JDABuilder.createDefault(code)
                                .enableCache(Arrays.stream(CacheFlag.values()).filter(f -> f.getRequiredIntent() == null || intents.contains(f.getRequiredIntent())).collect(Collectors.toList()))
                                .enableIntents(intents)
                                .setMemberCachePolicy(MemberCachePolicy.ALL)
                                .setAutoReconnect(true)
                                .setLargeThreshold(100000)
                                .setChunkingFilter(ChunkingFilter.ALL)
                                .build();
                    }
                    finally {
                        System.setErr(currentErr);
                    }
                    conn.client = jda;
                    jda.awaitReady();
                }
                catch (Exception ex) {
                    if (CoreConfiguration.debugVerbose) {
                        Debug.echoError(scriptEntry, ex);
                    }
                    Debug.echoError(scriptEntry, "Discord full connection attempt failed.");
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
                    Debug.echoError(ex);
                });
            }
            Bukkit.getScheduler().runTask(DenizenDiscordBot.instance, () -> {
                conn.flags = SavableMapFlagTracker.loadFlagFile(flagFilePathFor(conn.botID), true);
                ender.run();
            });
        }
    }


    public static String flagFilePathFor(String bot) {
        return DenizenDiscordBot.instance.getDataFolder().getPath() + "/flags/bot_" + Argument.prefixCharsAllowed.trimToMatches(CoreUtilities.toLowerCase(bot)) + ".dat";
    }

    public static void autoExecute(ScriptEntry scriptEntry,
                                   @ArgPrefixed @ArgName("id") String idString,
                                   @ArgPrefixed @ArgDefaultNull @ArgName("tokenfile") String tokenFile,
                                   @ArgPrefixed @ArgDefaultNull @ArgName("token") SecretTag token,
                                   @ArgPrefixed @ArgDefaultNull @ArgName("intents") ListTag intents) {
        if (tokenFile == null && token == null) {
            throw new InvalidArgumentsRuntimeException("Missing token SecretTag object!");
        }
        String id = CoreUtilities.toLowerCase(idString);
        if (DenizenDiscordBot.instance.connections.containsKey(id)) {
            Debug.echoError("Failed to connect: duplicate ID!");
            return;
        }
        DiscordConnection dc = new DiscordConnection();
        dc.botID = id;
        DenizenDiscordBot.instance.connections.put(id, dc);
        Bukkit.getScheduler().runTaskAsynchronously(DenizenDiscordBot.instance, () -> {
            String codeRaw;
            if (tokenFile != null) {
                DenizenDiscordBot.oldTokenFile.warn(scriptEntry);
                File f = new File(Denizen.getInstance().getDataFolder(), tokenFile);
                if (!Utilities.canReadFile(f)) {
                    Debug.echoError(scriptEntry, "Cannot read from that token file path due to security settings in Denizen/config.yml.");
                    scriptEntry.setFinished(true);
                    DenizenDiscordBot.instance.connections.remove(id);
                    return;
                }
                if (!f.exists()) {
                    Debug.echoError(scriptEntry, "Invalid tokenfile specified. File does not exist.");
                    scriptEntry.setFinished(true);
                    DenizenDiscordBot.instance.connections.remove(id);
                    return;
                }
                codeRaw = CoreUtilities.journallingLoadFile(f.getAbsolutePath());
                if (codeRaw == null || codeRaw.length() < 5 || codeRaw.length() > 200) {
                    Debug.echoError(scriptEntry, "Invalid tokenfile specified. File content doesn't look like a bot token.");
                    scriptEntry.setFinished(true);
                    DenizenDiscordBot.instance.connections.remove(id);
                    return;
                }
            }
            else {
                codeRaw = token.getValue();
            }
            codeRaw = codeRaw.trim();
            DiscordConnectThread dct = new DiscordConnectThread();
            dct.scriptEntry = scriptEntry;
            dct.code = codeRaw;
            dct.conn = dc;
            dct.ender = () -> scriptEntry.setFinished(true);
            if (intents != null) {
                try {
                    for (String intent : intents) {
                        if (CoreUtilities.equalsIgnoreCase(intent, "clear")) {
                            dct.intents.clear();
                        }
                        else {
                            dct.intents.add(GatewayIntent.valueOf(intent.toUpperCase()));
                        }
                    }
                }
                catch (IllegalArgumentException ex) {
                    Debug.echoError("Invalid 'intents' input - " + ex.getMessage());
                    scriptEntry.setFinished(true);
                    DenizenDiscordBot.instance.connections.remove(id);
                    return;
                }
            }
            dct.start();
        });
    }
}
