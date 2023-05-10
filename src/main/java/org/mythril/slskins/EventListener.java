package org.mythril.slskins;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.skinsrestorer.api.PlayerWrapper;
import net.skinsrestorer.api.exception.SkinRequestException;
import net.skinsrestorer.api.property.IProperty;
import net.wesjd.anvilgui.AnvilGUI;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.UUID;

public class EventListener implements Listener {

    @EventHandler
    public static void onSkinsGUI(InventoryClickEvent event) {
        String invTitle;

        try {
            invTitle = ((TextComponent) event.getView().title()).content();
        } catch (Exception e) {
            return;
        }

        if (!invTitle.contains("Скины")) return;
        onButtonClick(event);

    }

    private static void onButtonClick(InventoryClickEvent event) {
        event.setCancelled(true);

        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null) return;

        Player player = (Player) event.getWhoClicked();
        String nickname = player.getName();
        String skinName;
        String skinValue;
        String skinSignature;

        switch (clickedItem.getType()) {
            case PLAYER_HEAD:
                ClickType clickType = event.getClick();

                ItemMeta itemMeta = clickedItem.getItemMeta();
                if (itemMeta == null) return;

                skinName = itemMeta.getPersistentDataContainer().get(new NamespacedKey(SLSkins.plugin, "skinName"), PersistentDataType.STRING);

                if (clickType == ClickType.LEFT) {
                    skinValue = Utils.getSkinByName(nickname, skinName, Utils.DataType.VALUE);
                    skinSignature = Utils.getSkinByName(nickname, skinName, Utils.DataType.SIGNATURE);

                    try {
                        IProperty skin = SLSkins.getAPI().createPlatformProperty(nickname, skinValue, skinSignature);
                        SLSkins.getAPI().setSkinData(nickname, skin);
                        SLSkins.getAPI().setSkin(nickname, nickname);
                        SLSkins.getAPI().applySkin(new PlayerWrapper(player), skin);

                    } catch (SkinRequestException e) {
                        throw new RuntimeException(e);
                    }
                    player.sendMessage("§2Скин успешно изменён!");
                    break;
                }

                if (clickType == ClickType.SHIFT_LEFT) {
                    Utils.removeSkinByName(nickname, skinName);
                    player.sendMessage("§eСкин §6" + skinName + " §eуспешно удалён!");
                    player.closeInventory();
                    break;
                }


            case PAPER:
                skinName = SLSkins.getAPI().getSkinName(nickname);

                try {
                    IProperty skinData = SLSkins.getAPI().getSkinData(skinName);
                    if (skinData == null) {
                        player.sendMessage(Component.text("§4Для сохранения скина вам нужно его установить!"));
                        return;
                    }

                    skinValue = skinData.getValue();
                    skinSignature = skinData.getSignature();

                } catch (Exception e) {
                    player.sendMessage(Component.text("§4Для сохранения скина вам нужно его установить!"));
                    return;
                }

                player.playSound(player, Sound.BLOCK_BONE_BLOCK_HIT, 1, 1);
                ItemStack playerHead = new ItemStack(Material.PLAYER_HEAD);
                ItemMeta headMeta = playerHead.getItemMeta();
                headMeta.displayName(Component.text(nickname));
                GameProfile profile = new GameProfile(UUID.randomUUID(), null);
                profile.getProperties().put("textures", new Property("textures", skinValue));
                Field profileField;

                try {
                    profileField = headMeta.getClass().getDeclaredField("profile");
                    profileField.setAccessible(true);
                    profileField.set(headMeta, profile);
                    playerHead.setItemMeta(headMeta);
                } catch (NoSuchFieldException | IllegalArgumentException | IllegalAccessException e1) {
                    e1.printStackTrace();
                }

                new AnvilGUI.Builder().onClick((slot, stateSnapshot) -> {
                    if (slot == AnvilGUI.Slot.OUTPUT) {
                        String newSkinName = stateSnapshot.getText();

                        Utils.saveSkinByName(nickname, newSkinName, skinValue, skinSignature);
                        player.sendMessage("§2Скин с названием §e" + newSkinName + " §2успешно сохранён!");
                        return Collections.singletonList(AnvilGUI.ResponseAction.close());
                    }
                    return Collections.emptyList();
                }).text(nickname).itemLeft(playerHead).title("§2Название скина:").plugin(SLSkins.plugin).open(player);
        }
    }
}
