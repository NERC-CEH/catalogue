define [], () -> (view, model) ->
  showOtherManufacturer = ->
    $('.other-manufacturer').removeClass('is-hidden')
    $('.other-manufacturer').addClass('is-visible')
    $('.other-manufacturer input').val('')
    $('.other-manufacturer input').prop('disabled', no)
    $('.other-manufacturer input').focus()

  hideOtherManufacturer = ->
    $('.other-manufacturer').addClass('is-hidden')
    $('.other-manufacturer').removeClass('is-visible')
    $('.other-manufacturer input').val('')
    $('.other-manufacturer input').prop('disabled', yes)
    $('.other-manufacturer input').blur()

  initManufacturer = ->
    $('#manufacturer').unbind 'change'
    $('#manufacturer').change ->
      do hideOtherManufacturer
      value = $('#manufacturer').val()
      model.set('manufacturer', value)
      if value == 'other'
        do showOtherManufacturer
        do view.initInputs
        do initManufacturer
      else
        do hideOtherManufacturer
        do view.save

  updateManufacturer = ->
    do hideOtherManufacturer
    if $('#manufacturer').length
      $.ajax
        type: 'GET'
        url: '/elter/manufacturers'
        headers:
          Accept: "application/json"
        success: (manufacturers) ->
          $('.other-manufacturer').addClass('is-hidden')
          $('.other-manufacturer').removeClass('is-visible')
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
