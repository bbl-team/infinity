package com.benbenlaw.infinity.block.entity;

import com.benbenlaw.infinity.Infinity;
import com.benbenlaw.infinity.item.ModItems;
import com.benbenlaw.infinity.multiblock.MultiBlockManagers;
import com.benbenlaw.infinity.networking.ModMessages;
import com.benbenlaw.infinity.networking.packets.PacketSyncItemStackToClient;
import com.benbenlaw.infinity.recipe.GeneratorRecipe;
import com.benbenlaw.infinity.screen.InfinityGeneratorMenu;
import com.benbenlaw.infinity.util.ModEnergyStorage;
import com.benbenlaw.opolisutilities.recipe.DryingTableRecipe;
import com.benbenlaw.opolisutilities.recipe.NoInventoryRecipe;
import com.benbenlaw.opolisutilities.util.inventory.IInventoryHandlingBlockEntity;
import com.benbenlaw.opolisutilities.util.inventory.WrappedHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.EnergyStorage;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.benbenlaw.opolisutilities.block.custom.DryingTableBlock.WATERLOGGED;

public class InfinityGeneratorBlockEntity extends BlockEntity implements MenuProvider, IInventoryHandlingBlockEntity {

    private final ItemStackHandler itemHandler = new ItemStackHandler(1) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            assert level != null;
            if (!level.isClientSide()) {
                ModMessages.sendToClients(new PacketSyncItemStackToClient(this, worldPosition));
            }
        }
    };

    private LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.empty();
    private final Map<Direction, LazyOptional<WrappedHandler>> directionWrappedHandlerMap =
            Map.of(Direction.DOWN, LazyOptional.of(() -> new WrappedHandler(itemHandler, (i) -> i == 1, (i, s) -> false)),

                    Direction.UP, LazyOptional.of(() -> new WrappedHandler(itemHandler, (index) -> index == 0,
                            (index, stack) -> index == 0 && itemHandler.isItemValid(0, stack))),

                    Direction.NORTH, LazyOptional.of(() -> new WrappedHandler(itemHandler, (index) -> index == 0,
                            (index, stack) -> index == 0 && itemHandler.isItemValid(0, stack))),

                    Direction.SOUTH, LazyOptional.of(() -> new WrappedHandler(itemHandler, (index) -> index == 0,
                            (index, stack) -> index == 0 && itemHandler.isItemValid(0, stack))),

                    Direction.WEST, LazyOptional.of(() -> new WrappedHandler(itemHandler, (index) -> index == 0,
                            (index, stack) -> index == 0 && itemHandler.isItemValid(0, stack))),

                    Direction.EAST, LazyOptional.of(() -> new WrappedHandler(itemHandler, (index) -> index == 0,
                            (index, stack) -> index == 0 && itemHandler.isItemValid(0, stack)))
            );

    private LazyOptional<IEnergyStorage> lazyEnergyHandler = LazyOptional.empty();

    protected final ContainerData data;
    public int progress;
    public int maxProgress;
    public ItemStack input;
    public final ModEnergyStorage ENERGY_STORAGE = createEnergyStorage();
    public int RFPerTick;
    public int fuelDuration = 0;
    final int maxEnergyStorage = 1000000;
    final int maxEnergyTransfer = 1000000;
    public boolean hasStructure;
    public boolean hasFuel;
    public boolean hasEnoughPowerStorageAvailable;

    private ModEnergyStorage createEnergyStorage() {
        return new ModEnergyStorage(maxEnergyStorage, maxEnergyTransfer) {
            @Override
            public void onEnergyChanged() {
                setChanged();
                getLevel().sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
            }
        };
    }

    public boolean getHasStructure() {
        return hasStructure;
    }
    public boolean getHasFuel() {
        return hasStructure;
    }
    public boolean hasEnoughPowerStorageAvailable() {
        return hasStructure;
    }


    public int getProgress() {
        return progress;
    }

    public int getMaxProgress() {
        return maxProgress;
    }

    public ItemStack getInput() {
        return input;
    }

    public int getRFPerTick() {
        return RFPerTick;
    }

    @Override
    public void setHandler(ItemStackHandler handler) {
        copyHandlerContents(handler);
    }

    private void copyHandlerContents(ItemStackHandler handler) {
        for (int i = 0; i < handler.getSlots(); i++) {
            itemHandler.setStackInSlot(i, handler.getStackInSlot(i));
        }
    }

    public IEnergyStorage getEnergyStorage() {
        return this.ENERGY_STORAGE;
    }

    public ItemStackHandler getItemStackHandler() {
        return this.itemHandler;
    }

    public InfinityGeneratorBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(ModBlockEntities.INFINITY_GENERATOR_BLOCK_ENTITY.get(), blockPos, blockState);
        this.data = new ContainerData() {
            public int get(int index) {
                return switch (index) {
                    case 0 -> progress;
                    case 1 -> maxProgress;
                    default -> 0;
                };
            }

            public void set(int index, int value) {
                switch (index) {
                    case 0 -> progress = value;
                    case 1 -> maxProgress = value;
                }
            }

            public int getCount() {
                return 1; //Change to 1 from 2 to fix log spam not sure why it causes spam though

            }
        };
    }


    @Override
    public Component getDisplayName() {
        return Component.literal("Infinity Generator");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerID, Inventory inventory, Player player) {
        return new InfinityGeneratorMenu(containerID, inventory, this, this.data);
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap) {
        if (cap == ForgeCapabilities.ENERGY) {
            return lazyEnergyHandler.cast();
        }
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            return lazyItemHandler.cast();
        }
        return super.getCapability(cap);
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @javax.annotation.Nullable Direction side) {
        if (cap == ForgeCapabilities.ENERGY) {
            return lazyEnergyHandler.cast();
        }
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            return directionWrappedHandlerMap.get(side).cast();
        }

        return super.getCapability(cap, side);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        lazyItemHandler = LazyOptional.of(() -> itemHandler);
        lazyEnergyHandler = LazyOptional.of(() -> ENERGY_STORAGE);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        lazyItemHandler.invalidate();
        lazyEnergyHandler.invalidate();
        for (Direction dir : Direction.values()) {
            if (directionWrappedHandlerMap.containsKey(dir)) {
                directionWrappedHandlerMap.get(dir).invalidate();
            }
        }
    }


    @Override
    protected void saveAdditional(@NotNull CompoundTag tag) {
        tag.put("inventory", itemHandler.serializeNBT());
        tag.putInt("infinity_generator.progress", progress);
        tag.putInt("energy", ENERGY_STORAGE.getEnergyStored());
        super.saveAdditional(tag);
    }

    @Override
    public void load(@NotNull CompoundTag tag) {
        super.load(tag);
        itemHandler.deserializeNBT(tag.getCompound("inventory"));
        progress = tag.getInt("infinity_generator.progress");
        ENERGY_STORAGE.setEnergy(tag.getInt("energy"));
    }

    public void drops() {
        SimpleContainer inventory = new SimpleContainer(itemHandler.getSlots());
        for (int i = 0; i < itemHandler.getSlots(); i++) {
            inventory.setItem(i, itemHandler.getStackInSlot(i));
        }

        assert this.level != null;
        Containers.dropContents(this.level, this.worldPosition, inventory);
    }

    public void tick() {

        assert level != null;

        var result = MultiBlockManagers.POWER_MULTIBLOCKS.findStructure(level, this.worldPosition);
        assert level != null;

        if (result != null) {

            String foundPattern = result.ID();
            if (input == null) {
                SimpleContainer inventory = new SimpleContainer(this.itemHandler.getSlots());
                for (int i = 0; i < this.itemHandler.getSlots(); i++) {
                    inventory.setItem(i, this.itemHandler.getStackInSlot(i));
                }
                Optional<GeneratorRecipe> recipeMatch = level.getRecipeManager()
                        .getRecipeFor(GeneratorRecipe.Type.INSTANCE, inventory, level);
                if (recipeMatch.isPresent()) {

                    if (foundPattern.equals(recipeMatch.get().getPattern())) {
                        if (itemHandler.getStackInSlot(0).is(recipeMatch.get().getInputItem().getItem().asItem())) {
                            if (hasEnoughEnergyStorage(this, recipeMatch.get())) {
                                //Set recipe
                                this.input = recipeMatch.get().getInputItem();
                                this.RFPerTick = recipeMatch.get().getRFPerTick();
                                this.fuelDuration = recipeMatch.get().getFuelDuration();
                                setChanged(this.level, this.worldPosition, this.getBlockState());
                            }
                        }
                    }
                }
            }
        }
        //Running
        if (input != null && RFPerTick != 0 && fuelDuration != 0) {
            if (this.itemHandler.getStackInSlot(0).is(input.getItem()) && maxProgress == 0) {
                this.itemHandler.extractItem(0, 1, false);
                this.maxProgress = fuelDuration;
                this.progress = 0;
                setChanged(this.level, this.worldPosition, this.getBlockState());

            }
        }
        progress++;
        //Whilst running
        if (progress <= maxProgress) {
            level.addParticle(ParticleTypes.CRIMSON_SPORE, (double) this.worldPosition.getX() + 0.5D, (double) this.worldPosition.getY() + 0.5D, (double) this.worldPosition.getZ() + 0.5D, 0.5D, 0.5D, 0.5D);
            level.playSound(null, this.worldPosition, SoundEvents.BEACON_AMBIENT, SoundSource.BLOCKS);
            if (result == null) {
                resetGenerator();
                return;
            }

            this.ENERGY_STORAGE.receiveEnergy(RFPerTick, false);
            setChanged(this.level, this.worldPosition, this.getBlockState());
        }
        //End on running
        if (progress >= maxProgress) {
            resetGenerator();
        }
    }

    private void resetGenerator() {
        this.maxProgress = 0;
        this.progress = 0;
        this.RFPerTick = 0;
        input = null;
        setChanged(this.level, this.worldPosition, this.getBlockState());
    }

    boolean hasEnoughEnergyStorage (InfinityGeneratorBlockEntity entity, GeneratorRecipe recipe) {
        return maxEnergyStorage >= (recipe.getRFPerTick() * recipe.getFuelDuration())  + entity.getEnergyStorage().getEnergyStored();
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag() {
        return saveWithoutMetadata();
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        super.onDataPacket(net, pkt);
    }

}