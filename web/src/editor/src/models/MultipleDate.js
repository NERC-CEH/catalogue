import _ from 'underscore'
import Backbone from 'backbone'

export default Backbone.Model.extend({
// N.B. Use in Views that only has date attributes

    validate (attrs) {
        const labels = {
            creationDate: 'Creation date',
            publicationDate: 'Publication date',
            revisionDate: 'Revision date',
            supersededDate: 'Date superseded',
            deprecatedDate: 'Date deprecated',
            releasedDate: 'Date released',
            begin: 'Begin',
            end: 'End'
        }

        const dateRegEx = '^\\d{4}\\-(0[1-9]|1[0-2])\\-(0[1-9]|(1|2)[0-9]|3[0-1])$'
        const errors = []

        _.chain(attrs)
            .keys()
            .each(function (key) {
                const dateString = attrs[key]
                if (!dateString.match(dateRegExp)) {
                    errors.push({
                        message:
                            `${labels[key]} is wrong. The date format is yyyy-mm-dd`
                    })
                }

                if (isNaN(Date.parse(dateString))) {
                    return errors.push({
                        message:
                            `${labels[key]} doesn't look like a valid date`
                    })
                }
            })

        if (attrs.begin && attrs.end) {
            const begin = Date.parse(attrs.begin)
            const end = Date.parse(attrs.end)

            if (begin > end) {
                errors.push({
                    message:
                        'Collection of this data finished before it started!'
                })
            }
        }

        if (_.isEmpty(errors)) {
            // return nothing from Backbone.Model.validate
            // because returning something signals a validation error.

        } else {
            return errors
        }
    }
})
