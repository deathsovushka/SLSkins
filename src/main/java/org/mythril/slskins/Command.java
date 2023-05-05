package org.mythril.slskins;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.skinsrestorer.api.SkinsRestorerAPI;
import net.skinsrestorer.api.property.IProperty;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class Command implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, org.bukkit.command.@NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if(commandSender instanceof Player){
            if(!(commandSender.hasPermission("skiner.use"))) return true;
            Player p = (Player) commandSender;
            SkinsRestorerAPI api = SLSkins.getAPI();
            if(strings.length == 0){
                p.sendMessage("/slskins add <Название> - Загрузка текущего скина в библиотеку");
                p.sendMessage("/slskins remove <Название> - Удаление скина с этим названием из библиотеки");
                p.sendMessage("/slskins menu - Библиотека ваших скинов");
                return true;
            }
            if(strings[0].equals("add")){
                if(!(strings[1].isEmpty())){
                    IProperty skin = api.getSkinData(api.getSkinName(p.getName()));
                    if(skin != null){
                        String url = api.getSkinTextureUrl(skin);
                        if(!(SLSkins.skinlist.containsKey(p.getUniqueId() + "%#@#%" + strings[1]))){
                            SLSkins.skinlist.put(p.getUniqueId() + "%#@#%" + strings[1], url);
                            p.sendMessage("Скин добавлен!");
                        } else {
                            p.sendMessage("У вас уже есть скин с таким названием!");
                        }
                    }
                } else {
                    p.sendMessage("Используйте /slskins add <Название> для добавления вашего текущего скина");
                }
            } else if(strings[0].equals("menu")){
                boolean container = false;
                int index = 0;
                Inventory inventory = Bukkit.createInventory(null, 54, "Библиотека скинов");
                for (String string: SLSkins.skinlist.keySet()
                     ) {
                    if(string.contains(p.getUniqueId().toString())){
                        if(index > 52){
                            continue;
                        }
                        String[] strings1 = string.split("%#@#%");
                        container = true;
                        ItemStack itemstack = new ItemStack(Material.PLAYER_HEAD);
                        ItemMeta ItemMeta = itemstack.getItemMeta();
                        ItemMeta.displayName(Component.text(strings1[1], TextColor.color(255, 255, 255)).decoration(TextDecoration.ITALIC, false));
                        List<Component> lore = new ArrayList<>();
                        lore.add(Component.text(""));
                        lore.add(Component.text("  Нажмите для активации этого скина!"));
                        lore.add(Component.text(""));
                        ItemMeta.lore(lore);
                        ItemMeta.setCustomModelData(index);
                        itemstack.setItemMeta(ItemMeta);
                        inventory.setItem(index, itemstack);
                        index++;
                    }
                }
                if(!(container)){
                    p.sendMessage("У вас нет скинов, используйте /slskins add <Название> для добавления вашего текущего скина");
                } else {
                    SLSkins.list.add(p);
                    p.openInventory(inventory);
                }
            } else if(strings[0].equals("remove")){
                if(strings.length < 2){
                    p.sendMessage("Вы забыли указать название скина для удаления");
                    return true;
                }
                if(SLSkins.skinlist.containsKey(p.getUniqueId() + "%#@#%" + strings[1])){
                    SLSkins.skinlist.remove(p.getUniqueId() + "%#@#%" + strings[1]);
                    p.sendMessage("Скин удалён!");
                } else {
                    p.sendMessage("У вас нет скина с таким названием!");
                }
            } else {
                p.sendMessage("/slskins add <Название> - Загрузка текущего скина в библиотеку");
                p.sendMessage("/slskins remove <Название> - Удаление скина с этим названием из библиотеки");
                p.sendMessage("/slskins menu - Библиотека ваших скинов");
                return true;
            }
        } else {
            commandSender.sendMessage("Доступно только Спонсорам!");
        }
        return true;
    }
}
