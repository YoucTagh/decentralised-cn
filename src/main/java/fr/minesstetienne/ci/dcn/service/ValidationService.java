package fr.minesstetienne.ci.dcn.service;

import fr.minesstetienne.ci.dcn.dto.RepresentationDetail;
import fr.minesstetienne.ci.dcn.dto.ResourceDetail;
import org.apache.jena.graph.Graph;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.shacl.ShaclValidator;
import org.apache.jena.shacl.Shapes;
import org.apache.jena.shacl.ValidationReport;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import java.io.StringWriter;
import java.util.*;

/**
 * @author YoucTagh
 */
@Service
public class ValidationService {

    public ValidationService() {
    }

    public boolean checkConformance(String representationIRI, String profileIRI) {
        Graph shapesGraph = RDFDataMgr.loadGraph(profileIRI);
        Shapes shapes = Shapes.parse(shapesGraph);

        Graph dataGraph = RDFDataMgr.loadGraph(representationIRI);

        if (dataGraph.isEmpty()) {
            return false;
        }

        ValidationReport report = ShaclValidator.get().validate(shapes, dataGraph);

        int numberViolations = report.getEntries().size();
        return (numberViolations == 0);
    }

    public ResourceDetail checkConformance(ResourceDetail resourceDetail, String shapeIRI) {

        List<MediaType> mediaTypes = Arrays.asList(
                MediaType.parseMediaType("application/rdf+xml"),
                MediaType.parseMediaType("text/turtle"),
                MediaType.parseMediaType("application/n-triples"));
        boolean stopValidation;
        for (RepresentationDetail representationDetail : resourceDetail.getRepresentationDetails()) {
            if (representationDetail.getStatus().equals("200")) {
                stopValidation = false;
                for (int i = 0; i < mediaTypes.size() && !stopValidation; i++) {
                    MediaType mediaType = mediaTypes.get(i);
                    if (MediaType.parseMediaType(representationDetail.getContentType()).equalsTypeAndSubtype(mediaType)) {
                        try {

                            System.out.println("Validating Graph: " + representationDetail.getIri());

                            Graph shapesGraph = RDFDataMgr.loadGraph(shapeIRI);
                            Shapes shapes = Shapes.parse(shapesGraph);

                            Graph dataGraph = RDFDataMgr.loadGraph(representationDetail.getIri());

                            if (dataGraph.isEmpty()) {
                                representationDetail.setValid(false);
                                representationDetail.setTripleNumber(0L);
                                stopValidation = true;
                                continue;
                            } else {
                                representationDetail.setTripleNumber((long) dataGraph.size());
                            }
                            ValidationReport report = ShaclValidator.get().validate(shapes, dataGraph);
                            int numberViolations = report.getEntries().size();
                            representationDetail.setValid(numberViolations == 0);
                            StringWriter contentSW = new StringWriter();
                            RDFDataMgr.write(contentSW, dataGraph, Lang.TURTLE);
                            representationDetail.setContent(contentSW.toString());
                            stopValidation = true;
                        } catch (Exception ex) {
                            representationDetail.setValid(false);
                            representationDetail.setTripleNumber(0L);
                            representationDetail.setStatus("Unknown Error");
                        }
                    }
                }
            }
        }

        resourceDetail.getRepresentationDetails().sort((r1, r2) -> {
            if (!Objects.equals(r1.getStatus(), "200") || r1.getTripleNumber() == null)
                r1.setTripleNumber(-1L);
            if (!Objects.equals(r2.getStatus(), "200") || r2.getTripleNumber() == null)
                r2.setTripleNumber(-1L);
            if (r1.isValid() && !r2.isValid())
                return (-1) * r1.getTripleNumber().intValue();
            if (r2.isValid() && !r1.isValid())
                return r2.getTripleNumber().intValue();
            return Long.compare(r2.getTripleNumber(), r1.getTripleNumber());
        });

        return resourceDetail;
    }

}
