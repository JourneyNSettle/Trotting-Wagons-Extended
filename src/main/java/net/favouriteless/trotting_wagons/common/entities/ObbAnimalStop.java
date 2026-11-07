package net.favouriteless.trotting_wagons.common.entities;

import com.kltyton.bonehitboxlib.api.bone.collision.ObbCollisionMode;
import com.kltyton.bonehitboxlib.api.entity.BoneHitboxEntity;
import com.kltyton.bonehitboxlib.api.geometry.query.ObbServerGeometry;
import com.kltyton.bonehitboxlib.api.geometry.snapshot.ObbPartGeometrySnapshot;
import com.kltyton.bonehitboxlib.geometry.obb.ObbGeometry;
import com.kltyton.bonehitboxlib.geometry.obb.ObbGeometry.Overlap;
import com.kltyton.bonehitboxlib.geometry.obb.ObbGeometry.SurfaceHit;
import com.kltyton.bonehitboxlib.geometry.obb.ObbGeometry.SweepHit;
import net.favouriteless.trotting_wagons.client.render.entity.YellowBoxLayer;
import net.favouriteless.trotting_wagons.common.entities.base.AbstractWagon;
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
 *
 * The push runs after the animal tick and before the rider tick. Vanilla then
 * places the rider on the animal, so this class must not move the rider itself:
 * that shift was stored as the camera's previous position and showed up as shake.
 */
public final class ObbAnimalStop {

