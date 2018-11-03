package com.divroll.backend.model.action;

import org.immutables.value.Value;

@Value.Immutable
//@Value.Style(visibility = Value.Style.ImplementationVisibility.PRIVATE)
public interface LinkAction extends EntityAction {
    String linkName();
    String entityId();
}
