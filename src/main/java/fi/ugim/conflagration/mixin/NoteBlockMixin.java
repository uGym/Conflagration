package fi.ugim.conflagration.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.NoteBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(NoteBlock.class)
public class NoteBlockMixin {

    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    public void onUse(BlockState param0, Level param1, BlockPos param2, Player param3, InteractionHand param4, BlockHitResult param5, CallbackInfoReturnable<InteractionResult> cir) {
        cir.setReturnValue(InteractionResult.SUCCESS);
    }

    @Inject(method = "updateShape", at = @At("HEAD"), cancellable = true)
    public void onUpdateShape(BlockState oldState, Direction direction, BlockState param2, LevelAccessor level, BlockPos param4, BlockPos param5, CallbackInfoReturnable<BlockState> cir) {
        if (direction == Direction.DOWN) {
            cir.setReturnValue(oldState);
        }
    }

    @Inject(method = "neighborChanged", at = @At("HEAD"), cancellable = true)
    public void onNeighborChanged(BlockState param0, Level param1, BlockPos param2, Block param3, BlockPos param4, boolean param5, CallbackInfo ci) {
        ci.cancel();
    }

    @Inject(method = "attack", at = @At("HEAD"), cancellable = true)
    public void onAttack(BlockState param0, Level param1, BlockPos param2, Player param3, CallbackInfo ci) {
        ci.cancel();
    }

    @Inject(method = "triggerEvent", at = @At("HEAD"), cancellable = true)
    public void onTriggerEvent(BlockState param0, Level param1, BlockPos param2, int param3, int param4, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(false);
    }

}
