package konra.hacksito.client.module;

import net.minecraft.client.MinecraftClient;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;

public class NoFallModule extends Module {

    public NoFallModule() {
        super("NoFall");
    }

    @Override
    public void onEnable() {
        // nada pa init
    }

    @Override
    public void onDisable() {
        // limpiar distancia de caida otra vez por las dudas
        var player = MinecraftClient.getInstance().player;
        if (player != null) player.fallDistance = 0;
    }

    @Override
    public void onTick() {
        if (!enabled) return;
        var player = MinecraftClient.getInstance().player;
        if (player != null) {
            player.fallDistance = 0; // evitar cualquier daño de caida en el cliente
            // decirle al server que siempre estamos en el suelo para cancelar
            // la caida del lado del server
            MinecraftClient.getInstance().getNetworkHandler()
                    .sendPacket(new PlayerMoveC2SPacket.OnGroundOnly(true));
        }
    }
}