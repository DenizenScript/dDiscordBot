package com.denizenscript.ddiscordbot;

import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizencore.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizencore.objects.Element;
import net.aufdemrand.denizencore.objects.aH;
import net.aufdemrand.denizencore.scripts.ScriptEntry;
import net.aufdemrand.denizencore.scripts.commands.AbstractCommand;
import net.aufdemrand.denizencore.scripts.commands.Holdable;
import net.aufdemrand.denizencore.scripts.queues.ScriptQueue;
import net.aufdemrand.denizencore.utilities.CoreUtilities;
import org.bukkit.Bukkit;
import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.obj.*;
import sx.blah.discord.util.RequestBuffer;

public class DiscordCommand extends AbstractCommand implements Holdable {

    // <--[command]
    // @Name discord
    // @Syntax discord [id:<id>] [connect code:<botcode>/disconnect/message/addrole/removerole/status (status:<status>) (activity:<activity>)/rename] (<message>) (channel:<channel>) (user:<user>) (guild:<guild>) (role:<role>)
    // @Required 2
    // @Stable unstable
    // @Short Connects to and interacts with Discord.
    // @Author mcmonkey
    // @Plugin dDiscordBot
    // @Group external
    //
    // @Warning tags are not yet implemented!
    //
    // @Description
    // Connects to and interacts with Discord.
    //
    // Commands may fail if the bot does not have permission within the Discord group to perform them.
    //
    // When setting the status of the Discord bot, the status argument can be: ONLINE, DND, IDLE, INVISIBLE, OFFLINE or UNKNOWN,
    // and the activity argument can be: PLAYING, STREAMING, LISTENING or WATCHING.
    //
    // @Tags
    // TODO: Make tags
    //
    // @Usage
    // Use to connect to Discord via a bot code.
    // - discord id:mybot connect code:<def[code]>
    //
    // @Usage
    // Use to disconnect from Discord.
    // - discord id:mybot disconnect
    //
    // @Usage
    // Use to message a Discord channel.
    // - discord id:mybot message channel:<discord[mybot].server[Denizen].channel[bot-spam]> "Hello world!"
    //
    // @Usage
    // Use to send a message to a user through a private channel.
    // - discord id:mybot message user:<def[user_id]> "Hello world!"
    //
    // @Usage
    // Use to add a role on a user in a Discord guild.
    // - discord id:mybot addrole user:<def[user_id]> role:<def[role_id]> guild:<def[guild_id]>
    //
    // @Usage
    // Use to remove a role on a user in a Discord guild.
    // - discord id:mybot removerole user:<def[user_id]> role:<def[role_id]> guild:<def[guild_id]>
    //
    // @Usage
    // Use to set the online status of the bot, and clear the game status.
    // - discord id:mybot status "Minecraft" "status:ONLINE"
    //
    // @Usage
    // Use to set the game status of the bot.
    // - discord id:mybot status "Minecraft" "status:ONLINE" "activity:PLAYING"
    //
    // @Usage
    // Use to change the bot's nickname.
    // - discord id:mybot rename "<def[nickname]>" guild:<def[guild_id]>
    //
    // @Usage
    // Use to give a user a new nickname.
    // - discord id:mybot rename "<def[nickname]>" user:<def[user_id]> guild:<def[guild_id]>

    // -->

