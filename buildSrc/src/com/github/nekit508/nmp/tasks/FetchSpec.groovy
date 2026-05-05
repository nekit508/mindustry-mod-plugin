package com.github.nekit508.nmp.tasks

abstract class FetchSpec {
    int blockSize = 4096

    boolean doFetch() {
        var input = remote(),
            output = local(),
            buf = new byte[blockSize]

        long totalCount = 0
        for (int count; (count = input.read(buf)) != -1;) {
            output.write(buf, 0, count)
            totalCount += count
        }

        output.flush()

        true
    }

    abstract String inputDigest(InputStream stream);
    abstract String outputDigest(OutputStream stream);

    abstract InputStream remote();
    abstract OutputStream local();
}
