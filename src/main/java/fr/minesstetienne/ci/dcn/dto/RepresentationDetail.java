package fr.minesstetienne.ci.dcn.dto;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

/**
 * @author YoucTagh
 */
public class RepresentationDetail {
    private String iri;
    private HttpStatus status;

    private MediaType contentType;
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

    public HttpStatus getStatus() {
        return status;
    }

    public RepresentationDetail setStatus(HttpStatus status) {
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

    public MediaType getContentType() {
        return contentType;
    }

    public RepresentationDetail setContentType(MediaType contentType) {
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
