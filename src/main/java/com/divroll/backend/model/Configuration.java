package com.divroll.backend.model;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import org.immutables.value.Value;

@Value.Immutable
@Value.Style(visibility = Value.Style.ImplementationVisibility.PRIVATE)
public interface Configuration {

    String getXodusRoot();
    String getDefaultUserStore();
    String getDefaultRoleStore();
    String getMasterStore();
    String getFileStore();

//    private final String xodusRoot;
//    private final String defaultUserStore;
//    private final String defaultRoleStore;
//    private final String masterStore;
//    private final String fileStore;

//    public Configuration(String xodusRoot,
//                         String defaultUserStore,
//                         String defaultRoleStore,
//                         String masterStore,
//                         String fileStore) {
//        this.xodusRoot = xodusRoot;
//        this.defaultUserStore = defaultUserStore;
//        this.defaultRoleStore = defaultRoleStore;
//        this.masterStore = masterStore;
//        this.fileStore = fileStore;
//    }

}
