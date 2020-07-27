package uk.ac.ceh.gateway.catalogue.config;

import org.springframework.beans.factory.annotation.Qualifier;
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

    @Bean
    @Qualifier("superadmin")
    public CatalogueUser superadmin() throws UsernameAlreadyTakenException {
        CatalogueUser superadmin = new CatalogueUser()
            .setUsername("superadmin")
            .setEmail("superadmin@ceh.ac.uk");

        groupStore().grantGroupToUser(superadmin, CEH_GROUP_NAME);
        groupStore().grantGroupToUser(superadmin, EIDC_EDITOR);
        groupStore().grantGroupToUser(superadmin, EIDC_PUBLISHER);
        groupStore().grantGroupToUser(superadmin, MAINTENANCE_ROLE);
        groupStore().grantGroupToUser(superadmin, DATACITE_ROLE);
        groupStore().grantGroupToUser(superadmin, CMP_PUBLISHER);
        groupStore().grantGroupToUser(superadmin, ERAMMP_PUBLISHER);
        groupStore().grantGroupToUser(superadmin, NC_PUBLISHER);
        groupStore().grantGroupToUser(superadmin, M_PUBLISHER);
        groupStore().grantGroupToUser(superadmin, INMS_PUBLISHER);
        groupStore().grantGroupToUser(superadmin, OSDP_PUBLISHER);
        groupStore().grantGroupToUser(superadmin, SA_PUBLISHER);
        userStore().addUser(superadmin, "superadminpassword");
        return superadmin;
    }

    @Bean
    @Qualifier("bamboo")
    public CatalogueUser bamboo() throws UsernameAlreadyTakenException {
        CatalogueUser bamboo = new CatalogueUser()
            .setUsername("bamboo")
            .setEmail("bamboo@ceh.ac.uk");

        groupStore().grantGroupToUser(bamboo, CEH_GROUP_NAME);
        userStore().addUser(bamboo, "bamboopassword");
        return bamboo;
    }

    @Bean
    @Qualifier("readonly")
    public CatalogueUser readonly() throws UsernameAlreadyTakenException {
        CatalogueUser readonly = new CatalogueUser()
            .setUsername("readonly")
            .setEmail("readonly@ceh.ac.uk");

        groupStore().grantGroupToUser(readonly, READONLY_ROLE);
        userStore().addUser(readonly, "readonlypassword");
        return readonly;
    }

    @Bean
    @Qualifier("eidc-editor")
    public CatalogueUser eidcEditor() throws UsernameAlreadyTakenException {
        CatalogueUser editor = new CatalogueUser()
            .setUsername("eidc-editor")
            .setEmail("eidc-editor@ceh.ac.uk");

        groupStore().grantGroupToUser(editor, EIDC_EDITOR);
        userStore().addUser(editor, "editorpassword");
        return editor;
    }

    @Bean
    @Qualifier("eidc-publisher")
    public CatalogueUser eidcPublisher() throws UsernameAlreadyTakenException {
        CatalogueUser publisher = new CatalogueUser()
            .setUsername("eidc-publisher")
            .setEmail("eidc-publisher@ceh.ac.uk");

        groupStore().grantGroupToUser(publisher, EIDC_EDITOR);
        groupStore().grantGroupToUser(publisher, EIDC_PUBLISHER);
        userStore().addUser(publisher, "publisherpassword");
        return publisher;
    }

    @Bean
    @Qualifier("cmp-editor")
    public CatalogueUser cmpEditor() throws UsernameAlreadyTakenException {
        CatalogueUser editor = new CatalogueUser()
            .setUsername("cmp-editor")
            .setEmail("cmp-editor@ceh.ac.uk");

        groupStore().grantGroupToUser(editor, CMP_EDITOR);
        userStore().addUser(editor, "editorpassword");
        return editor;
    }

    @Bean
    @Qualifier("cmp-publisher")
    public CatalogueUser cmpPublisher() throws UsernameAlreadyTakenException {
        CatalogueUser publisher = new CatalogueUser()
            .setUsername("cmp-publisher")
            .setEmail("cmp-publisher@ceh.ac.uk");

        groupStore().grantGroupToUser(publisher, CMP_EDITOR);
        groupStore().grantGroupToUser(publisher, CMP_PUBLISHER);
        userStore().addUser(publisher, "publisherpassword");
        return publisher;
    }

    @Bean
    @Qualifier("elter-editor")
    public CatalogueUser elterEditor() throws UsernameAlreadyTakenException {
        CatalogueUser editor = new CatalogueUser()
                .setUsername("elter-editor")
                .setEmail("elter-editor@ceh.ac.uk");

        groupStore().grantGroupToUser(editor, ELTER_EDITOR);
        userStore().addUser(editor, "editorpassword");
        return editor;
    }

    @Bean
    @Qualifier("elter-publisher")
    public CatalogueUser elterPublisher() throws UsernameAlreadyTakenException {
        CatalogueUser publisher = new CatalogueUser()
                .setUsername("elter-publisher")
                .setEmail("elter-publisher@ceh.ac.uk");

        groupStore().grantGroupToUser(publisher, ELTER_EDITOR);
        groupStore().grantGroupToUser(publisher, ELTER_PUBLISHER);
        userStore().addUser(publisher, "publisherpassword");
        return publisher;
    }

    @Bean
    @Qualifier("nc-editor")
    public CatalogueUser ncEditor() throws UsernameAlreadyTakenException {
        CatalogueUser editor = new CatalogueUser()
            .setUsername("nc-editor")
            .setEmail("nc-editor@ceh.ac.uk");

        groupStore().grantGroupToUser(editor, NC_EDITOR);
        userStore().addUser(editor, "editorpassword");
        return editor;
    }

    @Bean
    @Qualifier("nc-publisher")
    public CatalogueUser ncPublisher() throws UsernameAlreadyTakenException {
        CatalogueUser publisher = new CatalogueUser()
            .setUsername("nc-publisher")
            .setEmail("nc-publisher@ceh.ac.uk");

        groupStore().grantGroupToUser(publisher, NC_EDITOR);
        groupStore().grantGroupToUser(publisher, NC_PUBLISHER);
        userStore().addUser(publisher, "publisherpassword");
        return publisher;
    }

    @Bean
    @Qualifier("erammp-editor")
    public CatalogueUser erammpEditor() throws UsernameAlreadyTakenException {
        CatalogueUser editor = new CatalogueUser()
            .setUsername("erammp-editor")
            .setEmail("erammp-editor@ceh.ac.uk");

        groupStore().grantGroupToUser(editor, ERAMMP_EDITOR);
        userStore().addUser(editor, "editorpassword");
        return editor;
    }

    @Bean
    @Qualifier("erammp-publisher")
    public CatalogueUser erammpPublisher() throws UsernameAlreadyTakenException {
        CatalogueUser publisher = new CatalogueUser()
            .setUsername("erammp-publisher")
            .setEmail("erammp-publisher@ceh.ac.uk");

        groupStore().grantGroupToUser(publisher, ERAMMP_EDITOR);
        groupStore().grantGroupToUser(publisher, ERAMMP_PUBLISHER);
        userStore().addUser(publisher, "publisherpassword");
        return publisher;
    }

    @Bean
    @Qualifier("assist-editor")
    public CatalogueUser assistEditor() throws UsernameAlreadyTakenException {
        CatalogueUser editor = new CatalogueUser()
            .setUsername("assist-editor")
            .setEmail("assist-editor@ceh.ac.uk");

        groupStore().grantGroupToUser(editor, ASSIST_EDITOR);
        userStore().addUser(editor, "editorpassword");
        return editor;
    }

    @Bean
    @Qualifier("assist-publisher")
    public CatalogueUser assistPublisher() throws UsernameAlreadyTakenException {
        CatalogueUser publisher = new CatalogueUser()
            .setUsername("assist-publisher")
            .setEmail("assist-publisher@ceh.ac.uk");

        groupStore().grantGroupToUser(publisher, ASSIST_EDITOR);
        groupStore().grantGroupToUser(publisher, ASSIST_PUBLISHER);
        userStore().addUser(publisher, "publisherpassword");
        return publisher;
    }

    @Bean
    @Qualifier("m-publisher")
    public CatalogueUser mPublisher() throws UsernameAlreadyTakenException {
        CatalogueUser publisher = new CatalogueUser()
            .setUsername("m-publisher")
            .setEmail("m-publisher@ceh.ac.uk");

        groupStore().grantGroupToUser(publisher, M_EDITOR);
        groupStore().grantGroupToUser(publisher, M_PUBLISHER);
        userStore().addUser(publisher, "publisherpassword");
        return publisher;
    }

    @Bean
    @Qualifier("inms-publisher")
    public CatalogueUser inmsPublisher() throws UsernameAlreadyTakenException {
        CatalogueUser publisher = new CatalogueUser()
            .setUsername("inms-publisher")
            .setEmail("inms-publisher@ceh.ac.uk");

        groupStore().grantGroupToUser(publisher, INMS_EDITOR);
        groupStore().grantGroupToUser(publisher, INMS_PUBLISHER);
        userStore().addUser(publisher, "publisherpassword");
        return publisher;
    }

    @Bean
    @Qualifier("osdp-publisher")
    public CatalogueUser osdpPublisher() throws UsernameAlreadyTakenException {
        CatalogueUser publisher = new CatalogueUser()
            .setUsername("osdp-publisher")
            .setEmail("osdp-publisher@ceh.ac.uk");

        groupStore().grantGroupToUser(publisher, OSDP_EDITOR);
        groupStore().grantGroupToUser(publisher, OSDP_PUBLISHER);
        userStore().addUser(publisher, "publisherpassword");
        return publisher;
    }

    @Bean
    @Qualifier("sa-publisher")
    public CatalogueUser saPublisher() throws UsernameAlreadyTakenException {
        CatalogueUser publisher = new CatalogueUser()
            .setUsername("sa-publisher")
            .setEmail("sa-publisher@ceh.ac.uk");

        groupStore().grantGroupToUser(publisher, SA_EDITOR);
        groupStore().grantGroupToUser(publisher, SA_PUBLISHER);
        userStore().addUser(publisher, "publisherpassword");
        return publisher;
    }

    @Bean
    @Qualifier("admin")
    public CatalogueUser admin() throws UsernameAlreadyTakenException {
        CatalogueUser admin = new CatalogueUser()
            .setUsername("admin")
            .setEmail("admin@ceh.ac.uk");

        groupStore().grantGroupToUser(admin, MAINTENANCE_ROLE);
        userStore().addUser(admin, "adminpassword");
        return admin;
    }

    @Bean
    public InMemoryGroupStore<CatalogueUser> groupStore() {
        InMemoryGroupStore<CatalogueUser> toReturn = new InMemoryGroupStore<>();
        //create groups
        toReturn.createGroup(CEH_GROUP_NAME, "Centre for Ecology & Hydrology");
        toReturn.createGroup(READONLY_ROLE, "Read only role");
        toReturn.createGroup(EIDC_EDITOR, "EIDC Editor Role");
        toReturn.createGroup(EIDC_PUBLISHER, "EIDC Publisher Role");
        toReturn.createGroup(CMP_EDITOR, "CMP Editor Role");
        toReturn.createGroup(CMP_PUBLISHER, "CMP Publisher Role");
        toReturn.createGroup(ELTER_EDITOR, "ELTER Editor Role");
        toReturn.createGroup(ELTER_PUBLISHER, "ELTER Publisher Role");
        toReturn.createGroup(ERAMMP_EDITOR, "ERAMMP Editor Role");
        toReturn.createGroup(ERAMMP_PUBLISHER, "ERAMMP Publisher Role");
        toReturn.createGroup(ASSIST_EDITOR, "ASSIST Editor Role");
        toReturn.createGroup(ASSIST_PUBLISHER, "ASSIST Publisher Role");
        toReturn.createGroup(NC_EDITOR, "NC Editor Role");
        toReturn.createGroup(NC_PUBLISHER, "NC Publisher Role");
        toReturn.createGroup(M_EDITOR, "M Editor Role");
        toReturn.createGroup(M_PUBLISHER, "M Publisher Role");
        toReturn.createGroup(INMS_EDITOR, "INMS Editor Role");
        toReturn.createGroup(INMS_PUBLISHER, "INMS Publisher Role");
        toReturn.createGroup(OSDP_EDITOR, "OSDP Editor Role");
        toReturn.createGroup(OSDP_PUBLISHER, "OSDP Publisher Role");
        toReturn.createGroup(SA_EDITOR, "SA Editor Role");
        toReturn.createGroup(SA_PUBLISHER, "SA Publisher Role");
        toReturn.createGroup(MAINTENANCE_ROLE, "System Admin Role");
        toReturn.createGroup(DATACITE_ROLE, "Datacite Role");
        return toReturn;
    }

    @Bean
    public InMemoryUserStore<CatalogueUser> userStore() {
        return new InMemoryUserStore<>();
    }
}
