import _ from 'underscore'
import SingleView from '../SingleView'
import parentTemplate from '../templates/Parent.tpl'
import childTemplate from '../templates/MultiString.tpl'
import template from '../templates/ChildLarge.tpl'

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
    SingleView.prototype.initialize.call(this, options)
    this.array = this.model.has(this.data.modelAttribute) ? _.clone(this.model.get(this.data.modelAttribute)) : [];
    (this.render)()

    return this.$('.existing').sortable({
      start: (event, ui) => {
        this._oldPosition = ui.item.index()
      },
      update: (event, ui) => {
        const toMove = (this.array.splice(this._oldPosition, 1))[0]
        this.array.splice(ui.item.index(), 0, toMove)
        return (this.updateModel)()
      }
    })
  },

  renderParent () {
    return this.$el.html(parentTemplate({ data: this.data }))
  },

  render () {
    (this.renderParent)()
    const $attach = this.$('.existing')
    _.each(this.array, (string, index) => {
      return $attach.append(this.childTemplate({
        data: _.extend({}, this.data, {
          index,
          value: string
        }
        )
      })
      )
    })
    return this
  },

  modify (event) {
    const $target = $(event.target)
    const index = $target.data('index')
    const value = $target.val()
    this.array.splice(index, 1, value)
    return (this.updateModel)()
  },

  removeChild (event) {
    (event.preventDefault)()
    const $target = $(event.currentTarget)
    const index = $target.data('index')
    this.array.splice(index, 1)
    this.$(`#input${this.data.modelAttribute}${index}`).remove()
    return (this.updateModel)()
  },

  addChild (event) {
    (event.preventDefault)()
    this.array.push('')
    const index = this.array.length - 1
    this.$('.existing').append(this.childTemplate({
      data: _.extend({}, this.data,
        { index })
    })
    )
    this.$(`#input${this.data.modelAttribute}${index} input`).focus()
    return (this.updateModel)()
  },

  updateModel () {
    return this.model.set(this.data.modelAttribute, _.clone(this.array))
  }
})