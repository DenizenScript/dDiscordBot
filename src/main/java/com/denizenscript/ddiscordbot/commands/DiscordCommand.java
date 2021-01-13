package com.denizenscript.ddiscordbot.commands;

import com.denizenscript.ddiscordbot.DenizenDiscordBot;
import com.denizenscript.ddiscordbot.DiscordConnection;
import com.denizenscript.ddiscordbot.objects.*;
import com.denizenscript.denizencore.objects.Argument;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsException;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;
import com.denizenscript.denizencore.scripts.commands.Holdable;
import com.denizenscript.denizencore.scripts.queues.ScriptQueue;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import org.bukkit.Bukkit;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

public class DiscordCommand extends AbstractCommand implements Holdable {

    public DiscordCommand() {
        setName("discord");
        setSyntax("discord [id:<id>] [connect code:<botcode>/disconnect/message/add_role/start_typing/stop_typing/remove_role/status (status:<status>) (activity:<activity>)/rename/edit_message/delete_message] (<message>) (message_id:<id>) (channel:<channel>) (user:<user>) (group:<group>) (role:<role>) (url:<url>)");
        setRequiredArguments(2, 12);
    }

    // <--[command]
    // @Name discord
    // @Syntax discord [id:<id>] [connect code:<botcode>/disconnect/message/add_role/start_typing/stop_typing/remove_role/status (status:<status>) (activity:<activity>)/rename/edit_message/delete_message] (<message>) (message_id:<id>) (channel:<channel>) (user:<user>) (group:<group>) (role:<role>) (url:<url>)
    // @Required 2
    // @Maximum 12
    // @Short Connects to and interacts with Discord.
    // @Plugin dDiscordBot
    // @Group external
    //
    // @Description
    // Connects to and interacts with Discord.
    //
    // Commands may fail if the bot does not have permission within the Discord group to perform them.
    //
    // When setting the status of the Discord bot, the status argument can be: ONLINE, DND, IDLE, or INVISIBLE,
    // and the activity argument can be: PLAYING, STREAMING, LISTENING, or WATCHING.
    // Streaming activity requires a 'url:' input.
    //
    // The command should always be ~waited for. See <@link language ~waitable>.
    //
    // Do not type your bot token code directly into the script.
    // The generally recommend way to track tokens is in a separate data YAML file.
    // Load the file, issue the connect command, then unload the file. Do not keep it in server memory any longer than needed.
    //
    // @Tags
    // <discord[<bot_id>]>
    // <entry[saveName].message_id> returns the ID of the sent message, when the command is ~waited for, and the 'message' argument is used.
    //
    // @Usage
    // Use to connect to Discord via a bot code.
    // - ~discord id:mybot connect code:<[code]>
    //
    // @Usage
    // Use to disconnect from Discord.
    // - ~discord id:mybot disconnect
    //
    // @Usage
    // Use to message a Discord channel.
    // - ~discord id:mybot message channel:<discord[mybot].group[Denizen].channel[bot-spam]> "Hello world!"
    //
    // @Usage
    // Use to message an embed to a Discord channel.
    // - ~discord id:mybot message channel:<discord[mybot].group[Denizen].channel[bot-spam]> "<discord_embed.with[title].as[hi].with[description].as[This is an embed!]>"
    //
    // @Usage
    // Use to message a Discord channel and record the ID.
    // - ~discord id:mybot message channel:<discord[mybot].group[Denizen].channel[bot-spam]> "Hello world!" save:sent
    // - announce "Sent as <entry[sent].message_id>"
    //
    // @Usage
    // Use to send a message to a user through a private channel.
    // - ~discord id:mybot message user:<[user]> "Hello world!"
    //
    // @Usage
    // Use to add a role on a user in a Discord guild.
    // - ~discord id:mybot add_role user:<[user]> role:<[role]> group:<[group]>
    //
    // @Usage
    // Use to remove a role on a user in a Discord guild.
    // - ~discord id:mybot remove_role user:<[user]> role:<[role]> group:<[group]>
    //
    // @Usage
    // Use to set the online status of the bot, and clear the game status.
    // - ~discord id:mybot status "Minecraft" "status:ONLINE"
    //
    // @Usage
    // Use to set the game status of the bot.
    // - ~discord id:mybot status "Minecraft" "status:ONLINE" "activity:PLAYING"
    //
    // @Usage
    // Use to change the bot's nickname.
    // - ~discord id:mybot rename "<[nickname]>" group:<[group]>
    //
    // @Usage
    // Use to give a user a new nickname.
    // - ~discord id:mybot rename "<[nickname]>" user:<[user]> group:<[group]>
    //
    // @Usage
    // Use to start typing in a specific channel.
    // - ~discord id:mybot start_typing channel:<[channel]>
    //
    // @Usage
    // Use to stop typing in a specific channel.
    // - ~discord id:mybot stop_typing channel:<[channel]>
    //
    // @Usage
    // Use to edit a message the bot has already sent.
    // - ~discord id:mybot edit_message channel:<[channel]> message_id:<[msg]> "Wow! It got edited!"
    //
    // @Usage
    // Use to delete a message the bot has already sent.
    // - ~discord id:mybot delete_message channel:<[channel]> message_id:<[msg]>
    //
    // -->

