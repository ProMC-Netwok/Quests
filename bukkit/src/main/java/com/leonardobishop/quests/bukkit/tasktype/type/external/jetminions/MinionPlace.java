package com.leonardobishop.quests.bukkit.tasktype.type.external.jetminions;

import com.leonardobishop.quests.bukkit.BukkitQuestsPlugin;
import com.leonardobishop.quests.bukkit.tasktype.BukkitTaskType;
import com.leonardobishop.quests.bukkit.util.TaskUtils;
import com.leonardobishop.quests.common.player.QPlayer;
import com.leonardobishop.quests.common.player.questprogressfile.TaskProgress;
import com.leonardobishop.quests.common.quest.Quest;
import com.leonardobishop.quests.common.quest.Task;
import me.jet315.minions.events.PostMinionPlaceEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

public class MinionPlace extends BukkitTaskType {

    private final BukkitQuestsPlugin plugin;

    public MinionPlace(BukkitQuestsPlugin plugin) {
        super("jetminions_place", TaskUtils.TASK_ATTRIBUTION_STRING, "Place down a set of minions.");
        this.plugin = plugin;

        super.addConfigValidator(TaskUtils.useRequiredConfigValidator(this, "minion-type"));
        super.addConfigValidator(TaskUtils.useRequiredConfigValidator(this, "amount"));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onMinionPlace(PostMinionPlaceEvent event) {
        QPlayer qPlayer = plugin.getPlayerManager().getPlayer(event.getPlayer().getUniqueId());
        // 检查玩家数据
        if (qPlayer == null) {
            return;
        }
        // 获取玩家实例
        Player player = event.getPlayer();
        // 遍历玩家所有激活的任务
        for (TaskUtils.PendingTask pendingTask : TaskUtils.getApplicableTasks(player, qPlayer, this)) {
            Quest quest = pendingTask.quest();
            Task task = pendingTask.task();
            TaskProgress taskProgress = pendingTask.taskProgress();
            // Debug 玩家放置信息
            super.debug("Player place JetMinions minion", quest.getId(), task.getId(), player.getUniqueId());

            Object typeObj = task.getConfigValue("minion-type");
            String type = "";
            if (typeObj instanceof String) type = (String) typeObj;

            if (!type.equalsIgnoreCase(event.getMinion().getIdentifier())) {
                super.debug("Minion identifier ('" + event.getMinion().getIdentifier() + "') does not match required id, continuing...", quest.getId(), task.getId(), player.getUniqueId());
                continue;
            }

            Object amountObj = task.getConfigValue("amount");
            int amount = amountObj == null ? 1 : (int) amountObj;

            int curProgress = TaskUtils.getIntegerTaskProgress(taskProgress);
            int newProgress = curProgress + 1;

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
