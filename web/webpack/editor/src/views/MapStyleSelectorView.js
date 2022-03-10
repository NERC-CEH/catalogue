/* eslint-disable
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
  'underscore',
  'cs!views/editor/ObjectInputView',
  'tpl!templates/editor/MapStyleSelector.tpl',
  'colorpicker'
], function (_, ObjectInputView, template) {
  return ObjectInputView.extend({

    template,

    events: {
      changeColor: 'setColour',
      'click a[data-symbol]': 'setSymbol'
    },

    buttonColour: '#fff',

    defaultColour: '#fff',

    symbols: {
      circle: {
        icon: '⬤', label: 'Circle'
      },
      square: {
        icon: '⬛', label: 'Square'
      }
    },

    initialize (options) {
      ObjectInputView.prototype.initialize.call(this, _.extend({}, options,
        { symbols: this.symbols })
      );

      (this.update)()

      this.$('input').colorpicker({ format: 'hex' })
      return this.listenTo(this.model, 'change:colour change:symbol', this.update)
    },

    update () {
      const color = this.model.get('colour')
      this.$('input').val(color)
      if (this.model.has('symbol')) {
        const symbol = this.model.get('symbol')
        this.$('.selected').html(this.symbols[symbol].icon)
        this.$('button').css({ backgroundColor: this.buttonColour })
      } else {
        this.$('.selected').html('&nbsp;')
        this.$('button').css({ backgroundColor: color })
      }

      return this.$('.icon').css({ color })
    },

    setColour () { return this.model.set('colour', this.$('input').val()) },

    setSymbol (e) {
      if ($(e.target).data('symbol') !== 'blank') {
        return this.model.set('symbol', $(e.target).data('symbol'))
      } else {
        return this.model.unset('symbol')
      }
    }
  })
})
