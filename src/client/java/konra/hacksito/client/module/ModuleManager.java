package konra.hacksito.client.module;

import java.util.ArrayList;
import java.util.List;

import konra.hacksito.client.module.FlyModule;
import konra.hacksito.client.module.SpeedModule;
import konra.hacksito.client.module.XrayModule;
import konra.hacksito.client.module.SwimmingModule;
import konra.hacksito.client.module.JesusModule;
import konra.hacksito.client.module.NoFallModule;
import konra.hacksito.client.module.FullBrightModule;
import konra.hacksito.client.module.AutoJumpModule;
import konra.hacksito.client.module.FreecamModule;
import konra.hacksito.client.module.HighJumpModule;
import konra.hacksito.client.module.AutoToolModule;
import konra.hacksito.client.module.InfoModule;
import konra.hacksito.client.module.SprintModule;
import konra.hacksito.client.module.SafeWalkModule;
import konra.hacksito.client.module.ParkourModule;
import konra.hacksito.client.module.PlayerESPModule;
import konra.hacksito.client.module.MobESPModule;
import konra.hacksito.client.module.AutoEatModule;
import konra.hacksito.client.module.ChatTimestampModule;
import konra.hacksito.client.module.NoHurtCamModule;
import konra.hacksito.client.module.AutoReconnectModule;
import konra.hacksito.client.module.FastPlaceModule;
import konra.hacksito.client.module.NoPushModule;
import konra.hacksito.client.module.AimAssistModule;
// --- NUEVO IMPORT ---
import konra.hacksito.client.module.WaypointModule; 

public class ModuleManager {

    private static final List<Module> modules = new ArrayList<>();

    public static void init() {
        // Movimiento y Supervivencia
        modules.add(new FlyModule());
        modules.add(new SpeedModule());
        modules.add(new XrayModule());
        modules.add(new SwimmingModule()); 
        modules.add(new JesusModule()); 
        modules.add(new NoFallModule());
        modules.add(new FullBrightModule());
        modules.add(new AutoJumpModule());
        modules.add(new AutoToolModule());
        modules.add(new FreecamModule());
        modules.add(new HighJumpModule());
        modules.add(new SprintModule());
        modules.add(new SafeWalkModule());
        modules.add(new ParkourModule());
        modules.add(new AutoEatModule());
        modules.add(new FastPlaceModule());
        modules.add(new NoPushModule());
        
        // Combate y Utilidad
        modules.add(new AimAssistModule());
        modules.add(new ClickHelperModule());
        modules.add(new NoHurtCamModule());
        modules.add(new ChatTimestampModule());
        modules.add(new AutoReconnectModule());
        
        // Renderizado y Visuales
        modules.add(new InfoModule());
        modules.add(new PlayerESPModule());
        modules.add(new MobESPModule());
        
        // --- REGISTRO DEL NUEVO MÓDULO ---
        modules.add(new WaypointModule()); 
    }

    public static List<Module> getModules() {
        return modules;
    }

    // Buscador de módulos por nombre (Ignora mayúsculas/minúsculas)
    public static Module getModule(String name) {
        for (Module m : modules) {
            if (m.getName().equalsIgnoreCase(name)) {
                return m;
            }
        }
        return null;
    }

    // Buscador de módulos por clase (Más seguro para código interno)
    public static <T extends Module> T getModule(Class<T> clazz) {
        for (Module m : modules) {
            if (m.getClass() == clazz) {
                return clazz.cast(m);
            }
        }
        return null;
    }
}