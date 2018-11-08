package util;

import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;

public class ComparableLinkedList<Comparable> extends LinkedList<Comparable>
    implements java.lang.Comparable {
    @Override
    public int compareTo(@NotNull Object o) {
        return 0;
    }
}
