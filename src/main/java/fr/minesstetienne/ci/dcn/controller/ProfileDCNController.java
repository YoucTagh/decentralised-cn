package fr.minesstetienne.ci.dcn.controller;

import fr.minesstetienne.ci.dcn.dto.AlternateHeaderItemProfile;
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
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    @RequestMapping(method = RequestMethod.GET, path = "/api", produces = {"text/turtle"})
    public ResponseEntity getBestRepresentationWithProfile(@RequestParam String iri, @Nullable @RequestHeader("accept-Profile") String profileURI) {

        if (profileURI == null) {
            return new ResponseEntity<>(HttpStatus.NOT_ACCEPTABLE);
        }

        ResponseEntity<String> representationIfAvailable = profileDCNService.getRepresentationIfAvailable(iri, profileURI);
        if (representationIfAvailable.getStatusCode().equals(HttpStatus.OK)) {
            return representationIfAvailable;
        } else {
            ResourceDetail resourceDetail = sameAsSearchService.findSameResources(iri, UtilService.getSemanticAcceptedMediaTypes());
            HttpHeaders headers = new HttpHeaders();

            //String bestRepresentationIRI;
            RepresentationDetail bestRepresentation = new RepresentationDetail()
                    .setValid(false)
                    .setTripleNumber(0L);

            ArrayList<AlternateHeaderItemProfile> alternateHeaderItems = new ArrayList<>();

            for (RepresentationDetail sameAsRepresentation : resourceDetail.getRepresentationDetails()) {
                if (sameAsRepresentation.getStatus().equals(HttpStatus.OK)) {
                    RepresentationDetail representationDetail = profileDCNService.checkConformanceOfRepresentation(sameAsRepresentation.getIri(), profileURI);
                    if (representationDetail.isValid()) {
                        if (UtilService.isBetterRepresentationThan(representationDetail, bestRepresentation)) {
                            if (bestRepresentation.isValid())
                                alternateHeaderItems.add(
                                        (AlternateHeaderItemProfile) new AlternateHeaderItemProfile()
                                                .setNumberOfTriples(bestRepresentation.getTripleNumber())
                                                .setIri(bestRepresentation.getIri())
                                                .setMediaType(bestRepresentation.getContentType()));
                            bestRepresentation = representationDetail;
                        } else {
                            alternateHeaderItems.add(
                                    (AlternateHeaderItemProfile) new AlternateHeaderItemProfile()
                                            .setNumberOfTriples(representationDetail.getTripleNumber())
                                            .setIri(representationDetail.getIri())
                                            .setMediaType(representationDetail.getContentType()));
                        }
                    }
                }
            }

            if (bestRepresentation.isValid()) {
                // Format Alternate header
                StringBuilder alternateHeaderSB = new StringBuilder();
                for (AlternateHeaderItemProfile alternateHeaderItem : alternateHeaderItems) {
                    alternateHeaderItem.setAcceptabilityValue((float) alternateHeaderItem.getNumberOfTriples() / bestRepresentation.getTripleNumber());
                    String toString = alternateHeaderItem.toString();
                    alternateHeaderSB.append(toString).append(", ");
                }

                headers.setContentType(MediaType.valueOf("text/turtle"));
                headers.set(HttpHeaders.LOCATION, bestRepresentation.getIri());
                headers.set(HttpHeaders.LINK, "<" + profileURI + ">" + ";rel=\"profile\"");
                headers.setVary(Stream.of("Accept-Profile").collect(Collectors.toList()));
                headers.set("Alternates", alternateHeaderSB.toString());

                return new ResponseEntity<>(bestRepresentation.getContent(), headers, HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.NOT_ACCEPTABLE);
            }
        }
    }
}
