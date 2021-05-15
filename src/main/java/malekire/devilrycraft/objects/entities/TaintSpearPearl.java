//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package malekire.devilrycraft.objects.entities;

import malekire.devilrycraft.common.DevilryEntities;
import malekire.devilrycraft.common.DevilryWeaponItems;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.item.Item;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;


public class TaintSpearPearl extends ThrownItemEntity {






    public TaintSpearPearl(DevilryEntities<TaintSpearPearl> entityEntityType, World world) {
        super(entityEntityType, world);

    }
    public TaintSpearPearl(World world, LivingEntity owner) {
        super(DevilryEntities.TAINT_SPEAR_PEARL, owner, world);
    }
    @Environment(EnvType.CLIENT)
    public TaintSpearPearl(World world, double x, double y, double z) {
        super(DevilryEntities.TAINT_SPEAR_PEARL, x, y, z, world);
    }




    protected Item getDefaultItem() {
        return DevilryWeaponItems.TAINT_CRYSTAL_SPEAR;
    }

    protected void onEntityHit(EntityHitResult entityHitResult) {
        super.onEntityHit(entityHitResult);
        entityHitResult.getEntity().damage(DamageSource.thrownProjectile(this, this.getOwner()), 0.0F);
    }

    protected void onCollision(HitResult hitResult) {
        super.onCollision(hitResult);
        Entity entity = this.getOwner();

        for(int i = 0; i < 32; ++i) {
            this.world.addParticle(ParticleTypes.PORTAL, this.getX(), this.getY() + this.random.nextDouble() * 2.0D, this.getZ(), this.random.nextGaussian(), 0.0D, this.random.nextGaussian());
        }

        if (!this.world.isClient && !this.removed) {
            if (entity instanceof ServerPlayerEntity) {
                ServerPlayerEntity serverPlayerEntity = (ServerPlayerEntity)entity;
                if (serverPlayerEntity.networkHandler.getConnection().isOpen()) {


                    if (entity.hasVehicle()) {
                        entity.stopRiding();
                    }

                    entity.requestTeleport(this.getX(), this.getY(), this.getZ());

                }
            } else if (entity != null) {
                entity.requestTeleport(this.getX(), this.getY(), this.getZ());
                entity.fallDistance = 0.0F;
            }

            this.remove();
        }

    }

    public void tick() {
        Entity entity = this.getOwner();
        if (entity instanceof PlayerEntity && !entity.isAlive()) {
            this.remove();
        } else {
            super.tick();
        }

    }

    @Nullable
    public Entity moveToWorld(ServerWorld destination) {
        Entity entity = this.getOwner();
        if (entity != null && entity.world.getRegistryKey() != destination.getRegistryKey()) {
            this.setOwner((Entity)null);
        }

        return super.moveToWorld(destination);
    }
}
