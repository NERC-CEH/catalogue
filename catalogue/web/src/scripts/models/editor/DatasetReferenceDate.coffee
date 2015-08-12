define [
  'underscore'
  'backbone'
], (_, Backbone) -> Backbone.Model.extend

  validate: (attrs) ->
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
            message: "#{key} is not in the correct date format of yyyy-mm-dd"
        
        if isNaN Date.parse dateString
          errors.push
            message: "#{key} is not a vaild date"

    console.log attrs
    if _.isEmpty errors
      # return nothing from Backbone.Model.validate
      # because returning something signals a validation error.
      return
    else
      return errors
