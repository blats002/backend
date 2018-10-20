package com.divroll.backend.model.builder;

import org.immutables.value.Value;

import java.util.Map;

@Value.Immutable
@Value.Style(visibility = Value.Style.ImplementationVisibility.PRIVATE)
public interface EntityClass {
    Map<String, Comparable> comparableMap();
    String[] read();
    String[] write();
    Boolean publicRead();
    Boolean publicWrite();
}
