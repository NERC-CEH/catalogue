/*
 * decaffeinate suggestions:
 * DS102: Remove unnecessary code created because of implicit returns
 * DS207: Consider shorter variations of null checks
 * Full docs: https://github.com/decaffeinate/decaffeinate/blob/master/docs/suggestions.md
 */
define([
  'underscore',
  'jquery',
  'backbone',
  'dropzone',
  'bootstrap',
  'cs!views/StudyAreaView',
  'cs!models/MapViewerApp',
  'cs!views/MapViewerAppView',
  'cs!models/SearchApp',
  'cs!views/SearchAppView',
  'cs!views/MessageView',
  'cs!routers/LayersRouter',
  'cs!routers/SearchRouter',
  'cs!models/EditorMetadata',
  'cs!views/GeminiEditorView',
  'cs!views/MonitoringEditorView',
  'cs!models/PermissionApp',
  'cs!routers/PermissionRouter',
  'cs!views/PermissionAppView',
  'cs!models/Catalogue',
  'cs!views/CatalogueView',
  'cs!views/ChartView',
  'cs!views/ModelEditorView',
  'cs!views/LinkEditorView',
  'cs!models/LinkEditorMetadata',
  'cs!views/CehModelEditorView',
  'cs!views/CehModelApplicationEditorView',
  'cs!views/OsdpAgentEditorView',
  'cs!views/OsdpDatasetEditorView',
  'cs!views/OsdpModelEditorView',
  'cs!views/OsdpSampleEditorView',
  'cs!views/OsdpPublicationEditorView',
  'cs!views/OsdpMonitoringActivityEditorView',
  'cs!views/OsdpMonitoringProgrammeEditorView',
  'cs!views/OsdpMonitoringFacilityEditorView',
  'cs!views/SampleArchiveEditorView',
  'cs!views/ErammpModelEditorView',
  'cs!views/ErammpDatacubeEditorView',
  'cs!models/DepositRequestModel',
  'cs!views/DepositRequestView',
  'cs!views/ClipboardCopyView',
  'cs!views/DataTypeEditorView',
  'cs!views/DocumentUploadView',
  'cs!models/DocumentUploadModel'
], function(
  _, $, Backbone, Dropzone, Bootstrap, StudyAreaView, MapViewerApp, MapViewerAppView, SearchApp, SearchAppView, MessageView, LayersRouter, SearchRouter,
    EditorMetadata, GeminiEditorView, MonitoringEditorView, PermissionApp, PermissionRouter, PermissionAppView, Catalogue, CatalogueView,
    ChartView, ModelEditorView, LinkEditorView, LinkEditorMetadata, CehModelEditorView, CehModelApplicationEditorView, OsdpAgentEditorView,
    OsdpDatasetEditorView, OsdpModelEditorView, OsdpSampleEditorView, OsdpPublicationEditorView, OsdpMonitoringActivityEditorView, OsdpMonitoringProgrammeEditorView,
    OsdpMonitoringFacilityEditorView, SampleArchiveEditorView, ErammpModelEditorView, ErammpDatacubeEditorView, DepositRequestModel, DepositRequestView,
    ClipboardCopyView, DataTypeEditorView, DocumentUploadView, DocumentUploadModel
) {

  return {
    /*
    This is the initalizer method for the entire requirejs project. Here we can
    set up the different applications and initialize any javascript code which
    we like globally.
    */
    initialize() {
      // shim
      // http://stackoverflow.com/a/646643
      if (String.prototype.startsWith == null) { String.prototype.startsWith = function(s) { return this.slice(0, s.length) === s; }; }
      if (String.prototype.endsWith == null) {   String.prototype.endsWith = function(s) { return (s === '') || (this.slice(-s.length) === s); }; }

      if ($('#document-upload').length) { (this.initDocumentUpload)(); }

      if ($('#deposit-request').length) { (this.initDepositRequest)(); }
      if ($('#studyarea-map').length) { (this.initStudyAreaMap)(); }
      if ($('#geometry-map').length) { (this.initGeometryMap)(); }
      if ($('#mapviewer').length) { (this.initMapviewer)(); }
      if ($('#search').length) { (this.initSearch)(); }
      if ($('.edit-control').length) { (this.initEditor)(); }
      if ($('.permission').length) { (this.initPermission)(); }
      if ($('.catalogue-control').length) { (this.initCatalogue)(); }
      if ($('.clipboard-copy').length) { (this.initClipboard)(); }

      $('.chart').each((i, e) => new ChartView({el: e}));
      return (Backbone.history.start)();
    },
  
    initDocumentUpload() {
      let view;
      const id = $('#document-upload').data('guid');
      const app = new DocumentUploadModel(null,
          {mediaType: 'application/vnd.upload-document+json'});
      app.id = id;
      app.set('id', id);
      return view = new DocumentUploadView({model: app});
    },

    initDepositRequest() {
      let view;
      const app = new DepositRequestModel;
      return view = new DepositRequestView({model: app});
    },

    initStudyAreaMap() {
      let view;
      return view = new StudyAreaView({
        el: '#studyarea-map'});
    },

    initGeometryMap() {
      let view;
      return view = new StudyAreaView({
        el: '#geometry-map'});
    },

    initClipboard() {
      let view;
      return view = new ClipboardCopyView({
        el: '.clipboard-copy'});
    },

    /*
    Initialize the map viewer app, view and router
    */
    initMapviewer() {
      const app    = new MapViewerApp();
      const view   = new MapViewerAppView({model: app});
      const router = new LayersRouter({model: app});

      return this.createMessageViewFor(app);
    },

    /*
    Initialize the search application
    */
    initSearch() {
      const app    = new SearchApp();
      const view   = new SearchAppView({model: app});
      const router = new SearchRouter({model: app, location: window.location});

      return this.createMessageViewFor(app);
    },

    /*
    Initialize the editor application
    */
    initEditor() {

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
        'erammp-datacube': {
          View: ErammpDatacubeEditorView,
          Model: EditorMetadata,
          mediaType: 'application/vnd.erammp-datacube+json'
        },
        'data-type': {
          View: DataTypeEditorView,
          Model: EditorMetadata,
          mediaType: 'application/vnd.data-type+json'
        }
      };

      // the create document dropdown
      const $editorCreate = $('#editorCreate');

      return $('.edit-control').on('click', function(event) {
        (event.preventDefault)();

        const title = $(event.target).data('documentType');
        const documentType = lookup[title];

        if ($editorCreate.length) {
          return new documentType.View({
            model: new documentType.Model(null, documentType, title),
            el: '#search'
          });
        } else {
          return $.ajax({
            url: $(location).attr('href'),
            dataType: 'json',
            accepts: {
              json: documentType.mediaType
            },
            success(data) {
              return new documentType.View({
                model: new documentType.Model(data, documentType, title),
                el: '#metadata'
              });
            }
          });
        }
      });
    },

    /*
    Initialize the permission application
    */
    initPermission() {
      const app = new PermissionApp();
      const view = new PermissionAppView({model: app});
      const router = new PermissionRouter({model: app});

      return this.createMessageViewFor(app);
    },

    /*
    Initialize the catalogue application
    */
    initCatalogue() {
      let catalogues = undefined;

      $.getJSON('/catalogues', data => catalogues = _.chain(data).map(c => ({
        value: c.id,
        label: c.title
      })).value());

      return $('.catalogue-control').on('click', function(event) {
        (event.preventDefault)();
        return $.getJSON($(event.target).attr('href'), function(data) {
          const model = new Catalogue(data);
          model.options = catalogues;

          return new CatalogueView({
            el: '#metadata',
            model
          });
        });
      });
    },

    /*
    Create a message view. Which listens to the supplied app model for messages (errors, info)
    */
    createMessageViewFor(app) { return new MessageView({model: app}); }
  };
});
