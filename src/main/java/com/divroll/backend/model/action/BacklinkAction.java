package com.divroll.backend.model.action;

import org.immutables.value.Value;

@Value.Immutable
//@Value.Style(visibility = Value.Style.ImplementationVisibility.PRIVATE)
public interface BacklinkAction extends EntityAction {
    String linkName();
    String entityId();
}
