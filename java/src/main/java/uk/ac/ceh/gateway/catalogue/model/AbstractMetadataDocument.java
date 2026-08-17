package uk.ac.ceh.gateway.catalogue.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.annotation.JsonSerialize;
import lombok.Data;
import lombok.experimental.Accessors;
import uk.ac.ceh.gateway.catalogue.gemini.Keyword;
import uk.ac.ceh.gateway.catalogue.gemini.OnlineResource;
import uk.ac.ceh.gateway.catalogue.gemini.ResourceConstraint;
import uk.ac.ceh.gateway.catalogue.gemini.ResourceIdentifier;
import uk.ac.ceh.gateway.catalogue.gemini.adapters.LocalDateTimeDeserializer;
import uk.ac.ceh.gateway.catalogue.gemini.adapters.LocalDateTimeSerializer;
import uk.ac.ceh.gateway.catalogue.util.CollectionFilter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Data
@Accessors(chain = true)
public abstract class AbstractMetadataDocument implements MetadataDocument {
    private String id, uri, type, title, description;
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime metadataDate;
    private List<ResourceIdentifier> resourceIdentifiers;
    @JsonIgnore
    private MetadataInfo metadata;
    private Set<Relationship> relationships;
    private List<Keyword> keywords;
    @JsonIgnore
    private List<Link> relRelation;
    @JsonIgnore
    private List<Link> relIsRequiredBy;
    @JsonIgnore
    private List<Link> relRequires;
    @JsonIgnore
    private List<Link> relPartOf;
    @JsonIgnore
    private List<Link> relHasPart;
    @JsonIgnore
    private List<Link> relAll;
    @JsonIgnore
    private List<Link> relReplaces;
    @JsonIgnore
    private List<Link> relSource;

    public Set<Relationship> getRelationships() {
        return Optional.ofNullable(relationships)
            .orElseGet(Collections::emptySet);
    }

    public List<ResourceIdentifier> getResourceIdentifiers() {
        return Optional.ofNullable(resourceIdentifiers)
            .orElseGet(Collections::emptyList);
    }

    @JsonIgnore
    public List<ResourceIdentifier> getMasterDocument() {
        return CollectionFilter.filterByPropertyRegex(
            resourceIdentifiers,
            ResourceIdentifier::getCode,
            ".+\\.catalogue\\.ceh\\.ac\\.uk\\/id\\/.+",
            false
        );
    }

