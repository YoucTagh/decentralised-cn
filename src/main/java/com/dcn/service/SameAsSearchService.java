package com.dcn.service;

import com.dcn.dto.RepresentationDetail;
import com.dcn.dto.ResourceDetail;
import com.dcn.dto.ResponseSameAsDTO;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author _
 */
@Service
public class SameAsSearchService {
    private final RestTemplate restTemplate;

    private final MediaTypeDCNService mediaTypeDCNService;

    public SameAsSearchService(RestTemplateBuilder restTemplateBuilder, MediaTypeDCNService mediaTypeDCNService) {
        this.restTemplate = restTemplateBuilder
                .setConnectTimeout(Duration.ofSeconds(2))
                .setReadTimeout(Duration.ofSeconds(2))
                .build();
        this.mediaTypeDCNService = mediaTypeDCNService;
    }

    public ResourceDetail findSameResources(String resourceIRI, List<MediaType> mediaTypeList) {

        String uri = UriComponentsBuilder
                .newInstance()
                .scheme("http")
                .host("sameas.org")
                .path("/")
                .queryParam("uri", resourceIRI)
                .build()
                .toUriString();

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Stream.of(MediaType.APPLICATION_JSON).collect(Collectors.toList()));
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<ResponseSameAsDTO[]> respEntity = restTemplate.exchange(uri, HttpMethod.GET, entity, ResponseSameAsDTO[].class);

        if (respEntity.getStatusCode().equals(HttpStatus.OK)) {
            ResponseSameAsDTO[] resp = respEntity.getBody();
            if (resp != null)
                return checkRepresentationAvailability(resp[0], mediaTypeList);
        }

        return new ResourceDetail().setMainIri(resourceIRI).setNumRepresentations("0");
    }

    private ResourceDetail checkRepresentationAvailability(ResponseSameAsDTO responseSameAsDTO, List<MediaType> mediaTypeList) {
        ResourceDetail resourceDetail = new ResourceDetail()
                .setMainIri(responseSameAsDTO.getUri())
                .setNumRepresentations(responseSameAsDTO.getNumDuplicates());

        responseSameAsDTO.getDuplicates().forEach(representationIRI -> {
            System.out.println(representationIRI);
            try {
                ResponseEntity<String> isRepresentationAvailable = mediaTypeDCNService.checkRepresentationIfAvailable(representationIRI, mediaTypeList);
                RepresentationDetail representationDetail = new RepresentationDetail()
                        .setIri(representationIRI)
                        .setStatus(isRepresentationAvailable.getStatusCode())
                        .setContentType(isRepresentationAvailable.getHeaders().getContentType());
                resourceDetail.getRepresentationDetails().add(representationDetail);
            } catch (HttpClientErrorException ex) {
                resourceDetail.getRepresentationDetails().add(new RepresentationDetail()
                        .setIri(responseSameAsDTO.getUri())
                        .setStatus(ex.getStatusCode())
                        .setContentType(ex.getResponseHeaders().getContentType()));
            } catch (Exception ex) {
                resourceDetail.getRepresentationDetails().add(new RepresentationDetail()
                        .setIri(responseSameAsDTO.getUri())
                        .setStatus(HttpStatus.INTERNAL_SERVER_ERROR)
                        .setContentType(MediaType.ALL));
            }
        });
        return resourceDetail;
    }

}