    public enum DiscordInstruction { CONNECT, DISCONNECT, MESSAGE, ADDROLE, REMOVEROLE, STATUS, RENAME }

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {
        // Interpret arguments
        for (aH.Argument arg : aH.interpret(scriptEntry.getArguments())) {

            if (!scriptEntry.hasObject("id")
                    && arg.matchesPrefix("id")) {
                scriptEntry.addObject("id", new Element(CoreUtilities.toLowerCase(arg.getValue())));
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
                    && arg.matchesPrefix("channel")) {
                scriptEntry.addObject("channel", arg.asElement());
            }
            else if (!scriptEntry.hasObject("user")
                    && arg.matchesPrefix("user")) {
                scriptEntry.addObject("user", arg.asElement());
            }
            else if (!scriptEntry.hasObject("guild")
                    && arg.matchesPrefix("guild")) {
                scriptEntry.addObject("guild", arg.asElement());
            }
            else if (!scriptEntry.hasObject("role")
                    && arg.matchesPrefix("role")) {
                scriptEntry.addObject("role", arg.asElement());
            }
            else if (!scriptEntry.hasObject("status")
                    && arg.matchesPrefix("status")) {
                scriptEntry.addObject("status", arg.asElement());
            }
            else if (!scriptEntry.hasObject("activity")
                    && arg.matchesPrefix("activity")) {
                scriptEntry.addObject("activity", arg.asElement());
            }
            else if (!scriptEntry.hasObject("message")) {
                scriptEntry.addObject("message", new Element(arg.raw_value));
            }
            else {
                arg.reportUnhandled();
            }
        }

        // Check for required information
        if (!scriptEntry.hasObject("id")) {
            throw new InvalidArgumentsException("Must have an ID!");
        }

        // Check for required information
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
                ClientBuilder cb = new ClientBuilder().withToken(code);
                cb.registerListener(conn);
                conn.client = cb.login();
            }
            catch (Exception ex) {
                Bukkit.getScheduler().runTask(dDiscordBot.instance, () -> {
                    dDiscordBot.instance.connections.remove(conn.botID);
                });
                dB.echoError(ex);
            }
            Bukkit.getScheduler().runTask(dDiscordBot.instance, ender);
        }
    }

    public static void errorMessage(ScriptQueue queue, String message) {
        Bukkit.getScheduler().scheduleSyncDelayedTask(dDiscordBot.instance, new Runnable() {
            @Override
            public void run() {
                dB.echoError(queue, message);
            }
        }, 0);
    }

    @Override
    public void execute(ScriptEntry scriptEntry) {

        // Fetch required objects
        Element id = scriptEntry.getElement("id");
        Element instruction = scriptEntry.getElement("instruction");
        Element code = scriptEntry.getElement("code");
        Element channel = scriptEntry.getElement("channel");
        Element message = scriptEntry.getElement("message");
        Element status = scriptEntry.getElement("status");
        Element activity = scriptEntry.getElement("activity");
        Element user = scriptEntry.getElement("user");
        Element guild = scriptEntry.getElement("guild");
        Element role = scriptEntry.getElement("role");

        // Debug the execution
        dB.report(scriptEntry, getName(), id.debug()
                + (channel != null ? channel.debug(): "")
                + instruction.debug()
                + (message != null ? message.debug(): "")
                + (user != null ? user.debug(): "")
                + (guild != null ? guild.debug(): "")
                + (role != null ? role.debug(): ""));

        IDiscordClient client;

        switch (DiscordInstruction.valueOf(instruction.asString().toUpperCase())) {
            case CONNECT:
                if (code == null) {
                    dB.echoError(scriptEntry.getResidingQueue(), "Failed to connect: no code given!");
                    return;
                }
                if (dDiscordBot.instance.connections.containsKey(id.asString())) {
                    dB.echoError(scriptEntry.getResidingQueue(), "Failed to connect: duplicate ID!");
                    return;
                }
                DiscordConnection dc = new DiscordConnection();
                dc.botID = id.asString();
                dDiscordBot.instance.connections.put(id.asString(), dc);
                DiscordConnectThread dct = new DiscordConnectThread();
                dct.code = code.asString();
                dct.conn = dc;
                dct.ender = () -> scriptEntry.setFinished(true);
                dct.start();
                break;
            case DISCONNECT:
                if (!dDiscordBot.instance.connections.containsKey(id.asString())) {
                    dB.echoError(scriptEntry.getResidingQueue(), "Failed to disconnect: unknown ID!");
                    return;
                }
                dDiscordBot.instance.connections.remove(id.asString()).client.logout();
                break;
            case MESSAGE:
                if (channel == null && user == null) {
                    dB.echoError(scriptEntry.getResidingQueue(), "Failed to send message: no channel given!");
                    return;
                }
                if (message == null) {
                    dB.echoError(scriptEntry.getResidingQueue(), "Failed to send message: no message given!");
                    return;
                }
                if (!dDiscordBot.instance.connections.containsKey(id.asString())) {
                    dB.echoError(scriptEntry.getResidingQueue(), "Failed to send message: unknown ID!");
                    return;
                }
                client = dDiscordBot.instance.connections.get(id.asString()).client;
                RequestBuffer.request(() -> {
                    if (channel == null) {
                        IUser userObj = client.getUserByID(user.asLong());
                        if (userObj == null) {
                            errorMessage(scriptEntry.getResidingQueue(), "User for ID '" + user.asLong() + "' not found.");
                            return;
                        }
                        client.getOrCreatePMChannel(userObj).sendMessage(message.asString());
                    }
                    else {
                        IChannel channelObj = client.getChannelByID(channel.asLong());
                        if (channelObj == null) {
                            errorMessage(scriptEntry.getResidingQueue(), "Channel for ID '" + channel.asLong() + "' not found.");
                            return;
                        }
                        channelObj.sendMessage(message.asString());
                    }
                });
                break;
            case ADDROLE:
                if (user == null) {
                    dB.echoError(scriptEntry.getResidingQueue(), "Failed to role: no user given!");
                    return;
                }
                if (guild == null) {
                    dB.echoError(scriptEntry.getResidingQueue(), "Failed to role: no guild given!");
                    return;
                }
                if (role == null) {
                    dB.echoError(scriptEntry.getResidingQueue(), "Failed to role: no role given!");
                    return;
                }
                if (!dDiscordBot.instance.connections.containsKey(id.asString())) {
                    dB.echoError(scriptEntry.getResidingQueue(), "Failed to role: unknown ID!");
                    return;
                }
                client = dDiscordBot.instance.connections.get(id.asString()).client;
                RequestBuffer.request(() -> {
                    IGuild iguild = client.getGuildByID(guild.asLong());
                    if (iguild == null) {
                        errorMessage(scriptEntry.getResidingQueue(), "Guild for ID '" + guild.asLong() + "' not found.");
                        return;
                    }
                    IUser userObj = client.getUserByID(user.asLong());
                    if (userObj == null) {
                        errorMessage(scriptEntry.getResidingQueue(), "User for ID '" + user.asLong() + "' not found.");
                        return;
                    }
                    IRole roleObj = iguild.getRoleByID(role.asLong());
                    if (roleObj == null) {
                        errorMessage(scriptEntry.getResidingQueue(), "Role for ID '" + role.asLong() + "' not found.");
                        return;
                    }
                    userObj.addRole(roleObj);
                });
                break;
            case REMOVEROLE:
                if (user == null) {
                    dB.echoError(scriptEntry.getResidingQueue(), "Failed to role: no user given!");
                    return;
                }
                if (guild == null) {
                    dB.echoError(scriptEntry.getResidingQueue(), "Failed to role: no guild given!");
                    return;
                }
                if (role == null) {
                    dB.echoError(scriptEntry.getResidingQueue(), "Failed to role: no role given!");
                    return;
                }
                if (!dDiscordBot.instance.connections.containsKey(id.asString())) {
                    dB.echoError(scriptEntry.getResidingQueue(), "Failed to role: unknown ID!");
                    return;
                }
                client = dDiscordBot.instance.connections.get(id.asString()).client;
                RequestBuffer.request(() -> {
                    IGuild iguild = client.getGuildByID(guild.asLong());
                    if (iguild == null) {
                        errorMessage(scriptEntry.getResidingQueue(), "Guild for ID '" + guild.asLong() + "' not found.");
                        return;
                    }
                    IUser userObj = client.getUserByID(user.asLong());
                    if (userObj == null) {
                        errorMessage(scriptEntry.getResidingQueue(), "User for ID '" + user.asLong() + "' not found.");
                        return;
                    }
                    IRole roleObj = iguild.getRoleByID(role.asLong());
                    if (roleObj == null) {
                        errorMessage(scriptEntry.getResidingQueue(), "Role for ID '" + role.asLong() + "' not found.");
                        return;
                    }
                    userObj.removeRole(roleObj);
                });
                break;
            case RENAME:
                client = dDiscordBot.instance.connections.get(id.asString()).client;
                long userId;
                if (user == null) {
                    userId = client.getOurUser().getLongID();
                }
                else {
                    userId = user.asLong();
                }
                if (guild == null) {
                    dB.echoError(scriptEntry.getResidingQueue(), "Failed to rename: no guild given!");
                    return;
                }
                if (!dDiscordBot.instance.connections.containsKey(id.asString())) {
                    dB.echoError(scriptEntry.getResidingQueue(), "Failed to rename: unknown ID!");
                    return;
                }
                if (message == null) {
                    dB.echoError(scriptEntry.getResidingQueue(), "Failed to rename: no name given!");
                    return;
                }
                RequestBuffer.request(() -> {
                    IGuild iguild = client.getGuildByID(guild.asLong());
                    if (iguild == null) {
                        errorMessage(scriptEntry.getResidingQueue(), "Guild for ID '" + guild.asLong() + "' not found.");
                        return;
                    }
                    IUser userObj = client.getUserByID(userId);
                    if (userObj == null) {
                        errorMessage(scriptEntry.getResidingQueue(), "User for ID '" + userId + "' not found.");
                        return;
                    }
                    iguild.setUserNickname(userObj, message.asString());
                });
                break;
            case STATUS:
                if (!dDiscordBot.instance.connections.containsKey(id.asString())) {
                    dB.echoError(scriptEntry.getResidingQueue(), "Failed to set status: unknown ID!");
                    return;
                }
                StatusType st = status == null ? StatusType.ONLINE : StatusType.valueOf(status.asString().toUpperCase());
                if (message == null) {
                    dDiscordBot.instance.connections.get(id.asString()).client.changePresence(st);
                    return;
                }
                ActivityType at = activity == null ? ActivityType.PLAYING : ActivityType.valueOf(activity.asString().toUpperCase());
                client = dDiscordBot.instance.connections.get(id.asString()).client;
                RequestBuffer.request(() -> {
                    client.changePresence(st, at, message.asString());
                });

                break;
        }
    }
}
