package fr.minesstetienne.ci.dcn.service;

import fr.minesstetienne.ci.dcn.dto.RepresentationDetail;
import fr.minesstetienne.ci.dcn.dto.ResourceDetail;
import org.apache.jena.atlas.web.HttpException;
import org.apache.jena.graph.Graph;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.shacl.ShaclValidator;
import org.apache.jena.shacl.Shapes;
import org.apache.jena.shacl.ValidationReport;
import org.springframework.http.HttpStatus;
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

    public RepresentationDetail checkConformance(String representationIRI, String profileIRI) {
        try {

            Graph shapesGraph = RDFDataMgr.loadGraph(profileIRI);
            Shapes shapes = Shapes.parse(shapesGraph);

            Graph dataGraph = RDFDataMgr.loadGraph(representationIRI);


            if (dataGraph.isEmpty()) {
                return new RepresentationDetail()
                        .setIri(representationIRI)
                        .setStatus(HttpStatus.NOT_ACCEPTABLE)
                        .setContentType(MediaType.parseMediaType("text/turtle"))
                        .setTripleNumber(0L)
                        .setValid(false);
            }

            ValidationReport report = ShaclValidator.get().validate(shapes, dataGraph);

            int numberViolations = report.getEntries().size();

            StringWriter contentSW = new StringWriter();
            RDFDataMgr.write(contentSW, dataGraph, Lang.TURTLE);

            return new RepresentationDetail()
                    .setValid(numberViolations == 0)
                    .setIri(representationIRI)
                    .setStatus((numberViolations == 0) ? HttpStatus.OK : HttpStatus.NOT_ACCEPTABLE)
                    .setContent(contentSW.toString())
                    .setContentType(MediaType.parseMediaType("text/turtle"))
                    .setTripleNumber((long) dataGraph.size());

        } catch (HttpException ex) {
            HttpStatus httpStatus = HttpStatus.resolve(ex.getStatusCode());
            return new RepresentationDetail()
                    .setIri(representationIRI)
                    .setStatus((httpStatus != null) ? httpStatus : HttpStatus.INTERNAL_SERVER_ERROR)
                    .setTripleNumber(0L)
                    .setValid(false);
        } catch (Exception ex) {
            return new RepresentationDetail()
                    .setIri(representationIRI)
                    .setStatus(HttpStatus.INTERNAL_SERVER_ERROR)
                    .setTripleNumber(0L)
                    .setValid(false);
        }
    }
//
//    public ResourceDetail checkConformance(ResourceDetail resourceDetail, String shapeIRI) {
//
//        List<MediaType> mediaTypes = UtilService.getSemanticAcceptedMediaTypes();
//        boolean stopValidation;
//        for (RepresentationDetail representationDetail : resourceDetail.getRepresentationDetails()) {
//            if (representationDetail.getStatus().equals(HttpStatus.OK)) {
//                stopValidation = false;
//                for (int i = 0; i < mediaTypes.size() && !stopValidation; i++) {
//                    MediaType mediaType = mediaTypes.get(i);
//                    if (representationDetail.getContentType().equalsTypeAndSubtype(mediaType)) {
//                        try {
//
//                            System.out.println("Validating Graph: " + representationDetail.getIri());
//
//                            Graph shapesGraph = RDFDataMgr.loadGraph(shapeIRI);
//                            Shapes shapes = Shapes.parse(shapesGraph);
//
//                            Graph dataGraph = RDFDataMgr.loadGraph(representationDetail.getIri());
//
//                            if (dataGraph.isEmpty()) {
//                                representationDetail.setValid(false);
//                                representationDetail.setTripleNumber(0L);
//                                stopValidation = true;
//                                continue;
//                            } else {
//                                representationDetail.setTripleNumber((long) dataGraph.size());
//                            }
//                            ValidationReport report = ShaclValidator.get().validate(shapes, dataGraph);
//                            int numberViolations = report.getEntries().size();
//                            representationDetail.setValid(numberViolations == 0);
//                            StringWriter contentSW = new StringWriter();
//                            RDFDataMgr.write(contentSW, dataGraph, Lang.TURTLE);
//                            representationDetail.setContent(contentSW.toString());
//                            stopValidation = true;
//                        } catch (Exception ex) {
//                            representationDetail.setValid(false);
//                            representationDetail.setTripleNumber(0L);
//                            representationDetail.setStatus(HttpStatus.INTERNAL_SERVER_ERROR);
//                        }
//                    }
//                }
//            }
//        }
//
//        resourceDetail.getRepresentationDetails().sort((r1, r2) -> {
//            if (!Objects.equals(r1.getStatus(), HttpStatus.OK) || r1.getTripleNumber() == null)
//                r1.setTripleNumber(-1L);
//            if (!Objects.equals(r2.getStatus(), HttpStatus.OK) || r2.getTripleNumber() == null)
//                r2.setTripleNumber(-1L);
//            if (r1.isValid() && !r2.isValid())
//                return (-1) * r1.getTripleNumber().intValue();
//            if (r2.isValid() && !r1.isValid())
//                return r2.getTripleNumber().intValue();
//            return Long.compare(r2.getTripleNumber(), r1.getTripleNumber());
//        });
//
//        return resourceDetail;
//    }

}
