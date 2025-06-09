package bilzox.acemix.studios;

import org.bukkit.plugin.java.JavaPlugin;

public final class FireworkEvent extends JavaPlugin {

    @Override
    public void onEnable() {
        saveDefaultConfig();
        new FireworkPacketBlocker(this).register();
    }

    @Override
    public void onDisable() {
    }
}
