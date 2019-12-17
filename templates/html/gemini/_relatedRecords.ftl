<!--  MACROS  
<#macro displayRelationships links title>
  <#if links?has_content>
      <div>
        <div class="col-sm-3">${title}</div>
        <div class="col-sm-9">
          <#list links as link>
            <a href="${link.href}">${link.title}</a><sep><br></sep>
          </#list>
        </div>
      </div>
  </#if>
</#macro>

<#macro relationship title relation>
  <#local links=jena.relationships(uri, relation) />
  <@displayRelationships links title/>
</#macro>

<#macro inverseRelationship title relation>
  <#local links=jena.inverseRelationships(uri, relation) />
  <@displayRelationships links title/>
</#macro>
-->
<div class="relationships clearfix alert-danger">
    <h2>RELATIONSHIPS</h2>
    <@relationship "Related" "http://vocabs.ceh.ac.uk/eidc#relatedTo" />
    <@inverseRelationship "Related" "http://vocabs.ceh.ac.uk/eidc#relatedTo" />

    <@relationship "Produces" "http://vocabs.ceh.ac.uk/eidc#produces" />
    <@relationship "Supersedes" "http://vocabs.ceh.ac.uk/eidc#supersedes" />
    <@relationship "Uses" "http://vocabs.ceh.ac.uk/eidc#uses" />
    <@relationship "Part of" "http://vocabs.ceh.ac.uk/eidc#partOf" />

    <@inverseRelationship "Is produced by" "http://vocabs.ceh.ac.uk/eidc#produces" />
    <@inverseRelationship "Superseded by" "http://vocabs.ceh.ac.uk/eidc#supersedes" />
    <@inverseRelationship "Is used by" "http://vocabs.ceh.ac.uk/eidc#uses" />
    <@inverseRelationship "Has part" "http://vocabs.ceh.ac.uk/eidc#partOf" />
</div>