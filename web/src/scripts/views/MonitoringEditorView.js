// TODO: This file was created by bulk-decaffeinate.
// Sanity-check the conversion and remove this comment.
/*
 * decaffeinate suggestions:
 * DS102: Remove unnecessary code created because of implicit returns
 * Full docs: https://github.com/decaffeinate/decaffeinate/blob/master/docs/suggestions.md
 */
define([
  'cs!views/EditorView',
  'cs!views/editor/SingleObjectView',
  'cs!views/editor/InputView',
  'cs!views/editor/TextareaView'
], function(EditorView, SingleObjectView, InputView, TextareaView) { return EditorView.extend({

  initialize() {
    this.sections = [{
      label: 'One',
      views: [
        new InputView({
          model: this.model,
          modelAttribute: 'title',
          label: 'Title',
          helpText: `\
<p>Title help</p>\
`
        }),

        new TextareaView({
          model: this.model,
          modelAttribute: 'description',
          label: 'Description',
          rows: 17,
          helpText: `\
<p>Description help</p>\
`
        })
      ]
    }
    ];

    return EditorView.prototype.initialize.apply(this);
  }
});
 });