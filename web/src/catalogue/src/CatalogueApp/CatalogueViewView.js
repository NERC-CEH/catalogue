import SingleView from '../../../editor/src/SingleView'
import { EditorView } from '../../../editor/src/index'

const CheckboxListView = SingleView.extend({

  events: {
    'change input[type=checkbox]': 'updateModel'
  },

  initialize (options) {
    SingleView.prototype.initialize.call(this, options)
    this.listenTo(this.model, 'change:value', this.render)
    this.render()
  },

  render () {
    SingleView.prototype.render.apply(this)
    const current = this.model.get('value') || []
    const catalogues = this.data.catalogues || []
    if (catalogues.length === 0) {
      this.$('.dataentry').html('<p class="text-muted">No other catalogues available.</p>')
    } else {
      const checkboxes = catalogues.map(cat => {
        const checked = current.includes(cat.id) ? 'checked' : ''
        return `<div class="form-check">
          <input class="form-check-input" type="checkbox" value="${cat.id}" id="cat-view-${cat.id}" ${checked}>
          <label class="form-check-label" for="cat-view-${cat.id}">${cat.title}</label>
        </div>`
      }).join('')
      this.$('.dataentry').html(checkboxes)
    }
    return this
  },

  updateModel () {
    const selected = []
    this.$('input[type=checkbox]:checked').each(function () {
      selected.push(this.value)
    })
    this.model.set('value', selected)
  }
})

export default EditorView.extend({

  initialize () {
    this.sections = [{
      label: 'One',
      title: 'Secondary Catalogues',
      views: [
        new CheckboxListView({
          model: this.model,
          modelAttribute: 'value',
          label: 'Secondary catalogues',
          catalogues: this.model.catalogues,
          helpText: '<p>Records in secondary catalogues appear in those catalogues’ search results but can only be edited in the primary catalogue.</p>'
        })
      ]
    }]
    return EditorView.prototype.initialize.apply(this)
  }
})
