package fr.minesstetienne.ci.dcn.service;

import fr.minesstetienne.ci.dcn.dto.RepresentationDetail;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author YoucTagh
 */
@Service
public class ProfileDCNService {

    private final RestTemplate restTemplate;
    private final ValidationService validationService;


    public ProfileDCNService(RestTemplateBuilder restTemplateBuilder, ValidationService validationService) {
        this.restTemplate = restTemplateBuilder
                .setConnectTimeout(Duration.ofSeconds(2))
                .setReadTimeout(Duration.ofSeconds(2))
                .build();
        this.validationService = validationService;
    }

    public ResponseEntity<String> getRepresentationIfAvailable(String iri,String profileIri) {
        RepresentationDetail representationDetail = validationService.checkConformance(iri, profileIri);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(representationDetail.getContentType());
        headers.set(HttpHeaders.LOCATION, representationDetail.getIri());
        headers.set(HttpHeaders.LINK, "<" + profileIri + ">" + ";rel=\"profile\"");
        headers.setVary(Stream.of("Accept-Profile").collect(Collectors.toList()));
        return new ResponseEntity<>(representationDetail.getContent(), headers, representationDetail.getStatus());

    }

    public RepresentationDetail checkConformanceOfRepresentation(String iri,String profileIri) {
        return validationService.checkConformance(iri, profileIri);
    }

    public ResponseEntity<String> checkRepresentationIfAvailable(String iri) {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(UtilService.getSemanticAcceptedMediaTypes());
        HttpEntity<String> entity = new HttpEntity<>(headers);
        return restTemplate.exchange(iri, HttpMethod.HEAD, entity, String.class);
    }
}
