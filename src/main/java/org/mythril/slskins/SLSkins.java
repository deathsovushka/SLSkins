package org.mythril.slskins;

import net.skinsrestorer.api.SkinsRestorerAPI;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public final class SLSkins extends JavaPlugin {

    public static SkinsRestorerAPI skinsRestorerAPI;
    public static SLSkins plugin;

    public static SkinsRestorerAPI getAPI() {
        return skinsRestorerAPI;
    }

    @Override
    public void onEnable() {
        plugin = this;
        this.saveDefaultConfig();
        skinsRestorerAPI = SkinsRestorerAPI.getApi();

        Utils.connect();

        Bukkit.getPluginManager().registerEvents(new EventListener(), this);
        Objects.requireNonNull(this.getCommand("slskins")).setExecutor(new Command());
    }

    @Override
    public void onDisable() {
        Utils.disconnect();
    }
}
