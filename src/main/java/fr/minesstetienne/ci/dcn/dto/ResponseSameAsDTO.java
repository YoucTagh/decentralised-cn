package fr.minesstetienne.ci.dcn.dto;

import java.util.List;

/**
 * @author YoucTagh
 */
public class ResponseSameAsDTO {

    private String uri;
    private String numDuplicates;
    private List<String> duplicates;

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getNumDuplicates() {
        return numDuplicates;
    }

    public void setNumDuplicates(String numDuplicates) {
        this.numDuplicates = numDuplicates;
    }

    public List<String> getDuplicates() {
        return duplicates;
    }

    public void setDuplicates(List<String> duplicates) {
        this.duplicates = duplicates;
    }

    @Override
    public String toString() {
        return "ResponseSameAsDTO{" +
                "uri='" + uri + '\'' +
                ", numDuplicates='" + numDuplicates + '\'' +
                ", duplicates=" + duplicates +
                '}';
    }
}


