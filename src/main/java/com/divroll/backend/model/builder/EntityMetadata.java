package com.divroll.backend.model.builder;

import org.immutables.value.Value;

import javax.annotation.Nullable;
import java.util.List;

@Value.Immutable
@Value.Style(visibility = Value.Style.ImplementationVisibility.PRIVATE)
public interface EntityMetadata {
  @Nullable
  List<String> uniqueProperties();
}
