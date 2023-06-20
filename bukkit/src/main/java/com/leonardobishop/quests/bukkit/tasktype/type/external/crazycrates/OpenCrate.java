package com.leonardobishop.quests.bukkit.tasktype.type.external.crazycrates;

import com.leonardobishop.quests.bukkit.BukkitQuestsPlugin;
import com.leonardobishop.quests.bukkit.tasktype.BukkitTaskType;
import com.leonardobishop.quests.bukkit.util.TaskUtils;
import com.leonardobishop.quests.common.player.QPlayer;
import com.leonardobishop.quests.common.player.questprogressfile.TaskProgress;
import com.leonardobishop.quests.common.quest.Quest;
import com.leonardobishop.quests.common.quest.Task;
import me.badbones69.crazycrates.api.events.PlayerPrizeEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

public class OpenCrate extends BukkitTaskType {
    /*
    Wiki
        Type: crazycrates_open
        Require: Amount(Integer)
        Option: Type(String)
    */
    private final BukkitQuestsPlugin plugin;

    public OpenCrate(BukkitQuestsPlugin plugin) {
        super("crazycrates_open", TaskUtils.TASK_ATTRIBUTION_STRING, "Open a crate.");
        this.plugin = plugin;
        super.addConfigValidator(TaskUtils.useIntegerConfigValidator(this, "amount"));
    }

    @EventHandler
    public void onCrateOpen(PlayerPrizeEvent event) {
        Player player = event.getPlayer();
        QPlayer qPlayer = plugin.getPlayerManager().getPlayer(player.getUniqueId());
        if (qPlayer == null) return;
        // loop all active tasks
        for (TaskUtils.PendingTask pendingTask : TaskUtils.getApplicableTasks(player, qPlayer, this)) {
            Quest quest = pendingTask.quest();
            Task task = pendingTask.task();
            TaskProgress taskProgress = pendingTask.taskProgress();
            super.debug("Player open crate", quest.getId(), task.getId(), player.getUniqueId());

            Object typeObj = task.getConfigValue("crate-type");
            String type = "";
            if (typeObj instanceof String) type = (String) typeObj;

            if (!type.equals("") && !type.equals(event.getCrate().getName())) {
                super.debug("Crate type ('" + event.getCrate().getName() + "') does not match required type, continuing...", quest.getId(), task.getId(), player.getUniqueId());
                continue;
            }

            Object amountObj = task.getConfigValue("amount");
            int amount = 1;
            if (amountObj instanceof Integer) amount = (int) amountObj;

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
