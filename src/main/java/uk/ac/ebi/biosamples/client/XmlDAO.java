package uk.ac.ebi.biosamples.client;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;

import org.springframework.http.HttpRequest;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import uk.ac.ebi.biosamples.jaxb.samplegroupexport.BioSampleGroupType;
import uk.ac.ebi.biosamples.jaxb.samplegroupexport.BioSampleType;


/**
 * Because the biosamples API is split over two endpoints - one for submissions, one for retrieval - this
 * class acts as a wrapper and routes requests to the endpoints as appropriate.
 * 
 * In general, the methods here are delegated to an appropriate Spring RestTemplate class and will 
 * throw runtime RestClientExceptions in case of problems. In particular, any HTTP status problems
 * will be communicated via throwing HttpStatusCodeException.
 * 
 * This class uses synchronised HTTP requests, so using the same instance in multiple threads will 
 * not improve performance.
 * 
 * By default it will connect to http://www.ebi.ac.uk/biosamples but this can be configured
 * 
 * @author faulcon
 *
 */
public class XmlDAO {
	
	protected RestTemplate readRestTemplate;
	protected RestTemplate writeRestTemplate;
	
	protected final MediaType XMLV1MEDIA = new MediaType("text", "xml_v1.0", Charset.forName("UTF-8"));
	
	protected String protocol = "https";
	protected String host = "www.ebi.ac.uk";
	protected String root = "biosamples";
	
	public XmlDAO() {
		setReadRestTemplate(new RestTemplate());
		setWriteRestTemplate(new RestTemplate());
	}
	
	public BioSampleType getBioSample(String accession) throws RestClientException {
		if (!accession.matches("SAM[END]A?[0-9]+")) {
			throw new IllegalArgumentException("Accession must be a valid biosample accession");
		}
		UriComponents uriComponents = UriComponentsBuilder.newInstance()
				.scheme(protocol)
				.host(host)
				.pathSegment(root, "xml", "sample", accession)
				.build();
		
		return readRestTemplate.getForObject(uriComponents.toUri(), BioSampleType.class);
	}
	
	public BioSampleGroupType getBioSampleGroup(String accession) throws RestClientException {
		if (!accession.matches("SAMEG[0-9]+")) {
			throw new IllegalArgumentException("Accession must be a valid biosample accession");
		}
		UriComponents uriComponents = UriComponentsBuilder.newInstance()
				.scheme(protocol)
				.host(host)
				.pathSegment(root, "xml", "group", accession)
				.build();
		
		return readRestTemplate.getForObject(uriComponents.toUri(), BioSampleGroupType.class);
	}
	
	/**
     * This allows the underlying rest templates to be replaced e.g. for testing
	 * @param readRestTemplate
	 */
	public void setReadRestTemplate(RestTemplate readRestTemplate) {
		if (readRestTemplate == null) throw new IllegalArgumentException("readRestTemplate must not be null");
		
		//because the read API has a weird content-type header of text/xml_v1.0
		//use an intercepter to convert that to regular text/xml
		List<ClientHttpRequestInterceptor> interceptors = readRestTemplate.getInterceptors();
		interceptors.add(new ClientHttpRequestInterceptor() {
			@Override
			public ClientHttpResponse intercept(HttpRequest request, byte[] body,
					ClientHttpRequestExecution execution) throws IOException {
				ClientHttpResponse response = execution.execute(request, body);
				if (response.getHeaders().getContentType().equals(XMLV1MEDIA)) {
					response.getHeaders().setContentType(MediaType.TEXT_XML);
				}
				return response;
			}
			
		});
		readRestTemplate.setInterceptors(interceptors);
		
		this.readRestTemplate = readRestTemplate;
	}

	/**
     * This allows the underlying rest templates to be replaced e.g. for testing
	 * @param writeRestTemplate
	 */
	public void setWriteRestTemplate(RestTemplate writeRestTemplate) {
		if (readRestTemplate == null) throw new IllegalArgumentException("writeRestTemplate must not be null");
		this.writeRestTemplate = writeRestTemplate;
	}
	
}
