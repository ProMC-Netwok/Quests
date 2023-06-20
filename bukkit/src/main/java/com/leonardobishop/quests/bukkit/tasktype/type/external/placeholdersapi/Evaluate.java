package com.leonardobishop.quests.bukkit.tasktype.type.external.placeholdersapi;

import com.leonardobishop.quests.bukkit.BukkitQuestsPlugin;
import com.leonardobishop.quests.bukkit.tasktype.BukkitTaskType;
import com.leonardobishop.quests.bukkit.util.TaskUtils;
import com.leonardobishop.quests.common.player.QPlayer;
import com.leonardobishop.quests.common.player.questprogressfile.TaskProgress;
import com.leonardobishop.quests.common.quest.Quest;
import com.leonardobishop.quests.common.quest.Task;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class Evaluate extends BukkitTaskType {

    private final BukkitQuestsPlugin plugin;
    private BukkitTask bukkitTask;

    public Evaluate(BukkitQuestsPlugin plugin) {
        super("placeholderapi_evaluate", TaskUtils.TASK_ATTRIBUTION_STRING, "Evaluate the result of a placeholder");
        this.plugin = plugin;

        super.addConfigValidator(TaskUtils.useRequiredConfigValidator(this, "placeholder"));
        super.addConfigValidator(TaskUtils.useRequiredConfigValidator(this, "evaluates"));
        super.addConfigValidator(TaskUtils.useRequiredConfigValidator(this, "operator"));
    }

    public void onLoad() {
        if (bukkitTask != null) return;
        bukkitTask = new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    QPlayer qPlayer = plugin.getPlayerManager().getPlayer(player.getUniqueId());
                    if (!player.isOnline() || qPlayer == null) continue;
                    handle(player, qPlayer);
                }
            }
        }.runTaskTimerAsynchronously(plugin, 40L, 40L);
    }

    private void handle(Player player, QPlayer qPlayer) {
        for (TaskUtils.PendingTask pendingTask : TaskUtils.getApplicableTasks(player, qPlayer, Evaluate.this)) {
            Task task = pendingTask.task();
            TaskProgress taskProgress = pendingTask.taskProgress();
            String placeholder = (String) task.getConfigValue("placeholder");

            Object eval = task.getConfigValue("evaluates");
            Operator operator = getOperator((String) task.getConfigValue("operator"));

            if (placeholder != null && eval != null) {
                String parse = PlaceholderAPI.setPlaceholders(player, placeholder);
                // 如果符号是等于或者不等于
                if ((operator == Operator.EQUAL && parse.equals(String.valueOf(eval)))) {
                    taskProgress.setCompleted(true);
                    return;
                }
                if (operator == Operator.NOT_EQUAL && !parse.equals(String.valueOf(eval))) {
                    taskProgress.setCompleted(true);
                    return;
                }

                if (!((String) eval).matches("^[0-9]+(\\.[0-9]+)?$")) continue;
                double numReq = Double.parseDouble((String) eval);

                if (!parse.matches("^[0-9]+(\\.[0-9]+)?$")) continue;
                double numHas = Double.parseDouble(parse);

                taskProgress.setProgress(numHas);
                switch (operator) {
                    case GREATER_THAN -> {
                        if (numReq < numHas) taskProgress.setCompleted(true);
                    }
                    case LESS_THAN -> {
                        if (numReq > numHas) taskProgress.setCompleted(true);
                    }
                    case GREATER_THAN_OR_EQUAL_TO -> {
                        if (numReq <= numHas) taskProgress.setCompleted(true);
                    }
                    case LESS_THAN_OR_EQUAL_TO -> {
                        if (numReq >= numHas) taskProgress.setCompleted(true);
                    }
                }
            }
        }
    }

    private Operator getOperator(String string) {
        return switch (string) {
            case "=" -> Operator.EQUAL;
            case "!=" -> Operator.NOT_EQUAL;
            case ">=" -> Operator.GREATER_THAN_OR_EQUAL_TO;
            case ">" -> Operator.GREATER_THAN;
            case "<=" -> Operator.LESS_THAN_OR_EQUAL_TO;
            case "<" -> Operator.LESS_THAN;
            default -> Operator.valueOf(string);
        };
    }

    enum Operator {
        EQUAL, NOT_EQUAL, GREATER_THAN, LESS_THAN, GREATER_THAN_OR_EQUAL_TO, LESS_THAN_OR_EQUAL_TO;
    }
}
