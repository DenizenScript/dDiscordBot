package com.denizenscript.ddiscordbot;

import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizencore.exceptions.CommandExecutionException;
import net.aufdemrand.denizencore.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizencore.objects.Element;
import net.aufdemrand.denizencore.objects.aH;
import net.aufdemrand.denizencore.scripts.ScriptEntry;
import net.aufdemrand.denizencore.scripts.commands.AbstractCommand;
import net.aufdemrand.denizencore.scripts.commands.Holdable;
import net.aufdemrand.denizencore.utilities.CoreUtilities;
import org.bukkit.Bukkit;
import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IRole;

import java.util.Objects;

public class DiscordCommand extends AbstractCommand implements Holdable {

    // <--[command]
    // @Name discord
    // @Syntax discord [id:<id>] [connect code:<botcode>/disconnect/message channel:<channel> <message>/role <add/remove/set> user:<usercode> role:<rolecode> guild:<guildcode>]
    // @Required 2
    // @Stable unstable
    // @Short Connects to and interacts with Discord.
    // @Author mcmonkey
    // @Plugin dDiscordBot
    // @Group external

    // @Description
    // Connects to and interacts with Discord.
    //
    // Enables interactions with Discord that allows your bot to send messages and listen in chat.
    // Useful for communicating between the server and Discord.
    // This command requires a Discord bot connected to a guild which can be created on the Discord developer page.
    // The developer page also explains on how to get your token to allow connection.
    // It is recommended to load the token, use it to connect, then unload it for security reasons. This can be done using the yaml command.
    // Specify the role argument for group management in a guild on Discord. This requires the bot to have permission for setting roles.
    // You can get role ids by typing '\@Rank' in a discord chat replacing 'Rank' with your rank name.
    // This will give a string where the role id is between '<@&' and '>'
    // Other ids can be seen by just right-clicking a user or channel and then on 'Copy ID' in developer mode.

    // @Tags
    // TODO: Make tags

    // @Usage
    // Use to connect to Discord via a bot code.
    // - discord id:mybot connect code:<def[code]>

    // @Usage
    // Use to disconnect from Discord.
    // - discord id:mybot disconnect

    // @Usage
    // Use to message a Discord channel.
    // - discord id:mybot message channel:<discord[mybot].server[Denizen].channel[bot-spam]> "Hello world!"

    // @Usage
    // Use to set a role on a user in a Discord guild.
    // - discord id:mybot role set user:123412341234 role:234523452345 guild:345634563456

    // @Usage
    // Use to add a role on a user in a Discord guild.
    // - discord id:mybot role add user:345893475890 role:324782895 guild:345763789345

    // @Usage
    // Use to remove a role on a user in a Discord guild.
    // - discord id:mybot role remove user:3426543543 role:435345436457656 guild:45756756343

    // -->

    public enum DiscordInstruction { CONNECT, DISCONNECT, MESSAGE, ROLE }

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
                IDiscordClient idc = new ClientBuilder().withToken(code).login();
                conn.client = idc;
                idc.getDispatcher().registerListener(conn);
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

    @Override
    public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {

        // Fetch required objects
        Element id = scriptEntry.getElement("id");
        Element instruction = scriptEntry.getElement("instruction");
        Element code = scriptEntry.getElement("code");
        Element channel = scriptEntry.getElement("channel");
        Element message = scriptEntry.getElement("message");
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
                if (channel == null) {
                    dB.echoError(scriptEntry.getResidingQueue(), "Failed to message: no channel given!");
                    return;
                }
                if (message == null) {
                    dB.echoError(scriptEntry.getResidingQueue(), "Failed to message: no message given!");
                    return;
                }
                if (!dDiscordBot.instance.connections.containsKey(id.asString())) {
                    dB.echoError(scriptEntry.getResidingQueue(), "Failed to message: unknown ID!");
                    return;
                }
                dDiscordBot.instance.connections.get(id.asString()).client.getChannelByID(channel.asLong()).sendMessage(message.asString());
                break;
            case ROLE:
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
                if (message == null) {
                    dB.echoError(scriptEntry.getResidingQueue(), "Failed to role: no action given!");
                    return;
                }
                if (!dDiscordBot.instance.connections.containsKey(id.asString())) {
                    dB.echoError(scriptEntry.getResidingQueue(), "Failed to role: unknown ID!");
                    return;
                }
                IDiscordClient client_d = dDiscordBot.instance.connections.get(id.asString()).client;
                IGuild guild_d = client_d.getGuildByID(guild.asLong());
                switch (message.toString().toLowerCase()) {
                    case "remove":
                        client_d.getUserByID(user.asLong()).removeRole(guild_d.getRoleByID(role.asLong()));
                        break;
                    case "add":
                        client_d.getUserByID(user.asLong()).addRole(guild_d.getRoleByID(role.asLong()));
                        break;
                    case "set":
                        guild_d.editUserRoles(client_d.getUserByID(user.asLong()), new IRole[]{guild_d.getRoleByID(role.asLong())});
                        break;
                    default:
                        dB.echoError(scriptEntry.getResidingQueue(), "Failed to role: Invalid action given - " + message.toString());
                        return;
                }
                break;

        }
    }
}
