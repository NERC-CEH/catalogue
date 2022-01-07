/*
 * decaffeinate suggestions:
 * DS102: Remove unnecessary code created because of implicit returns
 * Full docs: https://github.com/decaffeinate/decaffeinate/blob/master/docs/suggestions.md
 */
define([
  'cs!views/EditorView',
  'cs!views/editor/SingleObjectView',
  'cs!views/editor/InputView',
  'cs!views/editor/TextareaView',
  'cs!views/editor/ParentView',
  'cs!views/editor/KeywordView',
  'cs!views/editor/RelationshipView',
  'cs!views/editor/LinkView',
  'cs!views/editor/DatasetReferenceDateView',
  'cs!models/editor/MultipleDate',
  'cs!views/editor/TemporalExtentView',
  'cs!views/editor/ResourceIdentifierView',
  'cs!views/editor/ParametersMeasuredView',
  'cs!models/editor/BoundingBox',
  'cs!views/editor/BoundingBoxView'
], function(
  EditorView,
  SingleObjectView,
  InputView,
  TextareaView,
  ParentView,
  KeywordView,
  RelationshipView,
  LinkView,
  DatasetReferenceDateView,
  MultipleDate,
  TemporalExtentView,
  ResourceIdentifierView,
  ParametersMeasuredView,
  BoundingBox,
  BoundingBoxView
) { return EditorView.extend({

  initialize() {

    if (!this.model.has('type')) { this.model.set('type', 'dataset'); }

    this.sections = [{
      label: 'Basic Info',
      title: 'Basic Info',
      views: [

        new InputView({
          model: this.model,
          modelAttribute: 'title',
          label: 'Title',
          helpText: `\
<p>Name of dataset</p>\
`
        }),

        new TextareaView({
          model: this.model,
          modelAttribute: 'description',
          rows: 13,
          label: 'Description',
          helpText: `\
<p>Description of dataset</p>\
`
        }),

        new InputView({
          model: this.model,
          modelAttribute: 'format',
          label: 'Format',
          helpText: `\
<p>Format of dataset</p>\
`
        }),

        new InputView({
          model: this.model,
          modelAttribute: 'version',
          label: 'Version',
          helpText: `\
<p>Version of dataset</p>\
`
        }),

        new ParentView({
          model: this.model,
          modelAttribute: 'parametersMeasured',
          label: 'Parameters Measured',
          ObjectInputView: ParametersMeasuredView,
          multiline: true,
          helpText: `\
<p></p>\
`
        }),

        new SingleObjectView({
          model: this.model,
          modelAttribute: 'referenceDate',
          ModelType: MultipleDate,
          label: 'Reference Date',
          ObjectInputView: DatasetReferenceDateView,
          helpText: `\
<p>Publication, creation & revision dates</p>\
`
        }),

        new SingleObjectView({
          model: this.model,
          modelAttribute: 'temporalExtent',
          ModelType: MultipleDate,
          label: 'Temporal Extent',
          ObjectInputView: TemporalExtentView,
          helpText: `\
<p>Temporal Extent of dataset</p>\
`
        }),

        new SingleObjectView({
          model: this.model,
          modelAttribute: 'access',
          label: 'Access',
          ObjectInputView: LinkView,
          helpText: `\
<p>Access to dataset</p>\
`
        }),

        new SingleObjectView({
          model: this.model,
          modelAttribute: 'boundingBox',
          ModelType: BoundingBox,
          label: 'Bounding Box',
          ObjectInputView: BoundingBoxView,
          helpText: `\
<p>Bounding Box of Dataset</p>\
`
        }),

        new ParentView({
          model: this.model,
          modelAttribute: 'keywords',
          label: 'Keywords',
          ObjectInputView: KeywordView,
          helpText: `\
<p>Keywords for discovery</p>\
`
        }),

        new ParentView({
          model: this.model,
          modelAttribute: 'relationships',
          label: 'Relationships',
          ObjectInputView: RelationshipView,
          multiline: true,
          options: [
            {value: 'http://onto.nerc.ac.uk/CEHMD/rels/cites', label: 'Cites'},
            {value: 'http://onto.nerc.ac.uk/CEHMD/rels/related', label: 'Related'},
            {value: 'http://onto.nerc.ac.uk/CEHMD/rels/supercedes', label: 'Supercedes'},
            {value: 'http://onto.nerc.ac.uk/CEHMD/rels/produces', label: 'Produces'},
            {value: 'http://onto.nerc.ac.uk/CEHMD/rels/uses', label: 'Uses'}
          ],
          helpText: `\
<p>Relationships to other OSDP document types</p>\
`
        }),

        new ParentView({
          model: this.model,
          modelAttribute: 'resourceIdentifiers',
          label: 'Resource Identifiers',
          ObjectInputView: ResourceIdentifierView,
          helpText: `\
<p>A unique string or number used to identify the resource.</p>
<p> The codespace identifies the context in which the code is unique.</p>\
`
        })
      ]
    }
    ];

    return EditorView.prototype.initialize.apply(this);
  }
});
 });