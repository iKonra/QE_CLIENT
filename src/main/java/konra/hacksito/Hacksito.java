package konra.hacksito;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Usa el logger SLF4J que provee Fabric en vez de la referencia LOGGER del
 * narrador de Minecraft. la importacion estatica previa no existia en
 * los mappings y daba error de compilacion, y ``java.awt`` no esta en el
 * entorno del juego.
 */

public class Hacksito implements ModInitializer {

    public static final Logger LOGGER = LoggerFactory.getLogger("hacksito");

    @Override
    public void onInitialize() {

        LOGGER.info("Hello Fabric world!");



        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(CommandManager.literal("qe_client")
                    .executes(context -> {

                        ServerPlayerEntity player = context.getSource().getPlayer();

                        assert player != null;
                        player.getAbilities().allowFlying = !player.getAbilities().allowFlying;
                        player.getAbilities().flying = player.getAbilities().allowFlying;

                        player.sendAbilitiesUpdate();

                        // enviar mensaje fijo en vez de estado de vuelo
                        context.getSource().sendFeedback(() -> Text.literal("Bienvenido a QE CLIENT Made by Konra"), false);
                        return 1;
                    })
            );
        });



    }
}
