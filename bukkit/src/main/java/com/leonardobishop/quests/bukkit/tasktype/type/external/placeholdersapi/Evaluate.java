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
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class Evaluate extends BukkitTaskType {

    private final BukkitQuestsPlugin plugin;
    private final Map<UUID, BukkitTask> bukkitTaskMap = new HashMap<>();

    public Evaluate(BukkitQuestsPlugin plugin) {
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

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        if (bukkitTaskMap.containsKey(uuid)) {
            bukkitTaskMap.get(uuid).cancel();
            bukkitTaskMap.remove(uuid);
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        BukkitTask bukkitTask = new BukkitRunnable() {
            @Override
            public void run() {
                QPlayer qPlayer = plugin.getPlayerManager().getPlayer(uuid);
                Player player = event.getPlayer();
                if (qPlayer == null) {
                    bukkitTaskMap.remove(uuid);
                    cancel();
                }
                for (TaskUtils.PendingTask pendingTask : TaskUtils.getApplicableTasks(player, qPlayer, Evaluate.this)) {
                    Quest quest = pendingTask.quest();
                    Task task = pendingTask.task();
                    TaskProgress taskProgress = pendingTask.taskProgress();

                    Evaluate.super.debug("Polling PAPI for player", quest.getId(), task.getId(), uuid);

                    String placeholder = (String) task.getConfigValue("placeholder");
                    // 需要判定的值
                    Object eval = task.getConfigValue("evaluates");
                    Operator operator;
                    try {
                        operator = Operator.valueOf((String) task.getConfigValue("operator"));
                    } catch (IllegalArgumentException exception) {
                        Evaluate.super.debug("Operator was specified but no such type, continuing...", quest.getId(), task.getId(), player.getUniqueId());
                        continue;
                    }

                    Evaluate.super.debug("Operator = " + operator, quest.getId(), task.getId(), uuid);

                    if (placeholder != null && eval != null) {
                        String parse = PlaceholderAPI.setPlaceholders(player, placeholder);
                        // 如果符号是等于或者不等于
                        if (operator == Operator.EQUAL || operator == Operator.NOT_EQUAL) {
                            if ((operator == Operator.EQUAL && parse.equals(String.valueOf(eval)))
                                    || (operator == Operator.NOT_EQUAL && !parse.equals(String.valueOf(eval)))) {
                                Evaluate.super.debug("Marking task as complete", quest.getId(), task.getId(), player.getUniqueId());
                                taskProgress.setCompleted(true);
                            }
                            // 如果符号是大于(等于)或者小于(等于)
                        } else {
                            double numReq;
                            // 如果衡量值是数字
                            if (eval instanceof Number) {
                                numReq = (Double) eval;
                            } else {
                                if (!((String) eval).matches("^[0-9]+(\\.[0-9]+)?$")) continue;
                                numReq = Double.parseDouble((String) eval);
                            }

                            if (!parse.matches("^[0-9]+(\\.[0-9]+)?$")) continue;
                            double numHas = Double.parseDouble(parse);

                            Evaluate.super.debug("Evaluation = '" + parse + "'", quest.getId(), task.getId(), player.getUniqueId());

                            taskProgress.setProgress(numHas);
                            if (operator == Operator.GREATER_THAN && numHas > numReq
                                    || operator == Operator.LESS_THAN && numHas < numReq
                                    || operator == Operator.GREATER_THAN_OR_EQUAL_TO && numHas >= numReq
                                    || operator == Operator.LESS_THAN_OR_EQUAL_TO && numHas <= numReq) {
                                Evaluate.super.debug("Marking task as complete", quest.getId(), task.getId(), player.getUniqueId());
                                taskProgress.setCompleted(true);
                            }

                        }
                    }
                }
            }
        }.runTaskTimerAsynchronously(plugin, 20L, 20L);
        bukkitTaskMap.put(uuid, bukkitTask);
    }

    @Override
    public void onStart(Quest quest, Task task, UUID uuid) {
        BukkitTask bukkitTask = new BukkitRunnable() {
            @Override
            public void run() {
                QPlayer qPlayer = plugin.getPlayerManager().getPlayer(uuid);
                Player player = Bukkit.getPlayer(uuid);
                if (player == null || qPlayer == null) {
                    bukkitTaskMap.remove(uuid);
                    cancel();
                }
                for (TaskUtils.PendingTask pendingTask : TaskUtils.getApplicableTasks(player, qPlayer, Evaluate.this)) {
                    Quest quest = pendingTask.quest();
                    Task task = pendingTask.task();
                    TaskProgress taskProgress = pendingTask.taskProgress();

                    Evaluate.super.debug("Polling PAPI for player", quest.getId(), task.getId(), uuid);

                    String placeholder = (String) task.getConfigValue("placeholder");
                    // 需要判定的值
                    Object eval = task.getConfigValue("evaluates");
                    Operator operator;
                    try {
                        operator = Operator.valueOf((String) task.getConfigValue("operator"));
                    } catch (IllegalArgumentException exception) {
                        Evaluate.super.debug("Operator was specified but no such type, continuing...", quest.getId(), task.getId(), player.getUniqueId());
                        continue;
                    }

                    Evaluate.super.debug("Operator = " + operator, quest.getId(), task.getId(), uuid);

                    if (placeholder != null && eval != null) {
                        String parse = PlaceholderAPI.setPlaceholders(player, placeholder);
                        // 如果符号是等于或者不等于
                        if (operator == Operator.EQUAL || operator == Operator.NOT_EQUAL) {
                            if ((operator == Operator.EQUAL && parse.equals(String.valueOf(eval)))
                                    || (operator == Operator.NOT_EQUAL && !parse.equals(String.valueOf(eval)))) {
                                Evaluate.super.debug("Marking task as complete", quest.getId(), task.getId(), player.getUniqueId());
                                taskProgress.setCompleted(true);
                            }
                            // 如果符号是大于(等于)或者小于(等于)
                        } else {
                            double numReq;
                            // 如果衡量值是数字
                            if (eval instanceof Number) {
                                numReq = (Double) eval;
                            } else {
                                if (!((String) eval).matches("^[0-9]+(\\.[0-9]+)?$")) continue;
                                numReq = Double.parseDouble((String) eval);
                            }

                            if (!parse.matches("^[0-9]+(\\.[0-9]+)?$")) continue;
                            double numHas = Double.parseDouble(parse);

                            Evaluate.super.debug("Evaluation = '" + parse + "'", quest.getId(), task.getId(), player.getUniqueId());

                            taskProgress.setProgress(numHas);
                            if (operator == Operator.GREATER_THAN && numHas > numReq
                                    || operator == Operator.LESS_THAN && numHas < numReq
                                    || operator == Operator.GREATER_THAN_OR_EQUAL_TO && numHas >= numReq
                                    || operator == Operator.LESS_THAN_OR_EQUAL_TO && numHas <= numReq) {
                                Evaluate.super.debug("Marking task as complete", quest.getId(), task.getId(), player.getUniqueId());
                                taskProgress.setCompleted(true);
                            }

                        }
                    }
                }
            }
        }.runTaskTimerAsynchronously(plugin, 20L, 20L);
        bukkitTaskMap.put(uuid, bukkitTask);
    }

    @Override
    public void onDisable() {
        bukkitTaskMap.forEach((uuid, bukkitTask) -> {
            bukkitTask.cancel();
            bukkitTaskMap.remove(uuid);
        });
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
