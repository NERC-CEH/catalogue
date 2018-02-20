define [
    'tpl!templates/ManufacturerSelect.tpl'
], (
  Manufacturer
) -> (view, model) ->
  initManufacturer = ->
    $('#manufacturer').unbind 'change'
    $('#manufacturer').change ->
      $('.other-manufacturer').remove()
      value = $('#manufacturer').val()
      model.set('manufacturer', value)
      if value == 'other'
        $('#manufacturer').parent().after(Manufacturer())
        do view.initInputs
        do initManufacturer
      else
        do view.save

  updateManufacturer = ->
    $('.other-manufacturer').remove()
    if $('#manufacturer').length
      $.ajax
        type: 'GET'
        url: '/elter/manufacturers'
        headers:
          Accept: "application/json"
        success: (manufacturers) ->
          $('#manufacturer').html('')
          for index, manufacturer of manufacturers
            $('#manufacturer').append('<option value="' + manufacturer.id + '">' + manufacturer.title + '</option>')
          $('#manufacturer').append('<option id="other-manufacturer" value="other"">Other</option>')
          $('#manufacturer').val(model.get('manufacturer'))
    
    do initManufacturer

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
    

  ->
    do updateManufacturer
    do updateFoiType
