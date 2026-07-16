package top.ialdaiaxiariyay.gtbss.api.player;

import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.util.INBTSerializable;

import lombok.Getter;

public class PlayerSanity implements INBTSerializable<CompoundTag> {

    @Getter
    private int sanity = 80;

    public PlayerSanity() {}

    public void setSanity(int value) {
        this.sanity = Math.max(0, Math.min(100, value));
    }

    public void addSanity(int delta) {
        setSanity(sanity + delta);
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("sanity", sanity);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        sanity = tag.getInt("sanity");
    }
}
