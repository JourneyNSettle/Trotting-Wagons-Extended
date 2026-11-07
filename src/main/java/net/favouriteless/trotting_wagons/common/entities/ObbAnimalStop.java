package net.favouriteless.trotting_wagons.common.entities;

import com.kltyton.bonehitboxlib.api.bone.collision.ObbCollisionMode;
import com.kltyton.bonehitboxlib.api.entity.BoneHitboxEntity;
import com.kltyton.bonehitboxlib.api.geometry.query.ObbServerGeometry;
import com.kltyton.bonehitboxlib.api.geometry.snapshot.ObbPartGeometrySnapshot;
import com.kltyton.bonehitboxlib.geometry.obb.ObbGeometry;
import com.kltyton.bonehitboxlib.geometry.obb.ObbGeometry.Overlap;
import net.favouriteless.trotting_wagons.client.render.entity.YellowBoxLayer;
import net.favouriteless.trotting_wagons.common.entities.base.AbstractWagon;
import com.kltyton.bonehitboxlib.geometry.obb.ObbGeometry.SweepHit;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.WeakHashMap;

/**
 * BoneHitboxLib stops only the local player on foot. A ridden or loose animal
 * moves on its own, so the hard yellow boxes push that animal back out.
 */
public final class ObbAnimalStop {

    private static final double MAX_PUSH = 4.0D;
    private static final double SKIN = 0.08D;
    /** A face this upright is a roof the horse may stand on. */
    private static final double FLOOR_DOT = 0.65D;
    private static final double FLOOR_SKIN = 0.001D;
    /** Last feet position that was outside every yellow box. */
    private static final Map<Animal, Vec3> LAST_SAFE = new WeakHashMap<>();

    private ObbAnimalStop() {}

    public static void afterTick(Animal animal) {
        if(!animal.isAlive() || animal.getVehicle() instanceof AbstractWagon)
            return;
        if(animal.level().isClientSide) {
            // The yellow cubes are captured while the wagon is drawn. Push the ridden horse
            // out before LocalPlayer sends that position to the server.
            if(riddenByLocalPlayer(animal))
                pushClient(animal);
            return;
        }
        pushServer(animal);
    }

    private static boolean hasHardBox(Entity entity) {
        return entity instanceof BoneHitboxEntity;
    }

    /** True when the local rider is on this animal, so the client sends a position already outside the box. */
    private static boolean riddenByLocalPlayer(Animal animal) {
        for(Entity rider : animal.getPassengers()) {
            if(rider instanceof Player player && player.isLocalPlayer())
                return true;
        }
        return false;
    }

    private static void pushClient(Animal animal) {
        long now = animal.level().getGameTime();
        List<ObbGeometry> boxes = new ArrayList<>();
        for(var entry : YellowBoxLayer.captures()) {
            YellowBoxLayer.Capture capture = entry.getValue();
            if(now - capture.gameTime() > 40L)
                continue;
            Entity carrier = animal.level().getEntity(entry.getKey());
            if(carrier instanceof AbstractWagon hitched && hitched.harnesses(animal))
                continue;
            boxes.addAll(capture.boxes());
        }
        resolve(animal, boxes);
    }

    private static void pushServer(Animal animal) {
        List<Entity> wagons = animal.level().getEntities(animal, animal.getBoundingBox().inflate(0.5D), ObbAnimalStop::hasHardBox);
        List<ObbGeometry> boxes = new ArrayList<>();
        for(Entity wagon : wagons) {
            if(wagon instanceof AbstractWagon hitched && hitched.harnesses(animal))
                continue;
            for(ObbPartGeometrySnapshot part : ObbServerGeometry.parts(wagon)) {
                if(part.bone().collisionMode() == ObbCollisionMode.HARD)
                    boxes.add(part.geometry());
            }
        }
        resolve(animal, boxes);
    }

    /**
     * Stop on the yellow surface. A horse already inside is pushed out sideways, never down
     * into the ground. If that still leaves it inside, it returns to the last free spot.
     */
    private static void resolve(Animal animal, List<ObbGeometry> boxes) {
        if(boxes.isEmpty())
            return;
        Vec3 start = new Vec3(animal.xo, animal.yo, animal.zo);
        Vec3 delta = animal.position().subtract(start);
        boolean jumping = animal.getDeltaMovement().y > 0.0D;
        if(!inside(animal, start, boxes) && delta.lengthSqr() > 1.0E-8D) {
            Hit hit = firstContact(animal, start, delta, boxes);
            if(hit != null && isFloor(hit.normal)) {
                if(!jumping)
                    land(animal, start.y + delta.y * hit.time + FLOOR_SKIN);
            } else if(hit != null) {
                Vec3 stopped = start.add(delta.scale(hit.time)).add(outward(hit.normal).scale(SKIN));
                if(!wouldSuffocate(animal, stopped))
                    place(animal, stopped);
                else
                    place(animal, start);
                stopIntoBox(animal, outward(hit.normal));
            }
        }
        if(inside(animal, animal.position(), boxes))
            eject(animal, boxes, jumping);
        if(stuckInWall(animal, boxes)) {
            Vec3 safe = LAST_SAFE.get(animal);
            if(safe != null && !wouldSuffocate(animal, safe)) {
                place(animal, safe);
                animal.setDeltaMovement(Vec3.ZERO);
                animal.resetFallDistance();
            }
        } else if(!inside(animal, animal.position(), boxes)) {
            LAST_SAFE.put(animal, animal.position());
        }
    }

