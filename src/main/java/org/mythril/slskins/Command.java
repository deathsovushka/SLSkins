package org.mythril.slskins;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;

public class Command implements CommandExecutor {


    private static String getSkinNameByIndex(List<String> skinNames, int index) {
        String skinName;
        try {
            skinName = skinNames.get(index);
            return skinName;
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull org.bukkit.command.Command command, @NotNull String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Использование данной команды возможно только в игре!");
            return true;
        }
        if (!sender.hasPermission("slskins")) {
            sender.sendMessage("§4Данное меню доступно для для спонсорам!");
            return true;
        }

        Player player = (Player) sender;
        String nickname = player.getName();
        int invSize = 27;

        LinkedHashMap<String, String> playerSkins = Utils.getPlayerSkins(nickname);
        List<String> skinNames = new ArrayList<>(playerSkins.keySet());

        Inventory skinsInv = Bukkit.createInventory(null, invSize, Component.text("§8Скины §7|§8 " + nickname));


        List<Component> headLore = new ArrayList<>();
        headLore.add(Component.text("§f"));
        headLore.add(Component.text("§6Установить скин: §fЛКМ"));
        headLore.add(Component.text("§6Удалить скин: §fШИФТ-ЛКМ"));

        for (int i = 0; i < invSize; i++) {

            String skinName = getSkinNameByIndex(skinNames, i);
            if (skinName == null) break;

            ItemStack playerHead = new ItemStack(Material.PLAYER_HEAD);
            ItemMeta headMeta = playerHead.getItemMeta();
            headMeta.displayName(Component.text("§e" + skinName));
            headMeta.lore(headLore);
            headMeta.getPersistentDataContainer().set(new NamespacedKey(SLSkins.plugin, "skinName"), PersistentDataType.STRING, skinName);

            String skinValue = Utils.getSkinByName(nickname, skinName, Utils.DataType.VALUE);
            GameProfile profile = new GameProfile(UUID.randomUUID(), null);
            profile.getProperties().put("textures", new Property("textures", skinValue));

            Field profileField;
            try {
                profileField = headMeta.getClass().getDeclaredField("profile");
                profileField.setAccessible(true);
                profileField.set(headMeta, profile);
            } catch (NoSuchFieldException | IllegalArgumentException | IllegalAccessException e1) {
                e1.printStackTrace();
            }

            playerHead.setItemMeta(headMeta);
            skinsInv.setItem(i, playerHead);
        }

        int empty = skinsInv.firstEmpty();

        if (empty != -1) {

            ItemStack addButton = new ItemStack(Material.PAPER);
            ItemMeta buttonMeta = addButton.getItemMeta();
            buttonMeta.setCustomModelData(39);
            buttonMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            buttonMeta.addEnchant(Enchantment.DAMAGE_ALL, 1, false);
            buttonMeta.displayName(Component.text("§2+ §8| §fСоханить текущий"));
            addButton.setItemMeta(buttonMeta);

            skinsInv.setItem(empty, addButton);
        }

        player.openInventory(skinsInv);
        return true;
    }
}
