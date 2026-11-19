import _ from 'underscore'
import SingleView from '../../SingleView'

const content = _.template(`
<p class="previous-naming-convention form-control-static"><% lines.forEach(function (line, index) { %><% if (index) { %><br><% } %><%- line %><% }) %></p>
<p class="text-body-secondary">This agreement was written before file groups. Please describe these files as file groups below, so the upload page can check them, and then remove this naming convention.</p>
<button type="button" class="btn btn-outline-danger btn-sm remove-naming-convention">Remove naming convention</button>
`)

/**
 * The prose naming convention of an agreement written before file groups (dri-one #548): shown read-only
 * while its files are described as groups, with a way to remove it. Nothing can be typed into it, and an
 * agreement without one shows nothing.
 */
export default SingleView.extend({

  className: 'component component--previous-naming-convention',

  events: {
    'click .remove-naming-convention': 'removeConvention'
  },

  initialize (options) {
    SingleView.prototype.initialize.call(this, _.extend({ label: 'Previous naming convention' }, options))
    this.listenTo(this.model, `change:${this.data.modelAttribute}`, this.render)
    this.render()
  },

  render () {
    const value = this.model.get(this.data.modelAttribute)
    if (typeof value !== 'string' || value.trim() === '') {
      this.$el.empty()
      return this
    }
    SingleView.prototype.render.apply(this)
    this.$('.dataentry').append(content({ lines: value.split('\n') }))
    return this
  },

  // Not named remove: that is Backbone's View#remove, which teardown calls
  removeConvention () {
    this.model.unset(this.data.modelAttribute)
  }
})
