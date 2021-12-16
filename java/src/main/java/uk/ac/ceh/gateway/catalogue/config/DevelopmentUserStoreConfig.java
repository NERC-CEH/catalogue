package uk.ac.ceh.gateway.catalogue.config;

import lombok.val;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import uk.ac.ceh.components.userstore.UsernameAlreadyTakenException;
import uk.ac.ceh.components.userstore.inmemory.InMemoryGroupStore;
import uk.ac.ceh.components.userstore.inmemory.InMemoryUserStore;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;

import java.util.Arrays;

import static uk.ac.ceh.gateway.catalogue.controllers.DataciteController.DATACITE_ROLE;
import static uk.ac.ceh.gateway.catalogue.controllers.DocumentController.MAINTENANCE_ROLE;
import static uk.ac.ceh.gateway.catalogue.model.MetadataInfo.READONLY_GROUP;

/**
 * The following spring JavaConfig defines the beans required for the interacting
 * with a in memory user store. This is useful when developing the application
 * in an environment which can not contact Crowd
 * @see CrowdUserStoreConfig

 */
@Configuration
@Profile({"development", "test"})
public class DevelopmentUserStoreConfig {
    // Usernames used in tests
    public static final String ADMIN = "admin";
    public static final String EIDC_PUBLISHER_USERNAME = "eidc-publisher";
    public static final String UNPRIVILEGED_USERNAME = "unprivileged";
    public static final String UPLOADER_USERNAME = "uploader";


    public static final String CEH_GROUP_NAME = "CEH";

    // Catalogue specific roles
    public static final String ASSIST_EDITOR = "role_assist_editor";
    public static final String ASSIST_PUBLISHER = "role_assist_publisher";
    public static final String CMP_EDITOR = "role_cmp_editor";
    public static final String CMP_PUBLISHER = "role_cmp_publisher";
    public static final String DATALABS_EDITOR = "role_datalabs_editor";
    public static final String DATALABS_PUBLISHER = "role_datalabs_publisher";
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
    public static final String NM_EDITOR = "role_nm_editor";
    public static final String NM_PUBLISHER = "role_nm_publisher";
    public static final String NC_EDITOR = "role_nc_editor";
    public static final String NC_PUBLISHER = "role_nc_publisher";
    public static final String OSDP_EDITOR = "role_osdp_editor";
    public static final String OSDP_PUBLISHER = "role_osdp_publisher";
    public static final String SA_EDITOR = "role_sa_editor";
    public static final String SA_PUBLISHER = "role_sa_publisher";
    public static final String UKSCAPE_EDITOR = "role_ukscape_editor";
    public static final String UKSCAPE_PUBLISHER = "role_ukscape_publisher";

    private void addUserToGroup(CatalogueUser user, String... groups) {
        Arrays.stream(groups)
                .forEach(group -> groupStore().grantGroupToUser(user, group));
    }

    @Bean
    public CatalogueUser admin() throws UsernameAlreadyTakenException {
        val user = new CatalogueUser()
            .setUsername(ADMIN)
            .setEmail("admin@ceh.ac.uk");
        addUserToGroup(user, MAINTENANCE_ROLE);
        userStore().addUser(user, "password");
        return user;
    }

    @Bean
    public CatalogueUser assistEditor() throws UsernameAlreadyTakenException {
        val user = new CatalogueUser()
            .setUsername("assist-editor")
            .setEmail("assist-editor@ceh.ac.uk");
        addUserToGroup(user, ASSIST_EDITOR);
        userStore().addUser(user, "password");
        return user;
    }

    @Bean
    public CatalogueUser assistPublisher() throws UsernameAlreadyTakenException {
        val user = new CatalogueUser()
            .setUsername("assist-publisher")
            .setEmail("assist-publisher@ceh.ac.uk");
        addUserToGroup(user, ASSIST_EDITOR, ASSIST_PUBLISHER);
        userStore().addUser(user, "password");
        return user;
    }

    @Bean
    public CatalogueUser cmpEditor() throws UsernameAlreadyTakenException {
        val user = new CatalogueUser()
            .setUsername("cmp-editor")
            .setEmail("cmp-editor@ceh.ac.uk");
        addUserToGroup(user, CMP_EDITOR);
        userStore().addUser(user, "password");
        return user;
    }

    @Bean
    public CatalogueUser cmpPublisher() throws UsernameAlreadyTakenException {
        val user = new CatalogueUser()
            .setUsername("cmp-publisher")
            .setEmail("cmp-publisher@ceh.ac.uk");
        addUserToGroup(user, CMP_EDITOR, CMP_PUBLISHER);
        userStore().addUser(user, "password");
        return user;
    }

