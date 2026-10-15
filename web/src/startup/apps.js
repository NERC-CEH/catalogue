/* global window IntersectionObserver */
/* eslint no-new: "off" */
// What each page starts: a table of the element that marks a micro-app, and the function that starts
// it. index.js runs it for the page; the specs run it on a page of their own, with the loaders
// replaced, so none of this has to happen on import (NERC-CEH/dri-one#473).
import $ from 'jquery'
import Backbone from 'backbone'
import { load } from './load'

// Leaving the page, in one place, so a spec can see where to without going there
export const page = {
  navigate (url) { window.location.assign(url) }
}

const withRevision = (model, jqXHR) => {
  const etag = jqXHR.getResponseHeader('ETag')
  if (etag) {
    model.setRevision(etag.replace(/^"|"$/g, ''))
  }
  return model
}

export function editorLookup ({
  CehModelApplicationEditorView,
  CehModelEditorView, CodeDocumentEditorView,
  DataTypeEditorView,
  GeminiEditorView, InfrastructureRecordEditorView, ResearchActivityEditorView,
  LinkEditorView,
  MonitoringEditorView,
  MonitoringActivityEditorView,
  MonitoringFacilityEditorView,
  MonitoringNetworkEditorView,
  MonitoringProgrammeEditorView,
  NercModelEditorView,
  NercModelUseEditorView,
  SampleArchiveEditorView, ServiceAgreementEditorView, UkemsDocumentEditorView,
  EditorMetadata, LinkEditorMetadata, GeminiEditorMetadata, ServiceAgreement
}) {
  return {
    GEMINI_DOCUMENT: {
      View: GeminiEditorView,
      Model: GeminiEditorMetadata,
      mediaType: 'application/gemini+json'
    },
    EF_DOCUMENT: {
      View: MonitoringEditorView,
      Model: EditorMetadata,
      mediaType: 'application/monitoring+json'
    },
    CEH_MODEL: {
      View: CehModelEditorView,
      Model: EditorMetadata,
      mediaType: 'application/vnd.ceh.model+json'
    },
    CEH_MODEL_APPLICATION: {
      View: CehModelApplicationEditorView,
      Model: EditorMetadata,
      mediaType: 'application/vnd.ceh.model.application+json'
    },
    LINK_DOCUMENT: {
      View: LinkEditorView,
      Model: LinkEditorMetadata,
      mediaType: 'application/link+json'
    },
    'monitoring-activity': {
      View: MonitoringActivityEditorView,
      Model: EditorMetadata,
      mediaType: 'application/vnd.monitoring-activity+json'
    },
    'monitoring-programme': {
      View: MonitoringProgrammeEditorView,
      Model: EditorMetadata,
      mediaType: 'application/vnd.monitoring-programme+json'
    },
    'monitoring-facility': {
      View: MonitoringFacilityEditorView,
      Model: EditorMetadata,
      mediaType: 'application/vnd.monitoring-facility+json'
    },
    'monitoring-network': {
      View: MonitoringNetworkEditorView,
      Model: EditorMetadata,
      mediaType: 'application/vnd.monitoring-network+json'
    },
    'sample-archive': {
      View: SampleArchiveEditorView,
      Model: EditorMetadata,
      mediaType: 'application/vnd.sample-archive+json'
    },
    'nerc-model': {
      View: NercModelEditorView,
      Model: EditorMetadata,
      mediaType: 'application/vnd.nerc-model+json'
    },
    'nerc-model-use': {
      View: NercModelUseEditorView,
      Model: EditorMetadata,
      mediaType: 'application/vnd.nerc-model-use+json'
    },
    'data-type': {
      View: DataTypeEditorView,
      Model: EditorMetadata,
      mediaType: 'application/vnd.data-type+json'
    },
    'service-agreement': {
      View: ServiceAgreementEditorView,
      Model: ServiceAgreement,
      mediaType: 'application/json'
    },
    'ukems-document': {
      View: UkemsDocumentEditorView,
      Model: EditorMetadata,
      mediaType: 'application/vnd.ukems-document+json'
    },
    'code-document': {
      View: CodeDocumentEditorView,
      Model: EditorMetadata,
      mediaType: 'application/vnd.code-document+json'
    },
    infrastructurerecord: {
      View: InfrastructureRecordEditorView,
      Model: EditorMetadata,
      mediaType: 'application/vnd.infrastructure+json'
    },
    researchactivity: {
      View: ResearchActivityEditorView,
      Model: EditorMetadata,
      mediaType: 'application/vnd.researchactivity+json'
    }
  }
}

