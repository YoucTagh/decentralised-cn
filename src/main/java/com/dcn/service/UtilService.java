package com.dcn.service;

import com.dcn.dto.RepresentationDetail;
import org.apache.jena.shacl.validation.ReportEntry;
import org.apache.jena.shacl.validation.Severity;
import org.springframework.http.MediaType;

import java.util.Arrays;
import java.util.List;

/**
 * @author _
 */
public class UtilService {

    public static boolean isMediaTypeContainsInList(MediaType mediaType, List<MediaType> mediaTypeList) {
        for (MediaType m : mediaTypeList) {
            if (m.includes(mediaType))
                return true;
        }
        return false;
    }

    public static List<MediaType> getSemanticAcceptedMediaTypes() {
        return Arrays.asList(
                MediaType.parseMediaType("application/rdf+xml"),
                MediaType.parseMediaType("text/turtle"),
                MediaType.parseMediaType("application/n-triples"));
    }

    public static boolean isBetterRepresentationThan(RepresentationDetail representationDetail, RepresentationDetail bestRepresentation) {

        int newRepresentationWarningCount = 0;
        int newRepresentationInfoCount = 0;
        int bestRepresentationWarningCount = 0;
        int bestRepresentationInfoCount = 0;

        for (ReportEntry reportEntry : representationDetail.getValidationReport().getEntries()) {
            if (reportEntry.severity().equals(Severity.Warning)) {
                newRepresentationWarningCount++;
            } else if (reportEntry.severity().equals(Severity.Info)) {
                newRepresentationInfoCount++;
            }
        }
        for (ReportEntry reportEntry : bestRepresentation.getValidationReport().getEntries()) {
            if (reportEntry.severity().equals(Severity.Warning)) {
                bestRepresentationWarningCount++;
            } else if (reportEntry.severity().equals(Severity.Info)) {
                bestRepresentationInfoCount++;
            }
        }

        if (newRepresentationWarningCount != bestRepresentationWarningCount) {
            return newRepresentationWarningCount < bestRepresentationWarningCount;
        }

        if (newRepresentationInfoCount != bestRepresentationInfoCount) {
            return newRepresentationInfoCount < bestRepresentationInfoCount;
        }

        return representationDetail.getTripleNumber() > bestRepresentation.getTripleNumber();
    }

}