    @Bean
    public CatalogueUser datalabsEditor() throws UsernameAlreadyTakenException {
        val user = new CatalogueUser()
                .setUsername("datalabs-editor")
                .setEmail("datalabs-editor@ceh.ac.uk");
        addUserToGroup(user, DATALABS_EDITOR);
        userStore().addUser(user, "password");
        return user;
    }

    @Bean
    public CatalogueUser datalabsPublisher() throws UsernameAlreadyTakenException {
        val user = new CatalogueUser()
                .setUsername("datalabs-publisher")
                .setEmail("datalabs-publisher@ceh.ac.uk");
        addUserToGroup(user, DATALABS_EDITOR, DATALABS_PUBLISHER);
        userStore().addUser(user, "password");
        return user;
    }

    @Bean
    public CatalogueUser eidcEditor() throws UsernameAlreadyTakenException {
        val user = new CatalogueUser()
            .setUsername("eidc-editor")
            .setEmail("eidc-editor@ceh.ac.uk");
        addUserToGroup(user, EIDC_EDITOR);
        userStore().addUser(user, "password");
        return user;
    }

    @Bean
    public CatalogueUser eidcPublisher() throws UsernameAlreadyTakenException {
        val user = new CatalogueUser()
            .setUsername(EIDC_PUBLISHER_USERNAME)
            .setEmail("eidc-publisher@ceh.ac.uk");
        addUserToGroup(user, DATACITE_ROLE, EIDC_EDITOR, EIDC_PUBLISHER);
        userStore().addUser(user, "password");
        return user;
    }

    @Bean
    public CatalogueUser elterEditor() throws UsernameAlreadyTakenException {
        val user = new CatalogueUser()
            .setUsername("elter-editor")
            .setEmail("elter-editor@ceh.ac.uk");
        addUserToGroup(user, ELTER_EDITOR);
        userStore().addUser(user, "password");
        return user;
    }

    @Bean
    public CatalogueUser elterPublisher() throws UsernameAlreadyTakenException {
        val user = new CatalogueUser()
            .setUsername("elter-publisher")
            .setEmail("elter-publisher@ceh.ac.uk");
        addUserToGroup(user, ELTER_EDITOR, ELTER_PUBLISHER);
        userStore().addUser(user, "password");
        return user;
    }

    @Bean
    public CatalogueUser erammpEditor() throws UsernameAlreadyTakenException {
        val user = new CatalogueUser()
            .setUsername("erammp-editor")
            .setEmail("erammp-editor@ceh.ac.uk");
        addUserToGroup(user, ERAMMP_EDITOR);
        userStore().addUser(user, "password");
        return user;
    }

    @Bean
    public CatalogueUser erammpPublisher() throws UsernameAlreadyTakenException {
        val user = new CatalogueUser()
            .setUsername("erammp-publisher")
            .setEmail("erammp-publisher@ceh.ac.uk");
        addUserToGroup(user, ERAMMP_EDITOR, ERAMMP_PUBLISHER);
        userStore().addUser(user, "password");
        return user;
    }

    @Bean
    public CatalogueUser inmsPublisher() throws UsernameAlreadyTakenException {
        val user = new CatalogueUser()
            .setUsername("inms-publisher")
            .setEmail("inms-publisher@ceh.ac.uk");
        addUserToGroup(user, INMS_EDITOR, INMS_PUBLISHER);
        userStore().addUser(user, "password");
        return user;
    }

    @Bean
    public CatalogueUser mPublisher() throws UsernameAlreadyTakenException {
        val user = new CatalogueUser()
            .setUsername("m-publisher")
            .setEmail("m-publisher@ceh.ac.uk");
        addUserToGroup(user, M_EDITOR, M_PUBLISHER);
        userStore().addUser(user, "password");
        return user;
    }

    @Bean
    public CatalogueUser nmPublisher() throws UsernameAlreadyTakenException {
        val user = new CatalogueUser()
            .setUsername("nm-publisher")
            .setEmail("nm-publisher@ceh.ac.uk");
        addUserToGroup(user, NM_EDITOR, NM_PUBLISHER);
        userStore().addUser(user, "password");
        return user;
    }

    @Bean
    public CatalogueUser ncEditor() throws UsernameAlreadyTakenException {
        val user = new CatalogueUser()
            .setUsername("nc-editor")
            .setEmail("nc-editor@ceh.ac.uk");
        addUserToGroup(user, NC_EDITOR);
        userStore().addUser(user, "password");
        return user;
    }

    @Bean
    public CatalogueUser ncPublisher() throws UsernameAlreadyTakenException {
        val user = new CatalogueUser()
            .setUsername("nc-publisher")
            .setEmail("nc-publisher@ceh.ac.uk");
        addUserToGroup(user, NC_EDITOR, NC_PUBLISHER);
        userStore().addUser(user, "password");
        return user;
    }

