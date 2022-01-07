// TODO: This file was created by bulk-decaffeinate.
// Sanity-check the conversion and remove this comment.
/*
 * decaffeinate suggestions:
 * DS102: Remove unnecessary code created because of implicit returns
 * Full docs: https://github.com/decaffeinate/decaffeinate/blob/master/docs/suggestions.md
 */
define([
  'cs!views/EditorView',
  'cs!views/editor/LinkDocumentSelectorView',
  'cs!views/editor/ParentView',
  'cs!views/editor/KeywordVocabularyView'
  ], function(EditorView, LinkDocumentSelectorView, ParentView, KeywordVocabularyView) { return EditorView.extend({

  initialize() {
    this.sections = [{
      label: 'One',
      title:  'General information',
      views: [
        new ParentView({
          model: this.model,
          modelAttribute: 'additionalKeywords',
          label: 'Additional Keywords',
          ObjectInputView: KeywordVocabularyView,
          helpText: `\
<p>Keywords (preferably taken from a controlled vocabulary) categorising and describing the data resource.</p>\
`
        }),

        new LinkDocumentSelectorView({
          model: this.model,
          modelAttribute: 'linkedDocumentId',
          label: 'Identifier of linked Document',
          helpText: `\
<p>Metadata record linked to by this document.</p>\
`
        })
        ]
    }
    ];

    return EditorView.prototype.initialize.apply(this);
  }
  });
 });