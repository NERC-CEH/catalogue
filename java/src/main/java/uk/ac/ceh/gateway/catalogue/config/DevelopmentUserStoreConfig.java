package uk.ac.ceh.gateway.catalogue.config;

import lombok.val;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import uk.ac.ceh.components.userstore.UsernameAlreadyTakenException;
import uk.ac.ceh.components.userstore.inmemory.InMemoryGroupStore;
import uk.ac.ceh.components.userstore.inmemory.InMemoryUserStore;
import uk.ac.ceh.gateway.catalogue.controllers.DataciteController;
import uk.ac.ceh.gateway.catalogue.controllers.DocumentController;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.model.MetadataInfo;

import javax.annotation.PostConstruct;
import java.util.Arrays;

/**
 * The following spring JavaConfig defines the beans required for the interacting
 * with a in memory user store. This is useful when developing the application
 * in an environment which can not contact Crowd
 * @see CrowdUserStoreConfig

 */
@Configuration
@Profile("development")
public class DevelopmentUserStoreConfig {
    public static final String CEH_GROUP_NAME = "CEH";
    public static final String DATACITE_ROLE = DataciteController.DATACITE_ROLE;
    public static final String MAINTENANCE_ROLE = DocumentController.MAINTENANCE_ROLE;
    public static final String READONLY_ROLE = MetadataInfo.READONLY_GROUP;

    // Catalogue specific roles
    public static final String ASSIST_EDITOR = "role_assist_editor";
    public static final String ASSIST_PUBLISHER = "role_assist_publisher";
    public static final String CMP_EDITOR = "role_cmp_editor";
    public static final String CMP_PUBLISHER = "role_cmp_publisher";
    public static final String EIDC_EDITOR = "role_eidc_editor";
    public static final String EIDC_PUBLISHER = "role_eidc_publisher";
    public static final String ELTER_EDITOR = "role_elter_editor";
    public static final String ELTER_PUBLISHER = "role_elter_publisher";
    public static final String ERAMMP_EDITOR = "role_erammp_editor";
    public static final String ERAMMP_PUBLISHER = "role_erammp_publisher";
    public static final String INMS_EDITOR = "role_inms_editor";
    public static final String INMS_PUBLISHER = "role_inms_publisher";
    public static final String M_EDITOR = "role_m_editor";
    public static final String M_PUBLISHER = "role_m_publisher";
    public static final String NC_EDITOR = "role_nc_editor";
    public static final String NC_PUBLISHER = "role_nc_publisher";
    public static final String OSDP_EDITOR = "role_osdp_editor";
    public static final String OSDP_PUBLISHER = "role_osdp_publisher";
    public static final String SA_EDITOR = "role_sa_editor";
    public static final String SA_PUBLISHER = "role_sa_publisher";

    private void addUserToGroup(CatalogueUser user, String... groups) {
        Arrays.stream(groups)
                .forEach(group -> groupStore().grantGroupToUser(user, group));
    }

    @PostConstruct
    public void admin() throws UsernameAlreadyTakenException {
        val user = new CatalogueUser()
            .setUsername("admin")
            .setEmail("admin@ceh.ac.uk");
        addUserToGroup(user, MAINTENANCE_ROLE);
        userStore().addUser(user, "password");
    }

    @PostConstruct
    public void assistEditor() throws UsernameAlreadyTakenException {
        val user = new CatalogueUser()
            .setUsername("assist-editor")
            .setEmail("assist-editor@ceh.ac.uk");
        addUserToGroup(user, ASSIST_EDITOR);
        userStore().addUser(user, "password");
    }

    @PostConstruct
    public void assistPublisher() throws UsernameAlreadyTakenException {
        val user = new CatalogueUser()
            .setUsername("assist-publisher")
            .setEmail("assist-publisher@ceh.ac.uk");
        addUserToGroup(user, ASSIST_EDITOR, ASSIST_PUBLISHER);
        userStore().addUser(user, "password");
    }

    @PostConstruct
    public void cmpEditor() throws UsernameAlreadyTakenException {
        val user = new CatalogueUser()
            .setUsername("cmp-editor")
            .setEmail("cmp-editor@ceh.ac.uk");
        addUserToGroup(user, CMP_EDITOR);
        userStore().addUser(user, "password");
    }

