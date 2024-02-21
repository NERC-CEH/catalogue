<#-- @ftlvariable name="sparqlEndpoint" type="java.lang.String" -->
<head>
  <link href="https://unpkg.com/@triply/yasgui/build/yasgui.min.css" rel="stylesheet" type="text/css" />
  <script src="https://unpkg.com/@triply/yasgui/build/yasgui.min.js"></script>
  <title>EIDC - SPARQL Query</title>
</head>
<body>
<div id="yasgui"></div>
<script>
  const yasgui = new Yasgui(document.getElementById("yasgui"), {
    requestConfig: {
      endpoint: "${sparqlEndpoint}"
    }
  });
  const yasqe = yasgui.getTab().yasqe
  yasqe.addPrefixes({
    dcat: "http://www.w3.org/ns/dcat#",
    dcterm: "http://purl.org/dc/terms/"
  })
</script>
</body>
