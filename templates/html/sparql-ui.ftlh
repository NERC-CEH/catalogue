<#-- @ftlvariable name="sparqlEndpoint" type="java.lang.String" -->
<#-- @ftlvariable name="catalogueGraph" type="java.lang.String" -->
<head>
  <link href="https://unpkg.com/@triply/yasgui/build/yasgui.min.css" rel="stylesheet" type="text/css" />
  <script src="https://unpkg.com/@triply/yasgui/build/yasgui.min.js"></script>
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
<div id="yasgui"></div>
<script>
  const ENDPOINT = "${sparqlEndpoint}";
  const CATALOGUE_GRAPH = "${catalogueGraph}";

  const EXAMPLES = [
    {
      name: "Datasets by topic, labelled by the vocabulary",
      query: [
        "# The catalogue records which concept a dataset is about; the authority",
        "# publishes what that concept is called. Neither graph alone can answer this.",
        "PREFIX dcterms: <http://purl.org/dc/terms/>",
        "PREFIX skos:    <http://www.w3.org/2004/02/skos/core#>",
        "",
        "SELECT ?label (COUNT(DISTINCT ?dataset) AS ?datasets)",
        "WHERE {",
        "  GRAPH <" + CATALOGUE_GRAPH + "> {",
        "    ?dataset dcterms:subject ?concept .",
        "  }",
        "  GRAPH <http://www.eionet.europa.eu/gemet/> {",
        "    ?concept skos:prefLabel ?label .",
        "  }",
        "}",
        "GROUP BY ?label",
        "ORDER BY DESC(?datasets)",
        "LIMIT 25"
      ]
    },
    {
      name: "Which graphs are here, and how big",
      query: [
        "# Every named graph this endpoint holds. The catalogue's own is the large",
        "# one; the rest are authority vocabularies republished unchanged.",
        "SELECT ?graph (COUNT(*) AS ?triples)",
        "WHERE { GRAPH ?graph { ?s ?p ?o } }",
        "GROUP BY ?graph",
        "ORDER BY DESC(?triples)"
      ]
    },
    {
      name: "Who asserted what about one concept",
      query: [
        "# The same subject seen from each graph. Useful for telling the",
        "# catalogue's claims apart from the authority's.",
        "SELECT ?graph ?predicate ?object",
        "WHERE {",
        "  GRAPH ?graph {",
        "    <http://vocabs.lter-europe.net/EnvThes/30347> ?predicate ?object",
        "  }",
        "}",
        "ORDER BY ?graph ?predicate"
      ]
    },
    {
      name: "Concepts used by records but not yet labelled",
      query: [
        "# Where a vocabulary has not been harvested, or a concept has been",
        "# retired, the catalogue references a subject nothing describes.",
        "PREFIX dcterms: <http://purl.org/dc/terms/>",
        "PREFIX skos:    <http://www.w3.org/2004/02/skos/core#>",
        "",
        "SELECT ?concept (COUNT(DISTINCT ?dataset) AS ?datasets)",
        "WHERE {",
        "  GRAPH <" + CATALOGUE_GRAPH + "> {",
        "    ?dataset dcterms:subject ?concept .",
        "    FILTER(isIRI(?concept))",
        "  }",
        "  FILTER NOT EXISTS { GRAPH ?any { ?concept skos:prefLabel ?label } }",
        "}",
        "GROUP BY ?concept",
        "ORDER BY DESC(?datasets)",
        "LIMIT 25"
      ]
    },
    {
      name: "What species a dataset is about, named by Wikidata",
      query: [
        "# The catalogue records a subject as a bare identifier and says no more",
        "# about it: <.../entity/Q58697> and nothing else. Wikidata's graph is",
        "# what turns that into a name.",
        "#",
        "# Note ?name and ?taxonName are both optional and both worth asking",
        "# for. Some species have no English label in Wikidata at all -- the",
        "# scientific name is the only name they have, and for a species it is",
        "# arguably the better one anyway.",
        "PREFIX dcterms: <http://purl.org/dc/terms/>",
        "PREFIX rdfs:    <http://www.w3.org/2000/01/rdf-schema#>",
        "PREFIX schema:  <http://schema.org/>",
        "PREFIX wdt:     <http://www.wikidata.org/prop/direct/>",
        "",
        "SELECT ?title ?name ?taxonName ?kind ?description",
        "WHERE {",
        "  GRAPH <" + CATALOGUE_GRAPH + "> {",
        "    ?dataset dcterms:title ?title ;",
        "             dcterms:subject ?subject .",
        "  }",
        "  GRAPH <http://www.wikidata.org/entity/> {",
        "    ?subject wdt:P31 ?type .",
        "    OPTIONAL { ?subject rdfs:label ?name }",
        "    OPTIONAL { ?subject wdt:P225 ?taxonName }",
        "    OPTIONAL { ?subject schema:description ?description }",
        "    # The type is named in the same graph, so 'instance of Q16521'",
        "    # reads as 'taxon' without a second lookup.",
        "    OPTIONAL { ?type rdfs:label ?kind }",
        "  }",
        "}",
        "LIMIT 25"
      ]
    },
    {
      name: "Datasets, their licence and their funder",
      query: [
        "# Entirely within the catalogue graph, for comparison: no external",
        "# authority is involved in answering this one.",
        "PREFIX dcterms: <http://purl.org/dc/terms/>",
        "PREFIX frapo:   <http://purl.org/cerif/frapo/>",
        "PREFIX prov:    <http://www.w3.org/ns/prov#>",
        "",
        "SELECT ?title ?licence ?grantNumber",
        "WHERE {",
        "  GRAPH <" + CATALOGUE_GRAPH + "> {",
        "    ?dataset dcterms:title ?title ;",
        "             dcterms:license ?licence ;",
        "             prov:wasGeneratedBy ?grant .",
        "    ?grant frapo:hasGrantNumber ?grantNumber .",
        "  }",
        "}",
        "LIMIT 25"
      ]
    }
  ];

  const yasgui = new Yasgui(document.getElementById("yasgui"), {
    requestConfig: { endpoint: ENDPOINT }
  });

  yasgui.getTab().yasqe.addPrefixes({
    dcat: "http://www.w3.org/ns/dcat#",
    dcterm: "http://purl.org/dc/terms/",
    dcterms: "http://purl.org/dc/terms/",
    skos: "http://www.w3.org/2004/02/skos/core#",
    foaf: "http://xmlns.com/foaf/0.1/",
    prov: "http://www.w3.org/ns/prov#",
    frapo: "http://purl.org/cerif/frapo/",
    sd: "http://www.w3.org/ns/sparql-service-description#",
    void: "http://rdfs.org/ns/void#"
  });

  // Loaded on click rather than on page load: YASGUI persists tabs in
  // localStorage, so writing a query into the editor unprompted would discard
  // whatever the visitor was last working on.
  const list = document.getElementById("examples");
  EXAMPLES.forEach(function (example) {
    const item = document.createElement("li");
    const button = document.createElement("button");
    button.type = "button";
    button.textContent = example.name;
    button.addEventListener("click", function () {
      yasgui.getTab().yasqe.setValue(example.query.join("\n"));
    });
    item.appendChild(button);
    list.appendChild(item);
  });
</script>
</body>
