/*
 * decaffeinate suggestions:
 * DS102: Remove unnecessary code created because of implicit returns
 * Full docs: https://github.com/decaffeinate/decaffeinate/blob/master/docs/suggestions.md
 */
define([
  'cs!views/EditorView',
  'cs!views/editor/InputView',
  'cs!views/editor/TextareaView',
  'cs!views/editor/ParentView',
  'cs!views/editor/KeywordView',
  'cs!views/editor/RelationshipView'
], function(
  EditorView,
  InputView,
  TextareaView,
  ParentView,
  KeywordView,
  RelationshipView
) { return EditorView.extend({

  initialize() {

    if (!this.model.has('type')) { this.model.set('type', 'agent'); }

    this.sections = [{
      label: 'Basic Info',
      title: 'Basic Info',
      views: [

        new InputView({
          model: this.model,
          modelAttribute: 'title',
          label: 'Name',
          helpText: `\
<p>Name of agent</p>\
`
        }),

        new TextareaView({
          model: this.model,
          modelAttribute: 'address',
          rows: 5,
          label: 'Address',
          helpText: `\
<p>Address of agent</p>\
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
            {value: 'http://onto.nerc.ac.uk/CEHMD/rels/associatedWith', label: 'Associated with'},
            {value: 'http://onto.nerc.ac.uk/CEHMD/rels/creates', label: 'Creates'},
            {value: 'http://onto.nerc.ac.uk/CEHMD/rels/knows', label: 'Knows'},
            {value: 'http://onto.nerc.ac.uk/CEHMD/rels/responsibleFor', label: 'Responsible for'}
          ],
          helpText: `\
<p>Relationships to other OSDP document types</p>\
`
        })
      ]
    }
    ];

    return EditorView.prototype.initialize.apply(this);
  }
});
 });