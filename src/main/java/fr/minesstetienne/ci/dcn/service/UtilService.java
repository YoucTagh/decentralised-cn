package fr.minesstetienne.ci.dcn.service;

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

    public static List<MediaType> getSemanticAcceptedMediaTypes(){
        return Arrays.asList(
                MediaType.parseMediaType("application/rdf+xml"),
                MediaType.parseMediaType("text/turtle"),
                MediaType.parseMediaType("application/n-triples"));
    }

    public static boolean isBetterRepresentationThan(){
        return true;
    }

    public static void main(String[] args) {
        System.out.println(isMediaTypeContainsInList(
                MediaType.parseMediaType("text/turtle"),
                MediaType.parseMediaTypes("text/html")
        ));
    }
}
