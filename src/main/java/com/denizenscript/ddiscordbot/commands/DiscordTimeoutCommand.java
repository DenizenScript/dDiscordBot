package com.denizenscript.ddiscordbot.commands;

import com.denizenscript.ddiscordbot.DiscordCommandUtils;
import com.denizenscript.ddiscordbot.objects.DiscordBotTag;
import com.denizenscript.ddiscordbot.objects.DiscordGroupTag;
import com.denizenscript.ddiscordbot.objects.DiscordUserTag;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsRuntimeException;
import com.denizenscript.denizencore.objects.core.DurationTag;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;
import com.denizenscript.denizencore.scripts.commands.Holdable;
import com.denizenscript.denizencore.scripts.commands.generator.ArgDefaultNull;
import com.denizenscript.denizencore.scripts.commands.generator.ArgDefaultText;
import com.denizenscript.denizencore.scripts.commands.generator.ArgName;
import com.denizenscript.denizencore.scripts.commands.generator.ArgPrefixed;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.requests.restaction.AuditableRestAction;

import java.util.concurrent.TimeUnit;

public class DiscordTimeoutCommand extends AbstractCommand implements Holdable {

    public DiscordTimeoutCommand() {
        setName("discordtimeout");
        setSyntax("discordtimeout (id:<id>) ({add}/remove) [user:<user>] [group:<group>] (reason:<reason>) (duration:<duration>/{60s})");
        setRequiredArguments(2, 6);
        isProcedural = false;
        autoCompile();
    }

    // <--[command]
    // @Name discordtimeout
    // @Syntax discordtimeout (id:<bot>) ({add}/remove) [user:<user>] [group:<group>] (reason:<reason>) (duration:<duration>/{60s})
    // @Required 2
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
    // The command can be ~waited for. See <@link language ~waitable>.
    //
    // @Tags
    // <DiscordUserTag.is_timed_out[<group>]> returns if the user is timed out in a certain group.
    //
    // @Usage
    // # Put a user in timeout.
    // - discordtimeout id:my_bot add user:<[user]> group:<[group]>
    //
    // @Usage
    // # Put a user in timeout for a duration of 3 hours with a reason.
    // - discordtimeout id:my_bot add user:<[user]> group:<[group]> "reason:Was being troublesome!" duration:3h
    //
    // @Usage
    // # Remove a user from timeout.
    // - discordtimeout id:my_bot remove user:<[user]> group:<[group]>
    // -->

    public enum DiscordTimeoutInstruction { ADD, REMOVE }

    public static void autoExecute(ScriptEntry scriptEntry,
                                   @ArgName("id") @ArgPrefixed @ArgDefaultNull DiscordBotTag bot,
                                   @ArgName("instruction") @ArgDefaultText("add") DiscordTimeoutInstruction instruction,
                                   @ArgName("user") @ArgPrefixed DiscordUserTag user,
                                   @ArgName("group") @ArgPrefixed DiscordGroupTag group,
                                   @ArgName("reason") @ArgPrefixed @ArgDefaultNull String reason,
                                   @ArgName("duration") @ArgPrefixed @ArgDefaultText("60s") DurationTag duration) {
        bot = DiscordCommandUtils.inferBot(bot, user, group);
        if (group.bot == null) {
            group = new DiscordGroupTag(bot.bot, group.guild_id);
        }
        Member member = group.getGuild().getMemberById(user.user_id);
        if (member == null) {
            throw new InvalidArgumentsRuntimeException("Invalid user! Are they in the Discord Group?");
        }
        DiscordCommandUtils.cleanWait(scriptEntry, switch (instruction) {
            case ADD -> {
                AuditableRestAction<Void> timeoutAction = member.timeoutFor(duration.getSecondsAsInt(), TimeUnit.SECONDS);
                if (reason != null) {
                    timeoutAction = timeoutAction.reason(reason);
                }
                yield timeoutAction;
            }
            case REMOVE -> member.removeTimeout();
        });
    }
}
