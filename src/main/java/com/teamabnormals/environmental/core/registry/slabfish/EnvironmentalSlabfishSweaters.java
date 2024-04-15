package com.teamabnormals.environmental.core.registry.slabfish;

import com.teamabnormals.environmental.common.slabfish.SweaterType;
import com.teamabnormals.environmental.core.Environmental;
import com.teamabnormals.environmental.core.registry.EnvironmentalRegistries;
import net.minecraft.Util;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.item.DyeColor;

import java.util.Optional;

public class EnvironmentalSlabfishSweaters {
	public static final ResourceKey<SweaterType> EMPTY = createKey("empty");

	public static void bootstrap(BootstapContext<SweaterType> context) {
		context.register(EMPTY, new SweaterType(Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty()));
		for (DyeColor color : DyeColor.values()) {
			ResourceKey<SweaterType> key = createKey(color.getName());
			context.register(key, SweaterType.create(
					Component.translatable(Util.makeDescriptionId("slabfish.sweater", key.location())),
					new ResourceLocation(key.location().getNamespace(), "sweater/" + key.location().getPath()),
					Sheep.ITEM_BY_DYE.get(color).asItem()));
		}
	}

	public static ResourceKey<SweaterType> createKey(String name) {
		return ResourceKey.create(EnvironmentalRegistries.SLABFISH_SWEATER, new ResourceLocation(Environmental.MOD_ID, name));
	}
}
