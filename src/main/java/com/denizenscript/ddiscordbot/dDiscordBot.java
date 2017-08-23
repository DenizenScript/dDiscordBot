package com.denizenscript.ddiscordbot;

import net.aufdemrand.denizen.utilities.debugging.dB;
import org.bukkit.plugin.java.JavaPlugin;

public class dDiscordBot extends JavaPlugin {

    public static dDiscordBot instance;

    @Override
    public void onEnable() {
        dB.log("dDiscordBot loaded!");
        instance = this;
    }
}
