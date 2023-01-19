package com.denizenscript.ddiscordbot.commands;

import com.denizenscript.ddiscordbot.DenizenDiscordBot;
import com.denizenscript.ddiscordbot.DiscordConnection;
import com.denizenscript.denizen.Denizen;
import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizencore.DenizenCore;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsRuntimeException;
import com.denizenscript.denizencore.flags.SavableMapFlagTracker;
import com.denizenscript.denizencore.objects.Argument;
import com.denizenscript.denizencore.objects.core.ElementTag;
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
    // The command should usually be ~waited for. See <@link language ~waitable>.
    //
    // @Tags
    // <discord[<bot_id>]>
    //
    // @Usage
    // Use to connect to Discord with a token stored in secret file 'plugins/Denizen/secrets.secret' and send a message once connected.
    // - ~discordconnect id:mybot token:<secret[discord_bot_token]>
    // - discordmessage id:mybot channel:<[my_log_channel]> "Connected!"
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

    public static HashSet<GatewayIntent> defaultIntents = new HashSet<>(Arrays.asList(GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_EMOJIS_AND_STICKERS, GatewayIntent.GUILD_MESSAGE_REACTIONS,
            GatewayIntent.GUILD_MESSAGES, GatewayIntent.DIRECT_MESSAGE_REACTIONS, GatewayIntent.DIRECT_MESSAGES, GatewayIntent.MESSAGE_CONTENT));

    public static void runConnect(String code, DiscordConnection conn, ScriptEntry scriptEntry, HashSet<GatewayIntent> intents) {
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
        String file = flagFilePathFor(conn.botID);
        if (!new File(file + ".dat").exists() && new File(file + ".dat.dat").exists()) { // Patch prior mistake of filename
            new File(file + ".dat.dat").renameTo(new File(file + ".dat"));
        }
        DenizenCore.runOnMainThread(() -> {
            conn.flags = SavableMapFlagTracker.loadFlagFile(file, true);
            scriptEntry.setFinished(true);
        });
    }


    public static String flagFilePathFor(String bot) {
        return DenizenDiscordBot.instance.getDataFolder().getPath() + "/flags/bot_" + Argument.prefixCharsAllowed.trimToMatches(CoreUtilities.toLowerCase(bot));
    }

    public static void autoExecute(ScriptEntry scriptEntry,
                                   @ArgPrefixed @ArgName("id") String idString,
                                   @ArgPrefixed @ArgDefaultNull @ArgName("tokenfile") String tokenFile,
                                   @ArgPrefixed @ArgDefaultNull @ArgName("token") SecretTag token,
                                   @ArgPrefixed @ArgDefaultNull @ArgName("intents") ListTag intents) {
        String id = CoreUtilities.toLowerCase(idString);
        if (DenizenDiscordBot.instance.connections.containsKey(id)) {
            Debug.echoError("Failed to connect: duplicate ID!");
            return;
        }
        DiscordConnection dc = new DiscordConnection();
        dc.botID = id;
        DenizenDiscordBot.instance.connections.put(id, dc);
        String code = token != null ? token.getValue().trim() : null;
        if (tokenFile != null) {
            DenizenDiscordBot.oldTokenFile.warn(scriptEntry);
            File f = new File(Denizen.getInstance().getDataFolder(), tokenFile);
            if (!Utilities.canReadFile(f)) {
                DenizenDiscordBot.instance.connections.remove(id);
                throw new InvalidArgumentsRuntimeException("Cannot read from that token file path due to security settings in Denizen/config.yml.");
            }
            String codeRaw = f.exists() ? CoreUtilities.journallingLoadFile(f.getAbsolutePath()) : null;
            if (codeRaw == null) {
                DenizenDiscordBot.instance.connections.remove(id);
                throw new InvalidArgumentsRuntimeException("Invalid tokenfile specified. File does not exist.");
            }
            code = codeRaw.trim();
        }
        if (code == null) { // TODO: remove this and ArgDefaultNull when tokenFile is removed
            throw new InvalidArgumentsRuntimeException("Missing token SecretTag object!");
        }
        if (code.length() < 5 || code.length() > 200) {
            DenizenDiscordBot.instance.connections.remove(id);
            throw new InvalidArgumentsRuntimeException("Invalid token specified. Content doesn't look like a bot token.");
        }
        HashSet<GatewayIntent> intentSet = new HashSet<>(defaultIntents);
        if (intents != null) {
            for (String intent : intents) {
                if (CoreUtilities.equalsIgnoreCase(intent, "clear")) {
                    intentSet.clear();
                }
                else {
                    GatewayIntent actualIntent = ElementTag.asEnum(GatewayIntent.class, intent);
                    if (actualIntent == null) {
                        DenizenDiscordBot.instance.connections.remove(id);
                        throw new InvalidArgumentsRuntimeException("Invalid intent input '" + intent + "'");
                    }
                    intentSet.add(actualIntent);
                }
            }
        }
        String finalCode = code;
        DenizenCore.runAsync(() -> runConnect(finalCode, dc, scriptEntry, intentSet));
    }
}
