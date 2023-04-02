package com.leonardobishop.quests.bukkit.tasktype.type.external.mmoitems;

import com.leonardobishop.quests.bukkit.BukkitQuestsPlugin;
import com.leonardobishop.quests.bukkit.tasktype.BukkitTaskType;
import com.leonardobishop.quests.bukkit.util.TaskUtils;
import com.leonardobishop.quests.common.player.QPlayer;
import com.leonardobishop.quests.common.player.questprogressfile.TaskProgress;
import com.leonardobishop.quests.common.quest.Quest;
import com.leonardobishop.quests.common.quest.Task;
import io.lumine.mythic.lib.api.item.NBTItem;
import net.Indyuce.mmoitems.api.crafting.recipe.CraftingRecipe;
import net.Indyuce.mmoitems.api.crafting.recipe.Recipe;
import net.Indyuce.mmoitems.api.event.CraftMMOItemEvent;
import net.Indyuce.mmoitems.api.event.PlayerUseCraftingStationEvent;
import net.Indyuce.mmoitems.api.event.PlayerUseCraftingStationEvent.StationAction;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.inventory.ItemStack;

public class Craft extends BukkitTaskType {
    private final BukkitQuestsPlugin plugin;

    public Craft(BukkitQuestsPlugin plugin) {
        super("mmoitems_craft", TaskUtils.TASK_ATTRIBUTION_STRING, "Place down a set of minions.");
        this.plugin = plugin;
        super.addConfigValidator(TaskUtils.useRequiredConfigValidator(this, "mmoitems-type"));
        super.addConfigValidator(TaskUtils.useRequiredConfigValidator(this, "amount"));
        super.addConfigValidator(TaskUtils.useIntegerConfigValidator(this, "amount"));
    }

