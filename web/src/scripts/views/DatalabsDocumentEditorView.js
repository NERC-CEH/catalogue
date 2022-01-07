// TODO: This file was created by bulk-decaffeinate.
// Sanity-check the conversion and remove this comment.
/*
 * decaffeinate suggestions:
 * DS102: Remove unnecessary code created because of implicit returns
 * Full docs: https://github.com/decaffeinate/decaffeinate/blob/master/docs/suggestions.md
 */
define([
  'cs!views/EditorView',
  'cs!views/editor/InputView',
  'cs!views/editor/TextareaView',
  'cs!views/editor/ParentStringView'
], function(
  EditorView,
  InputView,
  TextareaView,
  ParentStringView
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
            label: 'Title'
          }),
          new InputView({
            model: this.model,
            modelAttribute: 'description',
            label: 'Description'
          }),
          new InputView({
            model: this.model,
            modelAttribute: 'version',
            label: 'Version'
          }),
          new InputView({
            model: this.model,
            modelAttribute: 'masterUrl',
            label: 'Master URL'
          }),
          new ParentStringView({
            model: this.model,
            modelAttribute: 'owners',
            label: 'Owners'
          })
      ]
    }
    ];

    return EditorView.prototype.initialize.apply(this);
  }
});
 });