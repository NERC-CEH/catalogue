package uk.ac.ceh.gateway.catalogue.linking;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.tdb.TDBFactory;
import static com.hp.hpl.jena.vocabulary.RSS.link;
import java.util.List;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import org.junit.Before;
import org.junit.Test;
import uk.ac.ceh.gateway.catalogue.gemini.Link;

/**
 *
 * @author cjohn
 */
public class JenaQueryingTest {
    private static final Property IDENTIFIER = ResourceFactory.createProperty("http://purl.org/dc/terms/identifier");
    private static final Property TITLE = ResourceFactory.createProperty("http://purl.org/dc/terms/title");
    private static final Property TYPE = ResourceFactory.createProperty("http://purl.org/dc/terms/type");
    private static final Property PART_OF = ResourceFactory.createProperty("http://purl.org/dc/terms/isPartOf");
    
    private Model model;
    private JenaQuerying service;
    
    @Before
    public void init() {
        Dataset dataset = TDBFactory.createDataset();
        model = dataset.getDefaultModel();
        service = new JenaQuerying(model);
    }
    
    @Test
    public void canGetLinkFromIdentifier() {
        //Given
        Resource resource = ResourceFactory.createProperty("http://my.resource.com/uuid");
        model.add(resource, IDENTIFIER, ResourceFactory.createPlainLiteral("uuid"));
        model.add(resource, TITLE, ResourceFactory.createPlainLiteral("title"));
        model.add(resource, TYPE, ResourceFactory.createPlainLiteral("crazy type"));
        
        //When
        Link link = service.getLink("uuid");
        
        //Then
        assertThat(link.getAssociationType(), equalTo("crazy type"));
        assertThat(link.getHref(), equalTo("http://my.resource.com/uuid"));
        assertThat(link.getTitle(), equalTo("title"));
    }
    
    @Test
    public void canGetReverseLinksFromIdentifier() {
        //Given
        Resource parent = ResourceFactory.createProperty("http://my.resource.com/parent");
        model.add(parent, IDENTIFIER, ResourceFactory.createPlainLiteral("parent"));
        
        Resource child = ResourceFactory.createProperty("http://my.resource.com/child");
        model.add(child, PART_OF, parent);
        model.add(child, IDENTIFIER, ResourceFactory.createPlainLiteral("child"));
        model.add(child, TITLE, ResourceFactory.createPlainLiteral("child record"));
        model.add(child, TYPE, ResourceFactory.createPlainLiteral("child type"));
        
        //When
        List<Link> children = service.getReverseLinks("parent");
        
        //Then
        assertThat(children.size(), is(1));
        Link childLink = children.get(0);
        assertThat(childLink.getAssociationType(), equalTo("child type"));
        assertThat(childLink.getHref(), equalTo("http://my.resource.com/child"));
        assertThat(childLink.getTitle(), equalTo("child record"));
        
    }
}
