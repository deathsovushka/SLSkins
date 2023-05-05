package org.mythril.slskins;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.skinsrestorer.api.PlayerWrapper;
import net.skinsrestorer.api.SkinVariant;
import net.skinsrestorer.api.SkinsRestorerAPI;
import net.skinsrestorer.api.exception.SkinRequestException;
import net.skinsrestorer.api.property.IProperty;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandExecutor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public final class SLSkins extends JavaPlugin implements @NotNull Listener {
    public static SkinsRestorerAPI skinsRestorerAPI;

    public static HashMap<String, String> skinlist = new HashMap<>();

    public static List<Player> list = new ArrayList<>();

    @Override
    public void onEnable() {
        loadHashMapFromFile();
        Objects.requireNonNull(this.getCommand("slskins")).setExecutor(new Command());
        Bukkit.getPluginManager().registerEvents(this, this);
        skinsRestorerAPI = SkinsRestorerAPI.getApi();
    }

    @Override
    public void onDisable() {
        saveHashMapToFile();
    }

    public static SkinsRestorerAPI getAPI(){
        return skinsRestorerAPI;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) throws SkinRequestException {
        if(list.contains((Player) event.getWhoClicked())){
            Player player = (Player) event.getWhoClicked();
            if(event.getCurrentItem() != null){
                event.setCancelled(true);
                ItemMeta itemMeta = event.getCurrentItem().getItemMeta();
                for (String string: skinlist.keySet()
                     ) {
                    if(string.contains(player.getUniqueId().toString())){
                        String[] strings = string.split("%#@#%");
                        ItemStack is = new ItemStack(Material.PLAYER_HEAD);
                        ItemMeta ItemMeta = is.getItemMeta();
                        ItemMeta.displayName(Component.text(strings[1], TextColor.color(255, 255, 255)).decoration(TextDecoration.ITALIC, false));
                        if(itemMeta.displayName().equals(ItemMeta.displayName())){
                            IProperty iProperty = getAPI().genSkinUrl(skinlist.get(string), SkinVariant.CLASSIC);
                            getAPI().applySkin(new PlayerWrapper(player), iProperty);
                            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "skin url " + event.getWhoClicked().getName() + " " + skinlist.get(string));
                            event.getWhoClicked().closeInventory();
                        }
                    }
                }
            }
        }
    }
    public static void saveHashMapToFile() {
        try {
            FileOutputStream fos = new FileOutputStream("myHashMap.ser");
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(skinlist);
            oos.close();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void loadHashMapFromFile() {
        try {
            FileInputStream fis = new FileInputStream("myHashMap.ser");
            ObjectInputStream ois = new ObjectInputStream(fis);
            skinlist = (HashMap<String, String>) ois.readObject();
            ois.close();
            fis.close();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event){
        list.remove((Player) event.getPlayer());
    }
}
