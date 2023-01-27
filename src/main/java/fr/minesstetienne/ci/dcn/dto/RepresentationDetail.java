package fr.minesstetienne.ci.dcn.dto;

/**
 * @author YoucTagh
 */
public class RepresentationDetail {
    private String iri;
    private String status;

    private String contentType;
    private String content;

    private Long tripleNumber;

    private boolean isValid;

    public String getIri() {
        return iri;
    }

    public RepresentationDetail setIri(String iri) {
        this.iri = iri;
        return this;
    }

    public String getStatus() {
        return status;
    }

    public RepresentationDetail setStatus(String status) {
        this.status = status;
        return this;
    }

    public String getContent() {
        return content;
    }

    public RepresentationDetail setContent(String content) {
        this.content = content;
        return this;
    }

    public String getContentType() {
        return contentType;
    }

    public RepresentationDetail setContentType(String contentType) {
        this.contentType = contentType;
        return this;
    }

    public Long getTripleNumber() {
        return tripleNumber;
    }

    public RepresentationDetail setTripleNumber(Long tripleNumber) {
        this.tripleNumber = tripleNumber;
        return this;
    }

    public boolean isValid() {
        return isValid;
    }

    public RepresentationDetail setValid(boolean valid) {
        isValid = valid;
        return this;
    }
}
