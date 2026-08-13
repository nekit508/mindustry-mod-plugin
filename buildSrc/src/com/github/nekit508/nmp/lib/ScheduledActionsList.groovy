package com.github.nekit508.nmp.lib

class ScheduledActionsList {
    protected final List<Runnable> actions

    protected var scheduling = false
    protected var finalized = false

    public var finalizeAfterSchedule = true
    protected StackTraceElement[] finalizedAt

    protected int actionsNum = 0

    ScheduledActionsList() {
        actions = new ArrayList<>()
    }

    int getSize() {
        return actionsNum
    }

    ScheduledActionsList plus(Runnable runnable) {
        checkFinalization()

        actionsNum += 1
        if (scheduling)
            runnable.run()
        else actions.add runnable

        this
    }

    void schedule() {
        scheduling = true
        actions*.run()
        scheduling = false

        if (finalizeAfterSchedule)
            finalizeList()
    }

    void finalizeList() {
        finalized = true
        finalizedAt = Thread.currentThread().stackTrace
    }

    void checkFinalization() {
        if (!finalized) return

        var cause = new Exception()
        cause.setStackTrace finalizedAt
        throw new RuntimeException("Actions list is finalized.\n", cause)
    }
}
