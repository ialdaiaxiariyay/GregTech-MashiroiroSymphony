package top.ialdaiaxiariyay.gtms.api.recipe.ingredient;

import com.gregtechceu.gtceu.api.capability.recipe.IO;

import net.minecraft.network.FriendlyByteBuf;

import lombok.With;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * A simplified Mana stack representing an amount of Mana (e.g., mana per tick).
 */
@With
public record ManaStack(long amount) {

    public static final ManaStack EMPTY = new ManaStack(0);

    public ManaStack(long amount) {
        this.amount = Math.max(0, amount);
    }

    public long getTotalMana() {
        return amount;
    }

    public boolean isEmpty() {
        return amount <= 0;
    }

    @Contract("_ -> new")
    public @NotNull ManaStack add(@NotNull ManaStack other) {
        return new ManaStack(this.amount + other.amount);
    }

    @Contract("_ -> new")
    public @NotNull ManaStack addAmount(long delta) {
        return new ManaStack(this.amount + delta);
    }

    @Contract("_ -> new")
    public @NotNull ManaStack multiplyAmount(long multiplier) {
        return new ManaStack(this.amount * multiplier);
    }

    public void toNetwork(@NotNull FriendlyByteBuf buf) {
        buf.writeVarLong(amount);
    }

    @Contract("_ -> new")
    public static @NotNull ManaStack fromNetwork(@NotNull FriendlyByteBuf buf) {
        return new ManaStack(buf.readVarLong());
    }

    // 添加到 ManaStack.java 中
    @With
    public record WithIO(ManaStack stack, IO io) {

        public static final WithIO EMPTY = new WithIO(ManaStack.EMPTY, IO.NONE);

        public WithIO {
            if (stack.isEmpty()) {
                io = IO.NONE;
            }
        }

        public WithIO(long amount, IO io) {
            this(new ManaStack(amount), io);
        }

        public boolean isEmpty() {
            return io == IO.NONE || stack.isEmpty();
        }

        public boolean isInput() {
            return io == IO.IN;
        }

        public boolean isOutput() {
            return io == IO.OUT;
        }

        public long amount() {
            return stack.amount();
        }

        public long signedAmount() {
            return isInput() ? amount() : -amount();
        }
    }
}
