/* eslint-disable
    no-multi-str,
    no-undef,
*/
// TODO: This file was created by bulk-decaffeinate.
// Fix any style issues and re-enable lint.
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
  'cs!models/editor/MultipleDate',
  'cs!views/editor/TemporalExtentView',
  'cs!views/editor/ResourceIdentifierView',
  'cs!models/editor/BoundingBox',
  'cs!views/editor/BoundingBoxView'
], function (
  EditorView,
  SingleObjectView,
  InputView,
  TextareaView,
  ParentView,
  KeywordView,
  RelationshipView,
  LinkView,
  MultipleDate,
  TemporalExtentView,
  ResourceIdentifierView,
  BoundingBox,
  BoundingBoxView
) {
  return EditorView.extend({

    initialize () {
      if (!this.model.has('type')) { this.model.set('type', 'monitoringProgramme') }

      this.sections = [{
        label: 'Basic Info',
        title: 'Basic Info',
        views: [

          new InputView({
            model: this.model,
            modelAttribute: 'title',
            label: 'Title',
            helpText: '\
<p>Name of Monitoring Programme</p>\
'
          }),

          new TextareaView({
            model: this.model,
            modelAttribute: 'description',
            rows: 13,
            label: 'Description',
            helpText: '\
<p>Description of Monitoring Programme</p>\
'
          }),

          new SingleObjectView({
            model: this.model,
            modelAttribute: 'temporalExtent',
            ModelType: MultipleDate,
            label: 'Temporal Extent',
            ObjectInputView: TemporalExtentView,
            helpText: '\
<p>Temporal Extent of Monitoring Programme</p>\
'
          }),

          new SingleObjectView({
            model: this.model,
            modelAttribute: 'boundingBox',
            ModelType: BoundingBox,
            label: 'Bounding Box',
            ObjectInputView: BoundingBoxView,
            helpText: '\
<p>Bounding Box of Monitoring Programme</p>\
'
          }),

          new ParentView({
            model: this.model,
            modelAttribute: 'keywords',
            label: 'Keywords',
            ObjectInputView: KeywordView,
            helpText: '\
<p>Keywords for discovery</p>\
'
          }),

          new ParentView({
            model: this.model,
            modelAttribute: 'relationships',
            label: 'Relationships',
            ObjectInputView: RelationshipView,
            multiline: true,
            options: [
              { value: 'http://onto.nerc.ac.uk/CEHMD/rels/associatedWith', label: 'Associated with' },
              { value: 'http://onto.nerc.ac.uk/CEHMD/rels/owns', label: 'Owns' },
              { value: 'http://onto.nerc.ac.uk/CEHMD/rels/setupFor', label: 'Setup for' }
            ],
            helpText: '\
<p>Relationships to other OSDP document types</p>\
'
          })
        ]
      }
      ]

      return EditorView.prototype.initialize.apply(this)
    }
  })
})
