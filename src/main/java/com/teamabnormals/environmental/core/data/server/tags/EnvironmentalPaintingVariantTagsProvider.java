package com.teamabnormals.environmental.core.data.server.tags;

import com.teamabnormals.environmental.core.Environmental;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.PaintingVariantTagsProvider;
import net.minecraft.tags.PaintingVariantTags;
import net.minecraftforge.common.data.ExistingFileHelper;

import static com.teamabnormals.environmental.core.registry.EnvironmentalPaintingVariants.*;

public class EnvironmentalPaintingVariantTagsProvider extends PaintingVariantTagsProvider {

	public EnvironmentalPaintingVariantTagsProvider(DataGenerator generator, ExistingFileHelper helper) {
		super(generator, Environmental.MOD_ID, helper);
	}

	@Override
	public void addTags() {
		this.tag(PaintingVariantTags.PLACEABLE).add(SNAKE_BLOCK.get(), SLABFISH.get(), ARCHIVE.get(), OPTIMAL_AERODYNAMICS.get(), IN_PLAINS_SIGHT.get(), THE_PLACE_WITHIN_THE_PINES.get());
	}
}