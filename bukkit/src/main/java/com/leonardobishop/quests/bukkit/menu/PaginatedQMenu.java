package com.leonardobishop.quests.bukkit.menu;

import com.leonardobishop.quests.bukkit.BukkitQuestsPlugin;
import com.leonardobishop.quests.bukkit.config.BukkitQuestsConfig;
import com.leonardobishop.quests.bukkit.menu.element.CustomMenuElement;
import com.leonardobishop.quests.bukkit.menu.element.MenuElement;
import com.leonardobishop.quests.bukkit.menu.element.PageNextMenuElement;
import com.leonardobishop.quests.bukkit.menu.element.PagePrevMenuElement;
import com.leonardobishop.quests.common.player.QPlayer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

public abstract class PaginatedQMenu extends QMenu {

    protected final String title;
    protected final boolean trim;
    private final BukkitQuestsPlugin plugin;
    protected int currentPage;
    protected int pageSize;
    protected int minPage;
    protected int maxPage;

    public PaginatedQMenu(QPlayer owner, String title, boolean trim, int pageSize, BukkitQuestsPlugin plugin) {
        super(owner);
        this.title = title;
        this.trim = trim;
        this.plugin = plugin;
        this.pageSize = pageSize;
        this.currentPage = 1;
        this.minPage = 1;
        this.maxPage = 1;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = Math.max(minPage, Math.min(maxPage, currentPage));
    }

    public int getMinPage() {
        return minPage;
    }

    public void setMinPage(int minPage) {
        this.minPage = minPage;
    }

    public int getMaxPage() {
        return maxPage;
    }

    public void setMaxPage(int maxPage) {
        this.maxPage = maxPage;
    }

    public void populate(String path, List<MenuElement> questElements, MenuElement backMenuElement) {
        Player player = Bukkit.getPlayer(owner.getPlayerUUID());
        // 如果玩家不在线
        if (player == null) return;
        // 新建一个菜单元素集合
        MenuElement[] staticElement = new MenuElement[pageSize];
        int staticCount = 0;

        // 获取自定义图标配置
        if (path != null) {
            // 判断配置文件是否是Section
            if (plugin.getConfig().isConfigurationSection(path)) {
                // 获取所有键值
                for (String key : plugin.getConfig().getConfigurationSection(path).getKeys(false)) {
                    // 获取该图标需要占用的Slot
                    List<String> slots = plugin.getConfig().getStringList(path + "." + key + ".slots");
                    // 是否是动态图标(更新变量)
                    boolean isAnime = plugin.getConfig().getBoolean(path + "." + key + ".static", false);

                    MenuElement menuElement;
                    if (plugin.getConfig().contains(path + "." + key + ".display")) {
                        ItemStack itemStack = plugin.getConfiguredItemStack(path + "." + key + ".display", plugin.getConfig());
                        List<String> commands = plugin.getQuestsConfig().getStringList(path + "." + key + ".commands");
                        menuElement = new CustomMenuElement(plugin, owner.getPlayerUUID(), player.getName(), itemStack, commands);
                    } else continue;

                    for (String slotString : slots) {
                        if (Pattern.matches("[0-9]+", slotString)) {
                            int slot = Integer.parseInt(slotString);
                            if (!isAnime) {
                                staticElement[slot] = menuElement;
                                staticCount++;
                            } else {
                                menuElements.put(slot, menuElement);
                            }
                        }
                    }
                }
            }
        }
        // 获取菜单最大尺寸(index)
        // 如果存在返回按键
        // 则减去 9 (一行) 如果没有则减去 0
        int maxSize = pageSize - (backMenuElement == null ? 0 : 9);
        // 获取任务配置
        BukkitQuestsConfig config = (BukkitQuestsConfig) plugin.getQuestsConfig();

        //Bukkit.getLogger().info("1 " + (Collections.max(menuElements.keySet())) + 1);
        //Bukkit.getLogger().info("2 " + (menuElements.keySet().size() + questElements.size() + staticCount));

        // 不知道怎么改了...
        if (menuElements.keySet().size() + questElements.size() + staticCount - currentPage * maxSize > 0) {
            MenuElement pageNextMenuElement = new PageNextMenuElement(config, this);
            staticElement[52] = pageNextMenuElement;
        } else {
            MenuElement pagePrevMenuElement = new PagePrevMenuElement(config, this);
            staticElement[46] = pagePrevMenuElement;
        }
//        menuElements.isEmpty() ? 0 : Collections.max(menuElements.keySet()) + 1 > maxSize

        boolean staticMenuElementsIsFull = true;
        for (MenuElement e : staticElement) {
            if (e == null) {
                staticMenuElementsIsFull = false;
                break;
            }
        }
        if (staticMenuElementsIsFull) {
            // moving on will result in an infinite loop
            return;
        }

        // fill in the remaining menu elements into empty slots <- 这他妈的是啥??
        int slot = 0;
        for (MenuElement element : questElements) {
            fillStaticMenuElements(slot, staticElement);
            while (menuElements.containsKey(slot)) {
                slot++;
                fillStaticMenuElements(slot, staticElement);
            }
            menuElements.put(slot, element);
        }

        this.minPage = 1;
        this.maxPage = (menuElements.isEmpty() ? 0 : Collections.max(menuElements.keySet())) / pageSize + 1;
    }

    private void fillStaticMenuElements(int slot, MenuElement[] staticMenuElements) {
        // 在最大范围内
        if (slot % pageSize == 0) {
            // 遍历输入的静态菜单元素
            for (int i = 0; i < staticMenuElements.length; i++) {
                // 如果i位置的元素是null
                if (staticMenuElements[i] == null) {
                    continue;
                }
                // 将静态菜单元素放置在菜单元素位置slot+i里
                menuElements.put(slot + i, staticMenuElements[i]);
            }
        }
    }

    @Override
    public Inventory draw() {
        int pageMin = pageSize * (currentPage - 1);
        int pageMax = pageSize * currentPage;

        Inventory inventory = Bukkit.createInventory(null, 54, title);

        int highestOnPage = 0;
        for (int pointer = pageMin; pointer < pageMax; pointer++) {
            if (menuElements.containsKey(pointer)) {
                inventory.setItem(pointer - ((currentPage - 1) * pageSize), menuElements.get(pointer).asItemStack());
                if (pointer + 1 > highestOnPage) highestOnPage = pointer + 1;
            }
        }

        if (trim && currentPage == 1) {
            int inventorySize = highestOnPage + (9 - highestOnPage % 9) * Math.min(1, highestOnPage % 9);
            inventorySize = inventorySize <= 0 ? 9 : inventorySize;
            if (inventorySize == 54) {
                return inventory;
            }

            Inventory trimmedInventory = Bukkit.createInventory(null, inventorySize, title);

            for (int slot = 0; slot < trimmedInventory.getSize(); slot++) {
                trimmedInventory.setItem(slot, inventory.getItem(slot));
            }
            return trimmedInventory;
        }

        return inventory;
    }

    @Override
    public @Nullable MenuElement getMenuElementAt(int slot) {
        int pageOffset = (currentPage - 1) * pageSize;
        return super.getMenuElementAt(slot + pageOffset);
    }
}
