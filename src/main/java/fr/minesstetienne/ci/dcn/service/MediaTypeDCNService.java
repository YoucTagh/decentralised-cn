package fr.minesstetienne.ci.dcn.service;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.List;

/**
 * @author YoucTagh
 */
@Service
public class MediaTypeDCNService {

    private final RestTemplate restTemplate;

    public MediaTypeDCNService(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder
                .setConnectTimeout(Duration.ofSeconds(2))
                .setReadTimeout(Duration.ofSeconds(2))
                .build();
    }

    public ResponseEntity<String> getRepresentationIfAvailable(String iri, List<MediaType> acceptHeader) {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(acceptHeader);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        return restTemplate.exchange(iri, HttpMethod.GET, entity, String.class);
    }

    public ResponseEntity<String> checkRepresentationIfAvailable(String iri, List<MediaType> acceptHeader) {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(acceptHeader);
        HttpEntity<String> entity = new HttpEntity<>(headers);
        return restTemplate.exchange(iri, HttpMethod.HEAD, entity, String.class);
    }
}
