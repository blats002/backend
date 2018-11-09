package util;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

public class ComparableHashMap<K extends Comparable, V extends Comparable> extends HashMap<K, V>
    implements Comparable<K> {

  @Override
  public int compareTo(@NotNull K o) {
    return 0;
  }
}
