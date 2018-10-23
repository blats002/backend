package com.divroll.backend.model.builder;

import org.immutables.value.Value;

import java.util.Map;

@Value.Immutable
@Value.Style(visibility = Value.Style.ImplementationVisibility.PRIVATE)
public interface EntityClass {
    Map<String, Comparable> comparableMap();
    @Value.Default
    default String[] read() {
        return null;
    }
    @Value.Default
    default String[] write() {
        return null;
    }
    Boolean publicRead();
    Boolean publicWrite();


}
