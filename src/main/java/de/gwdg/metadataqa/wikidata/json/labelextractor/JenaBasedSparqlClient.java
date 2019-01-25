package de.gwdg.metadataqa.wikidata.json.labelextractor;

import de.gwdg.metadataqa.wikidata.json.LabelExtractor;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.sparql.engine.http.QueryEngineHTTP;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JenaBasedSparqlClient implements LabelExtractor {

  public static final String SPARQL_ENDPOINT = "https://query.wikidata.org/sparql";
  public static final String QUERY_PATTERN = "PREFIX wd: <http://www.wikidata.org/entity/>\n" +
    "PREFIX wikibase: <http://wikiba.se/ontology#>\n" +
    "PREFIX bd: <http://www.bigdata.com/rdf#>\n" +
    "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
    "\n" +
    "SELECT ?label WHERE {\n" +
    "  wd:%s rdfs:label ?label .\n" +
    "  SERVICE wikibase:label { bd:serviceParam wikibase:language \"en\". }\n" +
    "}";

  public static void main(String[] args) {
    JenaBasedSparqlClient sparqlClient = new JenaBasedSparqlClient();
    String label = sparqlClient.getLabel("Q19007672");
    System.err.println(label);
  }

  public String getLabel(String entityId) {
    Query query = QueryFactory.create(String.format(QUERY_PATTERN, entityId));

    String value = entityId;
    try (QueryExecution qexec = QueryExecutionFactory.sparqlService(SPARQL_ENDPOINT, query)) {
      ((QueryEngineHTTP)qexec).addParam("timeout", "10000");
      // ((QueryEngineHTTP)qexec).setDefaultGraphURIs addParam("timeout", "10000");
      ResultSet rs = qexec.execSelect();
      while (rs.hasNext()) {
        QuerySolution hit = rs.next();
        RDFNode node = hit.get("label");
        String language = node.asLiteral().getLanguage();
        if (language.equals("en")) {
          value = node.asLiteral().getString();
          break;
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return value;
  }

  @Override
  public Map<String, String> getLabels(List<String> entityIds) {
    Map<String, String> labels = new HashMap<>();
    for (String entityId : entityIds) {
      labels.put(entityId, getLabel(entityId));
    }
    return labels;
  }
}
