import Catalogue from '../src/CatalogueApp/Catalogue'
import CatalogueViewModel from '../src/CatalogueApp/CatalogueViewModel'

describe('Catalogue model', () => {
  it('builds its url from the document id', () => {
    const model = new Catalogue({ id: 'abc-123' })
    expect(model.url()).toBe('/documents/abc-123/catalogue')
    expect(model.urlRoot()).toBe('/documents/abc-123/catalogue')
  })
})

describe('CatalogueViewModel', () => {
  it('builds its url from the document id', () => {
    const model = new CatalogueViewModel({ id: 'abc-123' })
    expect(model.url()).toBe('/documents/abc-123/catalogue-view')
  })
})
