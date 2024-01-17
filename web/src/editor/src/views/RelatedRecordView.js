import _ from 'underscore'
import $ from 'jquery'
import ObjectInputView from './ObjectInputView'
import template from '../templates/RelatedRecord'

export default ObjectInputView.extend({

    initialize () {
        this.template = template
        ObjectInputView.prototype.initialize.apply(this)
        const catalogue = $('html').data('catalogue')

        this.$('.autocomplete').autocomplete({
            minLength: 2,
            source: (request, response) => {
                let query
                const term = request.term.trim()
                if (_.isEmpty(term)) {
                    query = `/${catalogue}/documents`
                } else {
                    query = `/${catalogue}/documents?term=${request.term}`
                }

                $.getJSON(query, data => response(_.map(data.results, d => ({
                    value: d.title,
                    label: d.title,
                    identifier: d.identifier,
                    type: d.resourceType
                }))))
            },
            select: (event, ui) => {
                this.model.set('identifier', ui.item.identifier)
                this.$('.identifier').val(ui.item.identifier)
                this.model.set('href', `${window.location.origin}/id/${ui.item.identifier}`)
                this.$('.href').val(`${window.location.origin}/id/${ui.item.identifier}`)
                this.model.set('associationType', ui.item.type)
                this.$('.associationType').val(ui.item.type)
                this.model.set('title', ui.item.label)
                this.$('.title').val(ui.item.label)
                this.$('.relationshipSearch').addClass('hidden')
                this.$('.relationshipRecord').removeClass('hidden')
            }
        })

        const relId = this.model.get('identifier')
        if (relId != null) {
            this.$('.relationshipSearch').addClass('hidden')
            this.$('.relationshipRecord').removeClass('hidden')
        }
    },

    render () {
        ObjectInputView.prototype.render.apply(this)
        this.$('select.rel').val(this.model.get('rel'))
        return this
    }
})
