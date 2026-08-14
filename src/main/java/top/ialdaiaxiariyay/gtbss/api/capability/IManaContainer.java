package top.ialdaiaxiariyay.gtbss.api.capability;

import net.minecraft.core.Direction;

import java.math.BigInteger;

/**
 * Mana container interface for storing, inserting, and extracting Mana.
 * Mana is transferred as packets, each packet carrying a certain amount of Mana,
 * and multiple packets can be transferred per tick.
 */
public interface IManaContainer extends IManaInfoProvider {

    /**
     * This method is basically {@link #changeMana(long)}, but it also handles Mana packets.
     * This method should always be used when Mana is passed between blocks.
     *
     * @param manaPerPacket amount of Mana per packet (Mana to add / input packet size)
     * @param packetCount   number of packets to send (Mana to add / input packet count)
     * @return amount of accepted packets. 0 if nothing was accepted.
     */
    long acceptManaFromNetwork(Direction side, long manaPerPacket, long packetCount);

    /**
     * @return if this container accepts Mana from the given side
     */
    boolean inputsMana(Direction side);

    /**
     * @return if this container can output Mana to the given side
     */
    default boolean outputsMana(Direction side) {
        return true;
    }

    /**
     * This changes the amount stored.
     * <b>This should only be used internally</b> (e.g. draining while working or filling while generating).
     * For transfer between blocks use {@link #acceptManaFromNetwork(Direction, long, long)}!!!
     *
     * @param differenceAmount amount of Mana to add (>0) or remove (<0)
     * @return amount of Mana added or removed
     */
    long changeMana(long differenceAmount);

    /**
     * Adds specified amount of Mana to this Mana container
     *
     * @param manaToAdd amount of Mana to add
     * @return amount of Mana added
     */
    default long addMana(long manaToAdd) {
        return changeMana(manaToAdd);
    }

    /**
     * Removes specified amount of Mana from this Mana container
     *
     * @param manaToRemove amount of Mana to remove
     * @return amount of Mana removed
     */
    default long removeMana(long manaToRemove) {
        return -changeMana(-manaToRemove);
    }

    /**
     * @return the maximum amount of Mana that can be inserted
     */
    default long getManaCanBeInserted() {
        return getManaCapacity() - getManaStored();
    }

    /**
     * @return amount of currently stored Mana
     */
    long getManaStored();

    /**
     * @return maximum amount of storable Mana
     */
    long getManaCapacity();

    @Override
    default IManaInfoProvider.ManaInfo getManaInfo() {
        return new IManaInfoProvider.ManaInfo(BigInteger.valueOf(getManaCapacity()),
                BigInteger.valueOf(getManaStored()));
    }

    @Override
    default boolean supportsBigIntManaValues() {
        return false;
    }

    /**
     * @return maximum amount of output able Mana packets per tick
     */
    default long getOutputPacketCount() {
        return 0L;
    }

    /**
     * @return output Mana packet size
     */
    default long getOutputPacketSize() {
        return 0L;
    }

    /**
     * @return maximum amount of receivable Mana packets per tick
     */
    long getInputPacketCount();

    /**
     * @return input Mana packet size
     *         Exceeding this value may cause the machine to explode.
     */
    long getInputPacketSize();

    /**
     * @return input Mana per second
     */
    @Override
    default long getInputPerSec() {
        return 0L;
    }

    /**
     * @return output Mana per second
     */
    @Override
    default long getOutputPerSec() {
        return 0L;
    }

    IManaContainer DEFAULT = new IManaContainer() {

        @Override
        public long acceptManaFromNetwork(Direction side, long manaPerPacket, long packetCount) {
            return 0;
        }

        @Override
        public boolean inputsMana(Direction side) {
            return false;
        }

        @Override
        public long changeMana(long differenceAmount) {
            return 0;
        }

        @Override
        public long getManaStored() {
            return 0;
        }

        @Override
        public long getManaCapacity() {
            return 0;
        }

        @Override
        public long getInputPacketCount() {
            return 0;
        }

        @Override
        public long getInputPacketSize() {
            return 0;
        }
    };
}
