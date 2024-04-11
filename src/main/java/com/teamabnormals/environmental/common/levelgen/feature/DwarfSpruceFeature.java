package com.teamabnormals.environmental.common.levelgen.feature;

import com.mojang.serialization.Codec;
import com.teamabnormals.environmental.common.block.DwarfSpruceHeadBlock;
import com.teamabnormals.environmental.common.block.DwarfSprucePlantBlock;
import com.teamabnormals.environmental.common.levelgen.feature.configurations.DwarfSpruceConfiguration;
import com.teamabnormals.environmental.core.registry.EnvironmentalBlocks;
import com.teamabnormals.environmental.core.registry.EnvironmentalNoiseParameters;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

public class DwarfSpruceFeature extends Feature<DwarfSpruceConfiguration> {
	private volatile boolean initialized;
	private NormalNoise densityNoise;
	private NormalNoise heightNoise;

	public DwarfSpruceFeature(Codec<DwarfSpruceConfiguration> config) {
		super(config);
	}

	@Override
	public boolean place(FeaturePlaceContext<DwarfSpruceConfiguration> context) {
		if (!this.initialized) {
			synchronized (this) {
				if (!this.initialized) {
					this.densityNoise = NormalNoise.create(WorldgenRandom.Algorithm.LEGACY.newInstance(context.level().getSeed()).forkPositional().fromHashOf(EnvironmentalNoiseParameters.DWARF_SPRUCE_DENSITY.getKey().location()), EnvironmentalNoiseParameters.DWARF_SPRUCE_DENSITY.get());
					this.heightNoise = NormalNoise.create(WorldgenRandom.Algorithm.LEGACY.newInstance(context.level().getSeed()).forkPositional().fromHashOf(EnvironmentalNoiseParameters.DWARF_SPRUCE_HEIGHT_NOISE.getKey().location()), EnvironmentalNoiseParameters.DWARF_SPRUCE_HEIGHT_NOISE.get());
					this.initialized = true;
				}
			}
		}

		WorldGenLevel level = context.level();
		RandomSource random = context.random();
		BlockPos origin = context.origin();
		DwarfSpruceConfiguration config = context.config();

		int count = Math.max(0, (int) Math.ceil((this.densityNoise.getValue(origin.getX(), 0.0F, origin.getZ()) + config.noiseToCountRatio()) * config.density()));
		float patchheight = (float) this.heightNoise.getValue(origin.getX(), 0.0F, origin.getZ()) * 1.25F + 1.75F;

		MutableBlockPos mutable = new MutableBlockPos();
		boolean placed = false;

		for (int i = 0; i < count; i++) {
			int x = origin.getX() + random.nextInt(16);
			int z = origin.getZ() + random.nextInt(16);
			mutable.set(x, level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z), z);

			if (level.isEmptyBlock(mutable) && isDirt(level.getBlockState(mutable.below()))) {
				int height = Math.max(Math.round(patchheight + random.nextFloat() - random.nextFloat()), 1);

				for (int j = 1; j <= height; j++) {
					if (j == height || !level.isEmptyBlock(mutable.above())) {
						level.setBlock(mutable, EnvironmentalBlocks.DWARF_SPRUCE.get().defaultBlockState().setValue(DwarfSpruceHeadBlock.TOP, j != 1), 2);
						break;
					} else {
						level.setBlock(mutable, EnvironmentalBlocks.DWARF_SPRUCE_PLANT.get().defaultBlockState().setValue(DwarfSprucePlantBlock.BOTTOM, j == 1), 2);
					}

					mutable.move(Direction.UP);
				}

				placed = true;
			}
		}

		return placed;
	}
}