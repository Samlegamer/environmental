package com.teamabnormals.environmental.common.slabfish.condition;

import com.google.common.base.Suppliers;
import com.mojang.serialization.Codec;
import com.teamabnormals.environmental.common.entity.animal.slabfish.Slabfish;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * <p>A context used for determining what kinds of slabfish can be spawned.</p>
 *
 * @author Ocelot
 */
public class SlabfishConditionContext {
	private final ServerLevel level;
	private final Event event;
	private final Supplier<RandomSource> random;
	private final Supplier<String> name;
	private final Supplier<BlockPos> pos;
	private final Supplier<Holder<Biome>> biome;
	private final Supplier<Boolean> inRaid;
	private final Supplier<BlockState> inBlock;
	private final Supplier<FluidState> inFluid;
	private final Supplier<Time> time;
	private final Supplier<Integer> light;
	private final Map<LightLayer, Supplier<Integer>> lightTypes;
	private final Supplier<ResourceLocation> dimension;
	private final Supplier<ResourceLocation> slabfishType;
	private final Supplier<Boolean> breederInsomnia;
	private final Pair<ResourceLocation, ResourceLocation> parents;

	private SlabfishConditionContext(Slabfish slabfish, Event event, @Nullable ServerPlayer breeder, @Nullable Slabfish parent1, @Nullable Slabfish parent2) {
		this.level = (ServerLevel) slabfish.getCommandSenderWorld();
		this.event = event;
		this.random = Suppliers.memoize(level::getRandom);
		this.name = Suppliers.memoize(() -> slabfish.getDisplayName().getString().trim());
		this.pos = Suppliers.memoize(() -> new BlockPos(slabfish.blockPosition()));
		this.biome = Suppliers.memoize(() -> level.getBiome(this.pos.get()));
		this.inRaid = Suppliers.memoize(() -> level.getRaidAt(this.pos.get()) != null);
		this.inBlock = Suppliers.memoize(() -> level.getBlockState(this.pos.get()));
		this.inFluid = Suppliers.memoize(() -> level.getFluidState(this.pos.get()));
		this.time = Suppliers.memoize(() -> level.isDay() ? Time.DAY : Time.NIGHT);
		this.light = Suppliers.memoize(() -> level.getMaxLocalRawBrightness(this.pos.get()));
		this.lightTypes = new HashMap<>();
		for (LightLayer lightType : LightLayer.values())
			this.lightTypes.put(lightType, Suppliers.memoize(() -> level.getBrightness(lightType, this.pos.get())));
		this.dimension = Suppliers.memoize(() -> level.dimension().location());
		this.slabfishType = Suppliers.memoize(slabfish::getSlabfishTypeLocation);
		this.breederInsomnia = Suppliers.memoize(() -> breeder != null && breeder.getStats().getValue(Stats.CUSTOM.get(Stats.TIME_SINCE_REST)) >= 72000 && level.isNight());
		this.parents = parent1 != null && parent2 != null ? new ImmutablePair<>(parent1.getSlabfishTypeLocation(), parent2.getSlabfishTypeLocation()) : null;
	}

	/**
	 * Fetches a new context for the specified entity when spawned.
	 *
	 * @param slabfish The entity to focus on
	 * @return A new context with that slabfish as the focus
	 */
	public static SlabfishConditionContext spawned(Slabfish slabfish) {
		return new SlabfishConditionContext(slabfish, Event.SPAWN, null, null, null);
	}

	/**
	 * Fetches a new context for the specified entity when renamed.
	 *
	 * @param slabfish The entity to focus on
	 * @return A new context with that slabfish as the focus
	 */
	public static SlabfishConditionContext rename(Slabfish slabfish) {
		return new SlabfishConditionContext(slabfish, Event.RENAME, null, null, null);
	}

	/**
	 * Fetches a new context for the specified entity when struck by lightning.
	 *
	 * @param slabfish The entity to focus on
	 * @return A new context with that slabfish as the focus
	 */
	public static SlabfishConditionContext lightning(Slabfish slabfish) {
		return new SlabfishConditionContext(slabfish, Event.LIGHTNING, null, null, null);
	}

