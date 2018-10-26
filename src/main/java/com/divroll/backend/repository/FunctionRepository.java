package com.divroll.backend.repository;

public interface FunctionRepository {
    @Deprecated
    String createFunction(String appId, String functionName, String jar);
    @Deprecated
    boolean deleteFunction(String appId, String functionName);
    @Deprecated
    byte[] retrieveFunction(String appId, String functionName);
    byte[] retrieveFunctionEntity(String appId, String functionName);
}
