package dev.micalobia.fullslabs.util;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public interface EnumPayload<T extends Enum<T> & CustomPacketPayload> extends CustomPacketPayload {
    static <T extends Enum<T> & CustomPacketPayload> StreamCodec<RegistryFriendlyByteBuf, T> codecOf(Class<T> klass) {
        final var values = klass.getEnumConstants();
        return CustomPacketPayload.codec((value, buf) -> buf.writeVarInt(value.ordinal()), buf -> values[buf.readVarInt()]);
    }
}
