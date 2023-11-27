package com.teamabnormals.environmental.common.entity.ai.goal;

import com.teamabnormals.environmental.common.entity.animal.Tapir;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

public class HuntFloraGoal extends Goal {
	private final Tapir tapir;
	private Tapir partner;
	private int runDelay;
	private int lookTimer;
	private int loveTime;
	private Vec3 lookVector;

	private static final TargetingConditions PARTNER_TARGETING = TargetingConditions.forNonCombat().range(8.0D).ignoreLineOfSight();

	public HuntFloraGoal(Tapir tapir) {
		this.tapir = tapir;
		this.setFlags(EnumSet.of(Flag.MOVE, Flag.JUMP, Flag.LOOK));
	}

	@Override
	public boolean canUse() {
		if (this.runDelay > 0) {
			--this.runDelay;
			return false;
		} else {
			this.runDelay = this.adjustedTickDelay(20);
			if (this.tapir.hasTarget()) {
				return true;
			} else {
				return this.tapir.getTrackingTime() > 0 && this.tapir.getTrackingPos().isPresent();
			}
		}
	}

	@Override
	public boolean canContinueToUse() {
		return this.tapir.hasTarget() && this.tapir.getTrackingTime() != 0;
	}

	@Override
	public void start() {
		this.lookVector = new Vec3(1.0D, 0.0D, 1.0D);
		this.moveToTarget();
		this.tapir.setTracking(true);
	}

	@Override
	public void stop() {
		this.tapir.resetLove();
		this.tapir.setTracking(false);
		this.tapir.setTrackingPos(Optional.empty());
		this.tapir.setTrackingState(Optional.empty());
	}

	@Override
	public void tick() {
		int trackingTime = this.tapir.getTrackingTime();
		Optional<BlockPos> optionalPos = this.tapir.getTrackingPos();
		Vec3 pos = this.tapir.position();

		if (optionalPos.isPresent()) {
			BlockPos blockPos = optionalPos.get();
			Vec3 vector3d = new Vec3((blockPos.getX() + 0.5D) - pos.x(), 0.0D, (blockPos.getZ() + 0.5D) - pos.z()).normalize();

			this.tapir.getLookControl().setLookAt(pos.x() + vector3d.x() * this.lookVector.x(), this.tapir.getY() - 0.6D + this.lookVector.y(), pos.z() + vector3d.z() * this.lookVector.z(), (float) (this.tapir.getMaxHeadYRot() + 20), (float) this.tapir.getMaxHeadXRot());

			if (blockPos.closerThan(this.tapir.blockPosition(), 4.0D)) {
				if (trackingTime > 0)
					this.tapir.setTrackingTime(-800);
			} else {
				if (this.lookTimer-- <= 0) {
					this.lookTimer = this.adjustedTickDelay(18 + this.tapir.getRandom().nextInt(9));
					this.lookVector = new Vec3((double) this.tapir.getRandom().nextFloat() * 1.2D, (double) this.tapir.getRandom().nextFloat() * 0.4D, (double) this.tapir.getRandom().nextFloat() * 1.2D);
				}

				this.moveToTarget();
			}

			if (blockPos.closerThan(this.tapir.blockPosition(), 3.0D)) {
				if (this.tapir.isInLove()) {
					if (this.partner == null) {
						this.partner = this.getFreePartner();
					}

					if (partner != null && partner.isInLove()) {
						this.tapir.getLookControl().setLookAt(partner, 10.0F, (float)this.tapir.getMaxHeadXRot());
						this.loveTime++;
						if (this.tapir.distanceToSqr(partner) < 3.0D && loveTime > 30) {
							this.tapir.spawnChildFromBreeding((ServerLevel)this.tapir.level, partner);
						}
					}
				}
			}
		}
	}

	@Nullable
	private Tapir getFreePartner() {
		List<? extends Tapir> list = this.tapir.level.getNearbyEntities(Tapir.class, PARTNER_TARGETING, this.tapir, this.tapir.getBoundingBox().inflate(8.0D));
		double d0 = Double.MAX_VALUE;
		Tapir animal = null;

		for(Tapir animal1 : list) {
			if (this.tapir.canMate(animal1) && this.tapir.distanceToSqr(animal1) < d0) {
				animal = animal1;
				d0 = this.tapir.distanceToSqr(animal1);
			}
		}

		return animal;
	}

	private void moveToTarget() {
		Optional<BlockPos> optionalPos = this.tapir.getTrackingPos();
		if (optionalPos.isPresent()) {
			BlockPos pos = optionalPos.get();
			this.tapir.getNavigation().moveTo((double) ((float) pos.getX()) + 0.5D, pos.getY() + 1, (double) ((float) pos.getZ()) + 0.5D, 1.3D);
		}
	}
}