<#import "blocks.html.tpl" as blocks>
<#import "skeleton.html.tpl" as skeleton>
<#import "../underscore.tpl" as _>

<@skeleton.master title=title>
  <div id="metadata" class="container">

    <@blocks.title title type />
    <@blocks.description description />

    <#if inputData?? >
      <h3>Input Data</h3>
      <ul>
      <#list inputData as input>
        <li>${input}</li>
      </#list>
      </ul>
    </#if>

    <#if outputData?? >
      <h3>Output Data</h3>
      <ul>
      <#list outputData as output>
        <li>${output}</li>
      </#list>
      </ul>
    </#if>

  </div>
</@skeleton.master>
