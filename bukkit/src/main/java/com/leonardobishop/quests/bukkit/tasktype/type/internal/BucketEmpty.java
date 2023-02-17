package com.leonardobishop.quests.bukkit.tasktype.type.internal;

import com.leonardobishop.quests.bukkit.BukkitQuestsPlugin;
import com.leonardobishop.quests.bukkit.util.TaskUtils;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerBucketEmptyEvent;

public final class BucketEmpty extends BucketInteract {

    private final BukkitQuestsPlugin plugin;

    public BucketEmpty(BukkitQuestsPlugin plugin) {
        super("bucketempty", TaskUtils.TASK_ATTRIBUTION_STRING, "Empty a specific bucket.");
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBucketEmpty(PlayerBucketEmptyEvent event) {
        super.onBucket(event.getPlayer(), event.getBucket(), plugin);
    }

}
