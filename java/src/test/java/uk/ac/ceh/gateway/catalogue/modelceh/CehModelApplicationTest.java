package uk.ac.ceh.gateway.catalogue.modelceh;

import java.util.Arrays;
import java.util.Set;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.jupiter.api.Test;
import uk.ac.ceh.gateway.catalogue.model.Relationship;
import uk.ac.ceh.gateway.catalogue.modelceh.CehModelApplication.ModelInfo;

public class CehModelApplicationTest {

    @Test
    public void getRelationshipsFromModelInfo() {
        //Given
        ModelInfo modelInfo0 = new ModelInfo();
        modelInfo0.setId("e254c4dd-1086-4ebe-b7da-7521bdf2e13e");
        ModelInfo modelInfo1 = new ModelInfo();
        modelInfo1.setId("37eaa38c-f499-491b-9da9-a7777cdf7b5b");
        ModelInfo modelInfo2 = new ModelInfo(); // null id
        ModelInfo modelInfo3 = new ModelInfo(); // empty string
        modelInfo3.setId("");

        CehModelApplication modelApplication = new CehModelApplication();
        modelApplication.setModelInfos(Arrays.asList(
            modelInfo0, modelInfo1, modelInfo2
        ));

        //When
        Set<Relationship> actual = modelApplication.getRelationships();

        //Then
        assertThat("should only be 2 relationships", actual.size(), is(2));
    }

    @Test
    public void getRelationshipsNoModelInfo() {
        //Given
        CehModelApplication modelApplication = new CehModelApplication();

        //When
        Set<Relationship> actual = modelApplication.getRelationships();

        //Then
        assertThat("should be no relationships", actual.size(), is(0));
    }

}
