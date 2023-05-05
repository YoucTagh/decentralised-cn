package com.dcn.service;

import com.dcn.dto.RepresentationDetail;
import org.apache.jena.atlas.web.HttpException;
import org.apache.jena.graph.Graph;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.shacl.ShaclValidator;
import org.apache.jena.shacl.Shapes;
import org.apache.jena.shacl.ValidationReport;
import org.apache.jena.shacl.validation.Severity;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import java.io.StringWriter;
import java.util.stream.Collectors;

/**
 * @author _
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
            int numberViolations = report.getEntries().stream().filter(reportEntry -> reportEntry.severity().equals(Severity.Violation)).collect(Collectors.toSet()).size();

            StringWriter contentSW = new StringWriter();
            RDFDataMgr.write(contentSW, dataGraph, Lang.TURTLE);

            return new RepresentationDetail()
                    .setValid(numberViolations == 0)
                    .setIri(representationIRI)
                    .setStatus((numberViolations == 0) ? HttpStatus.OK : HttpStatus.NOT_ACCEPTABLE)
                    .setContent(contentSW.toString())
                    .setContentType(MediaType.parseMediaType("text/turtle"))
                    .setValidationReport(report)
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

}
