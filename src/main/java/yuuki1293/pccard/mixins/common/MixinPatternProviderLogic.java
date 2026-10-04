package yuuki1293.pccard.mixins.common;

import java.util.HashSet;
import java.util.Set;

import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.llamalad7.mixinextras.sugar.Local;

import appeng.api.crafting.IPatternDetails;
import appeng.api.stacks.AEKey;
import appeng.api.upgrades.IUpgradeableObject;
import appeng.helpers.patternprovider.PatternProviderLogic;
import appeng.helpers.patternprovider.PatternProviderLogicHost;
import yuuki1293.pccard.PCCard;
import yuuki1293.pccard.impl.PatternProviderLogicImpl;

// ExpandedAE replaces the smart-mode input set at priority 1001. Applying PCC first makes this modifier run last,
// preserving the programmed circuit in the final set.
@Mixin(value = PatternProviderLogic.class, remap = false, priority = 1100)
public abstract class MixinPatternProviderLogic implements IUpgradeableObject {

    @Shadow
    @Final
    private PatternProviderLogicHost host;

    @Shadow
    public abstract void updatePatterns();

    @Shadow
    @Final
    private appeng.api.networking.IManagedGridNode mainNode;

    @Shadow
    @Final
    private java.util.List<IPatternDetails> patterns;

    @Shadow
    @Final
    private java.util.List<appeng.api.stacks.GenericStack> sendList;

    // ---- temporary diagnostics: which check in pushPattern refuses the push ----
    @Inject(method = "pushPattern", at = @At("HEAD"))
    private void pCCard$diagHead(CallbackInfoReturnable<Boolean> cir,
        @Local(ordinal = 0, argsOnly = true) IPatternDetails patternDetails) {
        if (!isUpgradedWith(PCCard.PROGRAMMED_CIRCUIT_CARD_ITEM.get()) || !PatternProviderLogicImpl.diagReady("head"))
            return;
        org.slf4j.LoggerFactory.getLogger("PCC-DIAG")
            .info(
                "[PCC-DIAG] pushPattern provider={} sendListEmpty={} nodeActive={} patternKnown={} circuit={} inputs={}",
                this.host.getBlockEntity()
                    .getBlockPos(),
                this.sendList.isEmpty(),
                this.mainNode.isActive(),
                this.patterns.contains(patternDetails),
                PatternProviderLogicImpl.getCircuitNumber(patternDetails)
                    .orElse(-1),
                PatternProviderLogicImpl.describeInputs(patternDetails));
    }

    @Inject(method = "pushPattern", at = @At("RETURN"))
    private void pCCard$diagReturn(CallbackInfoReturnable<Boolean> cir) {
        if (!isUpgradedWith(PCCard.PROGRAMMED_CIRCUIT_CARD_ITEM.get())) return;
        if (!cir.getReturnValue() && PatternProviderLogicImpl.diagReady("refused")) {
            org.slf4j.LoggerFactory.getLogger("PCC-DIAG")
                .info(
                    "[PCC-DIAG] pushPattern REFUSED provider={}",
                    this.host.getBlockEntity()
                        .getBlockPos());
        }
    }

    @Inject(method = "adapterAcceptsAll", at = @At("HEAD"), require = 0)
    private void pCCard$diagAccepts(appeng.helpers.patternprovider.PatternProviderTarget target,
        appeng.api.stacks.KeyCounter[] inputHolder, CallbackInfoReturnable<Boolean> cir) {
        if (!isUpgradedWith(PCCard.PROGRAMMED_CIRCUIT_CARD_ITEM.get()) || !PatternProviderLogicImpl.diagReady("accepts"))
            return;
        var log = org.slf4j.LoggerFactory.getLogger("PCC-DIAG");
        for (var list : inputHolder) {
            for (var in : list) {
                log.info(
                    "[PCC-DIAG] simulate insert {} x{} -> accepted {}",
                    in.getKey(),
                    in.getLongValue(),
                    target.insert(in.getKey(), in.getLongValue(), appeng.api.config.Actionable.SIMULATE));
            }
        }
    }

    @ModifyArg(
        method = "updatePatterns",
        at = @At(
            value = "INVOKE",
            target = "Lappeng/api/crafting/PatternDetailsHelper;decodePattern(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/level/Level;)Lappeng/api/crafting/IPatternDetails;"))
    private ItemStack updatePatterns(ItemStack stack) {
        if (!isUpgradedWith(PCCard.PROGRAMMED_CIRCUIT_CARD_ITEM.get())) return stack;

        return PatternProviderLogicImpl.updatePatterns(stack);
    }

    @ModifyArg(
        method = "pushPattern",
        at = @At(
            value = "INVOKE",
            target = "Lappeng/helpers/patternprovider/PatternProviderTarget;containsPatternInput(Ljava/util/Set;)Z"))
    private Set<AEKey> includeProgrammedCircuit(Set<AEKey> patternInputs,
        @Local(ordinal = 0, argsOnly = true) IPatternDetails patternDetails) {
        var result = new HashSet<>(patternInputs);
        PatternProviderLogicImpl.addCircuitToPatternInputs(patternDetails, result);
        return result;
    }

    @Inject(
        method = "pushPattern",
        at = @At(
            value = "INVOKE",
            target = "Lappeng/helpers/patternprovider/PatternProviderLogic;onPushPatternSuccess(Lappeng/api/crafting/IPatternDetails;)V"),
        require = 2)
    private void pushPattern(CallbackInfoReturnable<Boolean> cir,
        @Local(ordinal = 0, argsOnly = true) IPatternDetails patternDetails, @Local(ordinal = 0) Direction direction) {
        if (!isUpgradedWith(PCCard.PROGRAMMED_CIRCUIT_CARD_ITEM.get())) return;

        var be = this.host.getBlockEntity();
        var blockPoses = PatternProviderLogicImpl.getSendPos(be, direction);
        PatternProviderLogicImpl.setPCNumber(patternDetails, be, blockPoses);
    }

    /**
     * For AE2-fork by Cosmic-Frontier
     */
    @Inject(method = "onUpgradesChanged", at = @At(value = "HEAD"), require = 0)
    private void onUpgradesChanged(CallbackInfo ci) {
        this.updatePatterns();
    }
}
