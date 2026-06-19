package top.ialdaiaxiariyay.gtms.api.recipe.content;

import com.gregtechceu.gtceu.api.recipe.content.IContentSerializer;

import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;

import com.mojang.serialization.Codec;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import top.ialdaiaxiariyay.gtms.api.recipe.ingredient.ManaStack;

@RequiredArgsConstructor
public class SerializerManaStack implements IContentSerializer<ManaStack> {

    public static final SerializerManaStack INSTANCE = new SerializerManaStack();

    @Override
    public void toNetwork(FriendlyByteBuf buf, @NotNull ManaStack content) {
        content.toNetwork(buf);
    }

    @Override
    public ManaStack fromNetwork(FriendlyByteBuf buf) {
        return ManaStack.fromNetwork(buf);
    }

    @Override
    public Tag toNbt(@NotNull ManaStack content) {
        return net.minecraft.nbt.LongTag.valueOf(content.amount());
    }

    @Override
    public ManaStack fromNbt(Tag nbt) {
        if (nbt instanceof net.minecraft.nbt.NumericTag num) {
            return new ManaStack(num.getAsLong());
        }
        return ManaStack.EMPTY;
    }

    @Override
    public ManaStack of(Object o) {
        if (o instanceof Number n) return new ManaStack(n.longValue());
        if (o instanceof ManaStack ms) return ms;
        if (o instanceof CharSequence cs) {
            try {
                return new ManaStack(Long.parseLong(cs.toString()));
            } catch (NumberFormatException ignored) {}
        }
        return ManaStack.EMPTY;
    }

    @Override
    public ManaStack defaultValue() {
        return ManaStack.EMPTY;
    }

    @Override
    public Class<ManaStack> contentClass() {
        return ManaStack.class;
    }

    @Override
    public Codec<ManaStack> codec() {
        return Codec.LONG.xmap(ManaStack::new, ManaStack::amount);
    }
}
