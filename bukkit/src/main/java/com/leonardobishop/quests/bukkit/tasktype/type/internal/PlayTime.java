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

public final class PlayTime extends BukkitTaskType {

    private final BukkitQuestsPlugin plugin;
    private BukkitTask poll;

    public PlayTime(BukkitQuestsPlugin plugin) {
        super("playtime", TaskUtils.TASK_ATTRIBUTION_STRING, "Track the amount of playing time a user has been on");
        this.plugin = plugin;

        super.addConfigValidator(TaskUtils.useRequiredConfigValidator(this, "minutes"));
        super.addConfigValidator(TaskUtils.useIntegerConfigValidator(this, "minutes"));
    }


    @Override
    public void onReady() {
        if (this.poll == null) {
            this.poll = new BukkitRunnable() {
                @Override
                public void run() {
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        QPlayer qPlayer = plugin.getPlayerManager().getPlayer(player.getUniqueId());
                        if (qPlayer == null) {
                            continue;
                        }

                        for (TaskUtils.PendingTask pendingTask : TaskUtils.getApplicableTasks(player, qPlayer, PlayTime.this)) {
                            Quest quest = pendingTask.quest();
                            Task task = pendingTask.task();
                            TaskProgress taskProgress = pendingTask.taskProgress();

                            PlayTime.super.debug("Polling playtime for player", quest.getId(), task.getId(), player.getUniqueId());

                            boolean ignoreAfk = (boolean) task.getConfigValue("ignore-afk", false);

                            if (ignoreAfk && plugin.getEssentialsHook() == null) {
                                PlayTime.super.debug("ignore-afk is enabled, but Essentials is not detected on the server", quest.getId(), task.getId(), player.getUniqueId());
                            }

                            if (ignoreAfk
                                    && plugin.getEssentialsHook() != null
                                    && plugin.getEssentialsHook().isAfk(player)) {
                                PlayTime.super.debug("ignore-afk is enabled and Essentials reports player as afk, continuing...", quest.getId(), task.getId(), player.getUniqueId());
                                continue;
                            }

                            int minutes = (int) task.getConfigValue("minutes");
                            int progress = TaskUtils.incrementIntegerTaskProgress(taskProgress);
                            PlayTime.super.debug("Incrementing task progress (now " + progress + ")", quest.getId(), task.getId(), player.getUniqueId());

                            if (progress >= minutes) {
                                PlayTime.super.debug("Marking task as complete", quest.getId(), task.getId(), player.getUniqueId());
                                taskProgress.setCompleted(true);
                            }
                        }
                    }
                }
            }.runTaskTimer(plugin, 1200L, 1200L);
        }
    }

    @Override
    public void onDisable() {
//        if (this.poll != null) {
//            this.poll.cancel();
//        }
    }

}
