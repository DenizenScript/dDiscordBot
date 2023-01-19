package com.denizenscript.ddiscordbot;

import com.denizenscript.ddiscordbot.objects.*;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsRuntimeException;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import net.dv8tion.jda.api.requests.RestAction;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public class DiscordCommandUtils {

    public static DiscordBotTag inferBot(Object... options) {
        for (Object obj : options) {
            DiscordBotTag result = inferBotInternal(obj);
            if (result != null) {
                return result;
            }
        }
        throw new InvalidArgumentsRuntimeException("Unknown bot to use! Must specify 'id:' argument!");
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

    public static void cleanWait(ScriptEntry scriptEntry, ActionOrValue<?> action) {
        action.onErrorFlatMap(t -> {
            Debug.echoError(scriptEntry, t);
            scriptEntry.setFinished(true);
            return null;
        }).onSuccess((t) -> scriptEntry.setFinished(true)).queue();
    }

    public static void cleanWait(ScriptEntry scriptEntry, RestAction<?> action) {
        if (action == null) {
            scriptEntry.setFinished(true);
            return;
        }
        cleanWait(scriptEntry, new ActionOrValue<>(action));
    }

    public static class ActionOrValue<T> {
        public T raw;
        public RestAction<T> action;
        public ActionOrValue(T raw) {
            this.raw = raw;
        }
        public ActionOrValue(RestAction<T> action) {
            this.action = action;
        }
        public ActionOrValue<T> onErrorFlatMap(Function<Throwable, RestAction<T>> function) {
            if (action != null) {
                return new ActionOrValue<T>(action.onErrorFlatMap(function));
            }
            if (raw == null) {
                return new ActionOrValue<T>(function.apply(null));
            }
            return this;
        }
        public <O> ActionOrValue<O> flatMap(Function<T, RestAction<O>> function) {
            if (raw != null) {
                return new ActionOrValue<>(function.apply(raw));
            }
            else {
                return new ActionOrValue<>(action.flatMap(function));
            }
        }
        public ActionOrValue<T> onSuccess(Consumer<T> success) {
            if (action != null) {
                return new ActionOrValue<>(action.onSuccess(success));
            }
            success.accept(raw);
            return this;
        }
        public void queue() {
            if (action != null) {
                action.queue();
            }
        }
    }
}
