<#--
  This template defines a Web accessable folder (WAF). Essentially it emulates
  a directory listing like those generated by standard web servers.
-->
<#compress>
  <html>
    <head></head>
    <body>
      <#list files as file>
        <a href="${file}">${file}</a><br>
      </#list>
    </body>
  </html>
</#compress>