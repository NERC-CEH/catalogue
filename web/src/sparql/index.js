/*
 * The public SPARQL page (/sparql): YASGUI on the catalogue's Fuseki endpoint, with example queries.
 * Bundled rather than loaded from unpkg, where it was unversioned, so whatever unpkg served as latest
 * ran on the catalogue's origin (NERC-CEH/dri-one#367).
 */
import Yasgui from '@triply/yasgui'
import '@triply/yasgui/build/yasgui.min.css'

// Example queries, each a list of lines; catalogueGraph is the catalogue's own named graph
export const examples = catalogueGraph => [
  {
    name: 'Datasets by topic, labelled by the vocabulary',
    query: [
      '# The catalogue records which concept a dataset is about; the authority',
      '# publishes what that concept is called. Neither graph alone can answer this.',
      'PREFIX dcterms: <http://purl.org/dc/terms/>',
      'PREFIX skos:    <http://www.w3.org/2004/02/skos/core#>',
      '',
      'SELECT ?label (COUNT(DISTINCT ?dataset) AS ?datasets)',
      'WHERE {',
      '  GRAPH <' + catalogueGraph + '> {',
      '    ?dataset dcterms:subject ?concept .',
      '  }',
      '  GRAPH <http://www.eionet.europa.eu/gemet/> {',
      '    ?concept skos:prefLabel ?label .',
      '  }',
      '}',
      'GROUP BY ?label',
      'ORDER BY DESC(?datasets)',
      'LIMIT 25'
    ]
  },
  {
    name: 'Which graphs are here, and how big',
    query: [
      "# Every named graph this endpoint holds. The catalogue's own is the large",
      '# one; the rest are authority vocabularies republished unchanged.',
      'SELECT ?graph (COUNT(*) AS ?triples)',
      'WHERE { GRAPH ?graph { ?s ?p ?o } }',
      'GROUP BY ?graph',
      'ORDER BY DESC(?triples)'
    ]
  },
  {
    name: 'Who asserted what about one concept',
    query: [
      '# The same subject seen from each graph. Useful for telling the',
      "# catalogue's claims apart from the authority's.",
      'SELECT ?graph ?predicate ?object',
      'WHERE {',
      '  GRAPH ?graph {',
      '    <http://vocabs.lter-europe.net/EnvThes/30347> ?predicate ?object',
      '  }',
      '}',
      'ORDER BY ?graph ?predicate'
    ]
  },
  {
    name: 'Concepts used by records but not yet labelled',
    query: [
      '# Where a vocabulary has not been harvested, or a concept has been',
      '# retired, the catalogue references a subject nothing describes.',
      'PREFIX dcterms: <http://purl.org/dc/terms/>',
      'PREFIX skos:    <http://www.w3.org/2004/02/skos/core#>',
      '',
      'SELECT ?concept (COUNT(DISTINCT ?dataset) AS ?datasets)',
      'WHERE {',
      '  GRAPH <' + catalogueGraph + '> {',
      '    ?dataset dcterms:subject ?concept .',
      '    FILTER(isIRI(?concept))',
      '  }',
      '  FILTER NOT EXISTS { GRAPH ?any { ?concept skos:prefLabel ?label } }',
      '}',
      'GROUP BY ?concept',
      'ORDER BY DESC(?datasets)',
      'LIMIT 25'
    ]
  },
  {
    name: 'What species a dataset is about, named by Wikidata',
    query: [
      '# The catalogue records a subject as a bare identifier and says no more',
      "# about it: <.../entity/Q58697> and nothing else. Wikidata's graph is",
      '# what turns that into a name.',
      '#',
      '# Note ?name and ?taxonName are both optional and both worth asking',
      '# for. Some species have no English label in Wikidata at all -- the',
      '# scientific name is the only name they have, and for a species it is',
      '# arguably the better one anyway.',
      'PREFIX dcterms: <http://purl.org/dc/terms/>',
      'PREFIX rdfs:    <http://www.w3.org/2000/01/rdf-schema#>',
      'PREFIX schema:  <http://schema.org/>',
      'PREFIX wdt:     <http://www.wikidata.org/prop/direct/>',
      '',
      'SELECT ?title ?name ?taxonName ?kind ?description',
      'WHERE {',
      '  GRAPH <' + catalogueGraph + '> {',
      '    ?dataset dcterms:title ?title ;',
      '             dcterms:subject ?subject .',
      '  }',
      '  GRAPH <http://www.wikidata.org/entity/> {',
      '    ?subject wdt:P31 ?type .',
      '    OPTIONAL { ?subject rdfs:label ?name }',
      '    OPTIONAL { ?subject wdt:P225 ?taxonName }',
      '    OPTIONAL { ?subject schema:description ?description }',
      "    # The type is named in the same graph, so 'instance of Q16521'",
      "    # reads as 'taxon' without a second lookup.",
      '    OPTIONAL { ?type rdfs:label ?kind }',
      '  }',
      '}',
      'LIMIT 25'
    ]
  },
  {
    name: 'Datasets, their licence and their funder',
    query: [
      '# Entirely within the catalogue graph, for comparison: no external',
      '# authority is involved in answering this one.',
      'PREFIX dcterms: <http://purl.org/dc/terms/>',
      'PREFIX frapo:   <http://purl.org/cerif/frapo/>',
      'PREFIX prov:    <http://www.w3.org/ns/prov#>',
      '',
      'SELECT ?title ?licence ?grantNumber',
      'WHERE {',
      '  GRAPH <' + catalogueGraph + '> {',
      '    ?dataset dcterms:title ?title ;',
      '             dcterms:license ?licence ;',
      '             prov:wasGeneratedBy ?grant .',
      '    ?grant frapo:hasGrantNumber ?grantNumber .',
      '  }',
      '}',
      'LIMIT 25'
    ]
  }
]

const element = document.getElementById('yasgui')
const yasgui = new Yasgui(element, {
  requestConfig: { endpoint: element.dataset.endpoint },
  yasqe: {
    // The prefixes the editor suggests after PREFIX. YASQE fetches them from prefix.cc by default,
    // which the Content-Security-Policy refuses (connect-src), so they come from a list of the
    // namespaces the catalogue's graphs use, built as one of our own assets (NERC-CEH/dri-one#470)
    prefixCcApi: new URL('./prefixes.json', import.meta.url).href
  }
})

yasgui.getTab().yasqe.addPrefixes({
  dcat: 'http://www.w3.org/ns/dcat#',
  dcterm: 'http://purl.org/dc/terms/',
  dcterms: 'http://purl.org/dc/terms/',
  skos: 'http://www.w3.org/2004/02/skos/core#',
  foaf: 'http://xmlns.com/foaf/0.1/',
  prov: 'http://www.w3.org/ns/prov#',
  frapo: 'http://purl.org/cerif/frapo/',
  sd: 'http://www.w3.org/ns/sparql-service-description#',
  void: 'http://rdfs.org/ns/void#'
})

// Loaded on click rather than on page load: YASGUI persists tabs in localStorage, so writing a query
// into the editor unprompted would discard whatever the visitor was last working on.
const list = document.getElementById('examples')
examples(element.dataset.catalogueGraph).forEach(example => {
  const item = document.createElement('li')
  const button = document.createElement('button')
  button.type = 'button'
  button.textContent = example.name
  button.addEventListener('click', () => yasgui.getTab().yasqe.setValue(example.query.join('\n')))
  item.appendChild(button)
  list.appendChild(item)
})
