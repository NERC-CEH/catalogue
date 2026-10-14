/* global window */
import $ from 'jquery'
import Backbone from 'backbone'
import { load } from '../load'
import {
  APPS, editorLookup, initCatalogue, initCatalogueView, initClone, initDepositRequest, initEditor, initHubbub,
  initMetricsReport, initPermission, initSearch, initServiceAgreement, initSimpleUpload, initStudyAreaMap,
  navbarToggle, page, startApps, stickyHeader
} from '../apps'
import { fakeServer, spy } from '../../test/support'

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
    spyOn(Backbone.history, 'start')
  })

  afterEach(() => {
    fakeServer.uninstall()
    $(document.body).empty()
  })

  describe('startApps', () => {
    const app = (selector, start, router) => ({ selector, start, router })

    it('starts only the apps whose element is on the page, giving each its elements', async () => {
      pageWith('<div id="search"></div><a class="edit-control"></a><a class="edit-control"></a>')
      const search = spy('search')
      const editor = spy('editor')
      const metrics = spy('metrics')

      await startApps(document, [app('#search', search, true), app('.edit-control', editor), app('#metrics-report', metrics, true)])

      expect(search).toHaveBeenCalledTimes(1)
      expect(editor.calls.argsFor(0)[0].length).toBe(2)
      expect(metrics).not.toHaveBeenCalled()
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

    it('reports an app that failed to load, and starts history for the others', async () => {
      pageWith('<div id="search"></div>')
      spyOn(console, 'error')

      await startApps(document, [app('#search', () => Promise.reject(new Error('chunk failed')), true)])

      expect(console.error).toHaveBeenCalledWith(jasmine.objectContaining({ message: 'chunk failed' }))
      expect(Backbone.history.start).toHaveBeenCalled()
    })

    it('knows an app for each micro-app, each with a start function', () => {
      expect(APPS.map(each => each.selector)).toEqual([
        '.catalogue-control', '.catalogue-view-control', '.edit-control', '.nav-toggle', '.service-agreement',
        '.geometry-map', '.permission', '#search', '#simple-upload', '#document-upload', '#studyarea-map',
        '#metrics-report', '#deposit-request', '.clone-control'
      ])
      APPS.forEach(each => expect(each.start).withContext(each.selector).toEqual(jasmine.any(Function)))
    })
  })

  describe('the catalogue dialogs', () => {
    const catalogueApp = () => {
      const Model = recording(() => ({ setRevision: spy('setRevision') }))
      return { Model, View: recording(), linkHref: () => '/documents/abc/catalogue' }
    }

    it('moves a record: fetches it and the catalogues, then opens the dialog', async () => {
      pageWith('<a class="catalogue-control" href="#">Move</a>')
      const { Model, View, linkHref } = catalogueApp()
      spyOn(load, 'catalogueApp').and.resolveTo({ Catalogue: Model, CatalogueView: View, linkHref })
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
      spyOn(load, 'catalogueApp').and.resolveTo({ CatalogueViewModel: Model, CatalogueViewView: View, linkHref })
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
      const Model = recording(() => ({ setRevision: spy('setRevision') }))
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
      spyOn(load, 'editor').and.resolveTo(module)
      initEditor($('.edit-control'))

      $('.edit-control').trigger('click')
      await settle()

      expect(View.made[0][0].el).toBe('#search')
      expect(Model.made[0]).toEqual([null, jasmine.objectContaining({ mediaType: 'application/gemini+json' }), 'GEMINI_DOCUMENT'])
    })

    it('opens a saved record in the editor, fetched as its media type', async () => {
      pageWith('<div id="metadata" class="alert alert-danger missingResourceType"></div><a class="edit-control" data-document-type="GEMINI_DOCUMENT">Edit</a>')
      const { View, Model, module } = editorModule()
      spyOn(load, 'editor').and.resolveTo(module)
      initEditor($('.edit-control'))

      $('.edit-control').trigger('click')
      await settle()
      fakeServer.lastRequest().respondWith({ status: 200, contentType: 'application/json', responseText: '{"title":"Rainfall"}', responseHeaders: { ETag: '"r7"' } })

      expect(Model.made[0][0]).toEqual({ title: 'Rainfall' })
      expect(View.made[0][0].el).toBe('#metadata')
      expect(View.made[0][0].model.setRevision).toHaveBeenCalledWith('r7')
      expect($('#metadata').hasClass('missingResourceType')).toBeFalse()
    })
  })

  describe('the service agreement button', () => {
    beforeEach(() => spyOn(page, 'navigate'))

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
      spyOn(load, 'editor').and.resolveTo({ ServiceAgreementEditorView: View, ServiceAgreement: Model })
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
      pageWith('<span id="searchShareIcon"></span>')
      const createFacetSearch = spy('createFacetSearch')
      const initSearchShareIcon = spy('initSearchShareIcon')
      const SearchApp = recording()
      const SearchAppView = recording({ facetsPanelView: { createFacetSearch }, searchResultsView: { initSearchShareIcon } })
      const SearchRouter = recording()
      const MessageView = recording()
      spyOn(load, 'search').and.resolveTo({ SearchApp, SearchAppView, SearchRouter })
      spyOn(load, 'messageView').and.resolveTo(MessageView)

      await initSearch()

      const app = SearchAppView.made[0][0].model
      expect(app).toEqual(jasmine.any(SearchApp))
      expect(createFacetSearch).toHaveBeenCalledWith(app)
      expect(initSearchShareIcon.calls.argsFor(0)[0][0]).toBe($('#searchShareIcon')[0])
      expect(SearchRouter.made[0][0]).toEqual({ model: app, location: window.location })
      expect(MessageView.made[0][0].model).toBe(app)
    })

    it('permissions: the app, its view, router and messages', async () => {
      const PermissionApp = recording()
      const PermissionAppView = recording()
      const PermissionRouter = recording()
      const MessageView = recording()
      spyOn(load, 'permission').and.resolveTo({ PermissionApp, PermissionAppView, PermissionRouter })
      spyOn(load, 'messageView').and.resolveTo(MessageView)

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
      spyOn(load, 'metrics').and.resolveTo({ MetricsReportApp, MetricsReportForm, MetricsReportResults })
      spyOn(load, 'searchRouter').and.resolveTo(SearchRouter)

      await initMetricsReport()

      const app = MetricsReportForm.made[0][0].model
      expect(MetricsReportForm.made[0][0].el).toBe('#metrics-form')
      expect(MetricsReportResults.made[0][0]).toEqual({ el: '#metrics-table', model: app })
      expect(SearchRouter.made[0][0].model).toBe(app)
    })

    it('simple upload: to the dropzone\'s address', async () => {
      pageWith('<form id="simple-upload-dropzone" action="/upload/abc"></form>')
      const SimpleUploadView = recording()
      spyOn(load, 'simpleUpload').and.resolveTo({ SimpleUploadView })

      await initSimpleUpload()

      expect(SimpleUploadView.made[0][0]).toEqual({ el: '#simple-upload', url: '/upload/abc' })
    })

    it('Hubbub upload: for the dataset the page names', async () => {
      pageWith('<div id="document-upload" data-guid="abc"></div>')
      const UploadModel = recording()
      const UploadView = recording()
      spyOn(load, 'hubbub').and.resolveTo({ UploadModel, UploadView })

      await initHubbub($('#document-upload'))

      expect(UploadModel.made[0][0]).toEqual({ id: 'abc' })
      expect(UploadView.made[0][0].el).toBe('.document-upload')
    })

    it('deposit request: the form on a new request', async () => {
      const DepositRequestApp = recording()
      const DepositRequestFormView = recording()
      spyOn(load, 'depositRequest').and.resolveTo({ DepositRequestApp, DepositRequestFormView })

      await initDepositRequest()

      expect(DepositRequestFormView.made[0][0].el).toBe('#deposit-request')
      expect(DepositRequestFormView.made[0][0].model).toEqual(jasmine.any(DepositRequestApp))
    })

    it('maps: the study area view', async () => {
      const StudyAreaView = recording()
      spyOn(load, 'studyArea').and.resolveTo({ StudyAreaView })

      await initStudyAreaMap()

      expect(StudyAreaView.made[0][0]).toEqual({ el: '#studyarea-map' })
    })
  })

  describe('the page header', () => {
    it('opens and closes the navigation on small screens', () => {
      pageWith('<div class="navigation"></div><a class="nav-toggle"></a>')
      navbarToggle($('.nav-toggle'))

      $('.nav-toggle').trigger('click')
      expect($('.navigation').hasClass('reveal')).toBeTrue()
      expect($('.nav-toggle').hasClass('reveal')).toBeTrue()

      $('.nav-toggle').trigger('click')
      expect($('.navigation').hasClass('reveal')).toBeFalse()
    })

    it('shrinks once the page scrolls past it', () => {
      pageWith('<div id="header-sentinel"></div><header></header>')
      let report
      const observe = spy('observe')
      spyOn(window, 'IntersectionObserver').and.callFake(function (callback) {
        report = callback
        this.observe = observe
      })

      stickyHeader(document)
      report([{ isIntersecting: false }])
      expect(document.querySelector('header').classList.contains('stuck')).toBeTrue()
      report([{ isIntersecting: true }])

      expect(observe).toHaveBeenCalledWith(document.getElementById('header-sentinel'))
      expect(document.querySelector('header').classList.contains('stuck')).toBeFalse()
    })

    it('does nothing on a page without one', () => {
      pageWith('<div></div>')
      spyOn(window, 'IntersectionObserver')

      stickyHeader(document)

      expect(window.IntersectionObserver).not.toHaveBeenCalled()
    })
  })

  describe('copying a record', () => {
    function copyForm (answer) {
      pageWith('<form class="clone-control" action="/documents/abc/clone"></form>')
      const fire = spy('fire').and.resolveTo({ isConfirmed: answer })
      spyOn(load, 'swal').and.resolveTo({ fire })
      const form = $('.clone-control')[0]
      spyOn(form, 'submit')
      initClone($('.clone-control'))
      return { form, fire }
    }

    it('copies once the editor confirms', async () => {
      const { form, fire } = copyForm(true)

      $('.clone-control').trigger('submit')
      await settle()

      expect(fire).toHaveBeenCalledWith(jasmine.objectContaining({ title: 'Are you sure?' }))
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
