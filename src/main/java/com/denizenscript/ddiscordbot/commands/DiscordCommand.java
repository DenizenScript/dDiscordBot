package com.denizenscript.ddiscordbot.commands;

import com.denizenscript.ddiscordbot.DenizenDiscordBot;
import com.denizenscript.ddiscordbot.DiscordConnection;
import com.denizenscript.ddiscordbot.objects.*;
import com.denizenscript.denizen.Denizen;
import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;
import com.denizenscript.denizencore.scripts.commands.generator.*;
import com.denizenscript.denizencore.utilities.CoreConfiguration;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.Holdable;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import org.bukkit.Bukkit;

import java.io.File;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

public class DiscordCommand extends AbstractCommand implements Holdable {

    public DiscordCommand() {
        setName("discord");
        setSyntax("discord [id:<id>] [disconnect/add_role/start_typing/remove_role/status (status:<status>) (activity:<activity>)/rename] (<value>) (message_id:<id>) (channel:<channel>) (user:<user>) (group:<group>) (role:<role>) (url:<url>)");
        setRequiredArguments(2, 12);
        isProcedural = false;
        autoCompile();
    }

    // <--[command]
    // @Name discord
    // @Syntax discord [id:<id>] [disconnect/add_role/start_typing/remove_role/status (status:<status>) (activity:<activity>)/rename] (<value>) (message_id:<id>) (channel:<channel>) (user:<user>) (group:<group>) (role:<role>) (url:<url>)
    // @Required 2
    // @Maximum 12
    // @Short Interacts with Discord.
    // @Plugin dDiscordBot
    // @Guide https://guide.denizenscript.com/guides/expanding/ddiscordbot.html
    // @Group external
    //
    // @Description
    // Interacts with Discord.
    //
    // Commands may fail if the bot does not have permission within the Discord group to perform them.
    //
    // When setting the status of the Discord bot, the status argument can be: ONLINE, DND, IDLE, or INVISIBLE,
    // and the activity argument can be: PLAYING, STREAMING, LISTENING, or WATCHING.
    // Streaming activity requires a 'url:' input.
    //
    // The command should always be ~waited for. See <@link language ~waitable>.
    //
    // @Tags
    // <discord[<bot_id>]>
    //
    // @Usage
    // Use to disconnect from Discord.
    // - ~discord id:mybot disconnect
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
    // -->

    public enum DiscordInstruction { CONNECT, DISCONNECT, MESSAGE, ADD_ROLE, REMOVE_ROLE, STATUS, RENAME, START_TYPING, STOP_TYPING, EDIT_MESSAGE, DELETE_MESSAGE }

    static {
        DiscordConnectCommand.fixJDALogger();
    }

