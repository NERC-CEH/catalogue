import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import $ from 'jquery'
import Backbone from 'backbone'
import { load } from '../load'
import {
  APPS, editorLookup, initCatalogue, initCatalogueView, initClone, initDepositRequest, initEditor, initHubbub,
  initMetricsReport, initPermission, initSearch, initServiceAgreement, initSimpleUpload, initStudyAreaMap,
  initWmsMap, navbarToggle, page, startApps, stickyHeader
} from '../apps'
import { fakeServer } from '../../test/support'

// What each page starts, run on a page of the spec's own with the micro-apps' code replaced by
// recording fakes, so nothing is fetched and nothing happens on import (NERC-CEH/dri-one#473)
describe('Page start-up', () => {
  // A class that records how it was constructed
  const recording = (extra = {}) => {
    const Fake = class {
      constructor (...args) {
        Fake.made.push(args)
        Object.assign(this, typeof extra === 'function' ? extra() : extra)
      }
    }
    Fake.made = []
    return Fake
  }
  const settle = () => new Promise(resolve => setTimeout(resolve, 0))
  const pageWith = html => $(document.body).html(html)

  beforeEach(() => {
    fakeServer.install()
    vi.spyOn(Backbone.history, 'start').mockImplementation(() => {})
  })

  afterEach(() => {
    fakeServer.uninstall()
    $(document.body).empty()
  })

  describe('startApps', () => {
    const app = (selector, start, router) => ({ selector, start, router })

    it('starts only the apps whose element is on the page, giving each its elements', async () => {
      pageWith('<div id="search"></div><a class="edit-control"></a><a class="edit-control"></a>')
      const search = vi.fn()
      const editor = vi.fn()
      const metrics = vi.fn()

      const report = await startApps(document, [app('#search', search, true), app('.edit-control', editor), app('#metrics-report', metrics, true)])

      expect(search).toHaveBeenCalledTimes(1)
      expect(editor.mock.calls[0][0].length).toBe(2)
      expect(metrics).not.toHaveBeenCalled()
      expect(report).toEqual({ started: ['#search', '.edit-control'], failed: [] })
    })

    it('starts each app as the page loads, not a turn later', () => {
      pageWith('<button class="nav-toggle"></button>')
      const toggle = vi.fn()

      startApps(document, [app('.nav-toggle', toggle)])

      expect(toggle).toHaveBeenCalled()
    })

    it('starts history only once the apps with routers have started', async () => {
      pageWith('<div id="search"></div>')
      let finish
      const started = startApps(document, [app('#search', () => new Promise(resolve => { finish = resolve }), true)])
      await settle()
      expect(Backbone.history.start).not.toHaveBeenCalled()

      finish()
      await started

      expect(Backbone.history.start).toHaveBeenCalled()
    })

    it('doesn\'t hold history back for an app without a router', async () => {
      pageWith('<div id="search"></div><a class="edit-control"></a>')
      let finish
      const started = startApps(document, [app('#search', vi.fn(), true), app('.edit-control', () => new Promise(resolve => { finish = resolve }))])
      await settle()

      expect(Backbone.history.start).toHaveBeenCalled()
      finish()
      expect(await started).toEqual({ started: ['#search', '.edit-control'], failed: [] })
    })

    it('reports by name an app that failed to load, and starts history for the others', async () => {
      pageWith('<div id="search"></div><div id="metrics-report"></div>')
      vi.spyOn(console, 'error').mockImplementation(() => {})
      const error = new Error('chunk failed')

      const report = await startApps(document, [app('#search', vi.fn(), true), app('#metrics-report', () => Promise.reject(error), true)])

      expect(report).toEqual({ started: ['#search'], failed: [{ selector: '#metrics-report', reason: error }] })
      expect(console.error).toHaveBeenCalledWith('#metrics-report failed to start:', error)
      expect(Backbone.history.start).toHaveBeenCalled()
    })

    it('reports an app that throws as it starts, and still starts the rest', async () => {
      pageWith('<div id="search"></div><button class="nav-toggle"></button>')
      vi.spyOn(console, 'error').mockImplementation(() => {})
      const error = new TypeError('no element')
      const search = vi.fn()

      const report = await startApps(document, [app('.nav-toggle', () => { throw error }), app('#search', search, true)])

      expect(search).toHaveBeenCalled()
      expect(report).toEqual({ started: ['#search'], failed: [{ selector: '.nav-toggle', reason: error }] })
      expect(Backbone.history.start).toHaveBeenCalled()
    })

    it('knows an app for each micro-app, each with a start function', () => {
      expect(APPS.map(each => each.selector)).toEqual([
        '.catalogue-control', '.catalogue-view-control', '.edit-control', '.nav-toggle', '.service-agreement',
        '.geometry-map', '.permission', '#search', '#simple-upload', '#document-upload', '#studyarea-map',
        '#metrics-report', '#deposit-request', '.clone-control', '.wms-map-preview'
      ])
      APPS.forEach(each => expect(each.start, each.selector).toEqual(expect.any(Function)))
    })
  })

  describe('the catalogue dialogs', () => {
    const catalogueApp = () => {
      const Model = recording(() => ({ setRevision: vi.fn() }))
      return { Model, View: recording(), linkHref: () => '/documents/abc/catalogue' }
    }

    it('moves a record: fetches it and the catalogues, then opens the dialog', async () => {
      pageWith('<a class="catalogue-control" href="#">Move</a>')
      const { Model, View, linkHref } = catalogueApp()
      vi.spyOn(load, 'catalogueApp').mockResolvedValue({ Catalogue: Model, CatalogueView: View, linkHref })
      initCatalogue($('.catalogue-control'))

      $('.catalogue-control').trigger('click')
      await settle()
      fakeServer.lastRequest().respondWith({ status: 200, contentType: 'application/json', responseText: '{"id":"abc"}', responseHeaders: { ETag: '"r1"' } })
      expect(fakeServer.lastRequest().url).toBe('/catalogues')
      fakeServer.lastRequest().respondWith({ status: 200, contentType: 'application/json', responseText: '[{"id":"eidc","title":"EIDC"}]' })

      const [[options]] = View.made
      expect(options.el).toBe('#metadata')
      expect(options.model.options).toEqual([{ value: 'eidc', label: 'EIDC' }])
      expect(options.model.setRevision).toHaveBeenCalledWith('r1')
    })

    it('changes a record\'s view: fetches it and its catalogues, then opens the dialog', async () => {
      pageWith('<a class="catalogue-view-control" href="#">Views</a>')
      const { Model, View, linkHref } = catalogueApp()
      vi.spyOn(load, 'catalogueApp').mockResolvedValue({ CatalogueViewModel: Model, CatalogueViewView: View, linkHref })
      initCatalogueView($('.catalogue-view-control'))

      $('.catalogue-view-control').trigger('click')
      await settle()
      fakeServer.lastRequest().respondWith({ status: 200, contentType: 'application/json', responseText: '{"id":"abc"}' })
      expect(fakeServer.lastRequest().url).toBe('/catalogues?identifier=abc')
      fakeServer.lastRequest().respondWith({ status: 200, contentType: 'application/json', responseText: '[{"id":"eidc"}]' })

      expect(View.made[0][0].model.catalogues).toEqual([{ id: 'eidc' }])
      expect(View.made[0][0].model.setRevision).not.toHaveBeenCalled()
    })
  })

  describe('the editor', () => {
    const editorModule = () => {
      const View = recording()
      const Model = recording(() => ({ setRevision: vi.fn() }))
      return { View, Model, module: { GeminiEditorView: View, GeminiEditorMetadata: Model } }
    }

    it('knows each record type\'s editor, model and media type', () => {
      const names = ['GeminiEditorView', 'GeminiEditorMetadata', 'EditorMetadata', 'LinkEditorMetadata', 'ServiceAgreement',
        'MonitoringEditorView', 'LinkEditorView', 'ServiceAgreementEditorView', 'DataTypeEditorView']
      const module = Object.fromEntries(names.map(name => [name, name]))

      const lookup = editorLookup(module)

      expect(lookup.GEMINI_DOCUMENT).toEqual({ View: 'GeminiEditorView', Model: 'GeminiEditorMetadata', mediaType: 'application/gemini+json' })
      expect(lookup.LINK_DOCUMENT).toEqual({ View: 'LinkEditorView', Model: 'LinkEditorMetadata', mediaType: 'application/link+json' })
      expect(lookup['service-agreement'].Model).toBe('ServiceAgreement')
      expect(lookup['data-type'].mediaType).toBe('application/vnd.data-type+json')
      Object.values(lookup).forEach(type => expect(type.mediaType).toMatch(/^application\//))
    })

    it('opens a new record in the editor, from the search page\'s create menu', async () => {
      pageWith('<div id="editorCreate"></div><a class="edit-control" data-document-type="GEMINI_DOCUMENT">New</a>')
      const { View, Model, module } = editorModule()
      vi.spyOn(load, 'editor').mockResolvedValue(module)
      initEditor($('.edit-control'))

      $('.edit-control').trigger('click')
      await settle()

      expect(View.made[0][0].el).toBe('#search')
      expect(Model.made[0]).toEqual([null, expect.objectContaining({ mediaType: 'application/gemini+json' }), 'GEMINI_DOCUMENT'])
    })

    it('opens a saved record in the editor, fetched as its media type', async () => {
      pageWith('<div id="metadata" class="alert alert-danger missingResourceType"></div><a class="edit-control" data-document-type="GEMINI_DOCUMENT">Edit</a>')
      const { View, Model, module } = editorModule()
      vi.spyOn(load, 'editor').mockResolvedValue(module)
      initEditor($('.edit-control'))

      $('.edit-control').trigger('click')
      await settle()
      fakeServer.lastRequest().respondWith({ status: 200, contentType: 'application/json', responseText: '{"title":"Rainfall"}', responseHeaders: { ETag: '"r7"' } })

      expect(Model.made[0][0]).toEqual({ title: 'Rainfall' })
      expect(View.made[0][0].el).toBe('#metadata')
      expect(View.made[0][0].model.setRevision).toHaveBeenCalledWith('r7')
      expect($('#metadata').hasClass('missingResourceType')).toBe(false)
    })

    it('is reported by start-up when its code fails to load, though nothing waits on it', async () => {
      pageWith('<a class="edit-control" data-document-type="GEMINI_DOCUMENT">Edit</a>')
      vi.spyOn(console, 'error').mockImplementation(() => {})
      const error = new Error('Loading chunk editor failed')
      vi.spyOn(load, 'editor').mockRejectedValue(error)

      const report = await startApps(document)

      expect(report.failed).toEqual([{ selector: '.edit-control', reason: error }])
      expect(Backbone.history.start).toHaveBeenCalled()
    })
  })

  describe('the service agreement button', () => {
    beforeEach(() => vi.spyOn(page, 'navigate').mockImplementation(() => {}))

    it('goes to an existing agreement', () => {
      pageWith('<div id="service-agreement-gemini"></div><a class="service-agreement" data-id="abc">Agreement</a>')
      initServiceAgreement($('.service-agreement'))

      $('.service-agreement').trigger('click')
      fakeServer.lastRequest().respondWith({ status: 200, contentType: 'text/html', responseText: '' })

      expect(fakeServer.lastRequest().url).toBe('/service-agreement/abc')
      expect(page.navigate).toHaveBeenCalledWith('/service-agreement/abc')
    })

    it('starts a new agreement in the editor when there is none', async () => {
      pageWith('<div id="service-agreement-gemini"></div><a class="service-agreement" data-id="abc">Agreement</a>')
      const View = recording()
      const Model = recording()
      vi.spyOn(load, 'editor').mockResolvedValue({ ServiceAgreementEditorView: View, ServiceAgreement: Model })
      initServiceAgreement($('.service-agreement'))

      $('.service-agreement').trigger('click')
      fakeServer.lastRequest().respondWith({ status: 404, contentType: 'text/html', responseText: '' })
      await settle()

      expect(Model.made[0]).toEqual([{ eidcContactDetails: 'info@eidc.ac.uk' }, { id: 'abc' }])
      expect(View.made[0][0].el).toBe('#metadata')
      expect(page.navigate).not.toHaveBeenCalled()
    })

    it('does nothing except on a record\'s page', () => {
      pageWith('<a class="service-agreement" data-id="abc">Agreement</a>')
      initServiceAgreement($('.service-agreement'))

      $('.service-agreement').trigger('click')

      expect(fakeServer.requestCount()).toBe(0)
    })
  })

  describe('the apps that set up routers', () => {
    it('search: the app, its view, facet search, share icon, router and messages', async () => {
      pageWith('<span id="searchShare"></span>')
      const createFacetSearch = vi.fn()
      const initSearchShare = vi.fn()
      const SearchApp = recording()
      const SearchAppView = recording({ facetsPanelView: { createFacetSearch }, searchResultsView: { initSearchShare } })
      const SearchRouter = recording()
      const MessageView = recording()
      vi.spyOn(load, 'search').mockResolvedValue({ SearchApp, SearchAppView, SearchRouter })
      vi.spyOn(load, 'messageView').mockResolvedValue(MessageView)

      await initSearch()

      const app = SearchAppView.made[0][0].model
      expect(app).toEqual(expect.any(SearchApp))
      expect(createFacetSearch).toHaveBeenCalledWith(app)
      expect(initSearchShare.mock.calls[0][0][0]).toBe($('#searchShare')[0])
      expect(SearchRouter.made[0][0]).toEqual({ model: app, location: window.location })
      expect(MessageView.made[0][0].model).toBe(app)
    })

    it('permissions: the app, its view, router and messages', async () => {
      const PermissionApp = recording()
      const PermissionAppView = recording()
      const PermissionRouter = recording()
      const MessageView = recording()
      vi.spyOn(load, 'permission').mockResolvedValue({ PermissionApp, PermissionAppView, PermissionRouter })
      vi.spyOn(load, 'messageView').mockResolvedValue(MessageView)

      await initPermission()

      const app = PermissionAppView.made[0][0].model
      expect(PermissionRouter.made[0][0].model).toBe(app)
      expect(MessageView.made[0][0].model).toBe(app)
    })

    it('metrics: the form, results and router on one report', async () => {
      const MetricsReportApp = recording()
      const MetricsReportForm = recording()
      const MetricsReportResults = recording()
      const SearchRouter = recording()
      vi.spyOn(load, 'metrics').mockResolvedValue({ MetricsReportApp, MetricsReportForm, MetricsReportResults })
      vi.spyOn(load, 'searchRouter').mockResolvedValue(SearchRouter)

      await initMetricsReport()

      const app = MetricsReportForm.made[0][0].model
      expect(MetricsReportForm.made[0][0].el).toBe('#metrics-form')
      expect(MetricsReportResults.made[0][0]).toEqual({ el: '#metrics-table', model: app })
      expect(SearchRouter.made[0][0].model).toBe(app)
    })

    it('simple upload: to the dropzone\'s address', async () => {
      pageWith('<form id="simple-upload-dropzone" action="/upload/abc"></form>')
      const SimpleUploadView = recording()
      vi.spyOn(load, 'simpleUpload').mockResolvedValue({ SimpleUploadView })

      await initSimpleUpload()

      expect(SimpleUploadView.made[0][0]).toEqual({ el: '#simple-upload', url: '/upload/abc' })
    })

    it('Hubbub upload: for the dataset the page names', async () => {
      pageWith('<div id="document-upload" data-guid="abc"></div>')
      const UploadModel = recording()
      const UploadView = recording()
      vi.spyOn(load, 'hubbub').mockResolvedValue({ UploadModel, UploadView })

      await initHubbub($('#document-upload'))

      expect(UploadModel.made[0][0]).toEqual({ id: 'abc' })
      expect(UploadView.made[0][0].el).toBe('.document-upload')
    })

    it('deposit request: the form on a new request', async () => {
      const DepositRequestApp = recording()
      const DepositRequestFormView = recording()
      vi.spyOn(load, 'depositRequest').mockResolvedValue({ DepositRequestApp, DepositRequestFormView })

      await initDepositRequest()

      expect(DepositRequestFormView.made[0][0].el).toBe('#deposit-request')
      expect(DepositRequestFormView.made[0][0].model).toEqual(expect.any(DepositRequestApp))
    })

    it('maps: the study area view', async () => {
      const StudyAreaView = recording()
      vi.spyOn(load, 'studyArea').mockResolvedValue({ StudyAreaView })

      await initStudyAreaMap()

      expect(StudyAreaView.made[0][0]).toEqual({ el: '#studyarea-map' })
    })

    it('maps: a WMS preview for each Map section', async () => {
      pageWith('<div class="wms-map-preview"></div>')
      const WmsMapView = recording()
      vi.spyOn(load, 'wmsMap').mockResolvedValue({ WmsMapView })

      await initWmsMap($('.wms-map-preview'))

      expect(WmsMapView.made[0][0]).toEqual({ el: $('.wms-map-preview')[0] })
    })
  })

  describe('the page header', () => {
    it('opens and closes the navigation on small screens', () => {
      pageWith('<div class="navigation"></div><a class="nav-toggle"></a>')
      navbarToggle($('.nav-toggle'))

      $('.nav-toggle').trigger('click')
      expect($('.navigation').hasClass('reveal')).toBe(true)
      expect($('.nav-toggle').hasClass('reveal')).toBe(true)

      $('.nav-toggle').trigger('click')
      expect($('.navigation').hasClass('reveal')).toBe(false)
    })

    it('shrinks once the page scrolls past it', () => {
      pageWith('<div id="header-sentinel"></div><header></header>')
      let report
      const observe = vi.fn()
      vi.spyOn(window, 'IntersectionObserver').mockImplementation(function (callback) {
        report = callback
        this.observe = observe
      })

      stickyHeader(document)
      report([{ isIntersecting: false }])
      expect(document.querySelector('header').classList.contains('stuck')).toBe(true)
      report([{ isIntersecting: true }])

      expect(observe).toHaveBeenCalledWith(document.getElementById('header-sentinel'))
      expect(document.querySelector('header').classList.contains('stuck')).toBe(false)
    })

    it('does nothing on a page without one', () => {
      pageWith('<div></div>')
      vi.spyOn(window, 'IntersectionObserver').mockImplementation(() => {})

      stickyHeader(document)

      expect(window.IntersectionObserver).not.toHaveBeenCalled()
    })
  })

  describe('copying a record', () => {
    function copyForm (answer) {
      pageWith('<form class="clone-control" action="/documents/abc/clone"></form>')
      const fire = vi.fn().mockResolvedValue({ isConfirmed: answer })
      vi.spyOn(load, 'swal').mockResolvedValue({ fire })
      const form = $('.clone-control')[0]
      vi.spyOn(form, 'submit').mockImplementation(() => {})
      initClone($('.clone-control'))
      return { form, fire }
    }

    it('copies once the editor confirms', async () => {
      const { form, fire } = copyForm(true)

      $('.clone-control').trigger('submit')
      await settle()

      expect(fire).toHaveBeenCalledWith(expect.objectContaining({ title: 'Are you sure?' }))
      expect(form.submit).toHaveBeenCalled()
    })

    it('does nothing when the editor cancels', async () => {
      const { form } = copyForm(false)

      $('.clone-control').trigger('submit')
      await settle()

      expect(form.submit).not.toHaveBeenCalled()
    })
  })
})