    private static final double MAX_PUSH = 4.0D;
    /** A face this upright is a roof. Matches BoneHitboxLib's support cutoff. */
    private static final double FLOOR_DOT = 0.70D;
    private static final double FLOOR_SKIN = 0.001D;
    private static final double SUPPORT_SNAP_DOWN = 0.12D;
    private static final double SUPPORT_STEP_UP = 0.60D;
    private static final double SUPPORT_MARGIN = 0.05D;
    private static final int SLIDES = 4;
    /** Last feet position whose chest was outside every yellow box. */
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
        var captures = YellowBoxLayer.captures();
        while(captures.hasNext()) {
            var entry = captures.next();
            YellowBoxLayer.Capture capture = entry.getValue();
            Entity carrier = animal.level().getEntity(entry.getKey());
            if(carrier == null || !carrier.isAlive() || now - capture.gameTime() > 40L) {
                captures.remove();
                continue;
            }
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
     * Walls use a horizontal slide, the same idea as the player's hard sweep.
     * A roof is a separate vertical probe, so a diagonal corner is not treated
     * as ground and does not keep the move that would carry the animal through.
     */
    private static void resolve(Animal animal, List<ObbGeometry> boxes) {
        if(boxes.isEmpty())
            return;
        Vec3 start = new Vec3(animal.xo, animal.yo, animal.zo);
        Vec3 delta = animal.position().subtract(start);
        boolean jumping = animal.getDeltaMovement().y > 0.0D;
        Vec3 horizontal = stripIntoWalls(animal, start, new Vec3(delta.x, 0.0D, delta.z), boxes);
        Vec3 walked = slide(animal, start, horizontal, delta.y, boxes);

        double feetY = start.y + delta.y;
        Double support = jumping ? null : supportHeight(animal, walked.x, walked.z, start, feetY, boxes);
        if(support != null)
            feetY = support + FLOOR_SKIN;
        Vec3 next = new Vec3(walked.x, feetY, walked.z);
        if(wouldSuffocate(animal, next))
            next = new Vec3(start.x, feetY, start.z);
        if(!wouldSuffocate(animal, next))
            place(animal, next);
        if(support != null) {
            Vec3 velocity = animal.getDeltaMovement();
            if(velocity.y < 0.0D)
                animal.setDeltaMovement(velocity.x, 0.0D, velocity.z);
            animal.setOnGround(true);
        }

        if(chestInside(animal, animal.position(), boxes))
            eject(animal, boxes, delta);
        if(chestInside(animal, animal.position(), boxes)) {
            Vec3 safe = LAST_SAFE.get(animal);
            if(safe != null && safe.distanceToSqr(animal.position()) <= MAX_PUSH * MAX_PUSH && !wouldSuffocate(animal, safe)) {
                place(animal, safe);
                animal.setDeltaMovement(Vec3.ZERO);
                animal.resetFallDistance();
            }
        } else {
            LAST_SAFE.put(animal, animal.position());
        }
    }

    /** Drop the part of the step that would sink an already overlapping body deeper into a wall. */
    private static Vec3 stripIntoWalls(Animal animal, Vec3 feet, Vec3 horizontal, List<ObbGeometry> boxes) {
        Vec3 resolved = horizontal;
        ObbGeometry body = boxAt(animal, feet);
        for(ObbGeometry box : boxes) {
            Overlap overlap = body.overlap(box).orElse(null);
            if(overlap == null || isFloor(overlap.normal()))
                continue;
            Vec3 normal = horizontalNormal(overlap.normal());
            if(normal == Vec3.ZERO)
                continue;
            double inward = resolved.dot(normal);
            if(inward < 0.0D)
                resolved = resolved.subtract(normal.scale(inward));
        }
        return resolved;
    }

    /**
     * Walk the horizontal step, stopping on walls and keeping the slide along them.
     * A jump tests the raised body. A fall keeps the higher body, so a roof is not a wall.
     */
    private static Vec3 slide(Animal animal, Vec3 start, Vec3 horizontal, double vertical, List<ObbGeometry> boxes) {
        double sweepY = start.y + Math.max(vertical, 0.0D);
        Vec3 resolved = Vec3.ZERO;
        Vec3 remaining = horizontal;
        for(int iteration = 0; iteration < SLIDES && remaining.lengthSqr() > 1.0E-12D; iteration++) {
            Vec3 origin = new Vec3(start.x + resolved.x, sweepY, start.z + resolved.z);
            Hit hit = firstWall(animal, origin, remaining, boxes);
            if(hit == null) {
                // Already inside, or this step would finish inside. Eject or the last safe spot handles it.
                if(chestInside(animal, origin, boxes) || chestInside(animal, origin.add(remaining), boxes))
                    break;
                resolved = resolved.add(remaining);
                break;
            }
            double safe = hit.time;
            double length = remaining.length();
            if(length > 1.0E-4D)
                safe = Math.max(0.0D, safe - 1.0E-4D / length);
            Vec3 step = remaining.scale(safe);
            resolved = resolved.add(step);
            Vec3 leftover = remaining.scale(1.0D - safe);
            Vec3 normal = horizontalNormal(hit.normal);
            if(normal == Vec3.ZERO)
                break;
            double inward = leftover.dot(normal);
            if(inward < 0.0D)
                leftover = leftover.subtract(normal.scale(inward));
            if(step.lengthSqr() <= 1.0E-14D && leftover.distanceToSqr(remaining) <= 1.0E-14D)
                break;
            remaining = leftover;
            stopIntoBox(animal, normal);
        }
        resolved = clampHorizontal(resolved, horizontal);
        return new Vec3(start.x + resolved.x, start.y, start.z + resolved.z);
    }

    private static Vec3 clampHorizontal(Vec3 resolved, Vec3 requested) {
        double requestedLength = requested.horizontalDistance();
        if(requestedLength <= 1.0E-12D)
            return Vec3.ZERO;
        Vec3 direction = requested.scale(1.0D / requestedLength);
        double along = resolved.dot(direction);
        if(along < 0.0D)
            resolved = resolved.subtract(direction.scale(along));
        double length = resolved.horizontalDistance();
        if(length > requestedLength)
            resolved = resolved.scale(requestedLength / length);
        return resolved;
    }

    private static Hit firstWall(Animal animal, Vec3 feet, Vec3 delta, List<ObbGeometry> boxes) {
        ObbGeometry body = boxAt(animal, feet);
        double time = 1.0D;
        Vec3 normal = null;
        for(ObbGeometry box : boxes) {
            Optional<SweepHit> hit = body.sweepAgainst(box, delta);
            if(hit.isEmpty() || isFloor(hit.get().normal()) || hit.get().time() >= time)
                continue;
            time = hit.get().time();
            normal = hit.get().normal();
        }
        return normal == null ? null : new Hit(time, normal);
    }

    /**
     * Highest walkable face under the feet. The probe may start above the box,
     * but the face only counts when it is already next to the hooves, so a horse
     * on the ground is not lifted onto a roof.
     */
    private static Double supportHeight(Animal animal, double x, double z, Vec3 start, double endFeet, List<ObbGeometry> boxes) {
        boolean following = supportInBand(animal, start.x, start.z, start.y - SUPPORT_SNAP_DOWN, start.y + SUPPORT_MARGIN, boxes) != null;
        double minimumY = Math.min(start.y, endFeet) - SUPPORT_SNAP_DOWN;
        double maximumY = Math.max(start.y, endFeet) + (following ? SUPPORT_STEP_UP : SUPPORT_MARGIN);
        return supportInBand(animal, x, z, minimumY, maximumY, boxes);
    }

    private static Double supportInBand(Animal animal, double x, double z, double minimumY, double maximumY, List<ObbGeometry> boxes) {
        if(minimumY > maximumY)
            return null;
        double radius = Math.max(0.05D, animal.getBbWidth() * 0.40D);
        double[][] samples = {
                {0.0D, 0.0D},
                {radius, radius},
                {radius, -radius},
                {-radius, radius},
                {-radius, -radius}
        };
        Double best = null;
        double bestDistance = Double.MAX_VALUE;
        for(ObbGeometry box : boxes) {
            AABB bounds = box.worldBounds();
            if(x + radius < bounds.minX || x - radius > bounds.maxX || z + radius < bounds.minZ || z - radius > bounds.maxZ)
                continue;
            if(maximumY < bounds.minY - SUPPORT_MARGIN || minimumY > bounds.maxY + SUPPORT_MARGIN)
                continue;
            double rayFromY = Math.max(maximumY, bounds.maxY) + SUPPORT_MARGIN;
            double rayToY = Math.min(minimumY, bounds.minY) - SUPPORT_MARGIN;
            Double height = surfaceHeight(box, x, z, x, z, rayFromY, rayToY, minimumY, maximumY);
            double distance = 0.0D;
            if(height == null) {
                for(int index = 1; index < samples.length; index++) {
                    double sampleX = x + samples[index][0];
                    double sampleZ = z + samples[index][1];
                    Double sample = surfaceHeight(box, x, z, sampleX, sampleZ, rayFromY, rayToY, minimumY, maximumY);
                    if(sample == null)
                        continue;
                    double sampleDistance = samples[index][0] * samples[index][0] + samples[index][1] * samples[index][1];
                    if(height == null || sampleDistance < distance) {
                        height = sample;
                        distance = sampleDistance;
                    }
                }
            }
            if(height != null && (best == null || height > best + 1.0E-5D || (Math.abs(height - best) <= 1.0E-5D && distance < bestDistance))) {
                best = height;
                bestDistance = distance;
            }
        }
        return best;
    }

    private static Double surfaceHeight(ObbGeometry box, double centerX, double centerZ, double sampleX, double sampleZ,
                                        double rayFromY, double rayToY, double minimumY, double maximumY) {
        SurfaceHit hit = box.verticalSurfaceAt(sampleX, sampleZ, rayFromY, rayToY).orElse(null);
        if(hit == null || hit.normal().y() < FLOOR_DOT)
            return null;
        Vec3 normal = hit.normal();
        Vec3 point = hit.position();
        double height = point.y - (normal.x * (centerX - point.x) + normal.z * (centerZ - point.z)) / normal.y;
        if(!Double.isFinite(height) || height < minimumY - FLOOR_SKIN || height > maximumY + FLOOR_SKIN)
            return null;
        return height;
    }

    /** Push a chest that is actually inside a wall back out. Standing on a roof does not qualify. */
    private static void eject(Animal animal, List<ObbGeometry> boxes, Vec3 attempted) {
        for(int pass = 0; pass < SLIDES && chestInside(animal, animal.position(), boxes); pass++) {
            boolean moved = false;
            for(ObbGeometry box : boxes) {
                if(!containsChest(box, chest(animal, animal.position())))
                    continue;
                Overlap overlap = boxAt(animal, animal.position()).overlap(box).orElse(null);
                if(overlap == null || isFloor(overlap.normal()))
                    continue;
                Vec3 normal = horizontalNormal(overlap.correction());
                if(normal == Vec3.ZERO)
                    continue;
                // The nearest face can be the far side of a rotated box. That push
                // carries the animal through, so keep only a push against the step.
                if(attempted.horizontalDistanceSqr() > 1.0E-8D && normal.dot(attempted) > 0.0D)
                    continue;
                double depth = Math.sqrt(overlap.correction().x * overlap.correction().x + overlap.correction().z * overlap.correction().z);
                if(depth < 1.0E-6D || depth > MAX_PUSH)
                    continue;
                Vec3 next = animal.position().add(normal.scale(depth + FLOOR_SKIN));
                if(wouldSuffocate(animal, next))
                    continue;
                place(animal, next);
                stopIntoBox(animal, normal);
                moved = true;
            }
            if(!moved)
                break;
        }
    }

    private static boolean isFloor(Vec3 normal) {
        double length = normal.length();
        return length > 1.0E-6D && normal.y / length > FLOOR_DOT;
    }

    private static Vec3 horizontalNormal(Vec3 normal) {
        Vec3 flat = new Vec3(normal.x, 0.0D, normal.z);
        double length = flat.length();
        return length < 1.0E-6D ? Vec3.ZERO : flat.scale(1.0D / length);
    }

    private static boolean chestInside(Animal animal, Vec3 feet, List<ObbGeometry> boxes) {
        Vec3 chest = chest(animal, feet);
        for(ObbGeometry box : boxes) {
            if(containsChest(box, chest))
                return true;
        }
        return false;
    }

    /** The chest, not the legs. A hoof resting on a roof is outside the box. */
    private static Vec3 chest(Animal animal, Vec3 feet) {
        return feet.add(0.0D, animal.getBbHeight() * 0.45D, 0.0D);
    }

    private static boolean containsChest(ObbGeometry box, Vec3 chest) {
        Vec3 local = box.toLocalPosition(chest);
        AABB bounds = box.localBounds();
        return local.x > bounds.minX && local.x < bounds.maxX
                && local.y > bounds.minY && local.y < bounds.maxY
                && local.z > bounds.minZ && local.z < bounds.maxZ;
    }

    private static boolean wouldSuffocate(Animal animal, Vec3 feet) {
        Vec3 eye = feet.add(0.0D, animal.getEyeHeight(), 0.0D);
        BlockPos pos = BlockPos.containing(eye);
        BlockState state = animal.level().getBlockState(pos);
        return state.isSuffocating(animal.level(), pos);
    }

    private static void place(Animal animal, Vec3 feet) {
        animal.setPos(feet.x, feet.y, feet.z);
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

}