    @PostConstruct
    public void cmpPublisher() throws UsernameAlreadyTakenException {
        val user = new CatalogueUser()
            .setUsername("cmp-publisher")
            .setEmail("cmp-publisher@ceh.ac.uk");
        addUserToGroup(user, CMP_EDITOR, CMP_PUBLISHER);
        userStore().addUser(user, "password");
    }

    @PostConstruct
    public void eidcEditor() throws UsernameAlreadyTakenException {
        val user = new CatalogueUser()
            .setUsername("eidc-editor")
            .setEmail("eidc-editor@ceh.ac.uk");
        addUserToGroup(user, EIDC_EDITOR);
        userStore().addUser(user, "password");
    }

    @PostConstruct
    public void eidcPublisher() throws UsernameAlreadyTakenException {
        val user = new CatalogueUser()
            .setUsername("eidc-publisher")
            .setEmail("eidc-publisher@ceh.ac.uk");
        addUserToGroup(user, EIDC_EDITOR, EIDC_PUBLISHER);
        userStore().addUser(user, "password");
    }

    @PostConstruct
    public void elterEditor() throws UsernameAlreadyTakenException {
        val user = new CatalogueUser()
            .setUsername("elter-editor")
            .setEmail("elter-editor@ceh.ac.uk");
        addUserToGroup(user, ELTER_EDITOR);
        userStore().addUser(user, "password");
    }

    @PostConstruct
    public void elterPublisher() throws UsernameAlreadyTakenException {
        val user = new CatalogueUser()
            .setUsername("elter-publisher")
            .setEmail("elter-publisher@ceh.ac.uk");
        addUserToGroup(user, ELTER_EDITOR, ELTER_PUBLISHER);
        userStore().addUser(user, "password");
    }

    @PostConstruct
    public void erammpEditor() throws UsernameAlreadyTakenException {
        val user = new CatalogueUser()
            .setUsername("erammp-editor")
            .setEmail("erammp-editor@ceh.ac.uk");
        addUserToGroup(user, ERAMMP_EDITOR);
        userStore().addUser(user, "password");
    }

    @PostConstruct
    public void erammpPublisher() throws UsernameAlreadyTakenException {
        val user = new CatalogueUser()
            .setUsername("erammp-publisher")
            .setEmail("erammp-publisher@ceh.ac.uk");
        addUserToGroup(user, ERAMMP_EDITOR, ERAMMP_PUBLISHER);
        userStore().addUser(user, "password");
    }

    @PostConstruct
    public void inmsPublisher() throws UsernameAlreadyTakenException {
        val user = new CatalogueUser()
            .setUsername("inms-publisher")
            .setEmail("inms-publisher@ceh.ac.uk");
        addUserToGroup(user, INMS_EDITOR, INMS_PUBLISHER);
        userStore().addUser(user, "password");
    }

    @PostConstruct
    public void mPublisher() throws UsernameAlreadyTakenException {
        val user = new CatalogueUser()
            .setUsername("m-publisher")
            .setEmail("m-publisher@ceh.ac.uk");
        addUserToGroup(user, M_EDITOR, M_PUBLISHER);
        userStore().addUser(user, "password");
    }

    @PostConstruct
    public void ncEditor() throws UsernameAlreadyTakenException {
        val user = new CatalogueUser()
            .setUsername("nc-editor")
            .setEmail("nc-editor@ceh.ac.uk");
        addUserToGroup(user, NC_EDITOR);
        userStore().addUser(user, "password");
    }

    @PostConstruct
    public void ncPublisher() throws UsernameAlreadyTakenException {
        val user = new CatalogueUser()
            .setUsername("nc-publisher")
            .setEmail("nc-publisher@ceh.ac.uk");
        addUserToGroup(user, NC_EDITOR, NC_PUBLISHER);
        userStore().addUser(user, "password");
    }

    @PostConstruct
    public void osdpPublisher() throws UsernameAlreadyTakenException {
        val user = new CatalogueUser()
            .setUsername("osdp-publisher")
            .setEmail("osdp-publisher@ceh.ac.uk");
        addUserToGroup(user, OSDP_EDITOR, OSDP_PUBLISHER);
        userStore().addUser(user, "password");
    }

