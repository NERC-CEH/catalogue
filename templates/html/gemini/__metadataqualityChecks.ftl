<#if resourceType.value="dataset" || resourceType.value="nonGeographicDataset" || resourceType.value="application" || resourceType.value="signpost" || resourceType.value="service">

    <#-- Initialise sequence -->
    <#assign MD_checks=[]>

    <#-- Get data -->
    <#if useConstraints?has_content>
        <#assign licences = func.filter(useConstraints, "code", "license")>
    </#if>
    <#if datasetReferenceDate?has_content && datasetReferenceDate.publicationDate??>
        <#assign publicationDate = datasetReferenceDate.publicationDate>
    </#if>
    <#if responsibleParties?has_content>
        <#assign
            pocs = func.filter(responsibleParties, "role", "pointOfContact")
            authors = func.filter(responsibleParties, "role", "author")
            custodians = func.filter(responsibleParties, "role", "custodian")
            publishers = func.filter(responsibleParties, "role", "publisher")
        >
    </#if>
    <#if distributorContacts?has_content>
        <#assign
            distributors = func.filter(distributorContacts, "role", "distributor")
            nondistributors = func.filter(distributorContacts, "role", "distributor" true)
        >
    </#if>
    <#if descriptiveKeywords?has_content>
        <#assign
            INSPIREthemes = func.filter(descriptiveKeywords, "type", "INSPIRE Theme")
            otherKeywords = func.filter(descriptiveKeywords, "type", "INSPIRE Theme", true)
        >
    </#if>
    <#if onlineResources?has_content>
    <#assign
        orders = func.filter(onlineResources, "function", "order")
        downloads = func.filter(onlineResources, "function", "download")
        validOrders = func.filterRegex(orders, "url", "https://catalogue.ceh.ac.uk/download") + func.filterRegex(orders,"url", "http://catalogue.ceh.ac.uk/download")
        validDownloads = func.filterRegex(downloads, "url", "https://catalogue.ceh.ac.uk/datastore/eidchub/") + func.filterRegex(downloads, "url", "http://catalogue.ceh.ac.uk/datastore/eidchub/")
    >
    <#else>
        <#assign orders = [] downloads = []> 
    </#if>

    <#-- macros -->
        <#macro addResult test result="fail" severity=3 >
            <#assign MD_checks = MD_checks + [{"test":test, "result":result, "severity":severity}] >
        </#macro>
        
        <#macro isPresent elementName target="" severity=3>
            <#if target?has_content>
                <#assign result="pass">
            <#else>
                <#assign result="fail">
            </#if>
            <@addResult elementName + " is present", result, severity/>
        </#macro>

        <#macro hasCount elementName target="" severity=3 elements=1>
            <#if target?? && target?has_content && target?size=elements>
                <#assign result="pass">
            <#else>
                <#assign result="fail">
            </#if>
            <@addResult elementName + " count should be " + elements, result, severity/>
        </#macro>
    <#-- macros END -->

    <#-- Checks -->
    <@isPresent "Resource type" resourceType.value 1 />
    <@isPresent "Title" title 1 />
    <@isPresent "Description" description 1 />
    <@isPresent "Lineage" lineage 1 />
    <@isPresent "Resource status" resourceStatus 1 />
    <#if (metadata.state != 'draft') >
        <@isPresent "Publication date" publicationDate 1 />
    </#if>
    <@hasCount "Point of contact" pocs />
    <#if pocs?has_content>
        <#list pocs as poc>
            <#if poc.email == ''>
                <@addResult "Point of contact email address is missing" "fail" 1/>
            <#elseif poc.email != 'enquiries@ceh.ac.uk'>
                <@addResult "Point of contact email address is <span>" + poc.email + "</span>" />
            </#if>
        </#list>
    </#if>
    <@hasCount "Licence" licence 1 1 />

    <#if resourceType.value="dataset" || resourceType.value="nonGeographicDataset" || resourceType.value="application" || resourceType.value="signpost">
        <@isPresent "Authors" authors />
        <#if authors?has_content>
            <#list authors as author>
                <#if author.email == ''>
                    <@addResult "Author email address is missing" "fail" 1/>
                <#elseif author.email?ends_with("@ceh.ac.uk") && author.email != 'enquiries@ceh.ac.uk'>
                    <@addResult "Author email address is <span>" + author.email + "</span>"/>
                </#if>
            </#list>
        </#if>
        <@isPresent "Temporal extents" temporalExtents />
        <@isPresent "Topic category" topicCategories />
        <@isPresent "Data format" distributionFormats />
        <#if distributionFormats?has_content>
            <#list distributionFormats as distributionFormat>
                <#if distributionFormat.version == '' >
                    <@addResult "Format version is empty" "fail" 1/>
                </#if>
            </#list>
        </#if>
        <@hasCount "Custodian" custodians />
        <#if custodians?has_content>
            <#list custodians as custodian>
                <#if custodian.organisationName != 'Environmental Information Data Centre'>
                    <@addResult "Custodian name is <span>" + custodian.organisationName + "</span>"/>
                </#if>
                <#if custodian.email == ''>
                    <@addResult "Custodian email address is missing" "fail" 1/>
                <#elseif custodian.email != 'eidc@ceh.ac.uk'>
                    <@addResult "Custodian email address is <span>" + custodian.email + "</span>"/>
                </#if>
            </#list>
        </#if>
        <@hasCount "Publisher" publishers />
        <#if publishers?has_content>
            <#list publishers as publisher>
                <#if publisher.organisationName != 'NERC Environmental Information Data Centre'>
                    <@addResult "Publisher name is <span>" + publisher.organisationName + "</span>"/>
                </#if>
                <#if publisher.email == ''>
                    <@addResult "Publisher email address is missing" "fail" 1/>
                <#elseif publisher.email != 'eidc@ceh.ac.uk'>
                    <@addResult "Publisher email address is <span>" + publisher.email  + "</span>"/>
                </#if>
            </#list>
        </#if>
        <#if distributorContacts?has_content>
            <#if nondistributors?size gte 1>
                <@addResult "Distributor contact must have role 'distributor'" "fail" 1/>
            </#if>
            <#if distributors?size gt 1>
                <@hasCount "Distributor contact" distributors 1 1/>
            </#if>
        <#else>
            <@hasCount "Distributor contact" distributorContacts 1 1/>
        </#if>
        <#if (orders?size + downloads?size) = 0 >
            <@addResult "There are no orders/downloads"/>
        <#else>
            <#if (orders?size + downloads?size) gt 1 >
                <@addResult "There are multiple orders/downloads"/>
            </#if>
            <#if (downloads?size != validDownloads?size) || (orders?size != validOrders?size) >
                <@addResult "Orders/downloads do not have a valid EIDC url"/>
            </#if>
        </#if>
    </#if>

    <#if resourceType.value="dataset">
        <#if INSPIREthemes?has_content>
            <#list INSPIREthemes as INSPIREtheme>
                <#list INSPIREtheme.keywords as keyword>
                    <#if keyword.uri?has_content>
                        <#if keyword.uri?starts_with("http://inspire.ec.europa.eu/theme")>
                        <#else>
                            <@addResult "INSPIRE theme does not have correct URI" />
                        </#if>
                    <#else>
                    <@addResult "INSPIRE theme does not have a URI" />
                    </#if>
                </#list>    
            </#list>
        <#else>
            <@isPresent "INSPIRE theme" INSPIREthemes 1 />
        </#if>
        <@isPresent "Spatial extent" boundingBoxes 1 />
        <@isPresent "Spatial representation type" spatialRepresentationTypes 1 />
        <@isPresent "Spatial reference system" spatialReferenceSystems 1 />
    </#if>
    <#-- Checks END -->

</#if>