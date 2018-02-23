define [], (view, model) ->
  updateManufacturer = ->
    view.updateOtherable 'manufacturer',
      '/elter/manufacturers',
      (manufacturers) ->
        for index, manufacturer of manufacturers
          $('#manufacturer').append('<option value="' + manufacturer.id + '">' + manufacturer.title + '</option>')
      ,
      -> $('#manufacturer').append('<option id="other-manufacturer" value="other"">Other</option>')

  updateFoiType = ->
    $('#foi-type').unbind 'change'
    $('#foi-type').change ->
        $('#blank-option').remove()
        value = $('#foi-type').val()
        model.set('foiType', value)
        do view.save
        $('.foi-data').removeClass('is-notype')
        $('.foi-data').removeClass('is-monitoring')
        $('.foi-data').removeClass('is-verticalmonitoring')
        $('.foi-data').removeClass('is-composite')
        $('.foi-data').addClass('is-' + value.toLowerCase())

  updateReplacedBy = ->
    view.updateOtherable 'replacedBy',
      '/elter/temporal-procedures',
      (temporalProcedures) ->
        tps = []
        for temporalProcedure in temporalProcedures
          tps.push temporalProcedure if temporalProcedure.id != model.get 'id'
        replacedBy = model.get 'replacedBy'
        console.log(tps)

        for replacedById in replacedBy
          $('#replacedBy').append('<div>' + replacedById + '</div>')
      ->

  ->
    do updateManufacturer
    do updateFoiType
    do updateReplacedBy
