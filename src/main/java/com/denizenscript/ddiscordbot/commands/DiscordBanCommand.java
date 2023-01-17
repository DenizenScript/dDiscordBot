package com.denizenscript.ddiscordbot.commands;

import com.denizenscript.ddiscordbot.DenizenDiscordBot;
import com.denizenscript.ddiscordbot.objects.DiscordBotTag;
import com.denizenscript.ddiscordbot.objects.DiscordGroupTag;
import com.denizenscript.ddiscordbot.objects.DiscordUserTag;
import com.denizenscript.denizencore.objects.core.DurationTag;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;
import com.denizenscript.denizencore.scripts.commands.Holdable;
import com.denizenscript.denizencore.scripts.commands.generator.*;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.UserSnowflake;
import net.dv8tion.jda.api.requests.restaction.AuditableRestAction;
import org.bukkit.Bukkit;

import java.util.concurrent.TimeUnit;

public class DiscordBanCommand extends AbstractCommand implements Holdable {

    public DiscordBanCommand() {
        setName("discordban");
        setSyntax("discordban [id:<id>] ({add}/remove) [user:<user>] [group:<group>] (reason:<reason>) (deletion_timeframe:<time>/{0s})");
        setRequiredArguments(3, 6);
        isProcedural = false;
        autoCompile();
    }

    // <--[command]
    // @Name discordban
    // @Syntax discordban [id:<id>] ({add}/remove) [user:<user>] [group:<group>] (reason:<reason>) (deletion_timeframe:<time>/{0s})
    // @Required 3
    // @Maximum 6
    // @Short Bans or unbans a member from a group.
    // @Plugin dDiscordBot
    // @Guide https://guide.denizenscript.com/guides/expanding/ddiscordbot.html
    // @Group external
    //
    // @Description
    // Bans or unbans a member from a group.
    //
    // To ban a user, use the "add" argument. To unban a user, use the "remove" argument.
    // The group is required for both "add" and "remove" arguments, but "reason" can only be used with "add".
    // Reasons show up in the group's Audit Logs.
    //
    // The "deletion_timeframe" argument will, if set, delete all messages sent by the user being banned within the timeframe given.
    // The timeframe defaults to 0 seconds, which will not delete any messages. The timeframe cannot be greater than 7 days.
    // This argument can only be used when adding a ban using the "add" argument, although it is not required.
    //
    // The command should usually be ~waited for. See <@link language ~waitable>.
    //
    // @Tags
    // <DiscordUserTag.is_banned[<group>]> returns if the user is banned from a certain group.
    // <DiscordGroupTag.banned_members> returns a list of all banned members in a group.
    //
    // @Usage
    // # Bans a user with a reason and deletes all messages sent by the user in the past 2 hours.
    // - ~discordban id:mybot add user:<[user]> group:<[group]> "reason:Was being mean!" deletion_timeframe:2h
    //
    // @Usage
    // # Bans a user with but does not delete any messages sent and does not have a reason.
    // - ~discordban id:mybot add user:<[user]> group:<[group]>
    //
    // @Usage
    // # Unbans a user.
    // - ~discordban id:mybot remove user:<[user]> group:<[group]>
    // -->

    public enum DiscordBanInstruction { ADD, REMOVE }

    public static void autoExecute(ScriptEntry scriptEntry,
                                   @ArgPrefixed @ArgName("id") DiscordBotTag bot,
                                   @ArgName("instruction") @ArgDefaultText("add") DiscordBanInstruction instruction,
                                   @ArgPrefixed @ArgName("user") DiscordUserTag user,
                                   @ArgPrefixed @ArgName("group") DiscordGroupTag group,
                                   @ArgPrefixed @ArgDefaultNull @ArgName("reason") String reason,
                                   @ArgPrefixed @ArgDefaultText("0s") @ArgName("deletion_timeframe") DurationTag deletionTimeframe) {
        if (group.bot == null) {
            group = new DiscordGroupTag(bot.bot, group.guild_id);
        }
        Guild guild = group.getGuild();
        UserSnowflake userObj = UserSnowflake.fromId(user.user_id);
        Runnable runnable = () -> {
            try {
                switch (instruction) {
                    case ADD -> {
                        AuditableRestAction<Void> banAction = guild.ban(userObj, deletionTimeframe.getSecondsAsInt(), TimeUnit.SECONDS);
                        if (reason != null) {
                            banAction.reason(reason);
                        }
                        banAction.queue();
                    }
                    case REMOVE -> {
                        guild.unban(userObj).queue();
                    }
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
