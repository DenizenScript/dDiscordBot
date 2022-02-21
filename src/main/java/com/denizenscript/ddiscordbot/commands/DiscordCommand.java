package com.denizenscript.ddiscordbot.commands;

import com.denizenscript.ddiscordbot.DenizenDiscordBot;
import com.denizenscript.ddiscordbot.DiscordConnection;
import com.denizenscript.ddiscordbot.objects.*;
import com.denizenscript.denizen.Denizen;
import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizencore.objects.Argument;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsException;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.Holdable;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.*;
import org.bukkit.Bukkit;

import java.io.File;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

public class DiscordCommand extends AbstractDiscordCommand implements Holdable {

    public DiscordCommand() {
        setName("discord");
        setSyntax("discord [id:<id>] [disconnect/add_role/start_typing/remove_role/status (status:<status>) (activity:<activity>)/rename] (<value>) (message_id:<id>) (channel:<channel>) (user:<user>) (group:<group>) (role:<role>) (url:<url>)");
        setRequiredArguments(2, 12);
        isProcedural = false;
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

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {
        for (Argument arg : scriptEntry) {
            if (!scriptEntry.hasObject("id")
                    && arg.matchesPrefix("id")) {
                scriptEntry.addObject("id", new ElementTag(CoreUtilities.toLowerCase(arg.getValue())));
            }
            else if (!scriptEntry.hasObject("instruction")
                    && arg.matchesEnum(DiscordInstruction.class)) {
                scriptEntry.addObject("instruction", arg.asElement());
            }
            else if (!scriptEntry.hasObject("code")
                    && arg.matchesPrefix("code")) {
                scriptEntry.addObject("code", arg.asElement());
            }
            else if (!scriptEntry.hasObject("tokenfile")
                    && arg.matchesPrefix("tokenfile")) {
                scriptEntry.addObject("tokenfile", arg.asElement());
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
                scriptEntry.addObject("message", arg.getRawElement());
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

    static {
        DiscordConnectCommand.fixJDALogger();
    }

    @Override
    public void execute(ScriptEntry scriptEntry) {
        ElementTag id = scriptEntry.getElement("id");
        ElementTag instruction = scriptEntry.getElement("instruction");
        ElementTag code = scriptEntry.getElement("code"); // Intentionally do not debug this value.
        ElementTag tokenFile = scriptEntry.getElement("tokenfile");
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
            Debug.report(scriptEntry, getName(), id, channel, instruction, message, user, guild, role, status, activity, url, tokenFile, messageId);
        }
        Supplier<Boolean> requireClientID = () -> {
            if (!DenizenDiscordBot.instance.connections.containsKey(id.asString())) {
                handleError(scriptEntry, "Failed to process Discord " + instruction.asString() + " command: unknown ID!");
                scriptEntry.setFinished(true);
                return true;
            }
            return false;
        };
        Function<JDA, Boolean> requireClientObject = (_client) -> {
            if (_client == null) {
                handleError(scriptEntry, "The Discord bot '" + id.asString() + "'is not yet loaded.");
                scriptEntry.setFinished(true);
                return true;
            }
            return false;
        };
        BiFunction<Object, String, Boolean> requireObject = (obj, name) -> {
            if (obj == null) {
                handleError(scriptEntry, "Failed to process Discord " + instruction.asString() + " command: no " + name + " given!");
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
            if (code != null && scriptEntry.dbCallShouldDebug() && com.denizenscript.denizen.utilities.debugging.Debug.record) {
                handleError(scriptEntry, "You almost recorded debug of your Discord token - record automatically disabled to protect you.");
                com.denizenscript.denizen.utilities.debugging.Debug.record = false;
            }
        }
        Runnable executeCore = () -> {
            try {
                switch (instructionEnum) {
                    case CONNECT: {
                        DenizenDiscordBot.oldConnectCommand.warn(scriptEntry);
                        if (code == null && tokenFile == null) {
                            requireObject.apply(null, "tokenfile");
                            break;
                        }
                        if (DenizenDiscordBot.instance.connections.containsKey(id.asString())) {
                            handleError(scriptEntry, "Failed to connect: duplicate ID!");
                            break;
                        }
                        String codeRaw;
                        if (code != null) {
                            codeRaw = code.asString();
                        }
                        else {
                            File f = new File(Denizen.getInstance().getDataFolder(), tokenFile.asString());
                            if (!Utilities.canReadFile(f)) {
                                handleError(scriptEntry, "Cannot read from that token file path due to security settings in Denizen/config.yml.");
                                scriptEntry.setFinished(true);
                                break;
                            }
                            if (!f.exists()) {
                                handleError(scriptEntry, "Invalid tokenfile specified. File does not exist.");
                                scriptEntry.setFinished(true);
                                break;
                            }
                            codeRaw = CoreUtilities.journallingLoadFile(f.getAbsolutePath());
                            if (codeRaw == null || codeRaw.length() < 5 || codeRaw.length() > 200) {
                                handleError(scriptEntry, "Invalid tokenfile specified. File content doesn't look like a bot token.");
                                scriptEntry.setFinished(true);
                                break;
                            }
                            codeRaw = codeRaw.trim();
                        }
                        DiscordConnection dc = new DiscordConnection();
                        dc.botID = id.asString();
                        DenizenDiscordBot.instance.connections.put(id.asString(), dc);
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
                        DiscordConnection dc = DenizenDiscordBot.instance.connections.remove(id.asString());
                        if (dc.flags.modified) {
                            dc.flags.saveToFile(DiscordConnectCommand.flagFilePathFor(id.asString()));
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
                        JDA client = DenizenDiscordBot.instance.connections.get(id.asString()).client;
                        if (requireClientObject.apply(client)) {
                            return;
                        }
                        MessageChannel textChan;
                        if (channel == null) {
                            User userObj = client.getUserById(user.user_id);
                            if (userObj == null) {
                                handleError(scriptEntry, "Invalid or unrecognized user (given user ID not valid? Have you enabled the 'members' intent?).");
                                scriptEntry.setFinished(true);
                                return;
                            }
                            textChan = userObj.openPrivateChannel().complete();
                        }
                        else {
                            textChan = client.getTextChannelById(channel.channel_id);
                        }
                        if (textChan == null) {
                            handleError(scriptEntry, "No channel to send message to (channel ID invalid, or not a text channel?).");
                            scriptEntry.setFinished(true);
                            return;
                        }
                        Message sentMessage;
                        if (message.asString().startsWith("discordembed@")) {
                            MessageEmbed embed = DiscordEmbedTag.valueOf(message.asString(), scriptEntry.context).build(scriptEntry.context).build();
                            sentMessage = textChan.sendMessageEmbeds(embed).complete();
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
                        DenizenDiscordBot.oldEditMessage.warn(scriptEntry);
                        if (requireClientID.get() || requireChannel.get() || requireMessage.get() || requireMessageId.get()) {
                            return;
                        }
                        DiscordConnection connection = DenizenDiscordBot.instance.connections.get(id.asString());
                        if (requireClientObject.apply(connection == null ? null : connection.client)) {
                            return;
                        }
                        MessageChannel textChannel = (MessageChannel) connection.getChannel(channel.channel_id);
                        if (message.asString().startsWith("discordembed@")) {
                            MessageEmbed embed = DiscordEmbedTag.valueOf(message.asString(), scriptEntry.context).build(scriptEntry.context).build();
                            textChannel.editMessageEmbedsById(messageId.asLong(), embed).complete();
                        }
                        else {
                            textChannel.editMessageById(messageId.asLong(), message.asString()).complete();
                        }
                        scriptEntry.setFinished(true);
                        break;
                    }
                    case DELETE_MESSAGE: {
                        DenizenDiscordBot.oldDeleteMessage.warn(scriptEntry);
                        if (requireClientID.get() || requireChannel.get() || requireMessageId.get()) {
                            return;
                        }
                        DiscordConnection connection = DenizenDiscordBot.instance.connections.get(id.asString());
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
                        DiscordConnection connection = DenizenDiscordBot.instance.connections.get(id.asString());
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
                        switch (activityType) {
                            case "watching":
                                at = Activity.watching(message.asString());
                                break;
                            case "streaming":
                                at = Activity.streaming(message.asString(), url.asString());
                                break;
                            case "listening":
                                at = Activity.listening(message.asString());
                                break;
                            default:
                                at = Activity.playing(message.asString());
                                break;
                        }
                        String statusLower = status == null ? "online" : CoreUtilities.toLowerCase(status.asString());
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
                handleError(scriptEntry, ex);
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
