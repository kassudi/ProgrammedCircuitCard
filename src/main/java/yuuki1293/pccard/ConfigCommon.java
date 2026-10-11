package yuuki1293.pccard;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = PCCard.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ConfigCommon {

    private static final ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
    static final ForgeConfigSpec spec;

    private static final ForgeConfigSpec.IntValue SEARCH_DEPTH;
    private static final ForgeConfigSpec.BooleanValue SUBNET_BLOCKING;

    static {
        builder.comment("*****************************************");
        builder.comment("* Programmed Circuit Card Common Config *");
        builder.comment("*****************************************");

        SEARCH_DEPTH = builder.comment("Maximum depth for searching connected machines in subnet tree.")
            .comment("Higher values allow deeper traversal but may impact performance.")
            .defineInRange("search_depth", 5, 0, 100);

        SUBNET_BLOCKING = builder.comment("Make the Pattern Provider's blocking mode check the machines behind Storage Buses")
            .comment("on a subnet, not only the block in front of the provider.")
            .comment("Only applies to Pattern Providers with a Programmed Circuit Card installed.")
            .define("subnet_blocking", true);

        spec = builder.build();
    }

    public static int getSearchDepth() {
        return SEARCH_DEPTH.get();
    }

    public static boolean isSubnetBlocking() {
        return SUBNET_BLOCKING.get();
    }
}