    @Bean
    public CatalogueUser osdpPublisher() throws UsernameAlreadyTakenException {
        val user = new CatalogueUser()
            .setUsername("osdp-publisher")
            .setEmail("osdp-publisher@ceh.ac.uk");
        addUserToGroup(user, OSDP_EDITOR, OSDP_PUBLISHER);
        userStore().addUser(user, "password");
        return user;
    }

    @Bean
    public CatalogueUser readonly() throws UsernameAlreadyTakenException {
        val user = new CatalogueUser()
            .setUsername("readonly")
            .setEmail("readonly@ceh.ac.uk");
        addUserToGroup(user, READONLY_GROUP);
        userStore().addUser(user, "password");
        return user;
    }

    @Bean
    public CatalogueUser saPublisher() throws UsernameAlreadyTakenException {
        val user = new CatalogueUser()
            .setUsername("sa-publisher")
            .setEmail("sa-publisher@ceh.ac.uk");
        addUserToGroup(user, SA_EDITOR, SA_PUBLISHER);
        userStore().addUser(user, "password");
        return user;
    }

    @Bean
    public CatalogueUser superadmin() throws UsernameAlreadyTakenException {
        val user = new CatalogueUser()
            .setUsername("superadmin")
            .setEmail("superadmin@ceh.ac.uk");
        addUserToGroup(user, CEH_GROUP_NAME, EIDC_EDITOR, EIDC_PUBLISHER, MAINTENANCE_ROLE, DATACITE_ROLE);
        userStore().addUser(user, "password");
        return user;
    }

    @Bean
    public CatalogueUser ukscapePublisher() throws UsernameAlreadyTakenException {
        val user = new CatalogueUser()
            .setUsername("ukscape-publisher")
            .setEmail("ukscape-publisher@ceh.ac.uk");
        addUserToGroup(user, UKSCAPE_EDITOR, UKSCAPE_PUBLISHER);
        userStore().addUser(user, "password");
        return user;
    }

    @Bean
    public CatalogueUser unprivilegedUser() throws UsernameAlreadyTakenException {
        // Used in UploadControllerTest to check upload permissions
        val user = new CatalogueUser()
            .setUsername(UNPRIVILEGED_USERNAME)
            .setEmail("unprivileged@example.com");
        userStore().addUser(user, "password");
        return user;
    }

    @Bean
    public CatalogueUser uploader() throws UsernameAlreadyTakenException {
        // Used in UploadControllerTest to check upload permissions
        val user = new CatalogueUser()
            .setUsername(UPLOADER_USERNAME)
            .setEmail("uploader@example.com");
        userStore().addUser(user, "password");
        return user;
    }

    @SuppressWarnings("DuplicatedCode")
    @Bean
    public InMemoryGroupStore<CatalogueUser> groupStore() {
        val groupStore = new InMemoryGroupStore<CatalogueUser>();
        //create groups
        groupStore.createGroup(ASSIST_EDITOR, "");
        groupStore.createGroup(ASSIST_PUBLISHER, "");
        groupStore.createGroup(CEH_GROUP_NAME, "");
        groupStore.createGroup(CMP_EDITOR, "");
        groupStore.createGroup(CMP_PUBLISHER, "");
        groupStore.createGroup(DATACITE_ROLE, "");
        groupStore.createGroup(DATALABS_EDITOR, "");
        groupStore.createGroup(DATALABS_PUBLISHER, "");
        groupStore.createGroup(EIDC_EDITOR, "");
        groupStore.createGroup(EIDC_PUBLISHER, "");
        groupStore.createGroup(ELTER_EDITOR, "");
        groupStore.createGroup(ELTER_PUBLISHER, "");
        groupStore.createGroup(ERAMMP_EDITOR, "");
        groupStore.createGroup(ERAMMP_PUBLISHER, "");
        groupStore.createGroup(INMS_EDITOR, "");
        groupStore.createGroup(INMS_PUBLISHER, "");
        groupStore.createGroup(M_EDITOR, "");
        groupStore.createGroup(M_PUBLISHER, "");
        groupStore.createGroup(NM_EDITOR, "");
        groupStore.createGroup(NM_PUBLISHER, "");
        groupStore.createGroup(MAINTENANCE_ROLE, "");
        groupStore.createGroup(NC_EDITOR, "");
        groupStore.createGroup(NC_PUBLISHER, "");
        groupStore.createGroup(OSDP_EDITOR, "");
        groupStore.createGroup(OSDP_PUBLISHER, "");
        groupStore.createGroup(READONLY_GROUP, "");
        groupStore.createGroup(SA_EDITOR, "");
        groupStore.createGroup(SA_PUBLISHER, "");
        groupStore.createGroup(UKSCAPE_PUBLISHER, "");
        groupStore.createGroup(UKSCAPE_EDITOR, "");
        return groupStore;
    }

    @Bean
    public InMemoryUserStore<CatalogueUser> userStore() {
        return new InMemoryUserStore<>();
    }
}
