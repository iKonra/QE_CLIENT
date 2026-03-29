package konra.hacksito.client.module;

public abstract class Module {

    protected String name;
    protected boolean enabled;

    public Module(String name) {
        this.name = name;
        this.enabled = false;
    }

    public void toggle() {
        enabled = !enabled;

        if (enabled) {
            onEnable();
        } else {
            onDisable();
        }
    }

    public boolean isEnabled() {
        return enabled;
    }
    public String getName() {
        return name;
    }
    public abstract void onEnable();
    public abstract void onDisable();
    public void onTick() {}
}