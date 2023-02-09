import { EditorView, InputView } from '../index'
import {
  ParentView, ProvenanceLinkView, TextareaView
} from '../views'

export default EditorView.extend({

  initialize () {
    if (!this.model.has('type')) { this.model.set('type', 'prov-document') }

    this.sections = [{
      label: 'Basic Info',
      title: 'Basic Info',
      views: [
        new InputView({
          model: this.model,
          modelAttribute: 'title',
          label: 'Title'
        }),

        new TextareaView({
          model: this.model,
          modelAttribute: 'description',
          label: 'Description',
          rows: 5
        }),

        new ParentView({
          model: this.model,
          modelAttribute: 'provenanceLinks',
          label: 'Associations',
          ObjectInputView: ProvenanceLinkView,
          multiline: true
        })
      ]
    }
    ]

    return EditorView.prototype.initialize.apply(this)
  }
})