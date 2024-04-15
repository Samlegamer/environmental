package com.teamabnormals.environmental.core.data.server;

import com.teamabnormals.blueprint.core.registry.BlueprintDataPackRegistries;
import com.teamabnormals.environmental.core.Environmental;
import com.teamabnormals.environmental.core.data.server.modifiers.EnvironmentalBiomeModifiers;
import com.teamabnormals.environmental.core.data.server.modifiers.EnvironmentalBiomeSlices;
import com.teamabnormals.environmental.core.other.EnvironmentalDamageTypes;
import com.teamabnormals.environmental.core.registry.EnvironmentalRegistries;
import com.teamabnormals.environmental.core.registry.slabfish.EnvironmentalSlabfishBackpacks;
import com.teamabnormals.environmental.core.registry.slabfish.EnvironmentalSlabfishSweaters;
import com.teamabnormals.environmental.core.registry.slabfish.EnvironmentalSlabfishTypes;
import com.teamabnormals.environmental.core.registry.EnvironmentalBiomes;
import com.teamabnormals.environmental.core.registry.EnvironmentalFeatures.EnvironmentalConfiguredFeatures;
import com.teamabnormals.environmental.core.registry.EnvironmentalFeatures.EnvironmentalPlacedFeatures;
import com.teamabnormals.environmental.core.registry.EnvironmentalNoiseParameters;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.DatapackBuiltinEntriesProvider;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class EnvironmentalDatapackBuiltinEntriesProvider extends DatapackBuiltinEntriesProvider {

	public static final RegistrySetBuilder BUILDER = new RegistrySetBuilder()
			.add(Registries.CONFIGURED_FEATURE, EnvironmentalConfiguredFeatures::bootstrap)
			.add(Registries.PLACED_FEATURE, EnvironmentalPlacedFeatures::bootstrap)
			.add(Registries.NOISE, EnvironmentalNoiseParameters::bootstrap)
			.add(Registries.BIOME, EnvironmentalBiomes::bootstrap)
			.add(Registries.DAMAGE_TYPE, EnvironmentalDamageTypes::bootstrap)
			.add(BlueprintDataPackRegistries.MODDED_BIOME_SLICES, EnvironmentalBiomeSlices::bootstrap)
			.add(ForgeRegistries.Keys.BIOME_MODIFIERS, EnvironmentalBiomeModifiers::bootstrap)
			.add(EnvironmentalRegistries.SLABFISH_TYPE, EnvironmentalSlabfishTypes::bootstrap)
			.add(EnvironmentalRegistries.SLABFISH_SWEATER, EnvironmentalSlabfishSweaters::bootstrap)
			.add(EnvironmentalRegistries.SLABFISH_BACKPACK, EnvironmentalSlabfishBackpacks::bootstrap);

	public EnvironmentalDatapackBuiltinEntriesProvider(PackOutput output, CompletableFuture<Provider> provider) {
		super(output, provider, BUILDER, Set.of(Environmental.MOD_ID));
	}
}