    public enum DiscordInstruction { CONNECT, DISCONNECT, MESSAGE, ADD_ROLE, REMOVE_ROLE, STATUS, RENAME, START_TYPING, STOP_TYPING, EDIT_MESSAGE, DELETE_MESSAGE }

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {
        for (Argument arg : scriptEntry.getProcessedArgs()) {
            if (!scriptEntry.hasObject("id")
                    && arg.matchesPrefix("id")) {
                scriptEntry.addObject("id", new ElementTag(CoreUtilities.toLowerCase(arg.getValue())));
            }
            else if (!scriptEntry.hasObject("instruction")
                    && arg.matchesEnum(DiscordInstruction.values())) {
                scriptEntry.addObject("instruction", arg.asElement());
            }
            else if (!scriptEntry.hasObject("code")
                    && arg.matchesPrefix("code")) {
                scriptEntry.addObject("code", arg.asElement());
            }
            else if (!scriptEntry.hasObject("channel")
                    && arg.matchesPrefix("channel")
                    && arg.matchesArgumentType(DiscordChannelTag.class)) {
                scriptEntry.addObject("channel", arg.asType(DiscordChannelTag.class));
            }
            else if (!scriptEntry.hasObject("url")
                    && arg.matchesPrefix("url")) {
                scriptEntry.addObject("url", arg.asElement());
            }
            else if (!scriptEntry.hasObject("user")
                    && arg.matchesPrefix("user")
                    && arg.matchesArgumentType(DiscordUserTag.class)) {
                scriptEntry.addObject("user", arg.asType(DiscordUserTag.class));
            }
            else if (!scriptEntry.hasObject("group")
                    && arg.matchesPrefix("group")
                    && arg.matchesArgumentType(DiscordGroupTag.class)) {
                scriptEntry.addObject("group", arg.asType(DiscordGroupTag.class));
            }
            else if (!scriptEntry.hasObject("role")
                    && arg.matchesPrefix("role")
                    && arg.matchesArgumentType(DiscordRoleTag.class)) {
                scriptEntry.addObject("role", arg.asType(DiscordRoleTag.class));
            }
            else if (!scriptEntry.hasObject("status")
                    && arg.matchesPrefix("status")) {
                scriptEntry.addObject("status", arg.asElement());
            }
            else if (!scriptEntry.hasObject("activity")
                    && arg.matchesPrefix("activity")) {
                scriptEntry.addObject("activity", arg.asElement());
            }
            else if (!scriptEntry.hasObject("message_id")
                    && arg.matchesPrefix("message_id")) {
                scriptEntry.addObject("message_id", arg.asElement());
            }
            else if (!scriptEntry.hasObject("message")) {
                scriptEntry.addObject("message", new ElementTag(arg.getRawValue()));
            }
            else {
                arg.reportUnhandled();
            }
        }
        if (!scriptEntry.hasObject("id")) {
            throw new InvalidArgumentsException("Must have an ID!");
        }
        if (!scriptEntry.hasObject("instruction")) {
            throw new InvalidArgumentsException("Must have an instruction!");
        }
    }

    public static class DiscordConnectThread extends Thread {

        public String code;

        public DiscordConnection conn;

        public Runnable ender;

