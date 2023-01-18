package com.denizenscript.ddiscordbot.commands;

import com.denizenscript.ddiscordbot.DenizenDiscordBot;
import com.denizenscript.ddiscordbot.objects.DiscordBotTag;
import com.denizenscript.ddiscordbot.objects.DiscordGroupTag;
import com.denizenscript.ddiscordbot.objects.DiscordUserTag;
import com.denizenscript.denizencore.objects.core.DurationTag;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;
import com.denizenscript.denizencore.scripts.commands.Holdable;
import com.denizenscript.denizencore.scripts.commands.generator.ArgDefaultNull;
import com.denizenscript.denizencore.scripts.commands.generator.ArgDefaultText;
import com.denizenscript.denizencore.scripts.commands.generator.ArgName;
import com.denizenscript.denizencore.scripts.commands.generator.ArgPrefixed;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.requests.restaction.AuditableRestAction;
import org.bukkit.Bukkit;

import java.util.concurrent.TimeUnit;

public class DiscordTimeoutCommand extends AbstractCommand implements Holdable {

    public DiscordTimeoutCommand() {
        setName("discordtimeout");
        setSyntax("discordtimeout [id:<id>] ({add}/remove) [user:<user>] [group:<group>] (reason:<reason>) (duration:<duration>/{60s})");
        setRequiredArguments(3, 6);
        isProcedural = false;
        autoCompile();
    }

    // <--[command]
    // @Name discordtimeout
    // @Syntax discordtimeout [id:<id>] ({add}/remove) [user:<user>] [group:<group>] (reason:<reason>) (duration:<duration>/{60s})
    // @Required 3
    // @Maximum 6
    // @Short Puts a user in timeout.
    // @Plugin dDiscordBot
    // @Guide https://guide.denizenscript.com/guides/expanding/ddiscordbot.html
    // @Group external
    //
    // @Description
    // Puts a user in timeout.
    //
    // To put a user in timeout, use the "add" argument. To remove the timeout, use the "remove" argument.
    // The group is required for both "add" and "remove" arguments, but "reason" can only be used with "add".
    // Reasons show up in the group's Audit Logs.
    //
    // The timeout duration defaults to 60 seconds. The duration cannot be greater than 28 days.
    // This argument can only be used when putting a user in timeout using the "add" argument, although it is not required.
    //
    // The command should usually be ~waited for. See <@link language ~waitable>.
    //
    // @Tags
    // <DiscordUserTag.is_timed_out[<group>]> returns if the user is timed out in a certain group.
    //
    // @Usage
    // # Put a user in timeout.
    // - ~discordtimeout id:my_bot add user:<[user]> group:<[group]>
    //
    // @Usage
    // # Put a user in timeout for a duration of 3 hours with a reason.
    // - ~discordtimeout id:my_bot add user:<[user]> group:<[group]> "reason:Was being troublesome!" duration:3h
    //
    // @Usage
    // # Remove a user from timeout.
    // - ~discordtimeout id:my_bot remove user:<[user]> group:<[group]>
    // -->

    public enum DiscordTimeoutInstruction { ADD, REMOVE }

    public static void autoExecute(ScriptEntry scriptEntry,
                                   @ArgName("id") @ArgPrefixed DiscordBotTag bot,
                                   @ArgName("instruction") @ArgDefaultText("add") DiscordTimeoutInstruction instruction,
                                   @ArgName("user") @ArgPrefixed DiscordUserTag user,
                                   @ArgName("group") @ArgPrefixed DiscordGroupTag group,
                                   @ArgName("reason") @ArgPrefixed @ArgDefaultNull String reason,
                                   @ArgName("duration") @ArgPrefixed @ArgDefaultText("60s") DurationTag duration) {
        if (group.bot == null) {
            group = new DiscordGroupTag(bot.bot, group.guild_id);
        }
        Guild guild = group.getGuild();
        Member member = guild.getMemberById(user.user_id);
        if (member == null) {
            Debug.echoError("Invalid user! Are they in the Discord Group?");
        }
        Runnable runnable = () -> {
            try {
                switch (instruction) {
                    case ADD -> {
                        AuditableRestAction<Void> timeoutAction = member.timeoutFor(duration.getSecondsAsInt(), TimeUnit.SECONDS);
                        if (reason != null) {
                            timeoutAction.reason(reason);
                        }
                        timeoutAction.queue();
                    }
                    case REMOVE -> member.removeTimeout().queue();
                }
            }
            catch (Exception ex) {
                Debug.echoError(scriptEntry, ex);
            }
        };
        Bukkit.getScheduler().runTaskAsynchronously(DenizenDiscordBot.instance, () -> {
            runnable.run();
            scriptEntry.setFinished(true);
        });
    }
}
