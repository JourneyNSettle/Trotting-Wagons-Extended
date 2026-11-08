package net.favouriteless.trotting_wagons.client;

import com.kltyton.bonehitboxlib.geometry.obb.ObbGeometry;
import net.favouriteless.trotting_wagons.client.render.entity.YellowBoxLayer;
import net.favouriteless.trotting_wagons.common.entities.BaseWagon;
import net.favouriteless.trotting_wagons.common.entities.base.AbstractWagon;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.Iterator;
import java.util.Map;
import java.util.Optional;

/** Ray against the yellow boxes captured while the wagon was drawn. */
public final class YellowRay {

    private YellowRay() {}

    public static boolean hits(Entity entity, Player player) {
        double reach = player.entityInteractionRange();
        Vec3 eye = player.getEyePosition();
        Vec3 end = eye.add(player.getViewVector(1.0F).scale(reach));
        return closest(entity, eye, end) != null;
    }

    /** The yellow face of a wagon, and where the look meets it. */
    public record Strike(Entity entity, Vec3 point) {}

    /**
     * Closest yellow face in reach. The shortened pick box ends below a tall cover,
     * so a standing look at that cover never selects the wagon on its own.
     */
    public static Strike nearestWagon(Player player) {
        double reach = player.entityInteractionRange();
        Vec3 eye = player.getEyePosition();
        Vec3 end = eye.add(player.getViewVector(1.0F).scale(reach));
        Entity bestEntity = null;
        Vec3 bestHit = null;
        double bestDistance = reach * reach;
        Iterator<Map.Entry<Integer, YellowBoxLayer.Capture>> captures = YellowBoxLayer.captures();
        while(captures.hasNext()) {
            Entity entity = player.level().getEntity(captures.next().getKey());
            if(!(entity instanceof AbstractWagon) && !(entity instanceof BaseWagon))
                continue;
            if(!entity.isAlive() || !entity.isPickable())
                continue;
            Vec3 hit = closest(entity, eye, end);
            if(hit == null)
                continue;
            double distance = hit.distanceToSqr(eye);
            if(distance <= bestDistance) {
                bestDistance = distance;
                bestHit = hit;
                bestEntity = entity;
            }
        }
        return bestEntity == null ? null : new Strike(bestEntity, bestHit);
    }

    /** True when this face is closer than the block or other entity under the crosshair. */
    public static boolean isFirst(Player player, HitResult vanilla, Strike strike) {
        if(vanilla != null && vanilla.getType() == HitResult.Type.ENTITY
                && ((EntityHitResult)vanilla).getEntity() == strike.entity())
            return false;
        if(vanilla == null || vanilla.getType() == HitResult.Type.MISS)
            return true;
        Vec3 eye = player.getEyePosition();
        return strike.point().distanceToSqr(eye) < vanilla.getLocation().distanceToSqr(eye);
    }

    public static Vec3 closest(Entity entity, Vec3 from, Vec3 to) {
        YellowBoxLayer.Capture capture = YellowBoxLayer.capture(entity.getId());
        if(capture == null)
            return null;
        Vec3 best = null;
        double bestDistance = Double.MAX_VALUE;
        for(ObbGeometry box : capture.boxes()) {
            Vec3 hit = clip(box, from, to);
            if(hit == null)
                continue;
            double distance = hit.distanceToSqr(from);
            if(distance < bestDistance) {
                bestDistance = distance;
                best = hit;
            }
        }
        return best;
    }

    private static Vec3 clip(ObbGeometry box, Vec3 from, Vec3 to) {
        Vec3 localFrom = box.toLocalPosition(from);
        Vec3 localTo = box.toLocalPosition(to);
        AABB bounds = box.localBounds();
        Optional<Vec3> hit = bounds.clip(localFrom, localTo);
        if(hit.isEmpty())
            return null;
        Vec3 localDelta = localTo.subtract(localFrom);
        Vec3 localHit = hit.get();
        double t = 0.0D;
        if(Math.abs(localDelta.x) > 1.0E-8D)
            t = (localHit.x - localFrom.x) / localDelta.x;
        else if(Math.abs(localDelta.y) > 1.0E-8D)
            t = (localHit.y - localFrom.y) / localDelta.y;
        else if(Math.abs(localDelta.z) > 1.0E-8D)
            t = (localHit.z - localFrom.z) / localDelta.z;
        t = Math.max(0.0D, Math.min(1.0D, t));
        return from.add(to.subtract(from).scale(t));
    }

}
