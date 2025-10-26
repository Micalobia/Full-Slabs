package dev.micalobia.fullslabs.util;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;

public interface EnumPayload<T extends Enum<T> & CustomPayload> extends CustomPayload {
    static <T extends Enum<T> & CustomPayload> PacketCodec<RegistryByteBuf, T> codecOf(Class<T> klass) {
        final var values = klass.getEnumConstants();
        return CustomPayload.codecOf((value, buf) -> buf.writeInt(value.ordinal()), buf -> values[buf.readInt()]);
    }
}
