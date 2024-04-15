package com.teamabnormals.environmental.core.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction.Plane;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.PinkPetalsBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(PinkPetalsBlock.class)
public class PinkPetalsBlockMixin {

	@ModifyVariable(method = "<init>", at = @At("HEAD"), ordinal = 0, argsOnly = true)
	private static BlockBehaviour.Properties injected(Properties properties) {
		return properties.replaceable();
	}

	@Inject(method = "performBonemeal", at = @At("HEAD"), cancellable = true)
	public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state, CallbackInfo ci) {
		if (state.getValue(PinkPetalsBlock.AMOUNT) >= 4) {
			List<BlockPos> blocks = BlockPos.betweenClosedStream(pos.offset(-2, -1, -2), pos.offset(2, 1, 2))
					.filter(newPos -> Mth.abs(pos.getX() - newPos.getX()) != 2 || Mth.abs(pos.getZ() - newPos.getZ()) != 2)
					.filter(newPos -> state.canSurvive(level, newPos) && (level.isEmptyBlock(newPos) || (level.getBlockState(newPos).is(Blocks.PINK_PETALS) && level.getBlockState(newPos).getValue(PinkPetalsBlock.AMOUNT) < 4)))
					.map(BlockPos::immutable)
					.toList();

			if (!blocks.isEmpty()) {
				BlockPos newPos = blocks.get(random.nextInt(blocks.size()));

				BlockState currentState = level.getBlockState(newPos);
				if (currentState.is(Blocks.PINK_PETALS)) {
					level.setBlockAndUpdate(newPos, currentState.setValue(PinkPetalsBlock.AMOUNT, level.getBlockState(newPos).getValue(PinkPetalsBlock.AMOUNT) + 1));
				} else if (level.isEmptyBlock(newPos)) {
					level.setBlockAndUpdate(newPos, Blocks.PINK_PETALS.defaultBlockState().setValue(PinkPetalsBlock.FACING, Plane.HORIZONTAL.getRandomDirection(random)));
				}

				ci.cancel();
			}
		}
	}
}