	/**
	 * Fetches a new context for the specified entity with the two parents.
	 *
	 * @param slabfish The entity to focus on
	 * @param breeder  The player that bred the two parents together
	 * @param parent1  The first parent of breeding with
	 * @param parent2  The second parent of breeding
	 * @return A new context with that slabfish as the focus
	 */
	public static SlabfishConditionContext breeding(Slabfish slabfish, @Nullable ServerPlayer breeder, Slabfish parent1, Slabfish parent2) {
		return new SlabfishConditionContext(slabfish, Event.BREED, breeder, parent1, parent2);
	}

	/**
	 * @return The type of event this context is fired under
	 */
	public Event getEvent() {
		return event;
	}

	/**
	 * @return The slabfish world random number generator
	 */
	public RandomSource getRandom() {
		return this.random.get();
	}

	/**
	 * @return The name of the slabfish
	 */
	public String getName() {
		return this.name.get();
	}

	/**
	 * @return The position of the slabfish
	 */
	public BlockPos getPos() {
		return this.pos.get();
	}

	/**
	 * @return The biome the slabfish is in
	 */
	public Holder<Biome> getBiome() {
		return this.biome.get();
	}

	/**
	 * @return The time
	 */
	public Time getTime() {
		return this.time.get();
	}

	/**
	 * @return Whether or not a raid is currently ongoing
	 */
	public boolean isInRaid() {
		return this.inRaid.get();
	}

	/**
	 * @return Whether or not the slabfish is currently in that tag
	 */
	public Holder<Block> getBlock() {
		return this.inBlock.get().getBlockHolder();
	}

	/**
	 * @return Whether or not the slabfish is currently in that tag
	 */
	public Holder<Fluid> getFluid() {
		return this.inFluid.get().holder();
	}

	/**
	 * @return The light value at the slabfish position
	 */
	public int getLight() {
		return this.light.get();
	}

	/**
	 * Fetches light for the specified type of light
	 *
	 * @param lightType The type of light to get
	 * @return The sky light value at the slabfish position
	 */
	public int getLightFor(LightLayer lightType) {
		return this.lightTypes.get(lightType).get();
	}

	/**
	 * @return The dimension the slabfish is in
	 */
	public ResourceLocation getDimension() {
		return this.dimension.get();
	}

	/**
	 * @return The type of slabfish this slabfish was before trying to undergo a change
	 */
	public ResourceLocation getSlabfishType() {
		return this.slabfishType.get();
	}

	/**
	 * @return Whether or not the player that bred the two slabfish together has insomnia
	 */
	public boolean isBreederInsomnia() {
		return this.breederInsomnia.get();
	}

	/**
	 * @return The types of slabfish the parents were or null if there are no parents
	 */
	@Nullable
	public Pair<ResourceLocation, ResourceLocation> getParentTypes() {
		return this.parents;
	}

	/**
	 * The type a context can be fired under.
	 */
	public enum Event implements StringRepresentable {
		SPAWN("spawn"),
		RENAME("rename"),
		LIGHTNING("lightning"),
		BREED("breed");

		private static final Map<String, Event> BY_NAME = Arrays.stream(values()).collect(Collectors.toMap(Event::getSerializedName, Function.identity()));
		public static final Codec<Event> CODEC = StringRepresentable.fromEnum(Event::values);

		private final String name;

		Event(String name) {
			this.name = name;
		}

		@Nullable
		public static Event byName(String name) {
			return BY_NAME.get(name);
		}

		@Override
		public String getSerializedName() {
			return this.name;
		}
	}

	public enum Time implements StringRepresentable {
		DAY("day"),
		NIGHT("night");

		private static final Map<String, Time> BY_NAME = Arrays.stream(values()).collect(Collectors.toMap(Time::getSerializedName, Function.identity()));
		public static final Codec<Time> CODEC = StringRepresentable.fromEnum(Time::values);

		private final String name;

		Time(String name) {
			this.name = name;
		}

		@Nullable
		public static Time byName(String name) {
			return BY_NAME.get(name);
		}

		@Override
		public String getSerializedName() {
			return this.name;
		}
	}
}
