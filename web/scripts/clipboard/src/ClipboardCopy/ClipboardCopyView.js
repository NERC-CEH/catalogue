import Backbone from 'backbone'
import _ from 'underscore'
import template from './ClipboardCopy.tpl'
import 'jquery-ui/ui/widgets/tooltip'
// #
// # Add a <span class="clipboard-copy" data-selector="{my-selector}"></span>
// # to any element to copy to clipboard.
// # Replace {my-selector} with the selector for the element you want copied.
// #

export default Backbone.View.extend({

  events: {
    click: 'copyToClipboard'
  },

  initialize () {
    this.render()
  },

  render () {
    this.$el.html(_.template(template))
    this.$('button').tooltip({
      title: 'copied',
      trigger: 'click'
    })
    return this
  },

  copyToClipboard (event) {
    copy($(event.currentTarget).data('selector'))
    self.clearTimeout(this.timeout)
    this.timeout = self.setTimeout(function () { return this.$('button').tooltip('hide') }, 1000)
  }
})

const copy = function (selector) {
  const selection = self.getSelection()
  const range = document.createRange()
  const copyContent = document.querySelector(selector)

  if (copyContent) {
    range.selectNode(copyContent)
    selection.empty()
    selection.addRange(range)
    document.execCommand('copy')
    selection.empty()
  }
}
