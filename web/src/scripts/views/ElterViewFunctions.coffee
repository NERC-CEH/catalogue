define [
  'tpl!templates/ReplaceByTemporalProcedure.tpl'
], (
  ReplaceByTemporalProcedure
) -> (view, model) ->
  updateManufacturer = ->
    view.updateOtherable 'manufacturer',
      '/elter/manufacturers',
      (manufacturers) ->
        for index, manufacturer of manufacturers
          $('#manufacturer').append('<option value="' + manufacturer.id + '">' + manufacturer.title + '</option>')
        $('#manufacturer').append('<option id="other-manufacturer" value="other"">Other</option>')

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
        
        replacedBy = model.get('replacedBy') || []
        for index, replacedById of replacedBy
          title = replacedById
          options = []
          for tp in tps
            selected = tp.id == replacedById
            options.push(
              value: tp.id
              label: tp.title
              selected: selected
            )
            title = tp.title if selected

          $('#replacedBy').append(ReplaceByTemporalProcedure(
            index: index
            id: replacedById
            title: title
            options: options
            hasLink: true
          ))
        
        options = []
        for tp in tps
          selected = tp.id == replacedById
          options.push(
            value: tp.id
            label: tp.title
            selected: false
          )
        $('#replacedBy').append(ReplaceByTemporalProcedure(
          index: replacedBy.length
          id: ''
          title: ''
          options: [{ value: '', label: '', selected: true }].concat(options)
          hasLink: false
        ))

  ->
    do updateManufacturer
    do updateFoiType
    do updateReplacedBy
