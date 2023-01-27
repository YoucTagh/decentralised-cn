package fr.minesstetienne.ci.dcn.dto;

import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;

/**
 * @author YoucTagh
 */
@Accessors(chain = true)
public class ResourceDetail {
    private String mainIri;
    private String numRepresentations;
    private List<RepresentationDetail> representationDetails;

    public String getMainIri() {
        return mainIri;
    }

    public ResourceDetail setMainIri(String mainIri) {
        this.mainIri = mainIri;
        return this;
    }

    public String getNumRepresentations() {
        return numRepresentations;
    }

    public ResourceDetail setNumRepresentations(String numRepresentations) {
        this.numRepresentations = numRepresentations;
        return this;
    }

    public List<RepresentationDetail> getRepresentationDetails() {
        if (representationDetails == null)
            representationDetails = new ArrayList<>();
        return representationDetails;
    }

    public ResourceDetail setRepresentationDetails(List<RepresentationDetail> representationDetails) {
        this.representationDetails = representationDetails;
        return this;
    }
}


