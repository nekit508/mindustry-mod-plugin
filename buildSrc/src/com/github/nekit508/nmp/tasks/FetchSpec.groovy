package com.github.nekit508.nmp.tasks

import com.github.nekit508.nmp.lib.Utils
import org.gradle.api.logging.Logger

import java.util.function.Consumer

class DownloaderLogger implements Consumer<Long> {
    long prev_time = System.currentTimeMillis()
    long prev_count
    final bs = 4096 * 1024
    long prev = 0

    @Override
    void accept(Long count) {
        if (count - prev > bs) {
            long time = System.currentTimeMillis()

            logger().lifecycle("Downloaded ${(long) (count / 1024 / 1024)} mB. (avg ${((count - prev_count) / 1024D) / ((System.currentTimeMillis() - prev_time) / 1000D)} kBs/sec)")
            prev = (long) ((long) (count / bs)) * bs

            prev_time = time
            prev_count = count
        }
    }
}

abstract interface FetchSpec {
    default void doFetch() {
        if (hasDigest() && inputDigest() == outputDigest()) {
            logger().lifecycle("Identical digests - aborting fetch")
            return
        }

        if (isOffline()) {
            logger().lifecycle("warning: Working in offline mode - skip downloading")
            return
        }



        Utils.readFile remote(), local(), blockSize(), new DownloaderLogger()
    }

    abstract Logger logger();

    abstract boolean hasDigest();

    abstract String inputDigest();
    abstract String outputDigest();

    abstract int blockSize();

    abstract boolean isOffline();

    abstract InputStream remote();
    abstract OutputStream local();
}
