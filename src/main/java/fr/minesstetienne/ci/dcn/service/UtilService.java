package fr.minesstetienne.ci.dcn.service;

import fr.minesstetienne.ci.dcn.dto.RepresentationDetail;
import org.springframework.http.MediaType;

import java.util.Arrays;
import java.util.List;

/**
 * @author YoucTagh
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
        return representationDetail.getTripleNumber() > bestRepresentation.getTripleNumber();
    }

}
