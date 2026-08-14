package top.ialdaiaxiariyay.gtbss.api.wireless;

import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.ArrayDeque;

public class TimeWheel {

    private int firstUpdateTick = -1;
    private int lastUpdateTick = -1;

    public static class TIMESCALE {

        public final static int SECOND = 20;
        public final static int MINUTE = 20 * 60;
        public final static int HOUR = 20 * 60 * 60;
    }

    int length;
    int windowSize;
    int slotResolution;
    private final int slotNum;
    ArrayDeque<Slot> slots;
    private BigInteger sum = BigInteger.ZERO;
    private final int startIndex;
    private int currentIndex;

    public TimeWheel(int slotResolution, int slotNum, int windowStart) {
        this.length = 0;
        this.slotNum = slotNum;
        this.windowSize = slotResolution * slotNum;
        this.slotResolution = slotResolution <= 0 ? 20 : slotResolution;
        this.startIndex = (windowStart / slotResolution) % slotNum;
        this.currentIndex = startIndex;
        slots = new ArrayDeque<>(slotNum);
        slots.offer(new Slot());
    }

    public boolean tock() {
        if (slots.size() == slotNum) {
            Slot s = slots.poll();
            if (s != null) {
                sum = sum.subtract(s.sum);
            }
        }
        slots.offer(new Slot());
        currentIndex = (currentIndex + 1) % slotNum;
        return currentIndex == startIndex;
    }

    public void update(BigInteger value, int currentTick) {
        Slot slot = slots.peekLast();
        if (slot == null) return;
        slot.sum = slot.sum.add(value);
        sum = sum.add(value);
        this.lastUpdateTick = currentTick;
        if (firstUpdateTick == -1) firstUpdateTick = lastUpdateTick;
    }

    public @NotNull BigDecimal getAvgByTick() {
        if (firstUpdateTick == -1 || lastUpdateTick == -1) {
            return BigDecimal.ZERO;
        }

        long totalTicks = lastUpdateTick - firstUpdateTick + 1;
        long windowTicks = (long) slotResolution * slotNum;

        if (totalTicks < windowTicks) {
            if (totalTicks == 0) return BigDecimal.ZERO;
            return new BigDecimal(sum).divide(BigDecimal.valueOf(totalTicks), RoundingMode.HALF_UP);
        } else {
            long denominator = (long) slots.size() * slotResolution + (lastUpdateTick % slotResolution) -
                    slotResolution;
            if (denominator <= 0) {
                return BigDecimal.ZERO;
            }
            return new BigDecimal(sum).divide(BigDecimal.valueOf(denominator), RoundingMode.HALF_UP);
        }
    }

    public static class Slot {

        BigInteger sum = BigInteger.ZERO;
    }
}
