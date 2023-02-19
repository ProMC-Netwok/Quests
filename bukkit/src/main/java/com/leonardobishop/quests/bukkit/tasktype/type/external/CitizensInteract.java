package com.leonardobishop.quests.bukkit.tasktype.type.external;

import com.leonardobishop.quests.bukkit.BukkitQuestsPlugin;
import com.leonardobishop.quests.bukkit.tasktype.BukkitTaskType;
import com.leonardobishop.quests.bukkit.util.TaskUtils;
import com.leonardobishop.quests.common.player.QPlayer;
import com.leonardobishop.quests.common.player.questprogressfile.TaskProgress;
import com.leonardobishop.quests.common.quest.Quest;
import com.leonardobishop.quests.common.quest.Task;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

public final class CitizensInteract extends BukkitTaskType {

    private final BukkitQuestsPlugin plugin;

    public CitizensInteract(BukkitQuestsPlugin plugin) {
        super("citizens_interact", TaskUtils.TASK_ATTRIBUTION_STRING, "Interact with an NPC to complete the quest.");
        this.plugin = plugin;
        super.addConfigValidator(TaskUtils.useIntegerConfigValidator(this, "npc-id"));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onNPCClick(NPCRightClickEvent event) {
        QPlayer qPlayer = plugin.getPlayerManager().getPlayer(event.getClicker().getUniqueId());
        if (qPlayer == null) return;

        Player player = event.getClicker();
        for (TaskUtils.PendingTask pendingTask : TaskUtils.getApplicableTasks(player, qPlayer, this)) {
            Quest quest = pendingTask.quest();
            Task task = pendingTask.task();
            TaskProgress taskProgress = pendingTask.taskProgress();

            super.debug("Player clicked NPC", quest.getId(), task.getId(), player.getUniqueId());
            Object npcIdObj = task.getConfigValue("npc-id");
            if (npcIdObj == null) return;
            int npcId = -1;
            if (npcIdObj instanceof Integer) npcId = (int) npcIdObj;
            if (npcId != event.getNPC().getId()) {
                super.debug("NPC id ('" + event.getNPC().getId() + "') does not match required id, continuing...", quest.getId(), task.getId(), player.getUniqueId());
                continue;
            }
            super.debug("Marking task as complete", quest.getId(), task.getId(), player.getUniqueId());
            taskProgress.setCompleted(true);
        }
    }

}
