/* global window IntersectionObserver */
/* eslint no-new: "off" */
import $ from 'jquery'
import Backbone from 'backbone'
import 'bootstrap'

// Every page loads this file, so it holds only what every page needs. Each micro-app is a chunk of
// its own, fetched by import() only on a page that has it: a public record page doesn't download
// the editor, the maps or the upload tables (NERC-CEH/dri-one#364).
const editor = () => Promise.all([
  import(/* webpackChunkName: "editor" */ './editor/src/editors'),
  import(/* webpackChunkName: "editor" */ './editor/src'),
  import(/* webpackChunkName: "editor" */ './editor/src/models')
]).then(([editors, src, models]) => ({ ...editors, ...src, ...models }))
const catalogueApp = () => import(/* webpackChunkName: "catalogue" */ './catalogue/src/CatalogueApp')
const studyArea = () => import(/* webpackChunkName: "study-area" */ './study-area/src/View')
const messageView = () => import(/* webpackChunkName: "message" */ './search/src/views/MessageView')
const swal = () => import(/* webpackChunkName: "sweetalert" */ 'sweetalert2')

const $catalogue = $('.catalogue-control')
const $catalogueView = $('.catalogue-view-control')
const $documentUpload = $('#document-upload')
const $edit = $('.edit-control')
const $serviceAgreement = $('.service-agreement')
const $navbarToggle = $('.nav-toggle')
const $clone = $('.clone-control')

// Each micro-app that sets up a Backbone router adds its start-up here, and history starts once they
// have all run, as it did when they were all in this file
const apps = []

if ($catalogue.length) {
  initCatalogue()
}

if ($catalogueView.length) {
  initCatalogueView()
}

if ($edit.length) {
  initEditor()
}

if ($navbarToggle.length) {
  navbarToggle($navbarToggle)
}

if ($serviceAgreement.length) {
  initServiceAgreement()
}

if ($('.geometry-map').length) {
  apps.push(initGeometryMap())
}

if ($('.permission').length) {
  apps.push(initPermission())
}

if ($('#search').length) {
  apps.push(initSearch())
}

if ($('#simple-upload').length) {
  apps.push(initSimpleUpload())
}

if ($documentUpload.length) {
  apps.push(initHubbub())
}

if ($('#studyarea-map').length) {
  apps.push(initStudyAreaMap())
}

if ($('#metrics-report').length) {
  apps.push(initMetricsReport())
}

if ($('#deposit-request').length) {
  apps.push(initDepositRequest())
}

if ($clone.length) {
  initClone()
}

// A micro-app that fails to load is reported, and doesn't stop history starting for the others
Promise.allSettled(apps).then(results => {
  results.filter(result => result.status === 'rejected').forEach(result => console.error(result.reason))
  Backbone.history.start()
})
stickyHeader()

