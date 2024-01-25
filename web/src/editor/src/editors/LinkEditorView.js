import { KeywordVocabularyView, LinkDocumentSelectorView, ParentView } from '../views'
import EditorView from '../EditorView'

export default EditorView.extend({

  initialize () {
    this.sections = [{
      label: 'One',
      title: 'General information',
      views: [
        new ParentView({
          model: this.model,
          modelAttribute: 'additionalKeywords',
          label: 'Additional Keywords',
          ObjectInputView: KeywordVocabularyView,
          helpText: `
<p>Keywords (preferably taken from a controlled vocabulary) categorising and describing the data resource.</p>
`
        }),

        new LinkDocumentSelectorView({
          model: this.model,
          modelAttribute: 'linkedDocumentId',
          label: 'Identifier of linked Document',
          helpText: `
<p>Metadata record linked to by this document.</p>
`
        })
      ]
    }
    ]

    return EditorView.prototype.initialize.apply(this)
  }
})
