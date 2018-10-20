package com.divroll.backend.model.action;

import org.immutables.value.Value;

import java.util.Map;
import java.util.Optional;

@Value.Immutable
@Value.Style(visibility = Value.Style.ImplementationVisibility.PRIVATE)
public interface Action {
    enum ACTION_OP  { LINK, SET }
    ACTION_OP actionOp();
    Optional<String> entityType();
    Optional<Map<String,Comparable>> entity();
    Optional<String> linkName();
    Optional<String> backLinkName();
    // For SET action
    Optional<String> propertyName();
    Optional<String> referenceProperty();
    Optional<Comparable> propertyValue();
    Optional<Action> next();
}
