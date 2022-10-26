import _ from 'underscore'
import SingleView from '../SingleView'
import parentTemplate from '../templates/Parent.tpl'
import childTemplate from '../templates/MultiString.tpl'
import template from '../templates/ChildLarge.tpl'
import 'jquery-ui/ui/widgets/sortable'

export default SingleView.extend({

  events: {
    change: 'modify',
    'click .remove': 'removeChild',
    'click .add': 'addChild'
  },

  initialize (options) {
    if (typeof this.template === 'undefined') {
      this.template = _.template(template)
    }
    if (typeof this.childTemplate === 'undefined') {
      this.childTemplate = _.template(childTemplate)
    }
    if (typeof this.parentTemplate === 'undefined') {
      this.parentTemplate = _.template(parentTemplate)
    }
    SingleView.prototype.initialize.call(this, options)
    this.array = this.model.has(this.data.modelAttribute) ? _.clone(this.model.get(this.data.modelAttribute)) : []
    this.render()

    this.$('.existing').sortable({
      start: (event, ui) => {
        this._oldPosition = ui.item.index()
      },
      update: (event, ui) => {
        const toMove = (this.array.splice(this._oldPosition, 1))[0]
        this.array.splice(ui.item.index(), 0, toMove)
        this.updateModel()
      }
    })
  },

  renderParent () {
    this.$el.html(this.parentTemplate({ data: this.data }))
  },

  render () {
    this.renderParent()
    _.each(this.array, (string, index) => {
      return this.$('.existing').append(this.childTemplate({
        data: _.extend({}, this.data, {
          index,
          value: string
        })
      }))
    })
    return this
  },

  modify (event) {
    const $target = $(event.target)
    const index = $target.data('index')
    const value = $target.val()
    this.array.splice(index, 1, value)
    this.updateModel()
  },

  removeChild (event) {
    event.preventDefault()
    const $target = $(event.currentTarget)
    const index = $target.data('index')
    this.array.splice(index, 1)
    this.$(`#input${this.data.modelAttribute}${index}`).remove()
    this.updateModel()
  },

  addChild (event) {
    event.preventDefault()
    this.array.push('')
    const index = this.array.length - 1
    this.$('.existing').append(this.childTemplate({
      data: _.extend({}, this.data,
        { index })
    }))
    this.$(`#input${this.data.modelAttribute}${index} input`).focus()
    this.updateModel()
  },

  updateModel () {
    this.model.set(this.data.modelAttribute, _.clone(this.array))
  }
})
