package bilzox.acemix.studios;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.events.PacketContainer;
import com.cryptomorin.xseries.XSound;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

import org.bukkit.Location;
import java.util.List;

public class FireworkPacketBlocker {

    private final FireworkEvent plugin;
    private final List<String> blockedRegions;
    List<XSound> fireworkSounds = List.of(
            XSound.ENTITY_FIREWORK_ROCKET_LAUNCH,
            XSound.ENTITY_FIREWORK_ROCKET_BLAST,
            XSound.ENTITY_FIREWORK_ROCKET_BLAST_FAR,
            XSound.ENTITY_FIREWORK_ROCKET_LARGE_BLAST,
            XSound.ENTITY_FIREWORK_ROCKET_LARGE_BLAST_FAR,
            XSound.ENTITY_FIREWORK_ROCKET_TWINKLE,
            XSound.ENTITY_FIREWORK_ROCKET_TWINKLE_FAR
    );

    public FireworkPacketBlocker(FireworkEvent plugin) {
        this.plugin = plugin;
        this.blockedRegions = plugin.getConfig().getStringList("regions");
    }

    public void register() {
        ProtocolManager manager = ProtocolLibrary.getProtocolManager();

        manager.addPacketListener(new PacketAdapter(plugin, PacketType.Play.Server.NAMED_SOUND_EFFECT) {
            @Override
            public void onPacketSending(PacketEvent event) {
                PacketContainer packet = event.getPacket();

                try {
                    Object soundEffect = packet.getSoundEffects().read(0);
                    String soundKey = soundEffect.toString().toUpperCase();

                    XSound match = XSound.matchXSound(soundKey).orElse(null);
                    if (match != null && fireworkSounds.contains(match)) {
                        if (isInBlockedRegion(event.getPlayer().getLocation())) {
                            event.setCancelled(true);
                        }
                    }

                } catch (Exception e) {
                    plugin.getLogger().warning("XSound: " + e.getMessage());
                }
            }
        });


        manager.addPacketListener(new PacketAdapter(plugin, PacketType.Play.Server.ENTITY_STATUS) {
            @Override
            public void onPacketSending(PacketEvent event) {
                if (isInBlockedRegion(event.getPlayer().getLocation())) {
                    event.setCancelled(true);
                }
            }
        });

        manager.addPacketListener(new PacketAdapter(plugin, PacketType.Play.Server.EXPLOSION) {
            @Override
            public void onPacketSending(PacketEvent event) {
                if (isInBlockedRegion(event.getPlayer().getLocation())) {
                    event.setCancelled(true);
                }
            }
        });
    }

    private boolean isInBlockedRegion(Location loc) {
        RegionManager manager = WorldGuard.getInstance().getPlatform().getRegionContainer().get(BukkitAdapter.adapt(loc.getWorld()));
        if (manager == null) return false;

        ApplicableRegionSet regions = manager.getApplicableRegions(BukkitAdapter.asBlockVector(loc));
        for (ProtectedRegion region : regions) {
            if (blockedRegions.contains(region.getId())) {
                return true;
            }
        }
        return false;
    }
}