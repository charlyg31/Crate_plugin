package com.hazebyte.crate.cratereloaded.component.model;

public class CrateOpenResponse {
    private CrateOpenResponse() {}

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        public CrateOpenResponse build() { return new CrateOpenResponse(); }
    }

    @Override
    public String toString() { return "CrateOpenResponse()"; }
}
