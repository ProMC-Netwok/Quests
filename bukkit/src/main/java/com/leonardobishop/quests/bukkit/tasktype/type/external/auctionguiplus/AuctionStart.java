package com.leonardobishop.quests.bukkit.tasktype.type.external.auctionguiplus;

import com.leonardobishop.quests.bukkit.BukkitQuestsPlugin;
import com.leonardobishop.quests.bukkit.tasktype.BukkitTaskType;
import com.leonardobishop.quests.bukkit.util.TaskUtils;
import com.leonardobishop.quests.common.player.QPlayer;
import com.leonardobishop.quests.common.player.questprogressfile.TaskProgress;
import com.leonardobishop.quests.common.quest.Task;
import net.brcdev.auctiongui.event.AuctionPostStartEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

public class AuctionStart extends BukkitTaskType {
    private final BukkitQuestsPlugin plugin;

    public AuctionStart(BukkitQuestsPlugin plugin) {
        super("auctionguiplus_start", TaskUtils.TASK_ATTRIBUTION_STRING, "AuctionGUI+ player start auction.");
        this.plugin = plugin;
        super.addConfigValidator(TaskUtils.useRequiredConfigValidator(this, "amount"));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onAuctionStart(AuctionPostStartEvent event) {
        Player player = event.getPlayer();
        QPlayer qPlayer = plugin.getPlayerManager().getPlayer(player.getUniqueId());
        if (qPlayer == null) return;
        for (TaskUtils.PendingTask pendingTask : TaskUtils.getApplicableTasks(player, qPlayer, this)) {
            Task task = pendingTask.task();
            TaskProgress taskProgress = pendingTask.taskProgress();

            //super.debug("Player start auction", quest.getId(), task.getId(), player.getUniqueId());

            Object amountObj = task.getConfigValue("amount");
            int amount = amountObj == null ? 1 : (int) amountObj;

            int curProgress = TaskUtils.getIntegerTaskProgress(taskProgress);
            int newProgress = curProgress + event.getAuction().getItemStack().getAmount();

            taskProgress.setProgress(newProgress);

            //super.debug("Updating task progress (now " + newProgress + ")", quest.getId(), task.getId(), player.getUniqueId());

            if (newProgress >= amount) {
                //super.debug("Marking task as complete", quest.getId(), task.getId(), player.getUniqueId());
                taskProgress.setProgress(amount);
                taskProgress.setCompleted(true);
            }
        }
    }
}
