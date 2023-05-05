package com.dcn.controller;

import com.dcn.service.MediaTypeDCNService;
import com.dcn.service.UtilService;
import com.dcn.dto.AlternateHeaderItem;
import com.dcn.dto.RepresentationDetail;
import com.dcn.dto.ResourceDetail;
import com.dcn.service.SameAsSearchService;
import org.springframework.http.*;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author _
 */
@Controller
@RequestMapping("/dcn/api")
public class MediaTypeDCNController {

    private final SameAsSearchService sameAsSearchService;
    private final MediaTypeDCNService mediaTypeDCNService;

    public MediaTypeDCNController(SameAsSearchService sameAsSearchService, MediaTypeDCNService mediaTypeDCNService) {
        this.sameAsSearchService = sameAsSearchService;
        this.mediaTypeDCNService = mediaTypeDCNService;
    }

    @RequestMapping(method = RequestMethod.GET, path = "/media-type")
    public ResponseEntity getBestRepresentation(@RequestParam String iri, @Nullable @RequestHeader(name = "accept") String accept) {
        System.out.println("accept header: " + accept);
        List<MediaType> acceptHeaderMT;
        try {
            acceptHeaderMT = MediaType.parseMediaTypes((accept != null) ? accept : "*/*");
        } catch (InvalidMediaTypeException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_ACCEPTABLE);
        }
        ResponseEntity<String> representationIfAvailable = mediaTypeDCNService.getRepresentationIfAvailable(iri, acceptHeaderMT);
        if (representationIfAvailable.getStatusCode().equals(HttpStatus.OK)
                && UtilService.isMediaTypeContainsInList(representationIfAvailable.getHeaders().getContentType(), acceptHeaderMT)) {
            return representationIfAvailable;
        } else {
            ResourceDetail resourceDetail = sameAsSearchService.findSameResources(iri, MediaType.parseMediaTypes(accept));
            HttpHeaders headers = new HttpHeaders();

            for (int i = 0; i < resourceDetail.getRepresentationDetails().size(); i++) {

                RepresentationDetail representationDetail = resourceDetail.getRepresentationDetails().get(i);

                if (representationDetail.getStatus().equals(HttpStatus.OK)
                        && UtilService.isMediaTypeContainsInList(representationDetail.getContentType(), acceptHeaderMT)) {
                    headers.setContentType(representationDetail.getContentType());
                    headers.set(HttpHeaders.LOCATION, representationDetail.getIri());
                    headers.setVary(Stream.of(HttpHeaders.ACCEPT).collect(Collectors.toList()));

                    ResponseEntity<String> representation = mediaTypeDCNService.getRepresentationIfAvailable(representationDetail.getIri(), acceptHeaderMT);

                    ArrayList<AlternateHeaderItem> alternateHeaderItems = new ArrayList<>();

                    for (int j = i; j < resourceDetail.getRepresentationDetails().size(); j++) {
                        RepresentationDetail alternateRepresentation = resourceDetail.getRepresentationDetails().get(j);
                        if (alternateRepresentation.getStatus().equals(HttpStatus.OK)
                                && UtilService.isMediaTypeContainsInList(alternateRepresentation.getContentType(), acceptHeaderMT)) {
                            alternateHeaderItems.add(
                                    new AlternateHeaderItem()
                                            .setIri(alternateRepresentation.getIri())
                                            .setMediaType(alternateRepresentation.getContentType())
                                            .setAcceptabilityValue(1F)
                            );
                        }
                    }
                    // Format Alternate header
                    StringBuilder alternateHeaderSB = new StringBuilder();
                    for (AlternateHeaderItem alternateHeaderItem : alternateHeaderItems) {
                        String toString = alternateHeaderItem.toString();
                        alternateHeaderSB.append(toString).append(", ");
                    }
                    headers.set("Alternates", alternateHeaderSB.toString());

                    return new ResponseEntity<>(representation.getBody(), headers, HttpStatus.OK);
                }
            }

            return new ResponseEntity<>(HttpStatus.NOT_ACCEPTABLE);
        }
    }

}
