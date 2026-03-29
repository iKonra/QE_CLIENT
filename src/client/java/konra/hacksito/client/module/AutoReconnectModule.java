package konra.hacksito.client.module;

public class AutoReconnectModule extends Module {
    // Segundos de espera antes de reintentar
    public static int delay = 5; 

    public AutoReconnectModule() {
        super("AutoReconnect");
    }

    @Override
    public void onEnable() {}

    @Override
    public void onDisable() {}
}