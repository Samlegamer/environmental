package com.teamabnormals.environmental.common.levelgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.ProbabilityFeatureConfiguration;

public class CoarseDirtPatchFeature extends Feature<ProbabilityFeatureConfiguration> {

    public CoarseDirtPatchFeature(Codec<ProbabilityFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<ProbabilityFeatureConfiguration> context) {
        ProbabilityFeatureConfiguration config = context.config();
        WorldGenLevel worldIn = context.level();
        RandomSource rand = context.random();
        BlockPos pos = context.origin();
        int i = 0;
        BlockPos.MutableBlockPos blockpos$mutable = pos.mutable();
        if (rand.nextFloat() < config.probability) {
            int k = rand.nextInt(6) + 2;
            for (int l = pos.getX() - k; l <= pos.getX() + k; ++l) {
                for (int i1 = pos.getZ() - k; i1 <= pos.getZ() + k; ++i1) {
                    int j1 = l - pos.getX();
                    int k1 = i1 - pos.getZ();
                    if (j1 * j1 + k1 * k1 <= k * k) {
                        blockpos$mutable.set(l, worldIn.getHeight(Heightmap.Types.WORLD_SURFACE, l, i1) - 1, i1);
                        if (isDirt(worldIn.getBlockState(blockpos$mutable))) {
                            worldIn.setBlock(blockpos$mutable, Blocks.COARSE_DIRT.defaultBlockState(), 2);
                        }
                    }
                }
            }

            ++i;
        }

        return i > 0;
    }
}