package konra.hacksito.client.screen;

import konra.hacksito.client.HacksitoClient;
import konra.hacksito.client.module.Module;
import konra.hacksito.client.module.ModuleManager;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.lwjgl.glfw.GLFW;

import java.util.*;

public class HackConfigScreen extends Screen {
    private int scrollOffset = 0;
    private int contentHeight = 0;
    private final Map<ClickableWidget, Integer> baseY = new HashMap<>();
    private boolean awaitingKey = false;
    private String awaitingModule = null;
    private final Map<String, ButtonWidget> bindButtons = new HashMap<>();

    public HackConfigScreen() {
        super(Text.of("QE Client Configuration"));
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // 1. Fondo elegante degradado
        context.fillGradient(0, 0, this.width, this.height, 0xFA0A0A0A, 0xFA141414);
        
        // 2. SCISSOR: Corta los botones para que no se salgan del área central
        context.enableScissor(0, 42, this.width, this.height - 42);
        super.render(context, mouseX, mouseY, delta);
        context.disableScissor();

        // 3. HEADER FIJO
        context.fill(0, 0, this.width, 42, 0xFF000000); 
        context.fill(0, 40, this.width, 41, 0xFF55FFFF); // Línea cian
        context.drawCenteredTextWithShadow(this.textRenderer, "§b§lQE §fCLIENT §8| §7CONFIGURACIÓN", this.width / 2, 16, 0xFFFFFF);

        // 4. FOOTER FIJO
        context.fill(0, this.height - 42, this.width, this.height, 0xFF000000);
        context.fill(0, this.height - 42, this.width, this.height - 41, 0xFF55FFFF);
        
        // Indicador de Scroll si el contenido es largo
        if (contentHeight > (this.height - 100)) {
            context.fill(this.width - 4, 42, this.width - 2, this.height - 42, 0x33FFFFFF);
        }
    }

    @Override
    protected void init() {
        super.init();
        bindButtons.clear();
        baseY.clear();
        
        int xMid = this.width / 2;
        int yOffset = 55; 

        int toggleW = 150; 
        int bindW = 85;   
        int gearW = 20;   
        int totalWidth = toggleW + bindW + gearW + 10;
        int startX = xMid - (totalWidth / 2);

        // Clasificar módulos por categorías
        Map<String, List<Module>> categories = new LinkedHashMap<>();
        for (Module module : ModuleManager.getModules()) {
            String cat = categorize(module);
            categories.computeIfAbsent(cat, k -> new ArrayList<>()).add(module);
        }

        for (Map.Entry<String, List<Module>> entry : categories.entrySet()) {
            // Etiqueta de Categoría
            ButtonWidget catLabel = ButtonWidget.builder(Text.literal("§3» §f" + entry.getKey().toUpperCase() + " §3«"), b -> {})
                    .dimensions(xMid - 100, yOffset, 200, 20).build();
            catLabel.active = false;
            addModuleWidget(catLabel, yOffset);
            yOffset += 28;

            for (Module module : entry.getValue()) {
                final String name = module.getName();

                // 1. BOTÓN TOGGLE (Ahora funciona para todos, incluyendo ESP)
                ButtonWidget toggle = ButtonWidget.builder(getToggleText(name, module.isEnabled()), btn -> {
                    module.toggle();
                    btn.setMessage(getToggleText(name, module.isEnabled()));
                }).dimensions(startX, yOffset, toggleW, 20).build();
                addModuleWidget(toggle, yOffset);

                // 2. BOTÓN BIND
                KeyBinding kb = HacksitoClient.moduleKeyBindings.get(name);
                String keyText = (kb == null || kb.isUnbound()) ? "NONE" : kb.getBoundKeyLocalizedText().getString().toUpperCase();
                
                ButtonWidget bind = ButtonWidget.builder(Text.literal("§7KEY: §f" + keyText), btn -> {
                    awaitingKey = true;
                    awaitingModule = name;
                    btn.setMessage(Text.literal("§e[ ... ]"));
                }).dimensions(startX + toggleW + 5, yOffset, bindW, 20).build();
                bindButtons.put(name, bind);
                addModuleWidget(bind, yOffset);

                // 3. BOTÓN GEAR (Engranaje de ajustes)
                Screen cfg = getConfigScreen(name);
                if (cfg != null) {
                    ButtonWidget gear = ButtonWidget.builder(Text.literal("§b⚙"), btn -> this.client.setScreen(cfg))
                            .dimensions(startX + toggleW + bindW + 10, yOffset, gearW, 20).build();
                    addModuleWidget(gear, yOffset);
                }

                yOffset += 24;
            }
            yOffset += 18; 
        }
        contentHeight = yOffset;

        // Botón Salir
        this.addDrawableChild(ButtonWidget.builder(Text.of("§cGUARDAR Y SALIR"), btn -> this.client.setScreen(null))
                .dimensions(xMid - 60, this.height - 32, 120, 22).build());
        
        // Aplicar el scroll inicial
        mouseScrolled(0, 0, 0);
    }