        @Override
        public void run() {
            try {
                try {
                    // Try with intents
                    JDA jda = JDABuilder.createDefault(code)
                            .enableIntents(GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_EMOJIS,
                                    GatewayIntent.GUILD_MESSAGE_REACTIONS, GatewayIntent.GUILD_MESSAGES,
                                    GatewayIntent.DIRECT_MESSAGE_REACTIONS, GatewayIntent.DIRECT_MESSAGES)
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
                    Debug.echoError("Discord full connection attempt failed.");
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

    public static void errorMessage(ScriptQueue queue, String message) {
        Bukkit.getScheduler().scheduleSyncDelayedTask(DenizenDiscordBot.instance, new Runnable() {
            @Override
            public void run() {
                Debug.echoError(queue, message);
            }
        }, 0);
    }

    @Override
    public void execute(ScriptEntry scriptEntry) {
        ElementTag id = scriptEntry.getElement("id");
        ElementTag instruction = scriptEntry.getElement("instruction");
        ElementTag code = scriptEntry.getElement("code"); // Intentionally do not debug this value.
        DiscordChannelTag channel = scriptEntry.getObjectTag("channel");
        ElementTag message = scriptEntry.getElement("message");
        ElementTag status = scriptEntry.getElement("status");
        ElementTag activity = scriptEntry.getElement("activity");
        DiscordUserTag user = scriptEntry.getObjectTag("user");
        DiscordGroupTag guild = scriptEntry.getObjectTag("group");
        DiscordRoleTag role = scriptEntry.getObjectTag("role");
        ElementTag url = scriptEntry.getElement("url");
        ElementTag messageId = scriptEntry.getElement("message_id");
        if (scriptEntry.dbCallShouldDebug()) {
            Debug.report(scriptEntry, getName(), id.debug()
                    + (channel != null ? channel.debug() : "")
                    + instruction.debug()
                    + (message != null ? message.debug() : "")
                    + (user != null ? user.debug() : "")
                    + (guild != null ? guild.debug() : "")
                    + (role != null ? role.debug() : "")
                    + (status != null ? status.debug() : "")
                    + (activity != null ? activity.debug() : "")
                    + (url != null ? url.debug() : "")
                    + (messageId != null ? messageId.debug() : ""));
        }
        Supplier<Boolean> requireClientID = () -> {
            if (!DenizenDiscordBot.instance.connections.containsKey(id.asString())) {
                Debug.echoError(scriptEntry.getResidingQueue(), "Failed to process Discord " + instruction.asString() + " command: unknown ID!");
                scriptEntry.setFinished(true);
                return true;
            }
            return false;
        };
        Function<JDA, Boolean> requireClientObject = (_client) -> {
            if (_client == null) {
                Debug.echoError(scriptEntry.getResidingQueue(), "The Discord bot '" + id.asString() + "'is not yet loaded.");
                scriptEntry.setFinished(true);
                return true;
            }
            return false;
        };
        BiFunction<Object, String, Boolean> requireObject = (obj, name) -> {
            if (obj == null) {
                Debug.echoError(scriptEntry.getResidingQueue(), "Failed to process Discord " + instruction.asString() + " command: no " + name + " given!");
                scriptEntry.setFinished(true);
                return true;
            }
            return false;
        };
        Supplier<Boolean> requireUser = () -> requireObject.apply(user, "user");
        Supplier<Boolean> requireChannel = () -> requireObject.apply(channel, "channel");
        Supplier<Boolean> requireMessage = () -> requireObject.apply(message, "message");
        Supplier<Boolean> requireGuild = () -> requireObject.apply(guild, "guild");
        Supplier<Boolean> requireRole = () -> requireObject.apply(role, "role");
        Supplier<Boolean> requireMessageId = () -> requireObject.apply(messageId, "message_id");
        DiscordInstruction instructionEnum = DiscordInstruction.valueOf(instruction.asString().toUpperCase());
        if (instructionEnum == DiscordInstruction.CONNECT) {
            if (requireObject.apply(code, "code")) {
                return;
            }
            if (scriptEntry.dbCallShouldDebug() && com.denizenscript.denizen.utilities.debugging.Debug.record) {
                Debug.echoError("You almost recorded debug of your Discord token - record automatically disabled to protect you.");
                com.denizenscript.denizen.utilities.debugging.Debug.record = false;
            }
            if (DenizenDiscordBot.instance.connections.containsKey(id.asString())) {
                Debug.echoError(scriptEntry.getResidingQueue(), "Failed to connect: duplicate ID!");
                return;
            }
            DiscordConnection dc = new DiscordConnection();
            dc.botID = id.asString();
            DenizenDiscordBot.instance.connections.put(id.asString(), dc);
            DiscordConnectThread dct = new DiscordConnectThread();
            dct.code = code.asString();
            dct.conn = dc;
            dct.ender = () -> scriptEntry.setFinished(true);
            dct.start();
            return;
        }
        Runnable executeCore = () -> {
            switch (instructionEnum) {
                case DISCONNECT: {
                    if (requireClientID.get()) {
                        return;
                    }
                    JDA client = DenizenDiscordBot.instance.connections.remove(id.asString()).client;
                    client.shutdown();
                    try {
                        client.awaitStatus(JDA.Status.SHUTDOWN);
                    }
                    catch (InterruptedException ex) {
                        Debug.echoError(ex);
                    }
                    scriptEntry.setFinished(true);
                    break;
                }
                case MESSAGE: {
                    if (channel == null && user == null) {
                        if (!requireChannel.get()) {
                            requireUser.get();
                        }
                        scriptEntry.setFinished(true);
                        return;
                    }
                    if (requireClientID.get() || requireMessage.get()) {
                        return;
                    }
                    JDA client = DenizenDiscordBot.instance.connections.get(id.asString()).client;
                    if (requireClientObject.apply(client)) {
                        return;
                    }
                    MessageChannel textChan;
                    if (channel == null) {
                        User userObj = client.getUserById(user.user_id);
                        if (userObj == null) {
                            Debug.echoError("Invalid or unrecognized user (given user ID not valid? Have you enabled the 'members' intent?).");
                            scriptEntry.setFinished(true);
                            return;
                        }
                        textChan = userObj.openPrivateChannel().complete();
                    }
                    else {
                        textChan = client.getTextChannelById(channel.channel_id);
                    }
                    if (textChan == null) {
                        Debug.echoError("No channel to send message to (channel ID invalid, or not a text channel?).");
                        scriptEntry.setFinished(true);
                        return;
                    }
                    Message sentMessage;
                    if (message.asString().startsWith("discordembed@")) {
                        MessageEmbed embed = DiscordEmbedTag.valueOf(message.asString(), scriptEntry.context).build(scriptEntry.context).build();
                        sentMessage = textChan.sendMessage(embed).complete();
                    }
                    else {
                        sentMessage = textChan.sendMessage(message.asString()).complete();
                    }
                    scriptEntry.addObject("message_id", new ElementTag(sentMessage.getId()));
                    scriptEntry.setFinished(true);
                    break;
                }
                case ADD_ROLE: {
                    if (requireClientID.get() || requireUser.get() || requireGuild.get() || requireRole.get()) {
                        return;
                    }
                    JDA client = DenizenDiscordBot.instance.connections.get(id.asString()).client;
                    if (requireClientObject.apply(client)) {
                        return;
                    }
                    Guild guildObj = client.getGuildById(guild.guild_id);
                    Member memberObj = guildObj.getMemberById(user.user_id);
                    guildObj.addRoleToMember(memberObj, guildObj.getRoleById(role.role_id)).complete();
                    scriptEntry.setFinished(true);
                    break;
                }
                case REMOVE_ROLE: {
                    if (requireClientID.get() || requireUser.get() || requireRole.get() || requireGuild.get()) {
                        return;
                    }
                    JDA client = DenizenDiscordBot.instance.connections.get(id.asString()).client;
                    if (requireClientObject.apply(client)) {
                        return;
                    }
                    Guild guildObj = client.getGuildById(guild.guild_id);
                    Member memberObj = guildObj.getMemberById(user.user_id);
                    guildObj.removeRoleFromMember(memberObj, guildObj.getRoleById(role.role_id)).complete();
                    scriptEntry.setFinished(true);
                    break;
                }
                case EDIT_MESSAGE: {
                    if (requireClientID.get() || requireChannel.get() || requireMessage.get() || requireMessageId.get()) {
                        return;
                    }
                    JDA client = DenizenDiscordBot.instance.connections.get(id.asString()).client;
                    if (requireClientObject.apply(client)) {
                        return;
                    }
                    MessageChannel textChannel = client.getTextChannelById(channel.channel_id);
                    if (message.asString().startsWith("discordembed@")) {
                        MessageEmbed embed = DiscordEmbedTag.valueOf(message.asString(), scriptEntry.context).build(scriptEntry.context).build();
                        textChannel.editMessageById(messageId.asLong(), embed).complete();
                    }
                    else {
                        textChannel.editMessageById(messageId.asLong(), message.asString()).complete();
                    }
                    scriptEntry.setFinished(true);
                    break;
                }
                case DELETE_MESSAGE: {
                    if (requireClientID.get() || requireChannel.get() || requireMessageId.get()) {
                        return;
                    }
                    JDA client = DenizenDiscordBot.instance.connections.get(id.asString()).client;
                    if (requireClientObject.apply(client)) {
                        return;
                    }
                    client.getTextChannelById(channel.channel_id).deleteMessageById(messageId.asLong()).complete();
                    scriptEntry.setFinished(true);
                    break;
                }
                case START_TYPING: {
                    if (requireClientID.get() || requireChannel.get()) {
                        return;
                    }
                    JDA client = DenizenDiscordBot.instance.connections.get(id.asString()).client;
                    if (requireClientObject.apply(client)) {
                        return;
                    }
                    client.getTextChannelById(channel.channel_id).sendTyping().complete();
                    scriptEntry.setFinished(true);
                    break;
                }
                case STOP_TYPING: {
                    if (requireClientID.get() || requireChannel.get()) {
                        return;
                    }
                    JDA client = DenizenDiscordBot.instance.connections.get(id.asString()).client;
                    if (requireClientObject.apply(client)) {
                        return;
                    }
                    // TODO: ?
                    scriptEntry.setFinished(true);
                    break;
                }
                case RENAME: {
                    if (requireClientID.get() || requireGuild.get() || requireMessage.get()) {
                        return;
                    }
                    JDA client = DenizenDiscordBot.instance.connections.get(id.asString()).client;
                    if (requireClientObject.apply(client)) {
                        return;
                    }
                    long userId;
                    if (user == null) {
                        userId = client.getSelfUser().getIdLong();
                    }
                    else {
                        userId = user.user_id;
                    }
                    client.getGuildById(guild.guild_id).getMemberById(userId).modifyNickname(message.asString()).complete();
                    scriptEntry.setFinished(true);
                    break;
                }
                case STATUS: {
                    if (requireClientID.get()) {
                        return;
                    }
                    JDA client = DenizenDiscordBot.instance.connections.get(id.asString()).client;
                    if (requireClientObject.apply(client)) {
                        return;
                    }
                    Activity at;
                    String activityType = CoreUtilities.toLowerCase(activity.toString());
                    if (activityType.equals("watching")) {
                        at = Activity.watching(message.asString());
                    }
                    else if (activityType.equals("streaming")) {
                        at = Activity.streaming(message.asString(), url.asString());
                    }
                    else if (activityType.equals("listening")) {
                        at = Activity.listening(message.asString());
                    }
                    else {
                        at = Activity.playing(message.asString());
                    }
                    String statusLower = status == null ? "online" : CoreUtilities.toLowerCase(status.asString());
                    OnlineStatus statusType;
                    if (statusLower.equals("idle")) {
                        statusType = OnlineStatus.IDLE;
                    }
                    else if (statusLower.equals("dnd")) {
                        statusType = OnlineStatus.DO_NOT_DISTURB;
                    }
                    else if (statusLower.equals("invisible")) {
                        statusType = OnlineStatus.INVISIBLE;
                    }
                    else {
                        statusType = OnlineStatus.ONLINE;
                    }
                    client.getPresence().setPresence(statusType, at);
                    scriptEntry.setFinished(true);
                    break;
                }
            }
        };
        if (scriptEntry.shouldWaitFor()) {
            Bukkit.getScheduler().runTaskAsynchronously(DenizenDiscordBot.instance, executeCore);
        }
        else {
            executeCore.run();
        }
    }
}
