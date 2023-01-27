package fr.minesstetienne.ci.dcn.controller;

import fr.minesstetienne.ci.dcn.dto.AlternateHeaderItem;
import fr.minesstetienne.ci.dcn.dto.RepresentationDetail;
import fr.minesstetienne.ci.dcn.dto.ResourceDetail;
import fr.minesstetienne.ci.dcn.formsubmission.SameAsResource;
import fr.minesstetienne.ci.dcn.service.MediaTypeDCNService;
import fr.minesstetienne.ci.dcn.service.SameAsSearchService;
import fr.minesstetienne.ci.dcn.service.UtilService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

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


    @PostMapping("/validate")
    public String getRepresentation(@RequestBody MultiValueMap<String, String> formData, Model model) {
        boolean checkResult = false;
        model.addAttribute("result", checkResult ? "Valid" : "Not Valid");
        return "conformance-result";
    }

    @GetMapping(produces = {MediaType.TEXT_HTML_VALUE})
    public String homeView(Model model) {
        model.addAttribute("sameAsResource", new SameAsResource());
        return "home";
    }

    @PostMapping
    public String resultSubmit(@ModelAttribute SameAsResource sameAsResource, Model model) {

        ResourceDetail resourceDetail = sameAsSearchService.findSameResources(sameAsResource.getResourceIri(), MediaType.parseMediaTypes("*/*"));

        model.addAttribute("resourceDetail", resourceDetail);
        model.addAttribute("shapeGraphIri", sameAsResource.getShapeGraphIri());
        return "result";
    }

    @GetMapping("/api")
    public ResponseEntity getBestRepresentation(@RequestParam String iri, @Nullable @RequestHeader(HttpHeaders.ACCEPT) String acceptHeader) {

        List<MediaType> acceptHeaderMT = MediaType.parseMediaTypes((acceptHeader != null) ? acceptHeader : "*/*");
        ResponseEntity<String> representationIfAvailable = mediaTypeDCNService.getRepresentationIfAvailable(iri, acceptHeaderMT);
        if (representationIfAvailable.getStatusCode().equals(HttpStatus.OK)
                && UtilService.isMediaTypeContainsInList(representationIfAvailable.getHeaders().getContentType(), acceptHeaderMT)) {
            return representationIfAvailable;
        } else {
            ResourceDetail resourceDetail = sameAsSearchService.findSameResources(iri, MediaType.parseMediaTypes(acceptHeader));
            HttpHeaders headers = new HttpHeaders();

            for (int i = 0; i < resourceDetail.getRepresentationDetails().size(); i++) {

                RepresentationDetail representationDetail = resourceDetail.getRepresentationDetails().get(i);

                if (representationDetail.getStatus().equals("200")
                        && UtilService.isMediaTypeContainsInList(MediaType.parseMediaType(representationDetail.getContentType()), acceptHeaderMT)) {
                    headers.setContentType(MediaType.valueOf(representationDetail.getContentType()));
                    headers.set(HttpHeaders.LOCATION, representationDetail.getIri());
                    headers.setVary(List.of(HttpHeaders.ACCEPT));

                    ResponseEntity<String> representation = mediaTypeDCNService.getRepresentationIfAvailable(representationDetail.getIri(), acceptHeaderMT);

                    ArrayList<AlternateHeaderItem> alternateHeaderItems = new ArrayList<>();

                    for (int j = i; j < resourceDetail.getRepresentationDetails().size(); j++) {
                        RepresentationDetail alternateRepresentation = resourceDetail.getRepresentationDetails().get(j);
                        if (alternateRepresentation.getStatus().equals("200")
                                && UtilService.isMediaTypeContainsInList(MediaType.parseMediaType(representationDetail.getContentType()), acceptHeaderMT)) {
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
