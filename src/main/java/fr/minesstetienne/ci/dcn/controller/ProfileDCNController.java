package fr.minesstetienne.ci.dcn.controller;

import fr.minesstetienne.ci.dcn.dto.AlternateHeaderItem;
import fr.minesstetienne.ci.dcn.dto.RepresentationDetail;
import fr.minesstetienne.ci.dcn.dto.ResourceDetail;
import fr.minesstetienne.ci.dcn.service.ProfileDCNService;
import fr.minesstetienne.ci.dcn.service.SameAsSearchService;
import fr.minesstetienne.ci.dcn.service.UtilService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;

/**
 * @author YoucTagh
 */
@Controller
@RequestMapping("/dcn/profile")
public class ProfileDCNController {

    private final ProfileDCNService profileDCNService;
    private final SameAsSearchService sameAsSearchService;


    public ProfileDCNController(ProfileDCNService profileDCNService, SameAsSearchService sameAsSearchService) {
        this.profileDCNService = profileDCNService;
        this.sameAsSearchService = sameAsSearchService;
    }

    @GetMapping("/api")
    public ResponseEntity getBestRepresentationWithProfile(@RequestParam String iri, @Nullable @RequestHeader("Accept-Profile") String profileURI) {

        if (profileURI == null) {
            return new ResponseEntity<>(HttpStatus.NOT_ACCEPTABLE);
        }

        ResponseEntity<String> representationIfAvailable = profileDCNService.getRepresentationIfAvailable(iri, profileURI);
        if (representationIfAvailable.getStatusCode().equals(HttpStatus.OK)) {
            return representationIfAvailable;
        } else {
            ResourceDetail resourceDetail = sameAsSearchService.findSameResources(iri, UtilService.getSemanticAcceptedMediaTypes());
            HttpHeaders headers = new HttpHeaders();

            for (int i = 0; i < resourceDetail.getRepresentationDetails().size(); i++) {

                RepresentationDetail representationDetail = resourceDetail.getRepresentationDetails().get(i);

                if (representationDetail.getStatus().equals(HttpStatus.OK) && representationDetail.isValid()) {
                    headers.setContentType(MediaType.valueOf("text/turtle"));
                    headers.set(HttpHeaders.LOCATION, representationDetail.getIri());
                    headers.set(HttpHeaders.LINK, "<" + profileURI + ">" + ";rel=\"profile\"");
                    headers.setVary(List.of("Accept-Profile"));

                    ResponseEntity<String> representation = profileDCNService.getRepresentationIfAvailable(representationDetail.getIri(), profileURI);

                    ArrayList<AlternateHeaderItem> alternateHeaderItems = new ArrayList<>();

                    for (int j = i; j < resourceDetail.getRepresentationDetails().size(); j++) {
                        RepresentationDetail alternateRepresentation = resourceDetail.getRepresentationDetails().get(j);
                        if (alternateRepresentation.getStatus().equals(HttpStatus.OK) && alternateRepresentation.isValid()) {
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
