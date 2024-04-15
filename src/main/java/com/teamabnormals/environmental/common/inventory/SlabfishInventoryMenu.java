package com.teamabnormals.environmental.common.inventory;

import com.teamabnormals.environmental.common.entity.animal.slabfish.Slabfish;
import com.teamabnormals.environmental.common.slabfish.SlabfishType;
import com.teamabnormals.environmental.core.Environmental;
import com.teamabnormals.environmental.core.registry.EnvironmentalMenuTypes;
import com.teamabnormals.environmental.core.registry.EnvironmentalRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;

public class SlabfishInventoryMenu extends AbstractContainerMenu {

	private static final ResourceLocation[] SLOT_INDEX_NAMES = {
			new ResourceLocation(Environmental.MOD_ID, "item/slabfish_sweater_slot"),
			new ResourceLocation(Environmental.MOD_ID, "item/slabfish_backpack_slot"),
			new ResourceLocation(Environmental.MOD_ID, "item/slabfish_backpack_type_slot")
	};

	private final Container slabfishInventory;
	private final Slabfish slabfish;

	public SlabfishInventoryMenu(int windowId, Inventory playerInventory, @Nullable SlabfishInventory slabfishInventory, @Nullable Slabfish slabfish) {
		super(EnvironmentalMenuTypes.SLABFISH_INVENTORY.get(), windowId);
		this.slabfishInventory = slabfishInventory;
		this.slabfish = slabfish;
		if (slabfishInventory != null && slabfish != null) {
			slabfishInventory.startOpen(playerInventory.player);
			for (int i = 0; i < 3; i++) {
				this.addSlot(new Slot(slabfishInventory, i, 8, 18 + i * 18) {
					@Override
					public boolean mayPlace(ItemStack stack) {
						return slabfishInventory.canPlaceItem(this.getSlotIndex(), stack);
					}

					@Override
					public int getMaxStackSize() {
						return slabfishInventory.getSlotStackLimit(this.getSlotIndex());
					}

					@Override
					public boolean isActive() {
						if (this.getSlotIndex() != 2)
							return true;
						SlabfishType slabfishType = slabfish.getSlabfishType();
						return slabfish.hasBackpack() && (slabfishType.backpack().isEmpty() || EnvironmentalRegistries.slabfishBackpacks(slabfish.level()).get(slabfishType.backpack().get()) == null);
					}
				}).setBackground(InventoryMenu.BLOCK_ATLAS, SLOT_INDEX_NAMES[i]);
			}
			for (int k = 0; k < 3; ++k) {
				for (int l = 0; l < 5; ++l) {
					this.addSlot(new Slot(slabfishInventory, 3 + l + k * 5, 80 + l * 18, 18 + k * 18) {
						@Override
						public boolean isActive() {
							return slabfish.hasBackpack();
						}
					});
				}
			}
		}
		for (int i1 = 0; i1 < 3; ++i1) {
			for (int k1 = 0; k1 < 9; ++k1) {
				this.addSlot(new Slot(playerInventory, k1 + i1 * 9 + 9, 8 + k1 * 18, 102 + i1 * 18 + -18));
			}
		}
		for (int j1 = 0; j1 < 9; ++j1) {
			this.addSlot(new Slot(playerInventory, j1, 8 + j1 * 18, 142));
		}
	}

	public SlabfishInventoryMenu(int id, Inventory playerInventory) {
		this(id, playerInventory, null, null);
	}

	@Override
	public boolean stillValid(Player player) {
		return this.slabfishInventory.stillValid(player) && this.slabfish.isAlive() && this.slabfish.distanceTo(player) < 8.0F;
	}

	@Override
	public ItemStack quickMoveStack(Player player, int index) {
		ItemStack itemstack = ItemStack.EMPTY;
		Slot slot = this.slots.get(index);
		if (slot != null && slot.hasItem()) {
			ItemStack slotStack = slot.getItem();
			itemstack = slotStack.copy();
			int i = this.slabfishInventory.getContainerSize();
			if (index < i) {
				if (!this.moveItemStackTo(slotStack, i, this.slots.size(), true)) {
					return ItemStack.EMPTY;
				}
			} else if (this.getSlot(0).mayPlace(slotStack) && !this.getSlot(0).hasItem() && !this.moveItemStackTo(slotStack, 0, 1, false)) {
				return ItemStack.EMPTY;
			} else if (this.getSlot(1).mayPlace(slotStack) && !this.getSlot(1).hasItem() && !this.moveItemStackTo(slotStack, 1, 2, false)) {
				return ItemStack.EMPTY;
			} else if (this.getSlot(2).mayPlace(slotStack) && !this.getSlot(2).hasItem() && !this.moveItemStackTo(slotStack, 2, 3, false)) {
				return ItemStack.EMPTY;
			} else if (i <= 3 || !this.moveItemStackTo(slotStack, 3, i, false)) {
				int firstInvSlot = 18;
				int firstHotbarSlot = 45;
				int lastHotbarSlot = 53;
				if (index >= firstHotbarSlot && index <= lastHotbarSlot) {
					if (!this.moveItemStackTo(slotStack, firstInvSlot, firstHotbarSlot, false)) {
						return ItemStack.EMPTY;
					}
				} else if (index >= i && index < firstHotbarSlot) {
					if (!this.moveItemStackTo(slotStack, firstHotbarSlot, lastHotbarSlot + 1, false)) {
						return ItemStack.EMPTY;
					}
				}
			}

			if (slotStack.isEmpty()) {
				slot.set(ItemStack.EMPTY);
			} else {
				slot.setChanged();
			}
		}

		return itemstack;
	}

	@Override
	public void removed(Player player) {
		super.removed(player);
		this.slabfish.playersUsing--;
		this.slabfishInventory.stopOpen(player);
	}
}