export function initCatalogue ($controls) {
  $controls.on('click', function (event) {
    event.preventDefault()
    load.catalogueApp().then(({ Catalogue, CatalogueView, linkHref }) => {
      $.getJSON(linkHref(event), function (data, textStatus, jqXHR) {
        const model = withRevision(new Catalogue(data), jqXHR)
        $.getJSON('/catalogues', function (data) {
          model.options = data.map(val => ({ value: val.id, label: val.title }))
          new CatalogueView({
            el: '#metadata',
            model
          })
        })
      })
    })
  })
}

export function initCatalogueView ($controls) {
  $controls.on('click', function (event) {
    event.preventDefault()
    load.catalogueApp().then(({ CatalogueViewModel, CatalogueViewView, linkHref }) => {
      $.getJSON(linkHref(event), function (data, textStatus, jqXHR) {
        const model = withRevision(new CatalogueViewModel(data), jqXHR)
        $.getJSON(`/catalogues?identifier=${data.id}`, function (catalogues) {
          model.catalogues = catalogues
          new CatalogueViewView({
            el: '#metadata',
            model
          })
        })
      })
    })
  })
}

export function initEditor ($controls) {
  // Fetched now rather than on the click, so that the editor opens as promptly as it used to
  const lookup = load.editor().then(editorLookup)

  // the create document dropdown
  const $editorCreate = $('#editorCreate')

  $controls.on('click', function (event) {
    event.preventDefault()

    const title = $(event.target).data('documentType')

    return lookup.then(lookup => {
      const documentType = lookup[title]

      if ($editorCreate.length) {
        new documentType.View({
          model: new documentType.Model(null, documentType, title),
          el: '#search'
        })
      } else {
        $.ajax({
          url: $(window.location).attr('href'),
          dataType: 'json',
          accepts: {
            json: documentType.mediaType
          },
          success (data, textStatus, jqXHR) {
            $('#metadata').removeClass('alert alert-danger missingResourceType')
            new documentType.View({
              model: withRevision(new documentType.Model(data, documentType, title), jqXHR),
              el: '#metadata'
            })
          }
        })
      }
    })
  })
  // The editor's code, so that start-up reports it if it fails to load
  return lookup
}

export function initServiceAgreement ($controls) {
  const $gemini = $('#service-agreement-gemini')

  $controls.on('click', function (event) {
    event.preventDefault()
    const id = $(event.currentTarget).data('id')
    const data = { eidcContactDetails: 'info@eidc.ac.uk' }
    const options = { id }

    if ($gemini.length) {
      $.ajax({
        url: `/service-agreement/${id}`,
        type: 'GET',
        success () {
          page.navigate(`/service-agreement/${id}`)
        },
        error () {
          load.editor().then(({ ServiceAgreementEditorView, ServiceAgreement }) => {
            new ServiceAgreementEditorView({
              el: '#metadata',
              model: new ServiceAgreement(data, options)
            })
          })
        }
      })
    }
  })
}

// The record page's map (.geometry-map) and the study area map draw the same view
export async function initStudyAreaMap () {
  const { StudyAreaView } = await load.studyArea()
  new StudyAreaView({ el: '#studyarea-map' })
}

export async function initPermission () {
  const [{ PermissionApp, PermissionAppView, PermissionRouter }, MessageView] = await Promise.all([load.permission(), load.messageView()])
  const app = new PermissionApp()
  new PermissionAppView({ model: app })
  new PermissionRouter({ model: app })
  new MessageView({ model: app })
}

export async function initSearch () {
  const [{ SearchApp, SearchAppView, SearchRouter }, MessageView] = await Promise.all([load.search(), load.messageView()])
  const app = new SearchApp()
  const searchAppView = new SearchAppView({ model: app })
  searchAppView.facetsPanelView.createFacetSearch(app)
  searchAppView.searchResultsView.initSearchShare($('#searchShare'))
  new SearchRouter({ model: app, location: window.location })
  new MessageView({ model: app })
}

export async function initSimpleUpload () {
  const { SimpleUploadView } = await load.simpleUpload()
  new SimpleUploadView({
    el: '#simple-upload',
    url: $('#simple-upload-dropzone').attr('action')
  })
}

export async function initHubbub ($upload) {
  const { UploadModel, UploadView } = await load.hubbub()
  new UploadView({
    el: '.document-upload',
    model: new UploadModel({ id: $upload.data('guid') })
  })
}

