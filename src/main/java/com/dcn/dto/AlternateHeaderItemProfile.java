package com.dcn.dto;

/**
 * @author _
 */
public class AlternateHeaderItemProfile extends AlternateHeaderItem{
    private Long numberOfTriples;

    public Long getNumberOfTriples() {
        return numberOfTriples;
    }

    public AlternateHeaderItemProfile setNumberOfTriples(Long numberOfTriples) {
        this.numberOfTriples = numberOfTriples;
        return this;
    }
}