    private void addModuleWidget(ClickableWidget widget, int y) {
        this.addDrawableChild(widget);
        baseY.put(widget, y);
    }

    private static Text getToggleText(String name, boolean enabled) {
        String prefix = enabled ? "§b● " : "§8○ ";
        return Text.literal(prefix + name).formatted(enabled ? Formatting.WHITE : Formatting.GRAY);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (awaitingKey && awaitingModule != null) {
            KeyBinding kb = HacksitoClient.moduleKeyBindings.get(awaitingModule);
            if (kb != null) {
                if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                    kb.setBoundKey(InputUtil.UNKNOWN_KEY);
                } else {
                    kb.setBoundKey(InputUtil.fromKeyCode(keyCode, scanCode));
                }
                String keyName = kb.isUnbound() ? "NONE" : kb.getBoundKeyLocalizedText().getString().toUpperCase();
                if (bindButtons.containsKey(awaitingModule)) {
                    bindButtons.get(awaitingModule).setMessage(Text.literal("§7KEY: §f" + keyName));
                }
            }
            awaitingKey = false;
            awaitingModule = null;
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        scrollOffset -= (int) (amount * 24);
        int maxScroll = Math.max(0, contentHeight - (this.height - 100));
        scrollOffset = Math.min(Math.max(scrollOffset, 0), maxScroll);
        for (Map.Entry<ClickableWidget, Integer> entry : baseY.entrySet()) {
            entry.getKey().setY(entry.getValue() - scrollOffset);
        }
        return true;
    }

    private String categorize(Module module) {
        String n = module.getName().toLowerCase();
        if (n.contains("jump") || n.contains("speed") || n.contains("walk") || n.contains("fly")) return "Movement";
        if (n.contains("aim") || n.contains("aura")) return "Combat";
        if (n.contains("xray") || n.contains("esp") || n.contains("jesus")) return "World";
        return "Misc";
    }

    private Screen getConfigScreen(String name) {
        return switch (name.toLowerCase()) {
            case "aimassist" -> new AimAssistConfigScreen(this);
            case "xray" -> new XrayConfigScreen();
            case "jesus" -> new JesusConfigScreen();
            case "mobesp" -> new MobESPConfigScreen();
            case "highjump" -> new HighJumpConfigScreen();
            case "autotool" -> new AutoToolConfigScreen();
            case "info" -> new InfoConfigScreen();
            case "autoeat" -> new AutoEatConfigScreen();
            case "safewalk" -> new SafeWalkConfigScreen();
            case "fastplace" -> new FastPlaceConfigScreen();
            case "clickhelper" -> new ClickHelperConfigScreen(this);
            default -> null;
        };
    }
    
    public static Text getHintText() { 
        return text(konra.hacksito.client.module.XrayModule.showEnableHint, "Hint"); 
    }
    public static Text getItemsText() { 
        return text(konra.hacksito.client.module.XrayModule.showItems, "Items"); 
    }
    public static Text getExposedText() { 
        return text(konra.hacksito.client.module.XrayModule.onlyExposed, "Exposed"); 
    }
    public static Text getGlowText() { 
        return text(konra.hacksito.client.module.XrayModule.showGlow, "Glow"); 
    }

    private static Text text(boolean b, String s) { 
        return Text.literal(s + ": ").formatted(net.minecraft.util.Formatting.GRAY)
            .append(Text.literal(b ? "ON" : "OFF").formatted(b ? net.minecraft.util.Formatting.GREEN : net.minecraft.util.Formatting.RED)); 
    }
}