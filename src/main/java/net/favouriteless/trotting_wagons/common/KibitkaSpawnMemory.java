package net.favouriteless.trotting_wagons.common;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.core.HolderLookup;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

/** Respawn points a kibitka revoked while their owner was offline. Stored on the overworld. */
public final class KibitkaSpawnMemory extends SavedData {

    private static final String NAME = "trotting_wagons_kibitka_spawns";
    private static final SavedData.Factory<KibitkaSpawnMemory> FACTORY = new SavedData.Factory<>(
            KibitkaSpawnMemory::new, KibitkaSpawnMemory::load);

    private final List<Revoked> revoked = new ArrayList<>();

    public KibitkaSpawnMemory() {}

    public static void revoke(ServerLevel level, UUID id, ResourceKey<Level> dimension, BlockPos pos) {
        ServerPlayer player = level.getServer().getPlayerList().getPlayer(id);
        if(player != null) {
            clearMatch(player, dimension, pos);
            return;
        }
        get(level).add(id, dimension, pos);
    }

    public static void onLogin(ServerPlayer player) {
        if(!(player.level() instanceof ServerLevel level))
            return;
        get(level).clearLogin(player);
    }

    private static KibitkaSpawnMemory get(ServerLevel level) {
        return level.getServer().overworld().getDataStorage().computeIfAbsent(FACTORY, NAME);
    }

    private static void clearMatch(ServerPlayer player, ResourceKey<Level> dimension, BlockPos pos) {
        if(pos.equals(player.getRespawnPosition()) && dimension.equals(player.getRespawnDimension()))
            player.setRespawnPosition(Level.OVERWORLD, null, 0, false, false);
    }

    private void add(UUID id, ResourceKey<Level> dimension, BlockPos pos) {
        revoked.add(new Revoked(id, dimension, pos));
        setDirty();
    }

    private void clearLogin(ServerPlayer player) {
        boolean changed = false;
        Iterator<Revoked> entries = revoked.iterator();
        while(entries.hasNext()) {
            Revoked entry = entries.next();
            if(!entry.id.equals(player.getUUID()))
                continue;
            clearMatch(player, entry.dimension, entry.pos);
            entries.remove();
            changed = true;
        }
        if(changed)
            setDirty();
    }

    private static KibitkaSpawnMemory load(CompoundTag tag, HolderLookup.Provider registries) {
        KibitkaSpawnMemory data = new KibitkaSpawnMemory();
        ListTag list = tag.getList("Revoked", Tag.TAG_COMPOUND);
        for(int i = 0; i < list.size(); i++) {
            CompoundTag entry = list.getCompound(i);
            if(!entry.hasUUID("Id"))
                continue;
            ResourceLocation dimension = ResourceLocation.tryParse(entry.getString("Dim"));
            if(dimension == null)
                continue;
            data.revoked.add(new Revoked(
                    entry.getUUID("Id"),
                    ResourceKey.create(Registries.DIMENSION, dimension),
                    BlockPos.of(entry.getLong("Pos"))));
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        ListTag list = new ListTag();
        for(Revoked entry : revoked) {
            CompoundTag nbt = new CompoundTag();
            nbt.putUUID("Id", entry.id);
            nbt.putString("Dim", entry.dimension.location().toString());
            nbt.putLong("Pos", entry.pos.asLong());
            list.add(nbt);
        }
        tag.put("Revoked", list);
        return tag;
    }

    private record Revoked(UUID id, ResourceKey<Level> dimension, BlockPos pos) {}

}
