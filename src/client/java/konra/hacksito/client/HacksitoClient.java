package konra.hacksito.client;

import konra.hacksito.client.module.*;
import konra.hacksito.client.module.Module;
import konra.hacksito.client.screen.WaypointListScreen;
import konra.hacksito.client.screen.HackConfigScreen;
import konra.hacksito.client.utils.RenderUtils;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.render.*;
import net.minecraft.util.ActionResult;
import net.minecraft.text.Text;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.render.GameRenderer;
import org.lwjgl.glfw.GLFW;
import java.util.HashMap;
import java.util.Map;

public class HacksitoClient implements ClientModInitializer {
    public static HacksitoClient INSTANCE;
    public static final Map<String, KeyBinding> moduleKeyBindings = new HashMap<>();
    
    private static KeyBinding zoomKey;
    private static KeyBinding waypointGuiKey;
    private static KeyBinding addWaypointKey; 
    private static KeyBinding configKb;
    
    private static int savedFov = -1;
    private static double savedSpeed = -1;

    public static boolean isZooming() {
        return zoomKey != null && zoomKey.isPressed();
    }

    @Override
    public void onInitializeClient() {
        INSTANCE = this;
        ModuleManager.init();

        PlayerBlockBreakEvents.AFTER.register((world, player, pos, state, entity) -> {
            Module xray = ModuleManager.getModule("Xray");
            if (xray instanceof XrayModule xm && xm.isEnabled()) xm.forceScan();
        });

        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            Module xray = ModuleManager.getModule("Xray");
            if (xray instanceof XrayModule xm && xm.isEnabled()) xm.forceScan();
            return ActionResult.PASS;
        });

        for (Module module : ModuleManager.getModules()) {
            String name = module.getName();
            int defaultKey = GLFW.GLFW_KEY_UNKNOWN;
            if ("xray".equalsIgnoreCase(name)) defaultKey = GLFW.GLFW_KEY_X;
            
            KeyBinding kb = new KeyBinding("key.hacksito." + name.toLowerCase(), InputUtil.Type.KEYSYM, defaultKey, "Hacksito Client");
            moduleKeyBindings.put(name, KeyBindingHelper.registerKeyBinding(kb));
        }

        zoomKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.hacksito.zoom", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_C, "Hacksito Client"));
        waypointGuiKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.hacksito.waypointgui", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_V, "Hacksito Client"));
        addWaypointKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.hacksito.addwaypoint", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_N, "Hacksito Client"));
        configKb = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.hacksito.config", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_RIGHT_SHIFT, "Hacksito Client"));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null) return;

            if (configKb.wasPressed()) {
                client.setScreen(new HackConfigScreen());
            }

            if (addWaypointKey.wasPressed()) {
                Module wpModule = ModuleManager.getModule("Waypoints");
                if (wpModule instanceof WaypointModule wpm) {
                    String name = "Punto " + (wpm.getWaypoints().size() + 1);
                    wpm.addWaypoint(name, client.player.getPos(), 0.0f, 1.0f, 1.0f); // Color Cyan
                    client.player.sendMessage(Text.of("§a[Hacksito] Waypoint '" + name + "' creado!"), false);
                }
            }

            if (waypointGuiKey.wasPressed()) {
                Module wpModule = ModuleManager.getModule("Waypoints");
                if (wpModule instanceof WaypointModule) {
                    client.setScreen(new WaypointListScreen((WaypointModule) wpModule));
                }
            }

            for (Map.Entry<String, KeyBinding> entry : moduleKeyBindings.entrySet()) {
                while (entry.getValue().wasPressed()) {
                    Module mod = ModuleManager.getModule(entry.getKey());
                    if (mod != null) mod.toggle();
                }
            }

            handleZoom(client);
            for (Module module : ModuleManager.getModules()) module.onTick();
        });

        // --- RENDER DEL HUD ---
        HudRenderCallback.EVENT.register((drawContext, tickDelta) -> {
            if (MinecraftClient.getInstance().player == null) return;
            InfoModule.renderOverlay(drawContext);
        });

        // --- RENDER DEL MUNDO ---
        WorldRenderEvents.AFTER_TRANSLUCENT.register(context -> {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player == null || client.world == null) return;

            MatrixStack matrices = context.matrixStack();
            double camX = context.camera().getPos().x;
            double camY = context.camera().getPos().y;
            double camZ = context.camera().getPos().z;

            Module playerESP = ModuleManager.getModule("PlayerESP");
            Module mobESP = ModuleManager.getModule("MobESP");
            Module waypoints = ModuleManager.getModule("Waypoints");

            if ((playerESP != null && playerESP.isEnabled()) || (mobESP != null && mobESP.isEnabled())) {
                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();
                RenderSystem.disableDepthTest();
                RenderSystem.setShader(GameRenderer::getPositionColorProgram);

                if (playerESP instanceof PlayerESPModule && playerESP.isEnabled()) 
                    ((PlayerESPModule) playerESP).render(matrices, camX, camY, camZ);
                if (mobESP instanceof MobESPModule && mobESP.isEnabled()) 
                    ((MobESPModule) mobESP).render(matrices, camX, camY, camZ);
                
                RenderSystem.enableDepthTest();
            }

            if (waypoints != null && waypoints.isEnabled() && waypoints instanceof WaypointModule) {
                ((WaypointModule) waypoints).render(matrices, camX, camY, camZ);
            }
        });
    }

    private void handleZoom(MinecraftClient client) {
        if (zoomKey.isPressed()) {
            if (savedFov < 0) savedFov = (int) client.options.getFov().getValue();
            client.options.getFov().setValue(30);
            var attr = client.player.getAttributeInstance(net.minecraft.entity.attribute.EntityAttributes.GENERIC_MOVEMENT_SPEED);
            if (attr != null && savedSpeed < 0) {
                savedSpeed = attr.getBaseValue();
                attr.setBaseValue(savedSpeed * 0.3);
            }
        } else {
            if (savedFov >= 0) {
                client.options.getFov().setValue(savedFov);
                savedFov = -1;
            }
            if (savedSpeed >= 0) {
                var attr = client.player.getAttributeInstance(net.minecraft.entity.attribute.EntityAttributes.GENERIC_MOVEMENT_SPEED);
                if (attr != null) attr.setBaseValue(savedSpeed);
                savedSpeed = -1;
            }
        }
    }
}