    /**
     * 原版合成事件
     *
     * @param event 合成事件
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onItemCraft(CraftItemEvent event) {
        ItemStack item = event.getRecipe().getResult();
        // 如果不是MMOItems的物品则跳出
        if (isNotMMOItem(item)) return;
        HumanEntity humanEntity = event.getWhoClicked();
        if (!(humanEntity instanceof Player player)) return;
        QPlayer qPlayer = plugin.getPlayerManager().getPlayer(player.getUniqueId());
        if (qPlayer == null) return;
        // 遍历玩家所有激活的任务
        for (TaskUtils.PendingTask pendingTask : TaskUtils.getApplicableTasks(player, qPlayer, this)) {
            Quest quest = pendingTask.quest();
            Task task = pendingTask.task();
            TaskProgress taskProgress = pendingTask.taskProgress();
            // Debug 玩家放置信息
            super.debug("Player craft mmoitems", quest.getId(), task.getId(), player.getUniqueId());

            // 判断物品Type
            Object typeObj = task.getConfigValue("mmoitems-type");
            String type = "";
            if (typeObj instanceof String) type = (String) typeObj;
            if (!type.equalsIgnoreCase(NBTItem.get(item).getType())) {
                super.debug("MMOItems type ('" + NBTItem.get(item).getType() + "') does not match required type, continuing...", quest.getId(), task.getId(), player.getUniqueId());
                continue;
            }
            // 判断物品ID 如果config里的配置获取的是空值则跳过该判断
            Object idObj = task.getConfigValue("mmoitems-id");
            String id = "";
            if (idObj instanceof String) id = (String) idObj;
            if (!id.equals("")) {
                if (!id.equalsIgnoreCase(NBTItem.get(item).getString("MMOITEMS_ITEM_ID"))) {
                    super.debug("MMOItems id ('" + NBTItem.get(item).getString("MMOITEMS_ITEM_ID") + "') does not match required type, continuing...", quest.getId(), task.getId(), player.getUniqueId());
                    continue;
                }
            }


            Object amountObj = task.getConfigValue("amount");
            int amount = amountObj == null ? item.getAmount() : (int) amountObj;

            int curProgress = TaskUtils.getIntegerTaskProgress(taskProgress);
            int newProgress = curProgress + item.getAmount();

            taskProgress.setProgress(newProgress);

            super.debug("Updating task progress (now " + newProgress + ")", quest.getId(), task.getId(), player.getUniqueId());

            if (newProgress >= amount) {
                super.debug("Marking task as complete", quest.getId(), task.getId(), player.getUniqueId());
                taskProgress.setProgress(amount);
                taskProgress.setCompleted(true);
            }
        }
    }

    /**
     * 多物品的合成
     * This event is called by MMOItems "recipe-amounts" crafting system.
     *
     * @param event 合成事件
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onItemCraft(CraftMMOItemEvent event) {
        ItemStack item = event.getResult();
        // 如果不是MMOItems的物品则跳出
        if (isNotMMOItem(item)) return;
        Player player = event.getPlayer();
        QPlayer qPlayer = plugin.getPlayerManager().getPlayer(player.getUniqueId());
        if (qPlayer == null) return;
        // 遍历玩家所有激活的任务
        for (TaskUtils.PendingTask pendingTask : TaskUtils.getApplicableTasks(player, qPlayer, this)) {
            Quest quest = pendingTask.quest();
            Task task = pendingTask.task();
            TaskProgress taskProgress = pendingTask.taskProgress();
            // Debug 玩家放置信息
            super.debug("Player craft mmoitems", quest.getId(), task.getId(), player.getUniqueId());

            // 判断物品Type
            Object typeObj = task.getConfigValue("mmoitems-type");
            String type = "";
            if (typeObj instanceof String) type = (String) typeObj;
            if (!type.equalsIgnoreCase(NBTItem.get(item).getType())) {
                super.debug("MMOItems type ('" + NBTItem.get(item).getType() + "') does not match required type, continuing...", quest.getId(), task.getId(), player.getUniqueId());
                continue;
            }
            // 判断物品ID 如果config里的配置获取的是空值则跳过该判断
            Object idObj = task.getConfigValue("mmoitems-id");
            String id = "";
            if (idObj instanceof String) id = (String) idObj;
            if (!id.equals("")) {
                if (!id.equalsIgnoreCase(NBTItem.get(item).getString("MMOITEMS_ITEM_ID"))) {
                    super.debug("MMOItems id ('" + NBTItem.get(item).getString("MMOITEMS_ITEM_ID") + "') does not match required type, continuing...", quest.getId(), task.getId(), player.getUniqueId());
                    continue;
                }
            }


            Object amountObj = task.getConfigValue("amount");
            int amount = amountObj == null ? item.getAmount() : (int) amountObj;

            int curProgress = TaskUtils.getIntegerTaskProgress(taskProgress);
            int newProgress = curProgress + item.getAmount();

            taskProgress.setProgress(newProgress);

            super.debug("Updating task progress (now " + newProgress + ")", quest.getId(), task.getId(), player.getUniqueId());

            if (newProgress >= amount) {
                super.debug("Marking task as complete", quest.getId(), task.getId(), player.getUniqueId());
                taskProgress.setProgress(amount);
                taskProgress.setCompleted(true);
            }
        }
    }

    /**
     * 使用合成GUI
     *
     * @param event 合成事件
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onRecipeUse(PlayerUseCraftingStationEvent event) {
        StationAction action = event.getInteraction();
        // 如果是取消动作或者队列动作跳出
        if (action != StationAction.CANCEL_QUEUE && action != StationAction.CRAFTING_QUEUE) return;
        Recipe recipe = event.getRecipe();
        // 如果不是合成的配方跳出
        if (!(recipe instanceof CraftingRecipe)) return;
        ItemStack item = event.getResult();
        // 如果不是MMOItems的物品则跳出
        if (isNotMMOItem(item)) return;
        Player player = event.getPlayer();
        QPlayer qPlayer = plugin.getPlayerManager().getPlayer(player.getUniqueId());
        if (qPlayer == null) return;
        // 遍历玩家所有激活的任务
        for (TaskUtils.PendingTask pendingTask : TaskUtils.getApplicableTasks(player, qPlayer, this)) {
            Quest quest = pendingTask.quest();
            Task task = pendingTask.task();
            TaskProgress taskProgress = pendingTask.taskProgress();
            // Debug 玩家放置信息
            super.debug("Player craft mmoitems", quest.getId(), task.getId(), player.getUniqueId());

            // 判断物品Type
            Object typeObj = task.getConfigValue("mmoitems-type");
            String type = "";
            if (typeObj instanceof String) type = (String) typeObj;

            if (!type.equalsIgnoreCase(NBTItem.get(item).getType())) {
                super.debug("MMOItems type ('" + NBTItem.get(item).getType() + "') does not match required type, continuing...", quest.getId(), task.getId(), player.getUniqueId());
                continue;
            }
            // 判断物品ID 如果config里的配置获取的是空值则跳过该判断
            Object idObj = task.getConfigValue("mmoitems-id");
            String id = "";
            if (idObj instanceof String) id = (String) idObj;
            if (!id.equals("")) {
                if (!id.equalsIgnoreCase(NBTItem.get(item).getString("MMOITEMS_ITEM_ID"))) {
                    super.debug("MMOItems id ('" + NBTItem.get(item).getString("MMOITEMS_ITEM_ID") + "') does not match required type, continuing...", quest.getId(), task.getId(), player.getUniqueId());
                    continue;
                }
            }


            Object amountObj = task.getConfigValue("amount");
            int amount = amountObj == null ? item.getAmount() : (int) amountObj;

            int curProgress = TaskUtils.getIntegerTaskProgress(taskProgress);
            int newProgress = curProgress + item.getAmount();

            taskProgress.setProgress(newProgress);

            super.debug("Updating task progress (now " + newProgress + ")", quest.getId(), task.getId(), player.getUniqueId());

            if (newProgress >= amount) {
                super.debug("Marking task as complete", quest.getId(), task.getId(), player.getUniqueId());
                taskProgress.setProgress(amount);
                taskProgress.setCompleted(true);
            }
        }
    }

    /**
     * 判断目标物品是否是MMOItems的物品
     *
     * @param item 物品
     * @return 是否是MMOItems的物品
     */
    private boolean isNotMMOItem(ItemStack item) {
        final NBTItem nbtItem = NBTItem.get(item);
        if (nbtItem == null) return true;
        return (nbtItem.getString("MMOITEMS_ITEM_TYPE") == null || nbtItem.getString("MMOITEMS_ITEM_ID") == null);
    }


}