    public static void autoExecute(ScriptEntry scriptEntry,
                                   @ArgPrefixed @ArgName("id") String idString,
                                   @ArgName("instruction") DiscordInstruction instruction,
                                   @ArgPrefixed @ArgDefaultNull @ArgName("code") String code,
                                   @ArgPrefixed @ArgDefaultNull @ArgName("tokenfile") String tokenFile,
                                   @ArgPrefixed @ArgDefaultNull @ArgName("channel") DiscordChannelTag channel,
                                   @ArgRaw @ArgLinear @ArgDefaultNull @ArgName("message") String message,
                                   @ArgPrefixed @ArgDefaultNull @ArgName("status") String status,
                                   @ArgPrefixed @ArgDefaultNull @ArgName("activity") String activity,
                                   @ArgPrefixed @ArgDefaultNull @ArgName("user") DiscordUserTag user,
                                   @ArgPrefixed @ArgDefaultNull @ArgName("group") DiscordGroupTag guild,
                                   @ArgPrefixed @ArgDefaultNull @ArgName("role") DiscordRoleTag role,
                                   @ArgPrefixed @ArgDefaultNull @ArgName("url") String url,
                                   @ArgPrefixed @ArgDefaultNull @ArgName("message_id") ElementTag messageId) {
        String id = CoreUtilities.toLowerCase(idString);
        Supplier<Boolean> requireClientID = () -> {
            if (!DenizenDiscordBot.instance.connections.containsKey(id)) {
                Debug.echoError(scriptEntry, "Failed to process Discord " + instruction + " command: unknown ID!");
                scriptEntry.setFinished(true);
                return true;
            }
            return false;
        };
        Function<JDA, Boolean> requireClientObject = (_client) -> {
            if (_client == null) {
                Debug.echoError(scriptEntry, "The Discord bot '" + id + "'is not yet loaded.");
                scriptEntry.setFinished(true);
                return true;
            }
            return false;
        };
        BiFunction<Object, String, Boolean> requireObject = (obj, name) -> {
            if (obj == null) {
                Debug.echoError(scriptEntry, "Failed to process Discord " + instruction + " command: no " + name + " given!");
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
        if (instruction == DiscordInstruction.CONNECT) {
            if (code != null && scriptEntry.dbCallShouldDebug() && CoreConfiguration.shouldRecordDebug) {
                Debug.echoError(scriptEntry, "You almost recorded debug of your Discord token - record automatically disabled to protect you.");
                Debug.startRecording();
            }
        }
        Runnable executeCore = () -> {
            try {
                switch (instruction) {
                    case CONNECT: {
                        DenizenDiscordBot.oldConnectCommand.warn(scriptEntry);
                        if (code == null && tokenFile == null) {
                            requireObject.apply(null, "tokenfile");
                            break;
                        }
                        if (DenizenDiscordBot.instance.connections.containsKey(id)) {
                            Debug.echoError(scriptEntry, "Failed to connect: duplicate ID!");
                            break;
                        }
                        String codeRaw;
                        if (code != null) {
                            codeRaw = code;
                        }
                        else {
                            File f = new File(Denizen.getInstance().getDataFolder(), tokenFile);
                            if (!Utilities.canReadFile(f)) {
                                Debug.echoError(scriptEntry, "Cannot read from that token file path due to security settings in Denizen/config.yml.");
                                scriptEntry.setFinished(true);
                                break;
                            }
                            if (!f.exists()) {
                                Debug.echoError(scriptEntry, "Invalid tokenfile specified. File does not exist.");
                                scriptEntry.setFinished(true);
                                break;
                            }
                            codeRaw = CoreUtilities.journallingLoadFile(f.getAbsolutePath());
                            if (codeRaw == null || codeRaw.length() < 5 || codeRaw.length() > 200) {
                                Debug.echoError(scriptEntry, "Invalid tokenfile specified. File content doesn't look like a bot token.");
                                scriptEntry.setFinished(true);
                                break;
                            }
                            codeRaw = codeRaw.trim();
                        }
                        DiscordConnection dc = new DiscordConnection();
                        dc.botID = id;
                        DenizenDiscordBot.instance.connections.put(id, dc);
                        DiscordConnectCommand.DiscordConnectThread dct = new DiscordConnectCommand.DiscordConnectThread();
                        dct.code = codeRaw;
                        dct.conn = dc;
                        dct.ender = () -> scriptEntry.setFinished(true);
                        dct.start();
                        break;
                    }
                    case DISCONNECT: {
                        if (requireClientID.get()) {
                            return;
                        }
                        DiscordConnection dc = DenizenDiscordBot.instance.connections.remove(id);
                        if (dc.flags.modified) {
                            dc.flags.saveToFile(DiscordConnectCommand.flagFilePathFor(id));
                        }
                        dc.client.shutdown();
                        scriptEntry.setFinished(true);
                        break;
                    }
                    case MESSAGE: {
                        DenizenDiscordBot.oldMessageCommand.warn(scriptEntry);
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
                        JDA client = DenizenDiscordBot.instance.connections.get(id).client;
                        if (requireClientObject.apply(client)) {
                            return;
                        }
                        MessageChannel textChan;
                        if (channel == null) {
                            User userObj = client.getUserById(user.user_id);
                            if (userObj == null) {
                                Debug.echoError(scriptEntry, "Invalid or unrecognized user (given user ID not valid? Have you enabled the 'members' intent?).");
                                scriptEntry.setFinished(true);
                                return;
                            }
                            textChan = userObj.openPrivateChannel().complete();
                        }
                        else {
                            textChan = client.getTextChannelById(channel.channel_id);
                        }
                        if (textChan == null) {
                            Debug.echoError(scriptEntry, "No channel to send message to (channel ID invalid, or not a text channel?).");
                            scriptEntry.setFinished(true);
                            return;
                        }
                        Message sentMessage;
                        if (message.startsWith("discordembed@")) {
                            MessageEmbed embed = DiscordEmbedTag.valueOf(message, scriptEntry.context).build(scriptEntry.context).build();
                            sentMessage = textChan.sendMessageEmbeds(embed).complete();
                        }
                        else {
                            sentMessage = textChan.sendMessage(message).complete();
                        }
                        scriptEntry.addObject("message_id", new ElementTag(sentMessage.getId()));
                        scriptEntry.setFinished(true);
                        break;
                    }
                    case ADD_ROLE: {
                        if (requireClientID.get() || requireUser.get() || requireGuild.get() || requireRole.get()) {
                            return;
                        }
                        JDA client = DenizenDiscordBot.instance.connections.get(id).client;
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
                        JDA client = DenizenDiscordBot.instance.connections.get(id).client;
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
                        DenizenDiscordBot.oldEditMessage.warn(scriptEntry);
                        if (requireClientID.get() || requireChannel.get() || requireMessage.get() || requireMessageId.get()) {
                            return;
                        }
                        DiscordConnection connection = DenizenDiscordBot.instance.connections.get(id);
                        if (requireClientObject.apply(connection == null ? null : connection.client)) {
                            return;
                        }
                        MessageChannel textChannel = (MessageChannel) connection.getChannel(channel.channel_id);
                        if (message.startsWith("discordembed@")) {
                            MessageEmbed embed = DiscordEmbedTag.valueOf(message, scriptEntry.context).build(scriptEntry.context).build();
                            textChannel.editMessageEmbedsById(messageId.asLong(), embed).complete();
                        }
                        else {
                            textChannel.editMessageById(messageId.asLong(), message).complete();
                        }
                        scriptEntry.setFinished(true);
                        break;
                    }
                    case DELETE_MESSAGE: {
                        DenizenDiscordBot.oldDeleteMessage.warn(scriptEntry);
                        if (requireClientID.get() || requireChannel.get() || requireMessageId.get()) {
                            return;
                        }
                        DiscordConnection connection = DenizenDiscordBot.instance.connections.get(id);
                        if (requireClientObject.apply(connection == null ? null : connection.client)) {
                            return;
                        }
                        ((MessageChannel) connection.getChannel(channel.channel_id)).deleteMessageById(messageId.asLong()).complete();
                        scriptEntry.setFinished(true);
                        break;
                    }
                    case START_TYPING: {
                        if (requireClientID.get() || requireChannel.get()) {
                            return;
                        }
                        DiscordConnection connection = DenizenDiscordBot.instance.connections.get(id);
                        if (requireClientObject.apply(connection == null ? null : connection.client)) {
                            return;
                        }
                        MessageChannel textChannel = (MessageChannel) connection.getChannel(channel.channel_id);
                        textChannel.sendTyping().complete();
                        scriptEntry.setFinished(true);
                        break;
                    }
                    case STOP_TYPING: {
                        DenizenDiscordBot.oldStopTyping.warn(scriptEntry);
                        if (requireClientID.get() || requireChannel.get()) {
                            return;
                        }
                        JDA client = DenizenDiscordBot.instance.connections.get(id).client;
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
                        JDA client = DenizenDiscordBot.instance.connections.get(id).client;
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
                        client.getGuildById(guild.guild_id).getMemberById(userId).modifyNickname(message).complete();
                        scriptEntry.setFinished(true);
                        break;
                    }
                    case STATUS: {
                        if (requireClientID.get()) {
                            return;
                        }
                        JDA client = DenizenDiscordBot.instance.connections.get(id).client;
                        if (requireClientObject.apply(client)) {
                            return;
                        }
                        Activity at;
                        String activityType = CoreUtilities.toLowerCase(activity.toString());
                        switch (activityType) {
                            case "watching":
                                at = Activity.watching(message);
                                break;
                            case "streaming":
                                at = Activity.streaming(message, url);
                                break;
                            case "listening":
                                at = Activity.listening(message);
                                break;
                            default:
                                at = Activity.playing(message);
                                break;
                        }
                        String statusLower = status == null ? "online" : CoreUtilities.toLowerCase(status);
                        OnlineStatus statusType;
                        switch (statusLower) {
                            case "idle":
                                statusType = OnlineStatus.IDLE;
                                break;
                            case "dnd":
                                statusType = OnlineStatus.DO_NOT_DISTURB;
                                break;
                            case "invisible":
                                statusType = OnlineStatus.INVISIBLE;
                                break;
                            default:
                                statusType = OnlineStatus.ONLINE;
                                break;
                        }
                        client.getPresence().setPresence(statusType, at);
                        scriptEntry.setFinished(true);
                        break;
                    }
                }
            }
            catch (Throwable ex) {
                Debug.echoError(scriptEntry, ex);
                scriptEntry.setFinished(true);
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
