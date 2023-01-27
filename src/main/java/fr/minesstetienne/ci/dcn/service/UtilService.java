package fr.minesstetienne.ci.dcn.service;

import org.springframework.http.MediaType;

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
}
