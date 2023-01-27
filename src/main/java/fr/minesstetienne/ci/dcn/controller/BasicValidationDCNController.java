package fr.minesstetienne.ci.dcn.controller;

import fr.minesstetienne.ci.dcn.dto.AlternateHeaderItem;
import fr.minesstetienne.ci.dcn.dto.RepresentationDetail;
import fr.minesstetienne.ci.dcn.dto.ResourceDetail;
import fr.minesstetienne.ci.dcn.formsubmission.SameAsResource;
import fr.minesstetienne.ci.dcn.service.SameAsSearchService;
import fr.minesstetienne.ci.dcn.service.ValidationService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
@RequestMapping("/dcn/basic-validation")
public class BasicValidationDCNController {

    private final ValidationService validationService;
    private final SameAsSearchService sameAsSearchService;

    public BasicValidationDCNController(ValidationService validationService, SameAsSearchService sameAsSearchService) {
        this.validationService = validationService;
        this.sameAsSearchService = sameAsSearchService;
    }


    @PostMapping("/validate")
    public String getRepresentation(@RequestBody MultiValueMap<String, String> formData, Model model) {
        boolean checkResult = validationService.checkConformance(formData.getFirst("repIri"), formData.getFirst("shapeIri"));
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

        ResourceDetail resourceDetail = sameAsSearchService.findSameResources(sameAsResource.getResourceIri(),MediaType.parseMediaTypes("*/*"));

        resourceDetail = validationService.checkConformance(resourceDetail, sameAsResource.getShapeGraphIri());

        model.addAttribute("resourceDetail", resourceDetail);
        model.addAttribute("shapeGraphIri", sameAsResource.getShapeGraphIri());
        return "result";
    }

    @GetMapping("/api")
    public ResponseEntity getBestRepresentation(@RequestParam String iri, @RequestHeader("Accept-Profile") String shaclIRI) {

        ResourceDetail resourceDetail = sameAsSearchService.findSameResources(iri,MediaType.parseMediaTypes("*/*"));
        resourceDetail = validationService.checkConformance(resourceDetail, shaclIRI);

        RepresentationDetail representationDetail = resourceDetail.getRepresentationDetails().get(0);

        if (representationDetail.getStatus().equals("200")) {
            if (representationDetail.isValid()) {
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.valueOf("text/turtle"));
                headers.set(HttpHeaders.LOCATION, representationDetail.getIri());
                headers.set(HttpHeaders.LINK, "<" + shaclIRI + ">" + ";rel=\"profile\"");
                headers.setVary(List.of("Accept-Profile"));

                ArrayList<AlternateHeaderItem> alternateHeaderItems = new ArrayList<>();
                for (int i = 1; i < resourceDetail.getRepresentationDetails().size(); i++) {
                    RepresentationDetail alternateRepresentation = resourceDetail.getRepresentationDetails().get(i);
                    if (alternateRepresentation.isValid()) {
                        alternateHeaderItems.add(
                                new AlternateHeaderItem()
                                        .setIri(alternateRepresentation.getIri())
                                        .setMediaType(alternateRepresentation.getContentType())
                                        .setAcceptabilityValue((float) alternateRepresentation.getTripleNumber() / representationDetail.getTripleNumber())
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

                return new ResponseEntity<>(representationDetail.getContent(), headers, HttpStatus.OK);
            } else
                return new ResponseEntity<>(representationDetail.getContent(), HttpStatus.NOT_ACCEPTABLE);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}
