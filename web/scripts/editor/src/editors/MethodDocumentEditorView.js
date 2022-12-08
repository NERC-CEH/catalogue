import { EditorView, InputView } from '../index'
import {
  TextareaView
} from '../views'

export default EditorView.extend({

  initialize () {
    if (!this.model.has('type')) { this.model.set('type', 'method-document') }

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
          rows: 8
        })
      ]
    }
    ]

    return EditorView.prototype.initialize.apply(this)
  }
})
