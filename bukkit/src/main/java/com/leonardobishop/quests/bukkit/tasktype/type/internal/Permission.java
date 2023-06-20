package com.leonardobishop.quests.bukkit.tasktype.type.internal;

import com.leonardobishop.quests.bukkit.BukkitQuestsPlugin;
import com.leonardobishop.quests.bukkit.tasktype.BukkitTaskType;
import com.leonardobishop.quests.bukkit.util.TaskUtils;
import com.leonardobishop.quests.common.player.QPlayer;
import com.leonardobishop.quests.common.player.questprogressfile.TaskProgress;
import com.leonardobishop.quests.common.quest.Quest;
import com.leonardobishop.quests.common.quest.Task;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public final class Permission extends BukkitTaskType {

    private final BukkitQuestsPlugin plugin;
    private BukkitTask poll;

    public Permission(BukkitQuestsPlugin plugin) {
        super("permission", TaskUtils.TASK_ATTRIBUTION_STRING, "Test if a player has a permission");
        this.plugin = plugin;

        super.addConfigValidator(TaskUtils.useRequiredConfigValidator(this, "permission"));
    }

    @Override
    public void onLoad() {
        this.poll = new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    QPlayer qPlayer = plugin.getPlayerManager().getPlayer(player.getUniqueId());
                    if (qPlayer == null) {
                        continue;
                    }
                    for (TaskUtils.PendingTask pendingTask : TaskUtils.getApplicableTasks(player, qPlayer, Permission.this)) {
                        Quest quest = pendingTask.quest();
                        Task task = pendingTask.task();
                        TaskProgress taskProgress = pendingTask.taskProgress();

                        Permission.super.debug("Polling permissions for player", quest.getId(), task.getId(), player.getUniqueId());

                        String permission = (String) task.getConfigValue("permission");
                        if (permission != null) {
                            Permission.super.debug("Checking permission '" + permission + "'", quest.getId(), task.getId(), player.getUniqueId());
                            if (player.hasPermission(permission)) {
                                Permission.super.debug("Player has permission", quest.getId(), task.getId(), player.getUniqueId());
                                Permission.super.debug("Marking task as complete", quest.getId(), task.getId(), player.getUniqueId());
                                taskProgress.setCompleted(true);
                            } else {
                                Permission.super.debug("Player does not have permission", quest.getId(), task.getId(), player.getUniqueId());
                            }
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 30L, 30L);
    }

    @Override
    public void onDisable() {
        if (this.poll != null) {
            this.poll.cancel();
        }
    }

}
