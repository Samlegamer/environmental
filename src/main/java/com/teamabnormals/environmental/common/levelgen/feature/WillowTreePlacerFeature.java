package com.teamabnormals.environmental.common.levelgen.feature;

import com.mojang.serialization.Codec;
import com.teamabnormals.environmental.core.registry.EnvironmentalFeatures.EnvironmentalConfiguredFeatures;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class WillowTreePlacerFeature extends Feature<NoneFeatureConfiguration> {

	public WillowTreePlacerFeature(Codec<NoneFeatureConfiguration> config) {
		super(config);
	}

	public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
		RandomSource random = context.random();
		WorldGenLevel level = context.level();
		ChunkGenerator chunkgenerator = context.chunkGenerator();
		BlockPos origin = context.origin();

		Registry<ConfiguredFeature<?, ?>> registry = level.registryAccess().registryOrThrow(Registries.CONFIGURED_FEATURE);

		if (this.shouldPlaceWeepingWillow(level, origin, random))
			return registry.getOrThrow(EnvironmentalConfiguredFeatures.WEEPING_WILLOW).place(level, chunkgenerator, random, origin);
		else
			return registry.getOrThrow(EnvironmentalConfiguredFeatures.WILLOW).place(level, chunkgenerator, random, origin);
	}

	private boolean shouldPlaceWeepingWillow(WorldGenLevel level, BlockPos pos, RandomSource random) {
		if (level.getFluidState(pos).is(FluidTags.WATER))
			return random.nextInt(8) == 0;

		for (BlockPos blockpos : BlockPos.betweenClosed(pos.offset(-1, -1, -1), pos.offset(1, -1, 1))) {
			if (level.getFluidState(blockpos).is(FluidTags.WATER))
				return true;
		}

		return false;
	}
}