package fr.minesstetienne.ci.dcn.dto;

/**
 * @author YoucTagh
 */

public class AlternateHeaderItem {
    private String iri;
    private float acceptabilityValue;
    private String mediaType;

    public AlternateHeaderItem setIri(String iri) {
        this.iri = iri;
        return this;
    }

    public AlternateHeaderItem setAcceptabilityValue(float acceptabilityValue) {
        this.acceptabilityValue = acceptabilityValue;
        return this;
    }

    public AlternateHeaderItem setMediaType(String mediaType) {
        this.mediaType = mediaType;
        return this;
    }

    @Override
    public String toString() {
        return "{"
                + "\"" + iri + "\" "
                + acceptabilityValue
                + " {type " + mediaType + "}" +
                "}";
    }
}
