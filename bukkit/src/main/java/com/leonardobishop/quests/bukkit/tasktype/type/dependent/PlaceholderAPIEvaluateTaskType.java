package com.leonardobishop.quests.bukkit.tasktype.type.dependent;

import com.leonardobishop.quests.bukkit.BukkitQuestsPlugin;
import com.leonardobishop.quests.bukkit.tasktype.BukkitTaskType;
import com.leonardobishop.quests.bukkit.util.TaskUtils;
import com.leonardobishop.quests.common.config.ConfigProblem;
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

public final class PlaceholderAPIEvaluateTaskType extends BukkitTaskType {

    private final BukkitQuestsPlugin plugin;
    private BukkitTask poll;

    public PlaceholderAPIEvaluateTaskType(BukkitQuestsPlugin plugin) {
        super("placeholderapi_evaluate", TaskUtils.TASK_ATTRIBUTION_STRING, "Evaluate the result of a placeholder");
        this.plugin = plugin;

        super.addConfigValidator(TaskUtils.useRequiredConfigValidator(this, "placeholder"));
        super.addConfigValidator(TaskUtils.useRequiredConfigValidator(this, "evaluates"));
        super.addConfigValidator(TaskUtils.useRequiredConfigValidator(this, "operator"));
        super.addConfigValidator(TaskUtils.useAcceptedValuesConfigValidator(this, Arrays.asList(
                "GREATER_THAN",
                "GREATER_THAN_OR_EQUAL_TO",
                "LESS_THAN",
                "LESS_THAN_OR_EQUAL_TO",
                "EQUAL",
                "NOT_EQUAL"
        ), "operator"));
    }

    @Override
    public void onReady() {
        this.poll = new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    QPlayer qPlayer = plugin.getPlayerManager().getPlayer(player.getUniqueId());
                    if (qPlayer == null) {
                        continue;
                    }

                    for (TaskUtils.PendingTask pendingTask : TaskUtils.getApplicableTasks(player, qPlayer, PlaceholderAPIEvaluateTaskType.this)) {
                        Quest quest = pendingTask.quest();
                        Task task = pendingTask.task();
                        TaskProgress taskProgress = pendingTask.taskProgress();

                        PlaceholderAPIEvaluateTaskType.super.debug("Polling PAPI for player", quest.getId(), task.getId(), player.getUniqueId());

                        String placeholder = (String) task.getConfigValue("placeholder");
                        // 需要判定的值
                        Object eval = task.getConfigValue("evaluates");
                        Operator operator;
                        try {
                            operator = Operator.valueOf((String) task.getConfigValue("operator"));
                        } catch (IllegalArgumentException exception) {
                            PlaceholderAPIEvaluateTaskType.super.debug("Operator was specified but no such type, continuing...", quest.getId(), task.getId(), player.getUniqueId());
                            continue;
                        }

                        PlaceholderAPIEvaluateTaskType.super.debug("Operator = " + operator, quest.getId(), task.getId(), player.getUniqueId());

                        if (placeholder != null && eval != null) {
                            String parse = PlaceholderAPI.setPlaceholders(player, placeholder);
                            // 如果符号是等于或者不等于
                            if (operator == Operator.EQUAL || operator == Operator.NOT_EQUAL) {
                                switch (operator) {
                                    case EQUAL -> {
                                        if (parse.equals(String.valueOf(eval))) {
                                            PlaceholderAPIEvaluateTaskType.super.debug("Marking task as complete", quest.getId(), task.getId(), player.getUniqueId());
                                            taskProgress.setCompleted(true);
                                        }
                                    }
                                    case NOT_EQUAL -> {
                                        if (!parse.equals(String.valueOf(eval))) {
                                            PlaceholderAPIEvaluateTaskType.super.debug("Marking task as complete", quest.getId(), task.getId(), player.getUniqueId());
                                            taskProgress.setCompleted(true);
                                        }
                                    }
                                }
                                // 如果符号是大于(等于)或者小于(等于)
                            } else {
                                double numReq;
                                // 如果衡量值是数字
                                if (eval instanceof Number) {
                                    numReq = ((Number) eval).doubleValue();
                                } else if (eval instanceof String) {
                                    try {
                                        numReq = Double.parseDouble((String) eval);
                                    } catch (NumberFormatException exception) {
                                        PlaceholderAPIEvaluateTaskType.super.debug("Numeric operator was specified but configured string to evaluate to cannot be parsed into a double, continuing...", quest.getId(), task.getId(), player.getUniqueId());
                                        continue;
                                    }
                                } else {
                                    PlaceholderAPIEvaluateTaskType.super.debug("Unknown evaluates type, continuing...", quest.getId(), task.getId(), player.getUniqueId());
                                    continue;
                                }

                                double numHas;
                                try {
                                    numHas = Double.parseDouble(parse);
                                } catch (NumberFormatException ex) {
                                    PlaceholderAPIEvaluateTaskType.super.debug("Numeric operator was specified but evaluated string cannot be parsed into a double, continuing...", quest.getId(), task.getId(), player.getUniqueId());
                                    continue;
                                }


                                PlaceholderAPIEvaluateTaskType.super.debug("Evaluation = '" + parse + "'", quest.getId(), task.getId(), player.getUniqueId());


                                taskProgress.setProgress(numHas);
                                switch (operator) {
                                    case GREATER_THAN -> {
                                        if (numHas > numReq) {
                                            PlaceholderAPIEvaluateTaskType.super.debug("Marking task as complete", quest.getId(), task.getId(), player.getUniqueId());
                                            taskProgress.setCompleted(true);
                                        }
                                    }
                                    case LESS_THAN -> {
                                        if (numHas < numReq) {
                                            PlaceholderAPIEvaluateTaskType.super.debug("Marking task as complete", quest.getId(), task.getId(), player.getUniqueId());
                                            taskProgress.setCompleted(true);
                                        }
                                    }
                                    case GREATER_THAN_OR_EQUAL_TO -> {
                                        if (numHas >= numReq) {
                                            PlaceholderAPIEvaluateTaskType.super.debug("Marking task as complete", quest.getId(), task.getId(), player.getUniqueId());
                                            taskProgress.setCompleted(true);
                                        }
                                    }
                                    case LESS_THAN_OR_EQUAL_TO -> {
                                        if (numHas <= numReq) {
                                            PlaceholderAPIEvaluateTaskType.super.debug("Marking task as complete", quest.getId(), task.getId(), player.getUniqueId());
                                            taskProgress.setCompleted(true);
                                        }
                                    }
                                    default -> {
                                    }
                                }

                            }
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 20L, 20L);
    }

    @Override
    public void onDisable() {
        if (this.poll != null) {
            this.poll.cancel();
        }
    }

    enum Operator {
        EQUAL,
        NOT_EQUAL,
        GREATER_THAN,
        LESS_THAN,
        GREATER_THAN_OR_EQUAL_TO,
        LESS_THAN_OR_EQUAL_TO;
    }
}
