package fr.minesstetienne.ci.dcn.dto;

import org.springframework.http.MediaType;

/**
 * @author YoucTagh
 */

public class AlternateHeaderItem {
    private String iri;
    private float acceptabilityValue;
    private MediaType mediaType;

    public AlternateHeaderItem setIri(String iri) {
        this.iri = iri;
        return this;
    }

    public AlternateHeaderItem setAcceptabilityValue(float acceptabilityValue) {
        this.acceptabilityValue = acceptabilityValue;
        return this;
    }

    public AlternateHeaderItem setMediaType(MediaType mediaType) {
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