    @Override
    @JsonIgnore
    public String getMetadataDateTime() {
        return Optional.ofNullable(metadataDate)
            .map(md -> md.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
            .orElse("");
    }

    @Override
    @JsonIgnore
    public List<Keyword> getAllKeywords() {
        return keywords;
    }

    @JsonProperty("relRelation")
    public List<Link> getRelRelation() {
        return Optional.ofNullable(relRelation)
            .orElseGet(Collections::emptyList);
    }

    @JsonIgnore
    public void setRelRelation(List<Link> relRelation) {
        this.relRelation = relRelation;
    }

    @JsonProperty("relIsRequiredBy")
    public List<Link> getRelIsRequiredBy() {
        return Optional.ofNullable(relIsRequiredBy)
            .orElseGet(Collections::emptyList);
    }

    @JsonIgnore
    public void setRelIsRequiredBy(List<Link> relIsRequiredBy) {
        this.relIsRequiredBy = relIsRequiredBy;
    }

    @JsonProperty("relRequires")
    public List<Link> getRelRequires() {
        return Optional.ofNullable(relRequires)
            .orElseGet(Collections::emptyList);
    }

    @JsonIgnore
    public void setRelRequires(List<Link> relRequires) {
        this.relRequires = relRequires;
    }

    @JsonProperty("relPartOf")
    public List<Link> getRelPartOf() {
        return Optional.ofNullable(relPartOf)
            .orElseGet(Collections::emptyList);
    }

    @JsonIgnore
    public void setRelPartOf(List<Link> relPartOf) {
        this.relPartOf = relPartOf;
    }

    @JsonProperty("relHasPart")
    public List<Link> getRelHasPart() {
        return Optional.ofNullable(relHasPart)
            .orElseGet(Collections::emptyList);
    }

    @JsonIgnore
    public void setRelHasPart(List<Link> relHasPart) {
        this.relHasPart = relHasPart;
    }

    @JsonProperty("relAll")
    public List<Link> getRelAll() {
        return Optional.ofNullable(relAll)
            .orElseGet(Collections::emptyList);
    }

    @JsonIgnore
    public void setRelAll(List<Link> relAll) {
        this.relAll = relAll;
    }

    @JsonProperty("relReplaces")
    public List<Link> getRelReplaces() {
        return Optional.ofNullable(relReplaces)
            .orElseGet(Collections::emptyList);
    }

    @JsonIgnore
    public void setRelReplaces(List<Link> relReplaces) {
        this.relReplaces = relReplaces;
    }

    @JsonProperty("relSource")
    public List<Link> getRelSource() {
        return Optional.ofNullable(relSource)
            .orElseGet(Collections::emptyList);
    }

    @JsonIgnore
    public void setRelSource(List<Link> relSource) {
        this.relSource = relSource;
    }

    @Override
    public MetadataDocument addAdditionalKeywords(List<Keyword> additionalKeywords) {
        keywords = Optional.ofNullable(keywords).orElseGet(ArrayList::new);

        keywords.addAll(additionalKeywords);
        return this;
    }

    public List<OnlineResource> filterOnlineResources(List<OnlineResource> onlineResource, String filterVal) {
        return CollectionFilter.filterByProperty(
            onlineResource,
            OnlineResource::getFunction,
            filterVal,
            false
        );
    }

    public List<OnlineResource> excludeOnlineResources(List<OnlineResource> onlineResource, String filterVal) {
        return CollectionFilter.filterByProperty(
            onlineResource,
            OnlineResource::getFunction,
            filterVal,
            true
        );
    }

    public List<Supplemental> filterSupplemental(List<Supplemental> supplemental, String filterVal) {
        return CollectionFilter.filterByProperty(
            supplemental,
            Supplemental::getFunction,
            filterVal,
            false
        );
    }

    public List<Supplemental> excludeSupplemental(List<Supplemental> supplemental, String filterVal) {
        return CollectionFilter.filterByProperty(
            supplemental,
            Supplemental::getFunction,
            filterVal,
            true
        );
    }

    public List<ResponsibleParty> filterResponsibleParty(List<ResponsibleParty> contacts, String filterVal) {
        return CollectionFilter.filterByProperty(
            contacts,
            ResponsibleParty::getRole,
            filterVal,
            false
        );
    }

    public List<ResponsibleParty> excludeResponsibleParty(List<ResponsibleParty> contacts, String filterVal) {
        return CollectionFilter.filterByProperty(
            contacts,
            ResponsibleParty::getRole,
            filterVal,
            true
        );
    }

    public List<OnlineResource> filterOnlineResourcesUrl(List<OnlineResource> onlineResource, String regexVal) {
        return CollectionFilter.filterByPropertyRegex(
            onlineResource,
            OnlineResource::getUrl,
            regexVal,
            false
        );
    }

    public List<OnlineResource> excludeOnlineResourcesUrl(List<OnlineResource> onlineResource, String regexVal) {
        return CollectionFilter.filterByPropertyRegex(
            onlineResource,
            OnlineResource::getUrl,
            regexVal,
            true
        );
    }

    public List<ResourceConstraint> filterResourceConstraint(List<ResourceConstraint> resourceConstraint, String filterVal) {
        return CollectionFilter.filterByProperty(
            resourceConstraint,
            ResourceConstraint::getCode,
            filterVal,
            false
        );
    }

    public List<ResourceConstraint> excludeResourceConstraint(List<ResourceConstraint> resourceConstraint, String filterVal) {
        return CollectionFilter.filterByProperty(
            resourceConstraint,
            ResourceConstraint::getCode,
            filterVal,
            true
        );
    }

}
