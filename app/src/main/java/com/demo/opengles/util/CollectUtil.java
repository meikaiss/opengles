package com.demo.opengles.util;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class CollectUtil {

    public interface Executor<T> {
        void execute(T t);
    }

    public interface ValueComputer<T> {
        int computeValue(T t);
    }

    public static <T> void execute(List<T> list, Executor<T> executor) {
        if (list == null || list.isEmpty() || executor == null) {
            return;
        }

        for (int i = 0; i < list.size(); i++) {
            executor.execute(list.get(i));
        }
    }

    public static <T> T max(List<T> list, ValueComputer<T> valueComputer) {
        if (list == null || list.isEmpty() || valueComputer == null) {
            return null;
        }

        return Collections.max(list, new Comparator<T>() {
            @Override
            public int compare(T o1, T o2) {
                return valueComputer.computeValue(o1) - valueComputer.computeValue(o2);
            }
        });
    }

}
