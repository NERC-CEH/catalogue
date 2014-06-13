<#macro chrome title>
<html>
  <head>
    <title>${title}</title>
  </head>
  <body>
    <h1>${title}</h1>
    <div>
        <#nested>
    </div>
  </body>
</html>
</#macro>