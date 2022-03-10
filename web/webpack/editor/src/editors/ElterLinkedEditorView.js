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
  'cs!views/editor/InputView',
  'cs!views/editor/ReadOnlyView'
], function (EditorView, InputView, ReadOnlyView) {
  return EditorView.extend({

    initialize () {
      this.model.set('title', 'This title will be replaced on a successful document retrieval')

      this.sections = [{
        label: 'General',
        title: '',
        views: [
          new ReadOnlyView({
            model: this.model,
            modelAttribute: 'title',
            label: 'Title'
          }),

          new InputView({
            model: this.model,
            modelAttribute: 'linkedDocumentUri',
            label: 'Linked document URL',
            helpText: `\
<p>For creating linked documents, add the URL here.</p>
<p>This should be a link to a metadata document in JSON format.</p>\
`
          }),

          new InputView({
            model: this.model,
            modelAttribute: 'linkedDocumentType',
            label: 'Linked document type',
            helpText: '\
<p>Enter the type of the linked document, if applicable.</p>\
'
          })
        ]
      }
      ]

      return EditorView.prototype.initialize.apply(this)
    }
  })
})
