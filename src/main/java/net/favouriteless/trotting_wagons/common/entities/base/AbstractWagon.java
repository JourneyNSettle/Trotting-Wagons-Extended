package net.favouriteless.trotting_wagons.common.entities.base;

import net.favouriteless.trotting_wagons.ServerConfig;
import net.favouriteless.trotting_wagons.TrottingWagons;
import net.favouriteless.trotting_wagons.client.TWClient;
import net.favouriteless.trotting_wagons.common.init.TWItems;
import net.favouriteless.trotting_wagons.common.util.LevelUtils;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.favouriteless.trotting_wagons.common.network.packets.SyncSeatsPayload;
import net.minecraft.network.chat.Component;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.network.syncher.SynchedEntityData.Builder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;
import java.util.Random;
import java.util.UUID;

public abstract class AbstractWagon extends AbstractGeckolibVehicle {

    private static final Random RANDOM = new Random();
    public static final int WHEEL_COUNT = 4;
    private static final float WHEEL_SAG = 14F;
    private static final float FRONT_SAG = 10F;
    private static final double WHEEL_SINK = 0.45D;

    private static final EntityDataAccessor<Float> DATA_ID_HEALTH = SynchedEntityData.defineId(AbstractWagon.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<OptionalInt> DATA_ID_COLOR = SynchedEntityData.defineId(AbstractWagon.class, EntityDataSerializers.OPTIONAL_UNSIGNED_INT);
    private static final EntityDataAccessor<Byte> DATA_WHEELS = SynchedEntityData.defineId(AbstractWagon.class, EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<Integer> DATA_HITCH_LEFT = SynchedEntityData.defineId(AbstractWagon.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_HITCH_RIGHT = SynchedEntityData.defineId(AbstractWagon.class, EntityDataSerializers.INT);

    public static final double GRAVITY = 0.04D;
    public static final int SPEED_LEVELS = 3;
    public static final double HORIZONTAL_TETHER = 0.5D * 0.5D; // Use sqr for performance
    public static final int BOARD_COOLDOWN_TICKS = 60;
    public static final double ANIMAL_BOARD_RANGE = 1.0D;
    public static final double HITCH_RANGE = 1.5D;
    private static final String BOARD_COOLDOWN = "tw_board_cooldown";

    public final double speed;
    private final double acceleration;
    private final float turnRate;
    private final int maxHealth;

    private final double horseX;
    private final double horseY;
    private final float shaftAngle;

    public UUID owner;
    private int speedLevel = 0;
    private double currentSpeed;

    private final Mob[] horses = new Mob[2];
    private final UUID[] horseUuids = new UUID[2];

    private final WagonSeat[] seats;
    private final Entity[] seatEntities;
    private final int[] seatIds;
    private final boolean[] seatLocked;
    private final Map<UUID, Integer> savedSeatByUuid = new HashMap<>();
    private int pendingSeat = -1;

    private final NonNullList<ItemStack> wheels = NonNullList.withSize(WHEEL_COUNT, ItemStack.EMPTY);
    private final ItemStackHandler wheelHandler = new ItemStackHandler(wheels) {
        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return stack.is(TWItems.WHEEL.get());
        }

        @Override
        public int getSlotLimit(int slot) {
            return 1;
        }

        @Override
        protected void onContentsChanged(int slot) {
            updateWheelMask();
        }
    };

    public AbstractWagon(EntityType<? extends AbstractWagon> type, Level level, double wheelWidth, double wheelLength, WagonSeat[] seats,
                         double speed, double acceleration, double turnRate, int maxHealth, double horseX, double horseY, float shaftAngle) {
        super(type, level, wheelWidth, wheelLength, seatPositions(seats));

        this.speed = speed;
        this.acceleration = acceleration;
        this.turnRate = (float)turnRate;
        this.maxHealth = maxHealth;
        this.horseX = horseX;
        this.horseY = horseY;
        this.shaftAngle = shaftAngle;
        this.seats = seats;
        this.seatEntities = new Entity[seats.length];
        this.seatIds = new int[seats.length];
        this.seatLocked = new boolean[seats.length];
        for(int i = 0; i < seatIds.length; i++)
            seatIds[i] = -1;

        setHealth(maxHealth);
        if(!level.isClientSide) {
            ItemStack wheel = new ItemStack(TWItems.WHEEL.get());
            for(int i = 0; i < WHEEL_COUNT; i++)
                wheelHandler.setStackInSlot(i, wheel.copy());
        }
    }

    private static Vec3[] seatPositions(WagonSeat[] seats) {
        Vec3[] positions = new Vec3[seats.length];
        for(int i = 0; i < seats.length; i++)
            positions[i] = seats[i].position();
        return positions;
    }

    @Override
    public void tick() {
        if(firstTick && level().isClientSide)
            TWClient.createWagonSound(this);

        if(!level().isClientSide) {
            validateHorses();
            if(hasFixedSeats())
                boardNearbyAnimals();
        }
        super.tick();
    }

    private void validateHorses() {
        if(firstTick || level().getGameTime() % 100 == 0) { // Repeated checks help resolve chunk loading issues.
            setHorse(getHorse(Side.LEFT), Side.LEFT);
            setHorse(getHorse(Side.RIGHT), Side.RIGHT);
        }

        validate(Side.LEFT);
        validate(Side.RIGHT);
    }

    private void validate(Side side) {
        Mob horse = getHorse(side);
        if(horse == null)
            return;
        if(!isHorseInRange(side))
            horse.setPos(getHorsePos(side));
    }

    private boolean isHorseInRange(Side side) {
        final Mob horse = getHorse(side);
        if(horse == null)
            return false;

        Vec3 displacement = horse.position().subtract(getHorsePos(side));
        return horse.isAlive() && displacement.horizontalDistanceSqr() < HORIZONTAL_TETHER;
    }

    @Override
    public void moveTick() {
        final Mob leftHorse = getHorse(Side.LEFT);
        final Mob rightHorse = getHorse(Side.RIGHT);

        if(!isVehicle() || !(this.getControllingPassenger() instanceof Player) || !hasPullers() || !hasAllWheels()) {
            speedLevel = 0;
            setSteeringImpulse(0);
        }
        if(!hasAllWheels())
            currentSpeed = 0;

        handleSteering();
        handleAcceleration();

        Vec3 forward = getForward().multiply(1, 0, 1);
        Vec3 velocity = forward.scale(currentSpeed);

        // The horses or wagon may get blocked so we simulate first and use the shortest horizontal distance.
        Vec3 leftMove = leftHorse != null ? leftHorse.collide(velocity) : null;
        Vec3 rightMove = rightHorse != null ? rightHorse.collide(velocity) : null;

        Vec3 move = collide(velocity);
        if(leftMove != null && leftMove.horizontalDistanceSqr() < move.horizontalDistanceSqr())
            move = leftMove;
        if(rightMove != null && rightMove.horizontalDistanceSqr() < move.horizontalDistanceSqr())
            move = rightMove;

        move = move.multiply(1, 0, 1); // "Flatten" the move vector so entities can individually handle their step heights.

        if(!isNoGravity())
            move = move.add(0, -GRAVITY, 0);

        if(leftHorse != null) {
            leftHorse.setDeltaMovement(move.add(0, leftHorse.getDeltaMovement().y, 0));
            leftHorse.move(MoverType.SELF, leftHorse.getDeltaMovement());
        }
        if(rightHorse != null) {
            rightHorse.setDeltaMovement(move.add(0, rightHorse.getDeltaMovement().y, 0));
            rightHorse.move(MoverType.SELF, rightHorse.getDeltaMovement());
        }

        setDeltaMovement(move.add(0, getDeltaMovement().y, 0));
        move(MoverType.SELF, getDeltaMovement());
        setXRot(rotlerp(getXRot(), calculateTargetXRot(), (float)(9 * currentSpeed / (SPEED_LEVELS * currentSpeed)))); // 7 is just a magic scaling number for smoothing.
    }

    private void handleSteering() {
        float speedPercent = (float)(currentSpeed / (SPEED_LEVELS * speed));
        float steer = -getSteeringImpulse() * speedPercent * turnRate;

        if(steer != 0) {
            final Mob leftHorse = getHorse(Side.LEFT);
            final Mob rightHorse = getHorse(Side.RIGHT);

            Vec3 leftDisplace = collideSteering(Side.LEFT, steer);
            Vec3 rightDisplace = collideSteering(Side.RIGHT, steer);
            boolean leftBlocked = leftHorse != null && leftDisplace.equals(Vec3.ZERO);
            boolean rightBlocked = rightHorse != null && rightDisplace.equals(Vec3.ZERO);

            if((leftHorse != null || rightHorse != null) && !leftBlocked && !rightBlocked) {
                setYRot(Mth.wrapDegrees(getYRot() + steer));

                float yRot = getYRot();
                if(leftHorse != null)
                    setHorsePos(leftHorse, leftHorse.position().add(leftDisplace), yRot);
                if(rightHorse != null)
                    setHorsePos(rightHorse, rightHorse.position().add(rightDisplace), yRot);
            }
        }
    }

    private void handleAcceleration() {
        double targetSpeed = speed * speedLevel;
        if(isReversing())
            targetSpeed *= -0.25D;

        double accel = speed / (acceleration * 20.0D);
        double speedDiff = targetSpeed - currentSpeed;

        currentSpeed += Mth.sign(speedDiff) * Math.min(Math.abs(accel), Math.abs(speedDiff));
    }

    private void setHorsePos(Mob horse, Vec3 pos, float rot) {
        if(horse == null)
            return;
        horse.setYRot(rot);
        horse.setYBodyRot(rot);
        horse.setYHeadRot(rot);
        horse.setPos(pos);
    }

    private Vec3 collideSteering(Side side, float angle) {
        Mob horse = getHorse(side);
        if(horse == null)
            return Vec3.ZERO;

        Vec3 desired = rotateY(hitchOffset(side), getYRot() + angle);
        Vec3 local = horse.position().subtract(position()).multiply(1, 0, 1); // Local starting position.

        Vec3 displacement = desired.subtract(local);
        Vec3 collidedDisplacement = horse.collide(displacement);

        if(!displacement.equals(collidedDisplacement.multiply(1, 0, 1)))
            return Vec3.ZERO;

        return collidedDisplacement;
    }

    private float rotlerp(float angle, float target, float max) {
        float d = Mth.wrapDegrees(target - angle);
        if(d > max)
            d = max;
        if(d < -max)
            d = -max;

        return angle + d;
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        if(!isAlive())
            return InteractionResult.PASS;

        final boolean isClientSide = level().isClientSide;

        if(player.isSecondaryUseActive()) {
            ItemStack stack = player.getItemInHand(hand);
            Item item = stack.getItem();

            if(stack.is(TWItems.HORSE_WHIP.get())) {
                if(!isClientSide)
                    toggleReversing();
                return InteractionResult.sidedSuccess(isClientSide);
            }

            if(getHealth() < maxHealth && stack.is(BuiltInRegistries.ITEM.get(ResourceLocation.parse(ServerConfig.INSTANCE.repairItem.get())))) {
                if(!isClientSide) {
                    stack.shrink(1);
                    setHealth(getHealth() + ServerConfig.INSTANCE.repairPerItem.get());
                    level().playSound(null, getX(), getY(), getZ(), SoundEvents.ANVIL_USE, SoundSource.MASTER, 0.3F, 1);
                }
                return InteractionResult.sidedSuccess(isClientSide);
            }

            if(stack.is(ItemTags.AXES)) {
                if(!isClientSide && player.getUUID().equals(owner))
                    onDestroyed(true);
                return InteractionResult.sidedSuccess(isClientSide);
            }

            if(TrottingWagons.DYE_ITEMS.containsKey(item)) {
                setColor(TrottingWagons.DYE_ITEMS.get(item));
                return InteractionResult.sidedSuccess(isClientSide);
            }

        }
        else if(player.getVehicle() != this) {
            if(!isClientSide) {
                player.setYRot(getYRot());
                player.setXRot(getXRot());

                return mountNearest(player) ? InteractionResult.CONSUME : InteractionResult.PASS;
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    public boolean hitchRiddenAnimal(Mob animal) {
        if(!isAllowedPuller(animal) || !hasFreeHitch() || !isNearTongue(animal) || findHitch(animal) != null)
            return false;
        if(animal.getVehicle() != null)
            animal.stopRiding();
        if(getHorse(Side.LEFT) == null)
            hitchHorse(animal, Side.LEFT);
        else if(hitchSlots() > 1)
            hitchHorse(animal, Side.RIGHT);
        return true;
    }

    public void awardSinner(ServerPlayer player) {
        Mob left = getHorse(Side.LEFT);
        Mob right = getHorse(Side.RIGHT);
        if(left == null || right == null || left.getType() == right.getType())
            return;
        if(player.level().getServer() == null)
            return;
        AdvancementHolder holder = player.level().getServer().getAdvancements().get(TrottingWagons.id("sinner"));
        if(holder != null)
            player.getAdvancements().award(holder, "hitch_different");
    }

    public void unhitch(Mob animal) {
        if(getHorse(Side.LEFT) == animal)
            setHorse(null, Side.LEFT);
        else if(getHorse(Side.RIGHT) == animal)
            setHorse(null, Side.RIGHT);
    }

    public boolean isHitched(Entity entity) {
        return entity == getHorse(Side.LEFT) || entity == getHorse(Side.RIGHT);
    }

    private void hitchHorse(Mob horse, Side side) {
        setHorse(horse, side);
        initHorse(horse, side);
    }

    private void initHorse(Mob horse, Side side) {
        if(horse == null)
            return;
        horse.ejectPassengers();
        horse.setDeltaMovement(Vec3.ZERO);
        setHorsePos(horse, getHorsePos(side), getYRot());
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if(isInvulnerableTo(source))
            return false;

        if(level().isClientSide || isRemoved())
            return true;

        level().playSound(null, blockPosition(), SoundEvents.WOOD_BREAK, SoundSource.MASTER);
        double w = getBbWidth();
        double h = getBbHeight();
        ((ServerLevel)level()).sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.SPRUCE_PLANKS.defaultBlockState()),
                getX(), getY(), getZ(), 100, w, h, w, 0);

        setHealth(getHealth() - amount);
        gameEvent(GameEvent.ENTITY_DAMAGE, source.getEntity());

        if(getHealth() <= 0)
            onDestroyed(false);

        return true;
    }

    protected void onDestroyed(boolean dropItem) {
        if(level().isClientSide)
            return;

        setHorse(null, Side.LEFT);
        setHorse(null, Side.RIGHT);

        ejectPassengers();
        Containers.dropContents(level(), blockPosition(), wheels);
        discard();

        if(!level().getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS))
            return;

        if(dropItem) {
            ItemStack pickResult = getPickResult();
            if(pickResult != null)
                spawnAtLocation(pickResult);
        }
        else {
            spawnAtLocation(new ItemStack(Items.OAK_PLANKS, RANDOM.nextInt(7)));
            spawnAtLocation(new ItemStack(Items.IRON_INGOT, RANDOM.nextInt(3)));
        }
    }

    @Override
    public Vec3 getDismountLocationForPassenger(LivingEntity passenger) {
        final double width = getBbWidth();
        double offset = Math.sqrt(width * width + width * width) / 2 + 0.05D;

        double pWidth = passenger.getBbWidth();
        double pOffset = Math.sqrt(pWidth * pWidth + pWidth * pWidth) / 2;

        float yRot = getYRot();

        pWidth = pWidth * 0.8F;

        Vec3 wOffset = new Vec3(0, passenger.getEyeHeight(), 0);

        Vec3 pos = position().add(rotateY(new Vec3(offset + pOffset, 0, 0), yRot)); // Left
        if(LevelUtils.isEmpty(level(), AABB.ofSize(pos.add(wOffset), pWidth, 1.0E-6D, pWidth)))
            return pos;

        pos = position().add(rotateY(new Vec3(-offset - pOffset, 0, 0), yRot)); // Right
        if(LevelUtils.isEmpty(level(), AABB.ofSize(pos.add(wOffset), pWidth, 1.0E-6D, pWidth)))
            return pos;

        pos = position().add(rotateY(new Vec3(0, 0, offset + pOffset), yRot)); // Front
        if(LevelUtils.isEmpty(level(), AABB.ofSize(pos.add(wOffset), pWidth, 1.0E-6D, pWidth)))
            return pos;

        pos = position().add(rotateY(new Vec3(0, 0, -offset - pOffset), yRot)); // Back
        if(LevelUtils.isEmpty(level(), AABB.ofSize(pos.add(wOffset), pWidth, 1.0E-6D, pWidth)))
            return pos;

        return super.getDismountLocationForPassenger(passenger);
    }

    @Override
    protected void defineSynchedData(Builder builder) {
        DyeColor color = getDefaultColor();
        builder.define(DATA_ID_COLOR, color != null ? OptionalInt.of(color.getId()) : OptionalInt.empty());
        builder.define(DATA_ID_HEALTH, (float)0);
        builder.define(DATA_WHEELS, (byte)0x0F);
        builder.define(DATA_HITCH_LEFT, -1);
        builder.define(DATA_HITCH_RIGHT, -1);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag nbt) {
        if(nbt.contains("color", 99))
            setColor(DyeColor.byId(nbt.getInt("color")));
        if(nbt.contains("owner"))
            owner = nbt.getUUID("owner");
        if(nbt.contains("leftHorse"))
            setHorseUUID(nbt.getUUID("leftHorse"), Side.LEFT);
        if(nbt.contains("rightHorse"))
            setHorseUUID(nbt.getUUID("rightHorse"), Side.RIGHT);
        savedSeatByUuid.clear();
        if(nbt.contains("seatLocks", Tag.TAG_BYTE_ARRAY)) {
            byte[] locks = nbt.getByteArray("seatLocks");
            for(int i = 0; i < locks.length && i < seatLocked.length; i++)
                seatLocked[i] = locks[i] != 0;
        }
        if(nbt.contains("seats", Tag.TAG_LIST)) {
            ListTag seatsTag = nbt.getList("seats", Tag.TAG_COMPOUND);
            for(int i = 0; i < seatsTag.size(); i++) {
                CompoundTag seatTag = seatsTag.getCompound(i);
                if(seatTag.hasUUID("id"))
                    savedSeatByUuid.put(seatTag.getUUID("id"), seatTag.getInt("seat"));
            }
        }
        if(nbt.contains("Wheels", Tag.TAG_COMPOUND)) {
            for(int i = 0; i < wheels.size(); i++)
                wheels.set(i, ItemStack.EMPTY);
            ContainerHelper.loadAllItems(nbt.getCompound("Wheels"), wheels, registryAccess());
        }
        updateWheelMask();
    }

    @Override
    public void addAdditionalSaveData(CompoundTag nbt) {
        if(getColor() != null)
            nbt.putByte("color", (byte)getColor().getId());
        if(owner != null)
            nbt.putUUID("owner", owner);
        if(getHorseUUID(Side.LEFT) != null)
            nbt.putUUID("leftHorse", getHorseUUID(Side.LEFT));
        if(getHorseUUID(Side.RIGHT) != null)
            nbt.putUUID("rightHorse", getHorseUUID(Side.RIGHT));
        ListTag seatsTag = new ListTag();
        for(int i = 0; i < seatEntities.length; i++) {
            if(seatEntities[i] == null)
                continue;
            CompoundTag seatTag = new CompoundTag();
            seatTag.putInt("seat", i);
            seatTag.putUUID("id", seatEntities[i].getUUID());
            seatsTag.add(seatTag);
        }
        nbt.put("seats", seatsTag);
        byte[] locks = new byte[seatLocked.length];
        for(int i = 0; i < seatLocked.length; i++)
            locks[i] = (byte)(seatLocked[i] ? 1 : 0);
        nbt.putByteArray("seatLocks", locks);
        CompoundTag wheelsTag = new CompoundTag();
        ContainerHelper.saveAllItems(wheelsTag, wheels, registryAccess());
        nbt.put("Wheels", wheelsTag);
    }

    public void tryCycleSpeed(Player player) {
        if(player == getControllingPassenger())
            speedLevel = (speedLevel + 1) % SPEED_LEVELS;
    }

    private Mob getHorse(Side side) {
        final UUID uuid = horseUuids[side.ordinal()];
        if(uuid == null) return null;

        Mob horse = horses[side.ordinal()];

        if(horse == null && level().getEntities().get(uuid) instanceof Mob mob)
            horse = mob;

        return horse != null && horse.isAlive() ? horse : null;
    }

    private void setHorse(Mob horse, Side side) {
        Mob old = getHorse(side);
        if(old != null && old != horse) {
            old.setNoAi(false);
            setBoardCooldown(old);
        }
        if(horse != null)
            horse.setNoAi(true);

        horses[side.ordinal()] = horse;
        horseUuids[side.ordinal()] = horse != null ? horse.getUUID() : null;
        entityData.set(side == Side.LEFT ? DATA_HITCH_LEFT : DATA_HITCH_RIGHT, horse == null ? -1 : horse.getId());
    }

    private UUID getHorseUUID(Side side) {
        return horseUuids[side.ordinal()];
    }

    private void setHorseUUID(UUID uuid, Side side) {
        horseUuids[side.ordinal()] = uuid;
    }

    private Vec3 getHorsePos(Side side) {
        return position().add(rotateY(hitchOffset(side), getYRot()));
    }

    public float getShaftAngle() {
        return shaftAngle;
    }

    public float tongueAngle(float bodyPitch, float bodyRoll, double drop) {
        if(getWheelMask() == 0)
            return 0;
        double pitch = Math.toRadians(bodyPitch);
        double roll = Math.toRadians(bodyRoll);
        boolean hitched = getHitchEntityId(Side.LEFT) >= 0 || getHitchEntityId(Side.RIGHT) >= 0;
        double reach = tongueLength();
        if(!hitched && pitch > 0)
            reach += 3D * getWheelLength();
        else if(!hitched && pitch < 0)
            reach += 1.25D * getWheelLength();
        double rest = tongueHeight() * Math.cos(roll) * Math.cos(pitch) - reach * Math.sin(pitch) - drop;
        double target = hitched ? 0.85D : 0;
        double sine = Mth.clamp((target - rest) / tongueLength(), -1D, 1D);
        return (float)Math.asin(sine);
    }

    protected double tongueLength() {
        return 57.12D / 16D;
    }

    protected double tongueHeight() {
        return 7.5D / 16D;
    }

    public DyeColor getColor() {
        OptionalInt i = entityData.get(DATA_ID_COLOR);
        return i.isPresent() ? DyeColor.byId(i.getAsInt()) : null;
    }

    public void setColor(DyeColor color) {
        entityData.set(DATA_ID_COLOR, color != null ? OptionalInt.of(color.getId()) : OptionalInt.empty());
    }

    protected DyeColor getDefaultColor() {
        return DyeColor.WHITE;
    }

    public float getHealth() {
        return entityData.get(DATA_ID_HEALTH);
    }

    public int getMaxHealth() {
        return maxHealth;
    }

    protected void setHealth(float health) {
        entityData.set(DATA_ID_HEALTH, Mth.clamp(health, 0, maxHealth));
    }

    @Override
    public boolean isControlledByLocalInstance() {
        return isEffectiveAi();
    }

    @Override
    public boolean causeFallDamage(float fallDistance, float multiplier, DamageSource source) {
        return !getType().is(EntityTypeTags.FALL_DAMAGE_IMMUNE);
    }

    public boolean hasFixedSeats() {
        return seats != null;
    }

    public int getSeatCount() {
        return seats.length;
    }

    public WagonSeat getSeat(int index) {
        return seats[index];
    }

    public int getSeatEntityId(int index) {
        return seatIds[index];
    }

    public boolean isSeatLocked(int index) {
        return seatLocked[index];
    }

    public boolean[] seatLocks() {
        return seatLocked.clone();
    }

    public boolean toggleSeatLock(Player player, int seat) {
        if(player.getVehicle() != this || seat < 0 || seat >= seats.length || !seats[seat].canLockAnimals())
            return false;
        seatLocked[seat] = !seatLocked[seat];
        syncSeats();
        return true;
    }

    public Vec3 getTongueAnchor() {
        return position().add(rotateY(new Vec3(0, 0, horseY), getYRot()));
    }

    public static void setBoardCooldown(Entity entity) {
        entity.getPersistentData().putLong(BOARD_COOLDOWN, entity.level().getGameTime() + BOARD_COOLDOWN_TICKS);
    }

    public static boolean isBoardCooldown(Entity entity) {
        return entity.level().getGameTime() < entity.getPersistentData().getLong(BOARD_COOLDOWN);
    }

    public static boolean isAllowedPuller(Entity entity) {
        try {
            return ServerConfig.INSTANCE.allowedEntities.get().contains(entity.getType().builtInRegistryHolder().key().location().toString());
        } catch(IllegalStateException ignored) {
            return true;
        }
    }

    public static AbstractWagon findHitch(Entity entity) {
        AABB box = entity.getBoundingBox().inflate(8.0D);
        for(AbstractWagon wagon : entity.level().getEntitiesOfClass(AbstractWagon.class, box)) {
            if(wagon.isHitched(entity))
                return wagon;
        }
        return null;
    }

    public static AbstractWagon findHitchTarget(Mob animal) {
        if(!isAllowedPuller(animal))
            return null;

        AbstractWagon best = null;
        double bestDist = HITCH_RANGE * HITCH_RANGE;
        AABB search = animal.getBoundingBox().inflate(8.0D);
        for(AbstractWagon wagon : animal.level().getEntitiesOfClass(AbstractWagon.class, search)) {
            if(!wagon.hasFreeHitch() || !wagon.isNearTongue(animal))
                continue;
            double dist = horizontalDistanceSqr(animal.position(), wagon.getTongueAnchor());
            if(dist <= bestDist) {
                bestDist = dist;
                best = wagon;
            }
        }
        return best;
    }

    public boolean mountNearest(Entity entity) {
        int seat = findNearestSeat(entity);
        if(seat < 0)
            return false;
        pendingSeat = seat;
        boolean mounted = entity.startRiding(this);
        pendingSeat = -1;
        return mounted;
    }

    public boolean ejectSeat(Player player, int seat) {
        if(player.getVehicle() != this || seat < 0 || seat >= seats.length)
            return false;
        if(!(seatEntities[seat] instanceof Mob mob) || seatEntities[seat] instanceof Player)
            return false;
        mob.stopRiding();
        return true;
    }

    public boolean canPlaceAnimal(int index, Entity animal) {
        if(index < 0 || index >= seats.length || !(animal instanceof LivingEntity) || animal instanceof Player)
            return false;
        int occupied = seatIds[index];
        if(occupied >= 0)
            return false;
        return seatAccepts(index, animal);
    }

    public int hitchSlots() {
        return 2;
    }

    public boolean canPlaceDraft(Entity animal, Side side) {
        if(side == Side.RIGHT && hitchSlots() < 2)
            return false;
        if(!(animal instanceof Mob) || animal instanceof Player || !isAllowedPuller(animal))
            return false;
        int occupied = getHitchEntityId(side);
        if(occupied >= 0 && occupied != animal.getId())
            return false;
        AbstractWagon other = findHitch(animal);
        return other == null || other == this;
    }

    public boolean moveAnimal(Player player, int animalId, int to, boolean draft) {
        if(player.getVehicle() != this)
            return false;
        int fromSeat = -1;
        LivingEntity animal = null;
        for(int i = 0; i < seatEntities.length; i++) {
            if(seatEntities[i] != null && seatEntities[i].getId() == animalId) {
                fromSeat = i;
                animal = seatEntities[i] instanceof LivingEntity living ? living : null;
            }
        }
        Side fromSide = null;
        if(hitchMatches(Side.LEFT, animalId))
            fromSide = Side.LEFT;
        else if(hitchMatches(Side.RIGHT, animalId))
            fromSide = Side.RIGHT;
        if(animal == null && fromSide != null && getHorse(fromSide) instanceof LivingEntity living)
            animal = living;
        if(!(animal instanceof Mob))
            return false;
        Mob mob = (Mob)animal;
        if(fromSeat < 0 && fromSide == null)
            return false;

        if(draft) {
            if(to < 0 || to >= hitchSlots())
                return false;
            Side target = to == 0 ? Side.LEFT : Side.RIGHT;
            if(fromSide == target || !canPlaceDraft(mob, target))
                return false;
            if(mob.getVehicle() != null)
                mob.stopRiding();
            if(fromSide != null)
                setHorse(null, fromSide);
            hitchHorse(mob, target);
            if(player instanceof ServerPlayer serverPlayer)
                awardSinner(serverPlayer);
            return true;
        }

        if(fromSeat == to || !canPlaceAnimal(to, mob))
            return false;
        if(fromSide != null)
            setHorse(null, fromSide);
        if(fromSeat >= 0) {
            seatEntities[fromSeat] = null;
            seatIds[fromSeat] = -1;
            seatEntities[to] = mob;
            seatIds[to] = mob.getId();
            syncSeats();
            return true;
        }
        pendingSeat = to;
        boolean seated = mob.startRiding(this);
        pendingSeat = -1;
        return seated;
    }

    public boolean ejectDraft(Player player, Side side) {
        if(player.getVehicle() != this || getHorse(side) == null)
            return false;
        setHorse(null, side);
        return true;
    }

    public ItemStackHandler getWheels() {
        return wheelHandler;
    }

    public int getWheelMask() {
        return entityData.get(DATA_WHEELS) & 0x0F;
    }

    public boolean hasAllWheels() {
        return getWheelMask() == 0x0F;
    }

    public WheelSet wheelSet() {
        Item same = null;
        boolean mismatch = false;
        for(int i = 0; i < wheels.size(); i++) {
            ItemStack stack = wheels.get(i);
            if(stack.isEmpty())
                return WheelSet.MISSING;
            if(same == null)
                same = stack.getItem();
            else if(stack.getItem() != same)
                mismatch = true;
        }
        return mismatch ? WheelSet.INCOMPATIBLE : WheelSet.REGULAR;
    }

    public WheelSag wheelSag() {
        boolean fl = wheelOn(0);
        boolean fr = wheelOn(1);
        boolean rl = wheelOn(2);
        boolean rr = wheelOn(3);
        int count = (fl ? 1 : 0) + (fr ? 1 : 0) + (rl ? 1 : 0) + (rr ? 1 : 0);
        if(count == 0)
            return new WheelSag(0, 0, WHEEL_SINK);
        if(count >= 3)
            return WheelSag.NONE;

        float half = WHEEL_SAG * 0.25F;
        float pitch = 0;
        float roll = 0;
        if(!fl && !fr && rl && rr)
            pitch = FRONT_SAG;
        else if(fl && fr && !rl && !rr)
            pitch = -WHEEL_SAG;
        else if(!fl && fr && !rl && rr)
            roll = -WHEEL_SAG;
        else if(fl && !fr && rl && !rr)
            roll = WHEEL_SAG;
        else if(!fl && fr && rl && !rr) {
            pitch = -WHEEL_SAG;
            roll = WHEEL_SAG;
        } else if(fl && !fr && !rl && rr) {
            pitch = -WHEEL_SAG;
            roll = -WHEEL_SAG;
        } else if(fl) {
            pitch = -half;
            roll = half;
        } else if(fr) {
            pitch = -half;
            roll = -half;
        } else if(rl) {
            pitch = half;
            roll = half;
        } else {
            pitch = half;
            roll = -half;
        }

        double drop = sagAnchor(pitch, roll, fl, fr, rl, rr);
        if(pitch == 0 && roll != 0)
            drop += 3D / 16D;
        else if(count == 2 && pitch != 0 && roll != 0)
            drop -= 12D / 16D;
        else if(count == 1)
            drop += 2D / 16D;
        return new WheelSag(pitch, roll, drop);
    }

    @Override
    protected Vec3 adjustLocalOffset(Vec3 local) {
        WheelSag sag = wheelSag();
        local = rotateZ(local, sag.roll());
        local = rotateX(local, sag.pitch());
        return local.subtract(0, sag.drop(), 0);
    }

    private boolean wheelOn(int index) {
        return (getWheelMask() & (1 << index)) != 0;
    }

    private double sagAnchor(float pitch, float roll, boolean fl, boolean fr, boolean rl, boolean rr) {
        double max = Double.NEGATIVE_INFINITY;
        double width = getWheelWidth();
        double length = getWheelLength();
        if(fl)
            max = Math.max(max, sagCornerY(-width, length, pitch, roll));
        if(fr)
            max = Math.max(max, sagCornerY(width, length, pitch, roll));
        if(rl)
            max = Math.max(max, sagCornerY(-width, -length, pitch, roll));
        if(rr)
            max = Math.max(max, sagCornerY(width, -length, pitch, roll));
        return max == Double.NEGATIVE_INFINITY ? 0 : Math.max(0, max);
    }

    private double sagCornerY(double x, double z, float pitch, float roll) {
        Vec3 turned = rotateX(rotateZ(x, 0, z, roll), pitch);
        return turned.y;
    }

    public int getHitchEntityId(Side side) {
        return entityData.get(side == Side.LEFT ? DATA_HITCH_LEFT : DATA_HITCH_RIGHT);
    }

    private boolean hitchMatches(Side side, int animalId) {
        return animalId >= 0 && getHitchEntityId(side) == animalId;
    }

    private void updateWheelMask() {
        int mask = 0;
        for(int i = 0; i < wheels.size(); i++) {
            if(!wheels.get(i).isEmpty())
                mask |= 1 << i;
        }
        entityData.set(DATA_WHEELS, (byte)mask);
    }

    public boolean movePlayerToSeat(Player player, int seat) {
        if(player.getVehicle() != this)
            return false;
        if(seat < 0 || seat >= seats.length || !seats[seat].acceptsPlayers())
            return false;
        if(seatEntities[seat] != null && seatEntities[seat] != player)
            return false;

        int current = seatIndexOf(player);
        if(current < 0 || current == seat)
            return false;

        seatEntities[current] = null;
        seatIds[current] = -1;
        seatEntities[seat] = player;
        seatIds[seat] = player.getId();
        syncSeats();
        return true;
    }

    @Override
    public LivingEntity getControllingPassenger() {
        for(int i = 0; i < seats.length; i++) {
            if(seats[i].kind() != WagonSeat.Kind.DRIVER)
                continue;
            Entity driver = seatEntities[i];
            if(driver == null && seatIds[i] >= 0)
                driver = level().getEntity(seatIds[i]);
            return driver instanceof Player player ? player : null;
        }
        return null;
    }

    @Override
    protected boolean canAddPassenger(Entity passenger) {
        if(!(passenger instanceof LivingEntity))
            return false;
        if(pendingSeat >= 0)
            return seatEntities[pendingSeat] == null && seatAccepts(pendingSeat, passenger);
        return findNearestSeat(passenger) >= 0;
    }

    @Override
    protected Vec3 getRiderOffset(Entity passenger) {
        int index = seatIndexOf(passenger);
        return index < 0 ? null : seats[index].position();
    }

    @Override
    protected void addPassenger(Entity passenger) {
        if(!level().isClientSide)
            assignSeat(passenger);
        super.addPassenger(passenger);
    }

    @Override
    protected void removePassenger(Entity passenger) {
        super.removePassenger(passenger);
        clearSeat(passenger);
        if(!level().isClientSide) {
            if(passenger instanceof Mob)
                setBoardCooldown(passenger);
            syncSeats();
        }
    }

    public void applySeatState(int[] ids, boolean[] locks) {
        for(int i = 0; i < seatIds.length && i < ids.length; i++)
            seatIds[i] = ids[i];
        for(int i = 0; i < seatLocked.length && i < locks.length; i++)
            seatLocked[i] = locks[i];
    }

    private void assignSeat(Entity passenger) {
        int seat = -1;
        Integer saved = savedSeatByUuid.remove(passenger.getUUID());
        if(saved != null && saved >= 0 && saved < seats.length && seatEntities[saved] == null)
            seat = saved;
        else if(pendingSeat >= 0 && seatEntities[pendingSeat] == null)
            seat = pendingSeat;
        else
            seat = findNearestSeat(passenger);

        if(seat < 0)
            return;
        seatEntities[seat] = passenger;
        seatIds[seat] = passenger.getId();
        syncSeats();
    }

    private void clearSeat(Entity passenger) {
        for(int i = 0; i < seatEntities.length; i++) {
            if(seatEntities[i] == passenger) {
                seatEntities[i] = null;
                seatIds[i] = -1;
            }
        }
    }

    private int seatIndexOf(Entity passenger) {
        for(int i = 0; i < seatEntities.length; i++) {
            if(seatEntities[i] == passenger)
                return i;
        }
        int id = passenger.getId();
        for(int i = 0; i < seatIds.length; i++) {
            if(seatIds[i] == id)
                return i;
        }
        return -1;
    }

    private int findNearestSeat(Entity entity) {
        int best = -1;
        double bestDist = Double.MAX_VALUE;
        for(int i = 0; i < seats.length; i++) {
            if(seatEntities[i] != null)
                continue;
            if(!seatAccepts(i, entity))
                continue;
            double dist = entity.position().distanceToSqr(offsetToWorld(seats[i].position(), false));
            if(dist < bestDist) {
                bestDist = dist;
                best = i;
            }
        }
        return best;
    }

    private boolean seatAccepts(int index, Entity entity) {
        if(entity.getType().is(TrottingWagons.CANNOT_MOUNT_WAGON))
            return false;
        AbstractWagon hitched = !(entity instanceof Player) ? findHitch(entity) : null;
        if(hitched != null && hitched != this)
            return false;
        WagonSeat seat = seats[index];
        boolean player = entity instanceof Player;
        if(seatLocked[index] && !player && seat.canLockAnimals())
            return false;
        boolean large = entity.getType().is(TrottingWagons.LARGE_WAGON_PASSENGER);
        return switch(seat.kind()) {
            case DRIVER, PLAYER -> player;
            case ANIMAL, UNIVERSAL -> !player && !large || player && seat.kind() == WagonSeat.Kind.UNIVERSAL;
            case ANIMALBIG -> !player;
            case DRAFT -> false;
        };
    }

    private void boardNearbyAnimals() {
        AABB box = getBoundingBox().inflate(ANIMAL_BOARD_RANGE);
        List<Mob> mobs = level().getEntitiesOfClass(Mob.class, box, this::canAutoBoard);
        mobs.sort(Comparator.comparingDouble(mob -> mob.distanceToSqr(this)));
        for(Mob mob : mobs)
            mountNearest(mob);
    }

    private boolean canAutoBoard(Mob mob) {
        if(!mob.isAlive() || mob.isPassenger() || mob.isVehicle() || findHitch(mob) != null || isBoardCooldown(mob))
            return false;
        if(mob.getType().is(TrottingWagons.CANNOT_MOUNT_WAGON))
            return false;
        if(mob instanceof TamableAnimal tamable && tamable.getOwnerUUID() != null) {
            Entity owner = tamable.getOwner();
            if(owner == null || (owner.getVehicle() != this && owner.distanceToSqr(mob) > 9.0D))
                return false;
        }
        return findNearestSeat(mob) >= 0;
    }

    private boolean hasPullers() {
        if(getHorse(Side.LEFT) == null)
            return false;
        return hitchSlots() < 2 || getHorse(Side.RIGHT) != null;
    }

    protected Vec3 hitchOffset(Side side) {
        return new Vec3(side == Side.LEFT ? -horseX : horseX, 0, horseY);
    }

    private boolean hasFreeHitch() {
        if(getHorse(Side.LEFT) == null)
            return true;
        return hitchSlots() > 1 && getHorse(Side.RIGHT) == null;
    }

    private boolean isNearTongue(Entity entity) {
        return horizontalDistanceSqr(entity.position(), getTongueAnchor()) <= HITCH_RANGE * HITCH_RANGE;
    }

    private static double horizontalDistanceSqr(Vec3 a, Vec3 b) {
        double dx = a.x - b.x;
        double dz = a.z - b.z;
        return dx * dx + dz * dz;
    }

    private void refreshSeatIds() {
        for(int i = 0; i < seatEntities.length; i++)
            seatIds[i] = seatEntities[i] == null ? -1 : seatEntities[i].getId();
    }

    public void sendSeatSync(ServerPlayer player) {
        refreshSeatIds();
        PacketDistributor.sendToPlayer(player, seatPayload());
    }

    private SyncSeatsPayload seatPayload() {
        return new SyncSeatsPayload(getId(), seatIds.clone(), seatLocked.clone());
    }

    private void syncSeats() {
        if(level().isClientSide)
            return;
        refreshSeatIds();
        PacketDistributor.sendToPlayersTrackingEntityAndSelf(this, seatPayload());
    }

    public enum Side {
        LEFT, RIGHT
    }

    public enum WheelSet {
        REGULAR("gui.trotting_wagons.wheels.regular"),
        MISSING("gui.trotting_wagons.wheels.missing"),
        INCOMPATIBLE("gui.trotting_wagons.wheels.incompatible");

        private final String key;

        WheelSet(String key) {
            this.key = key;
        }

        public Component label() {
            return Component.translatable(key);
        }
    }

    public record WheelSag(float pitch, float roll, double drop) {
        public static final WheelSag NONE = new WheelSag(0, 0, 0);
    }

}
