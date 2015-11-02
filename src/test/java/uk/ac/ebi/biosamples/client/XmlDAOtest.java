package uk.ac.ebi.biosamples.client;

import static org.junit.Assert.*;

import java.nio.charset.Charset;

import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;

import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

import org.springframework.web.client.RestTemplate;
import org.junit.Before;
import org.junit.Test;

import uk.ac.ebi.biosamples.jaxb.samplegroupexport.BioSampleGroupType;
import uk.ac.ebi.biosamples.jaxb.samplegroupexport.BioSampleType;

public class XmlDAOtest {

	private RestTemplate readRestTemplate;
	private MockRestServiceServer readMockServer;
	
	private RestTemplate writeRestTemplate;
	private MockRestServiceServer writeMockServer;
	
	private XmlDAO dao = new XmlDAO();

    @Before
    public void setUp() {
    	readRestTemplate = new RestTemplate();
		readMockServer = MockRestServiceServer.createServer(readRestTemplate);
    	writeRestTemplate = new RestTemplate();
		writeMockServer = MockRestServiceServer.createServer(writeRestTemplate);
		
		dao.setReadRestTemplate(readRestTemplate);
		dao.setWriteRestTemplate(writeRestTemplate);
    }
    
	@Test
	public void getSampleTest() {
		readMockServer.expect(requestTo("https://www.ebi.ac.uk/biosamples/xml/sample/SAMEA2186845"))
			.andRespond(withSuccess(new ClassPathResource("/SAMEA2186845.xml"),
					new MediaType("text", "xml_v1.0", Charset.forName("UTF-8"))));
		
		BioSampleType result = dao.getBioSample("SAMEA2186845");
		
		readMockServer.verify();
		
		assertEquals("SAMEA2186845", result.getId());
	}
    
	@Test
	public void getSampleGroupTest() {
		readMockServer.expect(requestTo("https://www.ebi.ac.uk/biosamples/xml/group/SAMEG306372"))
			.andRespond(withSuccess(new ClassPathResource("/SAMEG306372.xml"), 
					new MediaType("text", "xml_v1.0", Charset.forName("UTF-8"))));
		
		BioSampleGroupType result = dao.getBioSampleGroup("SAMEG306372");
		
		readMockServer.verify();
		
		assertEquals("SAMEG306372", result.getId());
	}
	
}
