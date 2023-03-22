package com.denizenscript.ddiscordbot;

import com.denizenscript.ddiscordbot.objects.*;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsRuntimeException;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import net.dv8tion.jda.api.requests.RestAction;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class DiscordCommandUtils {

    public static String inferBotNameNullable(Object... options) {
        DiscordBotTag bot = inferBotNullable(options);
        if (bot == null) {
            return null;
        }
        return bot.bot;
    }

    public static DiscordBotTag inferBot(Object... options) {
        DiscordBotTag bot = inferBotNullable(options);
        if (bot != null) {
            return bot;
        }
        throw new InvalidArgumentsRuntimeException("Unknown bot to use! Must specify 'id:' argument!");
    }

    public static DiscordBotTag inferBotNullable(Object[] options) {
        for (Object obj : options) {
            DiscordBotTag result = inferBotInternal(obj);
            if (result != null) {
                return result;
            }
        }
        return null;
    }

    public static DiscordBotTag inferBotInternal(Object obj) {
        if (obj instanceof DiscordBotTag botTag) {
            return botTag;
        }
        else if (obj instanceof DiscordUserTag user && user.bot != null) {
            return new DiscordBotTag(user.bot);
        }
        else if (obj instanceof DiscordRoleTag role && role.bot != null) {
            return new DiscordBotTag(role.bot);
        }
        else if (obj instanceof DiscordGroupTag group && group.bot != null) {
            return new DiscordBotTag(group.bot);
        }
        else if (obj instanceof DiscordChannelTag channel && channel.bot != null) {
            return new DiscordBotTag(channel.bot);
        }
        else if (obj instanceof List list) {
            for (Object subObj : list) {
                DiscordBotTag val = inferBotInternal(subObj);
                if (val != null) {
                    return val;
                }
            }
        }
        return null;
    }

    public static <T> RestAction<T> mapError(ScriptEntry scriptEntry, RestAction<T> action) {
        return action.onErrorMap(t -> {
            Debug.echoError(scriptEntry, t);
            scriptEntry.setFinished(true);
            return null;
        });
    }

    public static void cleanWait(ScriptEntry scriptEntry, CompletableFuture<?> action) {
        action.exceptionally(t -> {
            Debug.echoError(scriptEntry, t);
            scriptEntry.setFinished(true);
            return null;
        }).thenAccept((t) -> scriptEntry.setFinished(true));
    }

    public static void cleanWait(ScriptEntry scriptEntry, RestAction<?> action) {
        if (action == null) {
            scriptEntry.setFinished(true);
            return;
        }
        cleanWait(scriptEntry, mapError(scriptEntry, action).submit());
    }
}
