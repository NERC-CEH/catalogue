import $ from 'jquery'
import EditorMetadata from './EditorMetadata'
import LinkEditorMetadata from './LinkEditorMetadata'
import {
  CehModelApplicationEditorView,
  CodeDocumentEditorView,
  DataTypeEditorView,
  ElterLinkedEditorView,
  LinkEditorView,
  ModelEditorView,
  MonitoringEditorView,
  NercModelUseEditorView,
  UkemsDocumentEditorView,
  OsdpAgentEditorView,
  OsdpModelEditorView,
  OsdpPublicationEditorView,
  OsdpSampleEditorView,
  ElterEditorView,
  ErammpDatacubeEditorView,
  NercModelEditorView,
  ErammpModelEditorView,
  SampleArchiveEditorView,
  OsdpMonitoringFacilityEditorView,
  OsdpMonitoringProgrammeEditorView,
  OsdpMonitoringActivityEditorView,
  OsdpDatasetEditorView,
  CehModelEditorView,
  ServiceAgreementEditorView,
  GeminiEditorView,
  InfrastructureRecordEditorView
} from './editors'
import { ServiceAgreement } from './models'

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
  'osdp-monitoring-activity': {
    View: OsdpMonitoringActivityEditorView,
    Model: EditorMetadata,
    mediaType: 'application/vnd.osdp.monitoring-activity+json'
  },
  'osdp-monitoring-programme': {
    View: OsdpMonitoringProgrammeEditorView,
    Model: EditorMetadata,
    mediaType: 'application/vnd.osdp.monitoring-programme+json'
  },
  'osdp-monitoring-facility': {
    View: OsdpMonitoringFacilityEditorView,
    Model: EditorMetadata,
    mediaType: 'application/vnd.osdp.monitoring-facility+json'
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

if ($('.edit-control').length) { initEditor() }
if ($('.service-agreement').length) { initServiceAgreement() }

function initEditor () {
// the create document dropdown
  const $editorCreate = $('#editorCreate')

  $('.edit-control').on('click', function (event) {
    event.preventDefault()

    const title = $(event.target).data('documentType')
    const documentType = lookup[title]

    if ($editorCreate.length) {
      return new documentType.View({
        model: new documentType.Model(null, documentType, title),
        el: '#search'
      })
    } else {
      $.ajax({
        url: $(location).attr('href'),
        dataType: 'json',
        accepts: {
          json: documentType.mediaType
        },
        success (data) {
          // eslint-disable-next-line no-unused-vars
          const document = new documentType.View({
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

  $('.service-agreement').on('click', function (event) {
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
          // eslint-disable-next-line no-unused-vars
          const serviceAgreement = new ServiceAgreementEditorView({
            el: '#metadata',
            model: new ServiceAgreement(data, options)
          })
        }
      })
    }
  })
}
