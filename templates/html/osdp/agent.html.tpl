<#import "../skeleton.html.tpl" as skeleton>
<#import "../blocks.html.tpl" as b>

<@skeleton.master title=title catalogue=catalogues.retrieve(metadata.catalogue)><#escape x as x?html>
    <@b.metadataContainer "ceh-model">
      <@b.admin />
      <#if title?? && role?? && address??>
        <@b.sectionHeading>Basic Information</@b.sectionHeading>
        <#if title?has_content>
          <@b.key "Title" "Name">${title}</@b.key>
        </#if>
        <#if role?has_content>
          <@b.key "Role" "Role of agent">${role}</@b.key>
        </#if>
        <#if address?has_content>
          <@b.key "Address" "">
            <@b.linebreaks address />
          </@b.key>
        </#if>
      </#if>
    </@b.metadataContainer>
</#escape></@skeleton.master>