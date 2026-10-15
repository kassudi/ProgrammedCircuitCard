package yuuki1293.pccard;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = PCCard.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ConfigCommon {

    private static final ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
    static final ForgeConfigSpec spec;

    private static final ForgeConfigSpec.IntValue SEARCH_DEPTH;
    private static final ForgeConfigSpec.BooleanValue SUBNET_BLOCKING;
    private static final ForgeConfigSpec.BooleanValue SMART_SUBNET_BLOCKING;
    private static final ForgeConfigSpec.IntValue SMART_BLOCKING_MAX_BATCHES;
    private static final ForgeConfigSpec.BooleanValue BLOCKING_DEBUG;

    static {
        builder.comment("*****************************************");
        builder.comment("* Programmed Circuit Card Common Config *");
        builder.comment("*****************************************");

        SEARCH_DEPTH = builder.comment("Maximum depth for searching connected machines in subnet tree.")
            .comment("Higher values allow deeper traversal but may impact performance.")
            .defineInRange("search_depth", 5, 0, 100);

        SUBNET_BLOCKING = builder
            .comment("Make the Pattern Provider's blocking mode check the machines behind Storage Buses")
            .comment("on a subnet, not only the block in front of the provider.")
            .comment("Only applies to Pattern Providers with a Programmed Circuit Card installed.")
            .define("subnet_blocking", true);

        SMART_SUBNET_BLOCKING = builder.comment("Smart blocking for subnets (requires subnet_blocking).")
            .comment("Instead of blocking while the machine holds any input, only block while it holds inputs of a")
            .comment("different pattern or a different programmed circuit. The same pattern can be pushed again,")
            .comment("so several batches can sit in the machine and a parallel hatch can use them.")
            .define("smart_subnet_blocking", false);

        SMART_BLOCKING_MAX_BATCHES = builder
            .comment("Smart blocking: stop pushing the same pattern once the machine already holds this many batches.")
            .defineInRange("smart_blocking_max_batches", 8, 1, 1024);

        BLOCKING_DEBUG = builder
            .comment("Log every subnet blocking decision as [PCC-BLOCK] lines in the log. For troubleshooting.")
            .define("blocking_debug", false);

        spec = builder.build();
    }

    public static int getSearchDepth() {
        return SEARCH_DEPTH.get();
    }

    public static boolean isSubnetBlocking() {
        return SUBNET_BLOCKING.get();
    }

    public static boolean isSmartBlocking() {
        return SMART_SUBNET_BLOCKING.get();
    }

    public static boolean isBlockingDebug() {
        return BLOCKING_DEBUG.get();
    }

    public static int getSmartBlockingMaxBatches() {
        return SMART_BLOCKING_MAX_BATCHES.get();
    }
}
