/* global window */
/* eslint no-new: "off" */
import './globals'
import $ from 'jquery'
import Backbone from 'backbone'
import 'bootstrap'
import {
  CehModelApplicationEditorView,
  CehModelEditorView, CodeDocumentEditorView,
  DataTypeEditorView,
  ElterEditorView,
  ElterLinkedEditorView,
  ErammpDatacubeEditorView,
  ErammpModelEditorView,
  GeminiEditorView, InfrastructureRecordEditorView,
  LinkEditorView,
  ModelEditorView,
  MonitoringEditorView,
  MonitoringActivityEditorView,
  MonitoringFacilityEditorView,
  MonitoringNetworkEditorView,
  MonitoringProgrammeEditorView,
  NercModelEditorView,
  NercModelUseEditorView,
  OsdpAgentEditorView,
  OsdpDatasetEditorView,
  OsdpModelEditorView,
  OsdpPublicationEditorView,
  OsdpSampleEditorView,
  SampleArchiveEditorView, ServiceAgreementEditorView, UkemsDocumentEditorView
} from './editor/src/editors'
import { EditorMetadata } from './editor/src'
import LinkEditorMetadata from './editor/src/LinkEditorMetadata'
import { Catalogue, CatalogueView } from './catalogue/src/CatalogueApp'
import { StudyAreaView } from './study-area/src/View'
import { PermissionApp, PermissionAppView, PermissionRouter } from './permission/src/PermissionApp'
import SearchApp from './search/src/SearchApp'
import SearchAppView from './search/src/SearchAppView'
import SearchRouter from './search/src/SearchRouter'
import { SimpleUploadView } from './simple-upload/src/App'
import { MessageView } from './search/src/views'
import { ServiceAgreement } from './editor/src/models'
import { UploadModel, UploadView } from './hubbub/src/Upload'

const $catalogue = $('.catalogue-control')
const $documentUpload = $('#document-upload')
const $edit = $('.edit-control')
const $serviceAgreement = $('.service-agreement')
const $navbarToggle = $('.nav-toggle')
const $accordionToggle = $('.accordion-item')

if ($catalogue.length) {
  initCatalogue()
}

if ($edit.length) {
  initEditor()
}

if ($navbarToggle.length) {
  navbarToggle($navbarToggle)
}

if ($accordionToggle.length) {
  accordionToggle($accordionToggle)
}

if ($serviceAgreement.length) {
  initServiceAgreement()
}

if ($('#geometry-map').length) {
  initGeometryMap()
}

if ($('.permission').length) {
  initPermission()
}

if ($('#search').length) {
  initSearch()
}

if ($('#simple-upload').length) {
  initSimpleUpload()
}

if ($documentUpload.length) {
  initHubbub()
}

if ($('#studyarea-map').length) {
  initStudyAreaMap()
}

Backbone.history.start()

/* Initialize the catalogue application */
function initCatalogue () {
  $catalogue.on('click', function (event) {
    event.preventDefault()
    $.getJSON($(event.target).attr('href'), function (data) {
      const model = new Catalogue(data)
      $.getJSON('/catalogues', function (data) {
        model.options = data.map(val => ({ value: val.id, label: val.title }))
        new CatalogueView({
          el: '#metadata',
          model
        })
      })
    })
  })
}

function initEditor () {
  const lookup = {
    GEMINI_DOCUMENT: {
      View: GeminiEditorView,
      Model: EditorMetadata,
      mediaType: 'application/gemini+json'
    },
    EF_DOCUMENT: {
      View: MonitoringEditorView,
      Model: EditorMetadata,
      mediaType: 'application/monitoring+json'
    },
    IMP_DOCUMENT: {
      View: ModelEditorView,
      Model: EditorMetadata,
      mediaType: 'application/model+json'
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
    'osdp-agent': {
      View: OsdpAgentEditorView,
      Model: EditorMetadata,
      mediaType: 'application/vnd.osdp.agent+json'
    },
    'osdp-dataset': {
      View: OsdpDatasetEditorView,
      Model: EditorMetadata,
      mediaType: 'application/vnd.osdp.dataset+json'
    },
    'osdp-model': {
      View: OsdpModelEditorView,
      Model: EditorMetadata,
      mediaType: 'application/vnd.osdp.model+json'
    },
    'osdp-sample': {
      View: OsdpSampleEditorView,
      Model: EditorMetadata,
      mediaType: 'application/vnd.osdp.sample+json'
    },
    'osdp-publication': {
      View: OsdpPublicationEditorView,
      Model: EditorMetadata,
      mediaType: 'application/vnd.osdp.publication+json'
    },
    'sample-archive': {
      View: SampleArchiveEditorView,
      Model: EditorMetadata,
      mediaType: 'application/vnd.sample-archive+json'
    },
    'erammp-model': {
      View: ErammpModelEditorView,
      Model: EditorMetadata,
      mediaType: 'application/vnd.erammp-model+json'
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
    'erammp-datacube': {
      View: ErammpDatacubeEditorView,
      Model: EditorMetadata,
      mediaType: 'application/vnd.erammp-datacube+json'
    },
    'data-type': {
      View: DataTypeEditorView,
      Model: EditorMetadata,
      mediaType: 'application/vnd.data-type+json'
    },
    elter: {
      View: ElterEditorView,
      Model: EditorMetadata,
      mediaType: 'application/vnd.elter+json'
    },
    'linked-elter': {
      View: ElterLinkedEditorView,
      Model: EditorMetadata,
      mediaType: 'application/vnd.linked-elter+json'
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
    }
  }

  // the create document dropdown
  const $editorCreate = $('#editorCreate')

  $edit.on('click', function (event) {
    event.preventDefault()

    const title = $(event.target).data('documentType')
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
        success (data) {
          $('#metadata').removeClass('alert alert-danger missingResourceType')
          new documentType.View({
            model: new documentType.Model(data, documentType, title),
            el: '#metadata'
          })
        }
      })
    }
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
          new ServiceAgreementEditorView({
            el: '#metadata',
            model: new ServiceAgreement(data, options)
          })
        }
      })
    }
  })
}

function initGeometryMap () {
  new StudyAreaView({ el: '#studyarea-map' })
}

function initPermission () {
  const app = new PermissionApp()
  new PermissionAppView({ model: app })
  new PermissionRouter({ model: app })

  return createMessageViewFor(app)
}

function initSearch () {
  const app = new SearchApp()
  new SearchAppView({ model: app })
  new SearchRouter({ model: app, location: window.location })
  new MessageView({ model: app })
}

function initSimpleUpload () {
  const url = $('#simple-upload-dropzone').attr('action')
  new SimpleUploadView({
    el: '#simple-upload',
    url
  })
}

function initHubbub () {
  const id = $documentUpload.data('guid')
  new UploadView({
    el: '.document-upload',
    model: new UploadModel({ id })
  })
}

function initStudyAreaMap () {
  new StudyAreaView({ el: '#studyarea-map' })
}

/* Create a message view. Which listens to the supplied app model for messages (errors, info) */

function createMessageViewFor (app) {
  new MessageView({ model: app })
}

function navbarToggle ($navbarItems) {
  $($navbarItems[0]).on('click', () => {
    $('.navigation').toggleClass('reveal')
    $('.nav-toggle').toggleClass('reveal')
  })
}

function accordionToggle ($accordionItems) {
  $accordionItems.on('click', (e) => {
    const $currentTarget = $(e.currentTarget)
    if (!$currentTarget.hasClass('active')) {
      $('.accordion-item').removeClass('active')
      $currentTarget.toggleClass('active')
    }
  })
}
