package de.gwdg.metadataqa.wikidata.json;

/*
import org.eclipse.rdf4j.RDF4JException;
// import org.eclipse.rdf4j.http.client.HttpClientSessionManager;
// import org.eclipse.rdf4j.http.client.SPARQLProtocolSession;
// import org.eclipse.rdf4j.http.client.SharedHttpClientSessionManager;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.query.resultio.TupleQueryResultFormat;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.repository.sparql.SPARQLConnection;
import org.eclipse.rdf4j.sail.inferencer.fc.ForwardChainingRDFSInferencer;
import org.eclipse.rdf4j.sail.memory.MemoryStore;
*/

import java.util.List;

public class SparqlCliendRdf4j {

  String queryEndpointUrl;
  String updateEndpointUrl;

  public static void main(String[] args) {
    /*
    try {
      RepositoryConnection con = new SPARQLConnection(this, createHTTPClient(), false);
      Repository repository = new SailRepository(new ForwardChainingRDFSInferencer(new MemoryStore()));
      RepositoryConnection con = repository.getConnection();
      try {
        String queryString = " SELECT ?x ?y WHERE { ?x ?p ?y } ";
        TupleQuery tupleQuery = con.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
        TupleQueryResult result = tupleQuery.evaluate();
        try {
          List<String> bindingNames = result.getBindingNames();
          while (result.hasNext()) {
            BindingSet bindingSet = result.next();
            Value firstValue = bindingSet.getValue(bindingNames.get(0));
            Value secondValue = bindingSet.getValue(bindingNames.get(1));
            // do something interesting with the values here...
          }
        } finally {
          result.close();
        }
      } finally {
        con.close();
      }
    } catch (RDF4JException e) {
      // handle exception
    }
    */
  }

  /*
  private SPARQLProtocolSession createHTTPClient() {
    // initialize HTTP client
    SPARQLProtocolSession httpClient = getHttpClientSessionManager().createSPARQLProtocolSession(queryEndpointUrl, updateEndpointUrl);
    httpClient.setValueFactory(SimpleValueFactory.getInstance());
    httpClient.setPreferredTupleQueryResultFormat(TupleQueryResultFormat.SPARQL);
    // httpClient.setAdditionalHttpHeaders(additionalHttpHeaders);
    return httpClient;
  }

  public HttpClientSessionManager getHttpClientSessionManager() {
    HttpClientSessionManager result = new SharedHttpClientSessionManager();
    return result;
  }
  */
}