    @PostConstruct
    public void readonly() throws UsernameAlreadyTakenException {
        val user = new CatalogueUser()
            .setUsername("readonly")
            .setEmail("readonly@ceh.ac.uk");
        addUserToGroup(user, READONLY_ROLE);
        userStore().addUser(user, "password");
    }

    @PostConstruct
    public void saPublisher() throws UsernameAlreadyTakenException {
        val user = new CatalogueUser()
            .setUsername("sa-publisher")
            .setEmail("sa-publisher@ceh.ac.uk");
        addUserToGroup(user, SA_EDITOR, SA_PUBLISHER);
        userStore().addUser(user, "password");
    }

    @PostConstruct
    public void superadmin() throws UsernameAlreadyTakenException {
        val user = new CatalogueUser()
            .setUsername("superadmin")
            .setEmail("superadmin@ceh.ac.uk");
        addUserToGroup(user, CEH_GROUP_NAME, EIDC_EDITOR, EIDC_PUBLISHER, MAINTENANCE_ROLE, DATACITE_ROLE);
        userStore().addUser(user, "password");
    }

    @PostConstruct
    public void unprivilegedUser() throws UsernameAlreadyTakenException {
        // Used in UploadControllerTest to check upload permissions
        val user = new CatalogueUser()
            .setUsername("unprivileged")
            .setEmail("unprivileged@example.com");
        userStore().addUser(user, "password");
    }

    @PostConstruct
    public void uploader() throws UsernameAlreadyTakenException {
        // Used in UploadControllerTest to check upload permissions
        val user = new CatalogueUser()
            .setUsername("uploader")
            .setEmail("uploader@example.com");
        userStore().addUser(user, "password");
    }

    @Bean
    public InMemoryGroupStore<CatalogueUser> groupStore() {
        val groupStore = new InMemoryGroupStore<CatalogueUser>();
        //create groups
        groupStore.createGroup(ASSIST_EDITOR, "ASSIST Editor Role");
        groupStore.createGroup(ASSIST_PUBLISHER, "ASSIST Publisher Role");
        groupStore.createGroup(CEH_GROUP_NAME, "Centre for Ecology & Hydrology");
        groupStore.createGroup(CMP_EDITOR, "CMP Editor Role");
        groupStore.createGroup(CMP_PUBLISHER, "CMP Publisher Role");
        groupStore.createGroup(DATACITE_ROLE, "Datacite Role");
        groupStore.createGroup(EIDC_EDITOR, "EIDC Editor Role");
        groupStore.createGroup(EIDC_PUBLISHER, "EIDC Publisher Role");
        groupStore.createGroup(ELTER_EDITOR, "ELTER Editor Role");
        groupStore.createGroup(ELTER_PUBLISHER, "ELTER Publisher Role");
        groupStore.createGroup(ERAMMP_EDITOR, "ERAMMP Editor Role");
        groupStore.createGroup(ERAMMP_PUBLISHER, "ERAMMP Publisher Role");
        groupStore.createGroup(INMS_EDITOR, "INMS Editor Role");
        groupStore.createGroup(INMS_PUBLISHER, "INMS Publisher Role");
        groupStore.createGroup(M_EDITOR, "M Editor Role");
        groupStore.createGroup(M_PUBLISHER, "M Publisher Role");
        groupStore.createGroup(MAINTENANCE_ROLE, "System Admin Role");
        groupStore.createGroup(NC_EDITOR, "NC Editor Role");
        groupStore.createGroup(NC_PUBLISHER, "NC Publisher Role");
        groupStore.createGroup(OSDP_EDITOR, "OSDP Editor Role");
        groupStore.createGroup(OSDP_PUBLISHER, "OSDP Publisher Role");
        groupStore.createGroup(READONLY_ROLE, "Read only role");
        groupStore.createGroup(SA_EDITOR, "SA Editor Role");
        groupStore.createGroup(SA_PUBLISHER, "SA Publisher Role");
        return groupStore;
    }

    @Bean
    public InMemoryUserStore<CatalogueUser> userStore() {
        return new InMemoryUserStore<>();
    }
}
