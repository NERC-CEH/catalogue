define [
  'underscore'
  'backbone'
], (_, Backbone) -> Backbone.Model.extend
# N.B. Use in Views that only has date attributes

  validate: (attrs) ->
    labels =
      creationDate: 'Creation date'
      publicationDate: 'Publication date'
      revisionDate: 'Revision date'
      supersededDate: 'Date superseded'
      deprecatedDate: 'Date deprecated'
      releasedDate: 'Date released'
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
              "#{labels[key]} is wrong. The date format is supposed to be yyyy-mm-dd"

        if isNaN Date.parse dateString
          errors.push
            message:
              "#{labels[key]} doesn't look like a date to me"

    if attrs.begin && attrs.end
      begin = Date.parse attrs.begin
      end = Date.parse attrs.end

      if begin > end
        errors.push
          message:
            "Collection of this data finished before it started!"

    if _.isEmpty errors
      # return nothing from Backbone.Model.validate
      # because returning something signals a validation error.
      return
    else
      return errors
