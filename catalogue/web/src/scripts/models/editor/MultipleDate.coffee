define [
  'underscore'
  'backbone'
], (_, Backbone) -> Backbone.Model.extend
# N.B. Use in Views that only has date attributes

  validate: (attrs) ->
    labels =
      creationDate: 'Creation Date'
      publicationDate: 'Publication Date'
      revisionDate: 'Revision Date'
      begin: 'Begin'
      end: 'End'

    dateRegExp = ///
      ^       # begining of string
      \d{4}   # four digit year
      -       # dash
      \d{2}   # two digit month
      -       # dash
      \d{2}   # two digit day
      $       # end of string
      ///
    errors = []

    _.chain(attrs)
      .keys()
      .each (key) ->
        dateString = attrs[key]
        if not (dateString.match dateRegExp)
          errors.push
            message:
              "#{labels[key]} is not in the correct date format of yyyy-mm-dd"

        if isNaN Date.parse dateString
          errors.push
            message:
              "#{labels[key]} is not a vaild date"

    if attrs.begin && attrs.end
      begin = Date.parse attrs.begin
      end = Date.parse attrs.end

      if begin > end
        errors.push
          message:
            "End date is before Begin date"

    if _.isEmpty errors
      # return nothing from Backbone.Model.validate
      # because returning something signals a validation error.
      return
    else
      return errors
