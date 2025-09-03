package com.github.nekit508.nmp.lib

class ScheduledActionsList {
    final List<Runnable> actions
    protected boolean scheduling

    ScheduledActionsList() {
        actions = new ArrayList<>()
        scheduling = false
    }

    ScheduledActionsList plus(Runnable runnable) {
        if (scheduling)
            runnable.run()
        else actions.add runnable
        this
    }

    void schedule() {
        scheduling = true
        actions*.run()
        scheduling = false
    }
}