export async function initMetricsReport () {
  const [{ MetricsReportApp, MetricsReportForm, MetricsReportResults }, SearchRouter] = await Promise.all([load.metrics(), load.searchRouter()])
  const app = new MetricsReportApp()
  new MetricsReportForm({
    el: '#metrics-form',
    model: app
  })
  new MetricsReportResults({
    el: '#metrics-table',
    model: app
  })
  new SearchRouter({ model: app, location: window.location })
}

export async function initDepositRequest () {
  const { DepositRequestApp, DepositRequestFormView } = await load.depositRequest()
  new DepositRequestFormView({
    el: '#deposit-request',
    model: new DepositRequestApp()
  })
}

export function navbarToggle ($toggle) {
  $toggle.first().on('click', () => {
    $('.navigation').toggleClass('reveal')
    $('.nav-toggle').toggleClass('reveal')
  })
}

export function initClone ($forms) {
  $forms.on('submit', function (event) {
    event.preventDefault()
    const form = event.currentTarget

    return load.swal().then(Swal => Swal.fire({
      title: 'Are you sure?',
      text: 'This will create a copy of the record and save it as a draft',
      icon: 'warning',
      showCancelButton: true,
      confirmButtonText: 'Yes, copy it',
      cancelButtonText: 'Cancel'
    })).then((result) => {
      if (result.isConfirmed) {
        form.submit()
      }
    })
  })
}

export function stickyHeader (root = document) {
  const sentinel = root.getElementById ? root.getElementById('header-sentinel') : root.querySelector('#header-sentinel')
  const header = root.querySelector('header')
  if (!sentinel || !header) return
  new IntersectionObserver(([entry]) => {
    header.classList.toggle('stuck', !entry.isIntersecting)
  }).observe(sentinel)
}

// In the order the page used to run them. `router`: the app sets up a Backbone router, so history
// starts only once it has (or has failed to load).
export const APPS = [
  { selector: '.catalogue-control', start: initCatalogue },
  { selector: '.catalogue-view-control', start: initCatalogueView },
  { selector: '.edit-control', start: initEditor },
  { selector: '.nav-toggle', start: navbarToggle },
  { selector: '.service-agreement', start: initServiceAgreement },
  { selector: '.geometry-map', start: initStudyAreaMap, router: true },
  { selector: '.permission', start: initPermission, router: true },
  { selector: '#search', start: initSearch, router: true },
  { selector: '#simple-upload', start: initSimpleUpload, router: true },
  { selector: '#document-upload', start: initHubbub, router: true },
  { selector: '#studyarea-map', start: initStudyAreaMap, router: true },
  { selector: '#metrics-report', start: initMetricsReport, router: true },
  { selector: '#deposit-request', start: initDepositRequest, router: true },
  { selector: '.clone-control', start: initClone }
]

// Starts each app whose element is on the page, then Backbone history once the apps with routers have
// started. An app that fails to load is reported by name, and doesn't stop history starting for the
// others. Resolves, once every app has finished starting, to which of those on the page started and
// which failed: { started: ['#search'], failed: [{ selector: '#metrics-report', reason }] }. An app that
// loads its code only when clicked (the catalogue controls, clone) counts as started once its handler
// is in place; a failure on a later click isn't reported here.
export function startApps (root = document, apps = APPS) {
  const starting = []
  for (const { selector, start, router } of apps) {
    const $found = $(selector, root)
    if (!$found.length) continue
    let started
    try {
      started = Promise.resolve(start($found))
    } catch (error) {
      // An app that throws as it starts is reported like one whose code failed to load
      started = Promise.reject(error)
    }
    starting.push({ selector, router, started })
  }
  stickyHeader(root)

  const routers = starting.filter(app => app.router).map(app => app.started)
  const history = Promise.allSettled(routers).then(() => Backbone.history.start())
  const report = Promise.allSettled(starting.map(app => app.started)).then(results => {
    const failed = results.flatMap((result, i) =>
      result.status === 'rejected' ? [{ selector: starting[i].selector, reason: result.reason }] : [])
    failed.forEach(({ selector, reason }) => console.error(`${selector} failed to start:`, reason))
    return {
      started: starting.map(app => app.selector).filter(selector => !failed.some(each => each.selector === selector)),
      failed
    }
  })
  return Promise.all([history, report]).then(([, result]) => result)
}
