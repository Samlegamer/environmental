package com.teamabnormals.environmental.common.item.explorer;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableMultimap.Builder;
import com.google.common.collect.Multimap;
import com.teamabnormals.environmental.client.model.ArchitectBeltModel;
import com.teamabnormals.environmental.core.Environmental;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

import java.util.UUID;
import java.util.function.Consumer;

@EventBusSubscriber(modid = Environmental.MOD_ID)
public class ArchitectBeltItem extends ExplorerArmorItem {
	private static final String NBT_TAG = "ArchitectBeltUses";

	public ArchitectBeltItem(Properties properties) {
		super(ArmorItem.Type.LEGGINGS, properties);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void initializeClient(Consumer<IClientItemExtensions> consumer) {
		consumer.accept(new IClientItemExtensions() {
			@Override
			public HumanoidModel<?> getHumanoidArmorModel(LivingEntity entity, ItemStack stack, EquipmentSlot slot, HumanoidModel<?> properties) {
				return ArchitectBeltModel.INSTANCE;
			}
		});
	}

	@Override
	public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlot slot, ItemStack stack) {
		Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
		builder.putAll(super.getDefaultAttributeModifiers(this.getEquipmentSlot()));
		UUID uuid = UUID.fromString("D8499B04-0E66-4726-AB29-64469D734E0D");

		int uses = stack.getTag().getInt(NBT_TAG);
		int increase = getIncreaseForUses(uses);

		builder.put(ForgeMod.BLOCK_REACH.get(), new AttributeModifier(uuid, "Reach modifier", increase, AttributeModifier.Operation.ADDITION));

		return slot == this.getEquipmentSlot() ? builder.build() : super.getDefaultAttributeModifiers(slot);
	}

	@SubscribeEvent
	public static void placeBlockEvent(BlockEvent.EntityPlaceEvent event) {
		Entity entity = event.getEntity();
		BlockState state = event.getPlacedBlock();
		if (entity instanceof Player player && !state.is(Blocks.WATER)) {
			ItemStack stack = player.getItemBySlot(EquipmentSlot.LEGS);
			Item item = stack.getItem();
			if (item instanceof ArchitectBeltItem) {
				((ArchitectBeltItem) item).levelUp(stack, player);
			}
		}
	}

	@Override
	public String getUsesTag() {
		return NBT_TAG;
	}

	@Override
	public int[] getLevelCaps() {
		return new int[]{0, 100, 500, 1000, 2500};
	}
}
