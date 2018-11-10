package com.divroll.backend.model.builder;

import org.immutables.value.Value;

import javax.annotation.Nullable;

@Value.Immutable
@Value.Style(visibility = Value.Style.ImplementationVisibility.PRIVATE)
public interface CreateOption {
    enum CREATE_OPTION { SET_BLOB_ON_PROPERTY_EQUALS }
    @Nullable CREATE_OPTION createOption();
    @Nullable String referencePropertyName();
}
