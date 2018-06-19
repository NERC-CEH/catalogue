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
    <#if metadataPointsOfContact?has_content>
        <#assign
            metadataContact_pocs = func.filter(metadataPointsOfContact, "role", "pointOfContact")
            metadataContact_other = func.filter(metadataPointsOfContact, "role", "pointOfContact", true)
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
        searches = func.filter(onlineResources, "function", "search") + func.filter(onlineResources, "function", "information") 
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
        
        <#macro isMissing elementName target="" severity=3>
            <#if target?has_content>
                <#assign result="pass">
            <#else>
                <#assign result="fail">
            </#if>
            <@addResult elementName + " is missing", result, severity/>
        </#macro>

        <#macro hasCount elementName target="" severity=3 requirement=1>
            <#if target?? && target?has_content && target?size=requirement>
                <#assign result="pass">
            <#else>
                <#assign result="fail">
            </#if>
            <@addResult elementName + " count should be " + requirement, result, severity/>
        </#macro>
    <#-- macros END -->

    <#-- Checks -->
    <@isMissing "Resource type" resourceType.value 1 />
    <@isMissing "Title" title 1 />
    <@isMissing "Description" description 1 />
    <@isMissing "Lineage" lineage 1 />
    <@isMissing "Resource status" resourceStatus 1 />
    <#if (metadata.state != 'draft') >
        <@isMissing "Publication date" publicationDate 1 />
    </#if>
    
    <@isMissing "Metadata point of contact" metadataPointsOfContact 1 />
    <#if metadataPointsOfContact?has_content>
        <#if metadataContact_other?size gt 0>
            <@addResult "Metadata point of contact MUST have the role 'Point of contact'" "fail" 1/>
        </#if>
        <#if metadataContact_pocs?has_content>
            <@hasCount "Metadata point of contact" metadataContact_pocs />
        </#if>
    </#if>

    <@isMissing "Point of contact" pocs 1 />
    <#if pocs?has_content>
        <@hasCount "Point of contact" pocs />
        <#list pocs as poc>
            <#if poc.email == ''>
                <@addResult "Point of contact email address is missing" "fail" 1/>
            <#elseif poc.email != 'enquiries@ceh.ac.uk'>
                <@addResult "Point of contact email address is <span>" + poc.email + "</span>" />
            </#if>
        </#list>
    </#if>
    
    <#if resourceType.value != "aggregate">
        <@isMissing "Licence" licences 1 />
        <#if licences?has_content>
            <@hasCount "Licence" licences 1 1 />
        </#if>
        <@isMissing "Temporal extents" temporalExtents />
    </#if>  
    
    <#if resourceType.value="dataset" || resourceType.value="nonGeographicDataset" || resourceType.value="application" || resourceType.value="signpost">
        <@isMissing "Authors" authors />
        <#if authors?has_content>
            <#list authors as author>
                <#if author.email == ''>
                    <@addResult "Author email address is missing" "fail" 1/>
                <#elseif author.email?ends_with("@ceh.ac.uk") && author.email != 'enquiries@ceh.ac.uk'>
                    <@addResult "Author email address is <span>" + author.email + "</span>"/>
                </#if>
            </#list>
        </#if>
        
        <@isMissing "Topic category" topicCategories />
        <@isMissing "Data format" distributionFormats />
        <#if distributionFormats?has_content>
            <#list distributionFormats as distributionFormat>
                <#if distributionFormat.version == '' >
                    <@addResult "Format version is empty" "fail" 1/>
                </#if>
            </#list>
        </#if>

        <@isMissing "Custodian" custodians />
        <#if custodians?has_content>
            <@hasCount "Custodian" custodians 1 1/>
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

        <@isMissing "Publisher" publishers />
        <#if publishers?has_content>
            <@hasCount "Publisher" publishers 1 1/>
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

        <@isMissing "Distributor" distributorContacts 1/>
        <#if distributorContacts?has_content>
            <@hasCount "Distributor contact" distributorContacts 1 1/>
            <#if nondistributors?size gte 1>
                <@addResult "Distributor contact must have role 'distributor'" "fail" 1/>
            </#if>
            <#if distributors?size gt 1>
                <@hasCount "Distributor contact" distributors 1 1/>
            </#if>
        </#if>

        <#if resourceStatus?? && resourceStatus == 'Current' && resourceType.value !="signpost">
            <#if (orders?size + downloads?size) = 0 >
                <@addResult "There are no orders/downloads" "fail" 1/>
            <#else>
                <#if (orders?size + downloads?size) gt 1 >
                    <@addResult "There are multiple orders/downloads"/>
                </#if>
                <#if (downloads?size != validDownloads?size) || (orders?size != validOrders?size) >
                    <@addResult "Orders/downloads do not have a valid EIDC url"/>
                </#if>
            </#if>
        </#if>

    </#if>

    <#if resourceType.value="dataset">
        <#if INSPIREthemes?has_content>
            <#list INSPIREthemes as INSPIREtheme>
                <#if INSPIREtheme.keywords?size == 0>
                    <@addResult "INSPIRE theme is empty" "fail" 1/>
                </#if>
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
            <@isMissing "INSPIRE theme" INSPIREthemes 1 />
        </#if>
        
        <@isMissing "Spatial extent" boundingBoxes 1 />
        <@isMissing "Spatial representation type" spatialRepresentationTypes 1 />
        <@isMissing "Spatial reference system" spatialReferenceSystems 1 />

        <#if boundingBoxes?has_content>
            <#list boundingBoxes as box>
                <#assign
                    N = box.northBoundLatitude
                    S = box.southBoundLatitude
                    E = box.eastBoundLongitude
                    W = box.westBoundLongitude
                    Nscale = N?string["0.####"]?length-N?floor?length-1
                    Sscale = S?string["0.####"]?length-S?floor?length-1
                    Escale = E?string["0.####"]?length-E?floor?length-1
                    Wscale = W?string["0.####"]?length-W?floor?length-1
                >
                <#if Nscale gt 3 || Sscale gt 3 || Escale gt 3 || Wscale gt 3>
                    <@addResult "Bounding box coordinates are too precise (max 3 decimal places)" "fail" 1 />
                </#if>
                <#if N lt S>
                    <@addResult "Bounding box northern boundary is smaller than the southern" "fail" 1 />
                </#if>
                <#if E lt W>
                    <@addResult "Bounding box east boundary is smaller than the western" "fail" 1 />
                </#if>
            </#list>
        </#if>

    </#if>

    <#if resourceType.value="nonGeographicDataset">
        <#if boundingBoxes?has_content>
            <@addResult "The record has a bounding box but the reource type is Non-geographic dataset" />
        </#if>
        <#if spatialRepresentationTypes?has_content>
            <@addResult "The record has a spatial representation type but the reource type is Non-geographic dataset" />
        </#if>
        <#if spatialReferenceSystems?has_content>
            <@addResult "The record has a spatial reference system but the reource type is Non-geographic dataset" />
        </#if>
    </#if>

    <#if resourceType.value="signpost">
        <#if searches?? && searches?size gt 1 >
            <@addResult "There are more than one search/information links"/>
        <#else>
            <@isMissing "Search/information link" searches 1 />
        </#if>
    </#if>
    <#-- Checks END -->


<#assign
    problems = func.filter(MD_checks, "result", "fail")?sort_by("severity")
    errors = func.filter(problems, "severity", 1)
    warnings = func.filter(problems, "severity", 3)
>
</#if>