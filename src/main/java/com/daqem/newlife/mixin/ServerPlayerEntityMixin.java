package com.daqem.newlife.mixin;

import com.daqem.newlife.NewLife;
import com.daqem.newlife.entity.NewLifePlayerEntity;
import com.mojang.authlib.GameProfile;
import io.github.apace100.origins.component.OriginComponent;
import io.github.apace100.origins.origin.Origin;
import io.github.apace100.origins.origin.OriginLayer;
import io.github.apace100.origins.origin.OriginLayers;
import io.github.apace100.origins.origin.OriginRegistry;
import io.github.apace100.origins.registry.ModComponents;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.encryption.PlayerPublicKey;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameMode;
import net.minecraft.world.World;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.OptionalInt;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin extends PlayerEntity implements NewLifePlayerEntity {

    @Shadow public abstract OptionalInt openHandledScreen(@Nullable NamedScreenHandlerFactory factory);

    @Shadow public abstract void sendMessage(Text message, boolean actionBar);

    protected int rerolls;
    protected int lives;
    protected boolean died;
    protected List<Identifier> origins = new ArrayList<>();

    public ServerPlayerEntityMixin(World world, BlockPos pos, float yaw, GameProfile profile, @Nullable PlayerPublicKey publicKey) {
        super(world, pos, yaw, profile, publicKey);
    }

    @Override
    public boolean isSpectator() {
        return ((ServerPlayerEntity)(Object)this).interactionManager.getGameMode() == GameMode.SPECTATOR;
    }

    @Override
    public boolean isCreative() {
        return ((ServerPlayerEntity)(Object)this).interactionManager.getGameMode() == GameMode.CREATIVE;
    }

    @Override
    public int getLives() {
        return this.lives;
    }

    @Override
    public void setLives(int lives) {
        this.lives = lives;
    }

    @Override
    public boolean getDied() {
        return died;
    }

    @Override
    public List<Identifier> getOrigins() {
        return origins;
    }

    @Override
    public Origin assignRandomOrigin(boolean sendDeathMessage) {
        int count = 0;
        do {
            count = (count + 1);
            for (OriginLayer layer : OriginLayers.getLayers()) {
                if (layer.isEnabled()) {
                    final List<Identifier> randomOrigins = layer.getRandomOrigins(this);
                    final Origin origin = OriginRegistry.get(randomOrigins.get(this.getRandom().nextInt(randomOrigins.size())));
                    if (!(getOrigins().contains(origin.getIdentifier())) || count > 500) {
                        setOrigin(this, layer, origin, sendDeathMessage);
                        getOrigins().add(origin.getIdentifier());
                        return origin;
                    }
                }
            }
        } while (true);
    }

    private void setOrigin(PlayerEntity player, OriginLayer layer, Origin origin, boolean sendDeathMessage) {
        OriginComponent component = ModComponents.ORIGIN.get(player);
        component.setOrigin(layer, origin);
        OriginComponent.sync(player);
        boolean hadOriginBefore = component.hadOriginBefore();
        OriginComponent.partialOnChosen(player, hadOriginBefore, origin);
        if (sendDeathMessage) sendBroadcastMessage(origin);
    }

    private void sendBroadcastMessage(Origin origin) {
        int lives = this.lives >= 0 && this.lives <= NewLife.MAX_LIVES - 1 ? NewLife.MAX_LIVES - this.lives : 0;
        Text text;
        if (lives == 0) {
            text = Text.of(Formatting.DARK_RED + "" + Formatting.BOLD + getName().getString() + Formatting.RESET + "" + Formatting.RED + " is game over!");
        } else {
            text = Text.of(Formatting.DARK_RED + "" + Formatting.BOLD + getName().getString() + Formatting.RESET + "" + Formatting.RED + " has " + Formatting.DARK_RED + "" + Formatting.BOLD + lives + Formatting.RESET + "" + Formatting.RED + " lives left and received the " + Formatting.DARK_RED + "" + Formatting.BOLD + StringUtils.capitalize(origin.getIdentifier().toString().split(":")[1]) + Formatting.RESET + "" + Formatting.RED + " origin.");
        }
        if (getServer() != null) {
            for (ServerPlayerEntity serverPlayerEntity : getServer().getPlayerManager().getPlayerList()) {
                serverPlayerEntity.sendMessage(text, false);
            }
        }
    }

    @Inject(at = @At("TAIL"), method = "copyFrom(Lnet/minecraft/server/network/ServerPlayerEntity;Z)V")
    public void copyFromMixin(ServerPlayerEntity oldPlayer, boolean alive, CallbackInfo info) {
        NewLifePlayerEntity afterlifePlayer = (NewLifePlayerEntity)oldPlayer;
        this.lives = afterlifePlayer.getLives();
        this.origins = afterlifePlayer.getOrigins();
        this.died = afterlifePlayer.getDied();
        this.rerolls = afterlifePlayer.getRerolls();
    }

    @Inject(at = @At("TAIL"), method = "writeCustomDataToNbt(Lnet/minecraft/nbt/NbtCompound;)V")
    public void writeCustomDataToNbtMixin(NbtCompound nbt, CallbackInfo info) {
        nbt.putInt("AfterlifeLives", this.lives);
        nbt.putInt("AfterlifeRerolls", this.rerolls);
    }

    @Inject(at = @At("TAIL"), method = "readCustomDataFromNbt(Lnet/minecraft/nbt/NbtCompound;)V")
    public void readCustomDataFromNbtMixin(NbtCompound nbt, CallbackInfo info) {
        this.lives = nbt.getInt("AfterlifeLives");
        this.rerolls = nbt.getInt("AfterlifeRerolls");
    }

    @Inject(at = @At(value = "TAIL"), method = "onDeath")
    private  void onPlayerDeath(DamageSource source, CallbackInfo info) {
        NewLifePlayerEntity afterlifePlayer = this;
        afterlifePlayer.setLives(afterlifePlayer.getLives() + 1);
        died = true;
    }

    @Inject(at = @At(value = "TAIL"), method = "onSpawn")
    private void onPlayerSpawn(CallbackInfo info) {
        if (lives >= NewLife.MAX_LIVES) ((ServerPlayerEntity)(Object)this).changeGameMode(GameMode.SPECTATOR);
        if (died) {
            assignRandomOrigin(true);
            died = false;
        }
    }

    @Override
    public int getRerolls() {
        return rerolls;
    }

    public void setRerolls(int rerolls) {
        this.rerolls = rerolls;
    }
}
