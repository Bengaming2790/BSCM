package ca.techgarage.bscm.client;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.Screen;

@Environment(EnvType.CLIENT)
public abstract class BSCMModMenuIntegration implements ModMenuApi {

    private final Class<?> configClass;
    private final String filename;

    protected BSCMModMenuIntegration(Class<?> configClass, String filename) {
        this.configClass = configClass;
        this.filename = filename;
    }

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return (Screen parent) -> new BSCMConfigScreen(parent, configClass, filename);
    }
}