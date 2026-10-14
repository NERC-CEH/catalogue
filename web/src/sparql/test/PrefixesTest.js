import prefixes from '../prefixes.json'

// The prefixes the SPARQL page's editor suggests after PREFIX, in place of prefix.cc's (dri-one #470).
// YASQE inserts each as `name: <namespace>`, so a bad entry would be offered as a broken declaration.
describe('The SPARQL editor\'s prefix list', () => {
  const entries = Object.entries(prefixes)

  it('has the namespaces the page and its example queries declare', () => {
    expect(Object.keys(prefixes)).toEqual(jasmine.arrayContaining(
      ['dcat', 'dcterms', 'foaf', 'frapo', 'prov', 'rdfs', 'schema', 'sd', 'skos', 'void', 'wdt']))
  })

  it('names each prefix as SPARQL allows', () => {
    entries.forEach(([name]) => expect(name).toMatch(/^[A-Za-z][A-Za-z0-9_-]*$/))
  })

  it('gives each a full namespace IRI, ending as namespaces do', () => {
    entries.forEach(([name, namespace]) => {
      expect(new URL(namespace).protocol).withContext(name).toMatch(/^https?:$/)
      expect(namespace).withContext(name).toMatch(/[#/]$/)
    })
  })
})
