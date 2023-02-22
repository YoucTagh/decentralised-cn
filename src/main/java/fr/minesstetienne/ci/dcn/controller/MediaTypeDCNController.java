package fr.minesstetienne.ci.dcn.controller;

import fr.minesstetienne.ci.dcn.dto.AlternateHeaderItem;
import fr.minesstetienne.ci.dcn.dto.RepresentationDetail;
import fr.minesstetienne.ci.dcn.dto.ResourceDetail;
import fr.minesstetienne.ci.dcn.service.MediaTypeDCNService;
import fr.minesstetienne.ci.dcn.service.SameAsSearchService;
import fr.minesstetienne.ci.dcn.service.UtilService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author YoucTagh
 */
@Controller
@RequestMapping("/dcn/media-type")
public class MediaTypeDCNController {

    private final SameAsSearchService sameAsSearchService;
    private final MediaTypeDCNService mediaTypeDCNService;

    public MediaTypeDCNController(SameAsSearchService sameAsSearchService, MediaTypeDCNService mediaTypeDCNService) {
        this.sameAsSearchService = sameAsSearchService;
        this.mediaTypeDCNService = mediaTypeDCNService;
    }

    @RequestMapping(method = RequestMethod.GET, path = "/api")
    public ResponseEntity getBestRepresentation(@RequestParam String iri, @Nullable @RequestHeader(name = "accept") String accept) {
        System.out.println("accept header: " + accept);
        List<MediaType> acceptHeaderMT = MediaType.parseMediaTypes((accept != null) ? accept : "*/*");
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
