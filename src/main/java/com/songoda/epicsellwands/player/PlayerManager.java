package com.songoda.epicsellwands.player;

import com.songoda.epicsellwands.settings.Settings;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerManager {

    private Map<UUID, Long> activeCooldowns = new HashMap<>();

    public boolean hasActiveCooldown(Player player) {
        return activeCooldowns.containsKey(player.getUniqueId())
                && activeCooldowns.get(player.getUniqueId()) >= System.currentTimeMillis();
    }

    public void addNewCooldown(Player player) {
        this.activeCooldowns.put(player.getUniqueId(), System.currentTimeMillis()
                + (Settings.COOLDOWN.getInt() * 1000L));
    }

    public long getActiveCooldown(Player player) {
        return activeCooldowns.get(player.getUniqueId());
    }
}
