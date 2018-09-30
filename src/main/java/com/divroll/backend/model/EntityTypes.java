package com.divroll.backend.model;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

import java.util.List;

@XStreamAlias("entityTypes")
public class EntityTypes {
    @XStreamImplicit(itemFieldName = "results")
    private List<EntityType> results;
    private long skip;
    private long limit;

    public EntityTypes() {}

    public List<EntityType> getResults() {
        return results;
    }

    public void setResults(List<EntityType> results) {
        this.results = results;
    }

    public long getSkip() {
        return skip;
    }

    public void setSkip(long skip) {
        this.skip = skip;
    }

    public long getLimit() {
        return limit;
    }

    public void setLimit(long limit) {
        this.limit = limit;
    }
}