    private static Hit firstContact(Animal animal, Vec3 start, Vec3 delta, List<ObbGeometry> boxes) {
        ObbGeometry body = boxAt(animal, start);
        double time = 1.0D;
        Vec3 normal = null;
        for(ObbGeometry box : boxes) {
            Optional<SweepHit> hit = body.sweepAgainst(box, delta);
            if(hit.isPresent() && hit.get().time() < time) {
                time = hit.get().time();
                normal = hit.get().normal();
            }
        }
        return normal == null ? null : new Hit(time, normal);
    }

    /** The sweep normal points out of the wagon. A downward shove would bury the horse. */
    private static Vec3 outward(Vec3 normal) {
        if(normal.y >= 0.0D)
            return normal;
        Vec3 flat = new Vec3(normal.x, 0.0D, normal.z);
        return flat.lengthSqr() > 1.0E-6D ? flat.normalize() : new Vec3(0.0D, 1.0D, 0.0D);
    }

    private static boolean isFloor(Vec3 normal) {
        double length = normal.length();
        return length > 1.0E-6D && normal.y / length > FLOOR_DOT;
    }

    /** Keep the horizontal position and only cancel the fall. An upward velocity is a jump. */
    private static void land(Animal animal, double feetY) {
        if(wouldSuffocate(animal, new Vec3(animal.getX(), feetY, animal.getZ())))
            return;
        place(animal, new Vec3(animal.getX(), feetY, animal.getZ()));
        Vec3 velocity = animal.getDeltaMovement();
        if(velocity.y < 0.0D)
            animal.setDeltaMovement(velocity.x, 0.0D, velocity.z);
    }

    private static void eject(Animal animal, List<ObbGeometry> boxes, boolean jumping) {
        for(int pass = 0; pass < 4 && inside(animal, animal.position(), boxes); pass++) {
            boolean moved = false;
            double lift = 0.0D;
            for(ObbGeometry box : boxes) {
                Optional<Overlap> overlap = boxAt(animal, animal.position()).overlap(box);
                if(overlap.isEmpty())
                    continue;
                Vec3 correction = overlap.get().correction();
                if(isFloor(correction)) {
                    if(!jumping)
                        lift = Math.max(lift, correction.y);
                    continue;
                }
                if(correction.y < 0.0D)
                    correction = new Vec3(correction.x, 0.0D, correction.z);
                if(correction.lengthSqr() < 1.0E-8D)
                    continue;
                correction = correction.normalize().scale(correction.length() + SKIN);
                if(correction.lengthSqr() > MAX_PUSH * MAX_PUSH)
                    continue;
                Vec3 next = animal.position().add(correction);
                if(wouldSuffocate(animal, next))
                    continue;
                place(animal, next);
                stopIntoBox(animal, correction);
                moved = true;
            }
            if(lift > 0.0D) {
                land(animal, animal.getY() + lift + FLOOR_SKIN);
                moved = true;
            }
            if(!moved)
                break;
        }
    }

    /** A roof under the hooves is not a wall. Snapping back from it was cancelling the jump. */
    private static boolean stuckInWall(Animal animal, List<ObbGeometry> boxes) {
        ObbGeometry body = boxAt(animal, animal.position());
        for(ObbGeometry box : boxes) {
            Optional<Overlap> overlap = body.overlap(box);
            if(overlap.isPresent() && !isFloor(overlap.get().correction()))
                return true;
        }
        return false;
    }

    private static boolean inside(Animal animal, Vec3 feet, List<ObbGeometry> boxes) {
        ObbGeometry body = boxAt(animal, feet);
        for(ObbGeometry box : boxes) {
            if(body.overlap(box).isPresent())
                return true;
        }
        return false;
    }

    private static boolean wouldSuffocate(Animal animal, Vec3 feet) {
        Vec3 eye = feet.add(0.0D, animal.getEyeHeight(), 0.0D);
        BlockPos pos = BlockPos.containing(eye);
        BlockState state = animal.level().getBlockState(pos);
        return state.isSuffocating(animal.level(), pos);
    }

    private static void place(Animal animal, Vec3 feet) {
        shift(animal, feet.subtract(animal.position()));
        animal.resetFallDistance();
    }

    private record Hit(double time, Vec3 normal) {}

    private static ObbGeometry boxAt(Animal animal, Vec3 feet) {
        AABB box = animal.getBoundingBox();
        Vec3 center = feet.add(box.getCenter().subtract(animal.position()));
        Matrix4f toWorld = new Matrix4f().translate((float)center.x, (float)center.y, (float)center.z);
        double halfX = box.getXsize() * 0.5D;
        double halfY = box.getYsize() * 0.5D;
        double halfZ = box.getZsize() * 0.5D;
        return ObbGeometry.fromLocalBounds(new AABB(-halfX, -halfY, -halfZ, halfX, halfY, halfZ), toWorld);
    }

    private static void stopIntoBox(Animal animal, Vec3 outward) {
        double length = outward.length();
        if(length < 1.0E-6D)
            return;
        Vec3 out = outward.scale(1.0D / length);
        Vec3 velocity = animal.getDeltaMovement();
        double inward = velocity.dot(out);
        if(inward < 0.0D)
            animal.setDeltaMovement(velocity.subtract(out.scale(inward)));
    }

    private static void shift(Entity entity, Vec3 delta) {
        entity.setPos(entity.getX() + delta.x, entity.getY() + delta.y, entity.getZ() + delta.z);
        for(Entity rider : entity.getPassengers())
            rider.setPos(rider.getX() + delta.x, rider.getY() + delta.y, rider.getZ() + delta.z);
    }

}
