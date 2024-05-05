package com.yunji.titanrtx.manager.service.http.params;

import lombok.extern.slf4j.Slf4j;

/**
 * ParamPartitioner
 *
 * @author leihz
 * @since 2020-05-19 5:12 下午
 */
@Slf4j
public class ParamPartitioner {
    private static final int NUM_PARTITIONS = 10;

    public static int partition(Integer linkId) {
        byte[] keyBytes = serialize(linkId);
        if (keyBytes == null) {
            log.warn("LinkId is null,partition to default 0.");
            return 0;
        }
        // hash the keyBytes to choose a partition
        return toPositive(murmur2(keyBytes)) % NUM_PARTITIONS;
    }

    public static byte[] serialize(Integer data) {
        if (data == null)
            return null;

        return new byte[]{
                (byte) (data >>> 24),
                (byte) (data >>> 16),
                (byte) (data >>> 8),
                data.byteValue()
        };
    }

    public static int toPositive(int number) {
        return number & 0x7fffffff;
    }

    public static int murmur2(final byte[] data) {
        int length = data.length;
        int seed = 0x9747b28c;
        // 'm' and 'r' are mixing constants generated offline.
        // They're not really 'magic', they just happen to work well.
        final int m = 0x5bd1e995;
        final int r = 24;

        // Initialize the hash to a random value
        int h = seed ^ length;
        int length4 = length / 4;

        for (int i = 0; i < length4; i++) {
            final int i4 = i * 4;
            int k = (data[i4 + 0] & 0xff) + ((data[i4 + 1] & 0xff) << 8) + ((data[i4 + 2] & 0xff) << 16) + ((data[i4 + 3] & 0xff) << 24);
            k *= m;
            k ^= k >>> r;
            k *= m;
            h *= m;
            h ^= k;
        }

        // Handle the last few bytes of the input array
        switch (length % 4) {
            case 3:
                h ^= (data[(length & ~3) + 2] & 0xff) << 16;
            case 2:
                h ^= (data[(length & ~3) + 1] & 0xff) << 8;
            case 1:
                h ^= data[length & ~3] & 0xff;
                h *= m;
        }

        h ^= h >>> 13;
        h *= m;
        h ^= h >>> 15;

        return h;
    }

}
