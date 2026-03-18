package ca.techgarage.bscm;

import net.fabricmc.loader.api.FabricLoader;
import java.nio.file.Path;

public final class Bscm {

    private Bscm() {}


    public static void load(Class<?> configClass, String filename) {
        Path path = FabricLoader.getInstance()
                .getConfigDir()
                .resolve(filename + ".yaml");
        ConfigManager.load(configClass, path);
    }

    public static void save(Class<?> configClass, String filename) {
        Path path = FabricLoader.getInstance()
                .getConfigDir()
                .resolve(filename + ".yaml");
        try {
            ConfigManager.writeFile(configClass, path);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}