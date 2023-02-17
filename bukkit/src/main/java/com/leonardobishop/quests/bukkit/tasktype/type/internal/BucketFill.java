package com.leonardobishop.quests.bukkit.tasktype.type.internal;

import com.leonardobishop.quests.bukkit.BukkitQuestsPlugin;
import com.leonardobishop.quests.bukkit.util.TaskUtils;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerBucketFillEvent;

public final class BucketFill extends BucketInteract {

    private final BukkitQuestsPlugin plugin;

    public BucketFill(BukkitQuestsPlugin plugin) {
        super("bucketfill", TaskUtils.TASK_ATTRIBUTION_STRING, "Fill a specific bucket.");
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBucketFill(PlayerBucketFillEvent event) {
        if (event.getItemStack() == null) return;

        super.onBucket(event.getPlayer(), event.getItemStack().getType(), plugin);
    }

}
