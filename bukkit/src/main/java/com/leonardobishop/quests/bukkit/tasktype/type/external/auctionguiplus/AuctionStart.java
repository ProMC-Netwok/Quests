package com.leonardobishop.quests.bukkit.tasktype.type.external.auctionguiplus;

import com.leonardobishop.quests.bukkit.BukkitQuestsPlugin;
import com.leonardobishop.quests.bukkit.tasktype.BukkitTaskType;
import com.leonardobishop.quests.bukkit.util.TaskUtils;
import com.leonardobishop.quests.common.player.QPlayer;
import com.leonardobishop.quests.common.player.questprogressfile.TaskProgress;
import com.leonardobishop.quests.common.quest.Quest;
import com.leonardobishop.quests.common.quest.Task;
import net.brcdev.auctiongui.event.AuctionStartEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

public class AuctionStart extends BukkitTaskType {
    private final BukkitQuestsPlugin plugin;

    public AuctionStart(BukkitQuestsPlugin plugin) {
        super("auctionguiplus_start", TaskUtils.TASK_ATTRIBUTION_STRING, "AuctionGUI+ player start auction.");
        this.plugin = plugin;
        super.addConfigValidator(TaskUtils.useRequiredConfigValidator(this, "amount"));
    }

    @EventHandler
    public void onAuctionStart(AuctionStartEvent event) {
        QPlayer qPlayer = plugin.getPlayerManager().getPlayer(event.getPlayer().getUniqueId());
        // 检查玩家数据
        if (qPlayer == null) return;
        // 获取玩家实例
        Player player = event.getPlayer();
        // 遍历玩家所有激活的任务
        for (TaskUtils.PendingTask pendingTask : TaskUtils.getApplicableTasks(player, qPlayer, this)) {
            Quest quest = pendingTask.quest();
            Task task = pendingTask.task();
            TaskProgress taskProgress = pendingTask.taskProgress();
            // Debug 玩家放置信息
            super.debug("Player start auction", quest.getId(), task.getId(), player.getUniqueId());

            Object amountObj = task.getConfigValue("amount");
            int amount = amountObj == null ? event.getAuction().getItemStack().getAmount() : (int) amountObj;

            int curProgress = TaskUtils.getIntegerTaskProgress(taskProgress);
            int newProgress = curProgress + event.getAuction().getItemStack().getAmount();

            taskProgress.setProgress(newProgress);

            super.debug("Updating task progress (now " + newProgress + ")", quest.getId(), task.getId(), player.getUniqueId());

            if (newProgress >= amount) {
                super.debug("Marking task as complete", quest.getId(), task.getId(), player.getUniqueId());
                taskProgress.setProgress(amount);
                taskProgress.setCompleted(true);
            }
        }


    }
}
