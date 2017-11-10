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

public class DiscordCommand extends AbstractCommand implements Holdable {

    // <--[command]
    // @Name discord
    // @Syntax discord [id:<id>] [connect code:<botcode>/disconnect/message channel:<channel> <message>]
    // @Required 2
    // @Stable unstable
    // @Short Connects to and interacts with Discord.
    // @Author mcmonkey
    // @Plugin dDiscordBot
    // @Group external

    // @Description
    // Connects to and interacts with Discord.
    //
    // TODO: Document Command Details

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

    // -->

    public enum DiscordInstruction { CONNECT, DISCONNECT, MESSAGE }

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

        // Debug the execution
        dB.report(scriptEntry, getName(), id.debug()
                + (channel != null ? channel.debug(): "")
                + instruction.debug()
                + (message != null ? message.debug(): ""));

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
        }
    }
}
