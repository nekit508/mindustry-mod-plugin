package com.nekit508.simpleupdater.events;

import arc.func.Cons;
import arc.struct.ObjectMap;
import arc.struct.Seq;

public class Events {
    public static ObjectMap<Object, Seq<Cons<Object>>> events = new ObjectMap<>();

    public static <T> void on(T event, Cons<T> action) {
        if (!events.containsKey(event))
            events.put(event, new Seq<>());
        events.get(event).add((Cons<Object>) action);
    }

    public static void fire(Object event) {
        if (!events.containsKey(event)) return;

        Seq<Cons<Object>> actions = events.get(event);
        for (int i = 0; i < actions.size; i++) {
            actions.get(i).get(event);
        }
    }
}
