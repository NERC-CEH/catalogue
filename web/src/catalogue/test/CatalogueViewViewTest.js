import $ from 'jquery'
import CatalogueViewView from '../src/CatalogueApp/CatalogueViewView'
import CatalogueViewModel from '../src/CatalogueApp/CatalogueViewModel'
import { fakeServer, spy } from '../../test/support'

// The dialog that shares a record into other catalogues, opened from "Manage secondary catalogues" in a
// record's admin menu (startup/apps.js initCatalogueView), as an editor ticks and saves it (dri-one #473)
describe('Sharing a record into other catalogues', () => {
  let model, view

  const catalogues = [{ id: 'ukceh', title: 'UKCEH digital assets' }, { id: 'assist', title: 'About ASSIST' }]

  function open (attributes, offered = catalogues) {
    $(document.body).html('<div id="metadata"></div>')
    model = new CatalogueViewModel(attributes)
    model.catalogues = offered
    model.setRevision('r1')
    view = new CatalogueViewView({ el: '#metadata', model })
  }
  const box = id => view.$(`input[type=checkbox][value=${id}]`)

  beforeEach(() => {
    fakeServer.install()
    $('html').attr('data-catalogue', 'eidc')
  })

  afterEach(() => {
    fakeServer.uninstall()
    $('html').removeAttr('data-catalogue').removeData('catalogue')
    $('body').removeClass('edit-mode')
    $(document.body).empty()
  })

  it('offers each other catalogue, ticked where the record is shown already', () => {
    open({ id: 'abc', value: ['ukceh'] })

    expect(view.$('.form-check label').map((i, label) => label.textContent).get()).toEqual(['UKCEH digital assets', 'About ASSIST'])
    expect(box('ukceh').prop('checked')).toBeTrue()
    expect(box('assist').prop('checked')).toBeFalse()
  })

  it('reads a record shown in no other catalogue, which comes without a value', () => {
    open({ id: 'abc' })

    expect(view.$('input[type=checkbox]:checked').length).toBe(0)
  })

  it('says so when there is no other catalogue', () => {
    open({ id: 'abc' }, [])

    expect(view.$('.dataentry').text()).toBe('No other catalogues available.')
  })

  it('shows a catalogue\'s title as text', () => {
    open({ id: 'abc' }, [{ id: 'odd', title: 'Rivers <b>& lakes</b>' }])

    expect(view.$('.form-check label').text()).toBe('Rivers <b>& lakes</b>')
    expect(view.$('.form-check label b').length).toBe(0)
  })

  it('saves the catalogues ticked, against the revision it was opened at', () => {
    open({ id: 'abc', value: ['ukceh'] })

    box('assist').prop('checked', true).trigger('change')
    box('ukceh').prop('checked', false).trigger('change')
    expect(model.get('value')).toEqual(['assist'])
    view.$('#editorSave').trigger('click')

    const request = fakeServer.lastRequest()
    expect(request.method).toBe('PUT')
    expect(request.url).toBe('/documents/abc/catalogue-view')
    expect(JSON.parse(request.body).value).toEqual(['assist'])
    expect(request.headers['If-Match']).toBe('r1')
    const saved = spy('saved')
    model.on('sync', saved)
    request.respondWith({ status: 200, contentType: 'application/json', responseText: '{"id":"abc","value":["assist"]}', responseHeaders: { ETag: '"r2"' } })
    expect(saved).toHaveBeenCalled()
    expect(model.getRevision()).toBe('r2')
  })
})
