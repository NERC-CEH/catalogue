<#--
Create an individual EF link with either the title or the value attribute
as the body text of the link. Optionally supply a target value to specify
which frame the link should load in.
-->
<#macro link l target>
  <#assign display=l.title!l.value/>
  <#if l.href??>
    <a href="${l.href}" target="${target}">${display}</a>
  <#else>
    ${display}
  </#if>
</#macro>

<#--
Create a block of EF links with a given title as a heading
-->
<#macro links title arr target="_blank">
  <#if arr?has_content>
    <h3>${title}</h3>

    <ul class="list-unstyled">
      <#list arr as e>
        <li><@link e target /></li>
      </#list>
    </ul>
  </#if>
</#macro>
