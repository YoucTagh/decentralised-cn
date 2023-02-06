package fr.minesstetienne.ci.dcn.dto;

/**
 * @author YoucTagh
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
