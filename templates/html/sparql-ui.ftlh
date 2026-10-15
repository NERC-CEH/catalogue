<#-- @ftlvariable name="sparqlEndpoint" type="java.lang.String" -->
<#-- @ftlvariable name="catalogueGraph" type="java.lang.String" -->
<head>
  <#-- YASGUI and the examples, bundled (src/sparql/index.js) and found by their hashed names (dri-one #367) -->
  <#list assets.files("sparql") as file>
    <#if file?ends_with(".css")>
  <link rel="stylesheet" type="text/css" href="${file}">
    <#else>
  <script type="module" src="${file}"></script>
    </#if>
  </#list>
  <title>EIDC - SPARQL Query</title>
  <style>
    .examples { font-family: sans-serif; margin: 1rem 0 1.25rem; max-width: 60rem; }
    .examples h2 { font-size: 1rem; margin: 0 0 .25rem; }
    .examples p { font-size: .875rem; color: #444; margin: 0 0 .75rem; line-height: 1.5; }
    .examples ul { list-style: none; margin: 0; padding: 0; display: flex; flex-wrap: wrap; gap: .5rem; }
    .examples button {
      font: inherit; font-size: .8125rem; cursor: pointer;
      background: #f2f5f5; border: 1px solid #c8d4d4; border-radius: 3px;
      padding: .35rem .7rem; color: #14262b;
    }
    .examples button:hover { background: #e6ecec; }
    .examples button:focus-visible { outline: 2px solid #1f6f6b; outline-offset: 1px; }
  </style>
</head>
<body>
<div class="examples">
  <h2>Example queries</h2>
  <p>
    This endpoint keeps what the catalogue asserts about its own records separate
    from what each external authority asserts, in named graphs. The default graph
    is the union of them all, so a query without a <code>GRAPH</code> clause sees
    everything; naming a graph is how you ask who said something. Choose an
    example to load it into the editor.
  </p>
  <ul id="examples"></ul>
</div>
<div id="yasgui" data-endpoint="${sparqlEndpoint}" data-catalogue-graph="${catalogueGraph}"></div>
</body>
