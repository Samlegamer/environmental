package com.teamabnormals.environmental.common.block;

import com.teamabnormals.blueprint.core.util.item.filling.TargetedItemCategoryFiller;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.IForgeShearable;

import javax.annotation.Nullable;

public class HangingWisteriaLeavesBlock extends Block implements IForgeShearable {
	public static final EnumProperty<DoubleBlockHalf> HALF = BlockStateProperties.DOUBLE_BLOCK_HALF;
	protected static final VoxelShape WISTERIA_VINE_TOP = Block.box(1, 0, 1, 15, 16, 15);
	protected static final VoxelShape WISTERIA_VINE_BOTTOM = Block.box(2, 7, 2, 14, 16, 14);
	private static final TargetedItemCategoryFiller FILLER = new TargetedItemCategoryFiller(() -> Items.VINE);

	public HangingWisteriaLeavesBlock(Block.Properties properties) {
		super(properties);
		this.registerDefaultState(this.stateDefinition.any().setValue(HALF, DoubleBlockHalf.UPPER));
	}

	public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
		return state.getValue(HALF) == DoubleBlockHalf.UPPER ? WISTERIA_VINE_TOP : WISTERIA_VINE_BOTTOM;
	}

	@Override
	public VoxelShape getCollisionShape(BlockState state, BlockGetter reader, BlockPos pos, CollisionContext context) {
		return Shapes.empty();
	}

	@Override
	public void neighborChanged(BlockState state, Level world, BlockPos pos, Block block, BlockPos fromPos, boolean moving) {
		if (state.getValue(HALF) == DoubleBlockHalf.UPPER) {
			if (world.getBlockState(pos.above()).isAir()) {
				world.removeBlock(pos, false);
			} else if (world.getBlockState(pos.below()).isAir()) {
				world.setBlockAndUpdate(pos, state.setValue(HALF, DoubleBlockHalf.LOWER));
			}
		} else if (world.getBlockState(pos.above()).isAir()) {
			world.removeBlock(pos, false);
		}
	}

	public int getLightBlock(BlockState state, BlockGetter worldIn, BlockPos pos) {
		return 1;
	}

	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(HALF);
	}

	@OnlyIn(Dist.CLIENT)
	public void animateTick(BlockState stateIn, Level worldIn, BlockPos pos, RandomSource rand) {
		if (worldIn.isRainingAt(pos.above())) {
			if (rand.nextInt(15) == 1) {
				BlockPos blockpos = pos.below();
				BlockState blockstate = worldIn.getBlockState(blockpos);
				if (!blockstate.canOcclude() || !blockstate.isFaceSturdy(worldIn, blockpos, Direction.UP)) {
					double d0 = (float) pos.getX() + rand.nextFloat();
					double d1 = (double) pos.getY() - 0.05D;
					double d2 = (float) pos.getZ() + rand.nextFloat();
					worldIn.addParticle(ParticleTypes.DRIPPING_WATER, d0, d1, d2, 0.0D, 0.0D, 0.0D);
				}
			}
		}
	}

	@Override
	public boolean isLadder(BlockState state, LevelReader world, BlockPos pos, LivingEntity entity) {
		return true;
	}

	protected boolean isStateValid(Level worldIn, BlockPos pos) {
		BlockState state = worldIn.getBlockState(pos.above());
		return state.is(this) || state.is(BlockTags.LEAVES) || state.is(BlockTags.LOGS);
	}

	@Override
	@Nullable
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		Level world = context.getLevel();
		BlockPos pos = context.getClickedPos();
		if (isStateValid(world, pos)) {
			if (world.getBlockState(pos.above()) == defaultBlockState().setValue(HALF, DoubleBlockHalf.LOWER)) {
				world.setBlockAndUpdate(pos.above(), world.getBlockState(pos.above()).setValue(HALF, DoubleBlockHalf.UPPER));
			}
			return defaultBlockState().setValue(HALF, DoubleBlockHalf.LOWER);
		}
		return null;
	}

	@Override
	public void fillItemCategory(CreativeModeTab group, NonNullList<ItemStack> items) {
		FILLER.fillItem(this.asItem(), group, items);
	}
}