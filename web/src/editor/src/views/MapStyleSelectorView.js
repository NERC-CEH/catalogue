import _ from 'underscore'
import $ from 'jquery'
import ObjectInputView from './ObjectInputView'
import template from '../templates/MapStyleSelector'
import 'bootstrap-colorpicker'

export default ObjectInputView.extend({

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
        this.template = template
        ObjectInputView.prototype.initialize.call(this, _.extend({}, options,
            { symbols: this.symbols })
        )

        this.listenTo(this.model, 'change:colour change:symbol', this.update)
        this.update()

        const that = this
        $(document).ready(function () {
            that.$('#picker').change(function () {
                that.model.set('colour', that.$('#picker').val())
            })
        })
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

        this.$('.icon').css({ color })
        this.$('#picker').val(color)
    },

    setColour () {
        this.model.set('colour', this.$('input').val())
    },

    setSymbol (e) {
        if ($(e.target).data('symbol') !== 'blank') {
            this.model.set('symbol', $(e.target).data('symbol'))
        } else {
            this.model.unset('symbol')
        }
    }
})
