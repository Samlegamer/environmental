package com.teamabnormals.environmental.common.slabfish;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.teamabnormals.environmental.common.network.message.SSyncBackpackTypeMessage;
import com.teamabnormals.environmental.common.slabfish.condition.SlabfishConditionContext;
import com.teamabnormals.environmental.core.Environmental;
import com.teamabnormals.environmental.core.registry.slabfish.EnvironmentalSlabfishSweaters;
import com.teamabnormals.environmental.core.registry.slabfish.EnvironmentalSlabfishTypes;
import net.minecraft.Util;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.RandomSource;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.util.thread.EffectiveSide;
import net.minecraftforge.network.PacketDistributor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * <p>Loads all slabfish from data packs as a server implementation of {@link SlabfishManager}.</p>
 *
 * @author Ocelot
 */
public class SlabfishLoader extends SimpleJsonResourceReloadListener implements SlabfishManager {
	private static final Logger LOGGER = LogManager.getLogger();
	private static final Gson GSON = new GsonBuilder()
			.registerTypeAdapter(ResourceLocation.class, new ResourceLocation.Serializer())
			.registerTypeAdapter(Component.class, new Component.Serializer())
			.registerTypeAdapter(BackpackType.class, new BackpackType.Deserializer())
			.create();

	static SlabfishLoader instance;

	private final Map<ResourceLocation, BackpackType> backpackTypes;

	public SlabfishLoader() {
		super(GSON, "environmental/slabfish");
		this.backpackTypes = new HashMap<>();
		instance = this;
	}

	@Override
	protected void apply(Map<ResourceLocation, JsonElement> object, ResourceManager resourceManager, ProfilerFiller profiler) {
		Map<ResourceLocation, BackpackType> parsedBackpackTypes = new HashMap<>();

		// Force this to exist if there is no overriding JSON
		parsedBackpackTypes.put(BROWN_BACKPACK.getRegistryName(), BROWN_BACKPACK);

		object.forEach(((location, json) ->
		{
			if (location.getPath().startsWith("backpack")) {
				ResourceLocation registryName = new ResourceLocation(location.getNamespace(), location.getPath().substring("backpack/".length()));
				try {
					parsedBackpackTypes.put(registryName, GSON.fromJson(json, BackpackType.class).setRegistryName(registryName));
				} catch (Exception e) {
					LOGGER.error("Parsing error loading custom backpack " + registryName, e);
				}
			}
		}));

		LOGGER.info("Loaded " + parsedBackpackTypes.size() + " Backpack Types");

		this.backpackTypes.clear();
		this.backpackTypes.putAll(parsedBackpackTypes);

		if (EffectiveSide.get().isServer()) {
			Environmental.PLAY.send(PacketDistributor.ALL.noArg(), new SSyncBackpackTypeMessage());
		}
	}

	@Override
	public Optional<BackpackType> getBackpackType(ResourceLocation registryName) {
		return Optional.ofNullable(this.backpackTypes.get(registryName));
	}

	@Override
	public Optional<SlabfishType> getSlabfishType(Registry<SlabfishType> registry, Predicate<SlabfishType> predicate, SlabfishConditionContext context) {
		return registry.stream().filter(slabfishType -> !registry.getKey(slabfishType).equals(EnvironmentalSlabfishTypes.SWAMP.location()) && predicate.test(slabfishType) && slabfishType.test(context)).max(Comparator.comparingInt(SlabfishType::priority));
	}

	@Override
	public Optional<SweaterType> getSweaterType(Registry<SweaterType> registry, ItemStack stack) {
		return registry.stream().filter(sweaterType -> sweaterType != registry.get(EnvironmentalSlabfishSweaters.EMPTY) && sweaterType.test(stack)).findFirst();
	}

	@Override
	public Optional<BackpackType> getBackpackType(ItemStack stack) {
		return this.backpackTypes.values().stream().filter(backpackType -> backpackType.test(stack)).findFirst();
	}

	@Override
	public Optional<SlabfishType> getRandomSlabfishType(Registry<SlabfishType> registry, Predicate<SlabfishType> predicate, RandomSource random) {
		return Util.getRandomSafe(registry.stream().filter(predicate).collect(Collectors.toList()), random);
	}


	@Override
	public BackpackType[] getAllBackpackTypes() {
		return this.backpackTypes.values().toArray(new BackpackType[0]);
	}
}