/* Initialize the catalogue application */
function initCatalogue () {
  $catalogue.on('click', function (event) {
    event.preventDefault()
    catalogueApp().then(({ Catalogue, CatalogueView, linkHref }) => {
      $.getJSON(linkHref(event), function (data, textStatus, jqXHR) {
        const model = new Catalogue(data)
        const etag = jqXHR.getResponseHeader('ETag')
        if (etag) {
          model.setRevision(etag.replace(/^"|"$/g, ''))
        }
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

function initCatalogueView () {
  $catalogueView.on('click', function (event) {
    event.preventDefault()
    catalogueApp().then(({ CatalogueViewModel, CatalogueViewView, linkHref }) => {
      $.getJSON(linkHref(event), function (data, textStatus, jqXHR) {
        const model = new CatalogueViewModel(data)
        const etag = jqXHR.getResponseHeader('ETag')
        if (etag) {
          model.setRevision(etag.replace(/^"|"$/g, ''))
        }
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

function editorLookup ({
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

function initEditor () {
  // Fetched now rather than on the click, so that the editor opens as promptly as it used to
  const lookup = editor().then(editorLookup)

  // the create document dropdown
  const $editorCreate = $('#editorCreate')

  $edit.on('click', function (event) {
    event.preventDefault()

    const title = $(event.target).data('documentType')

    lookup.then(lookup => {
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
            const model = new documentType.Model(data, documentType, title)
            const etag = jqXHR.getResponseHeader('ETag')
            if (etag) {
              model.setRevision(etag.replace(/^"|"$/g, ''))
            }
            new documentType.View({
              model,
              el: '#metadata'
            })
          }
        })
      }
    })
  })
}

function initServiceAgreement () {
  const $gemini = $('#service-agreement-gemini')

  $serviceAgreement.on('click', function (event) {
    event.preventDefault()
    const id = $(event.currentTarget).data('id')
    const data = { eidcContactDetails: 'info@eidc.ac.uk' }
    const options = { id }

    if ($gemini.length) {
      $.ajax({
        url: `/service-agreement/${id}`,
        type: 'GET',
        success () {
          window.location.href = `/service-agreement/${id}`
        },
        error () {
          editor().then(({ ServiceAgreementEditorView, ServiceAgreement }) => {
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

async function initGeometryMap () {
  const { StudyAreaView } = await studyArea()
  new StudyAreaView({ el: '#studyarea-map' })
}

async function initPermission () {
  const [{ PermissionApp, PermissionAppView, PermissionRouter }, { default: MessageView }] = await Promise.all([
    import(/* webpackChunkName: "permission" */ './permission/src/PermissionApp'),
    messageView()
  ])
  const app = new PermissionApp()
  new PermissionAppView({ model: app })
  new PermissionRouter({ model: app })
  new MessageView({ model: app })
}

async function initSearch () {
  const [{ default: SearchApp }, { default: SearchAppView }, { default: SearchRouter }, { default: MessageView }] = await Promise.all([
    import(/* webpackChunkName: "search" */ './search/src/SearchApp'),
    import(/* webpackChunkName: "search" */ './search/src/SearchAppView'),
    import(/* webpackChunkName: "search" */ './search/src/SearchRouter'),
    messageView()
  ])
  const app = new SearchApp()
  const searchAppView = new SearchAppView({ model: app })
  searchAppView.facetsPanelView.createFacetSearch(app)
  searchAppView.searchResultsView.initSearchShareIcon($('#searchShareIcon'))
  new SearchRouter({ model: app, location: window.location })
  new MessageView({ model: app })
}

async function initSimpleUpload () {
  const { SimpleUploadView } = await import(/* webpackChunkName: "simple-upload" */ './simple-upload/src/App')
  const url = $('#simple-upload-dropzone').attr('action')
  new SimpleUploadView({
    el: '#simple-upload',
    url
  })
}

async function initHubbub () {
  const { UploadModel, UploadView } = await import(/* webpackChunkName: "hubbub" */ './hubbub/src/Upload')
  const id = $documentUpload.data('guid')
  new UploadView({
    el: '.document-upload',
    model: new UploadModel({ id })
  })
}

async function initStudyAreaMap () {
  const { StudyAreaView } = await studyArea()
  new StudyAreaView({ el: '#studyarea-map' })
}

function navbarToggle ($navbarItems) {
  $($navbarItems[0]).on('click', () => {
    $('.navigation').toggleClass('reveal')
    $('.nav-toggle').toggleClass('reveal')
  })
}

async function initMetricsReport () {
  const [{ MetricsReportApp, MetricsReportForm, MetricsReportResults }, { default: SearchRouter }] = await Promise.all([
    import(/* webpackChunkName: "metrics" */ './metrics/src'),
    import(/* webpackChunkName: "search" */ './search/src/SearchRouter')
  ])
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

async function initDepositRequest () {
  const [{ default: DepositRequestApp }, { default: DepositRequestFormView }] = await Promise.all([
    import(/* webpackChunkName: "deposit-request" */ './deposit-request/src/model/DepositRequestApp'),
    import(/* webpackChunkName: "deposit-request" */ './deposit-request/src/view/DepositRequestFormView')
  ])
  const app = new DepositRequestApp()
  new DepositRequestFormView({
    el: '#deposit-request',
    model: app
  })
}

function stickyHeader () {
  const sentinel = document.getElementById('header-sentinel')
  const header = document.querySelector('header')
  if (!sentinel || !header) return
  new IntersectionObserver(([entry]) => {
    header.classList.toggle('stuck', !entry.isIntersecting)
  }).observe(sentinel)
}

function initClone () {
  $('.clone-control').on('submit', function (event) {
    event.preventDefault()
    const form = event.currentTarget

    swal().then(({ default: Swal }) => Swal.fire({
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
