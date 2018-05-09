define [
], (
) -> (view, model) ->
  updateManufacturer = ->
    view.updateOtherable 'manufacturer',
      '/elter/manufacturers',
      (manufacturers) ->
        $('#manufacturer').append('<select name="manufacturer">')
        manufacturerId = model.get('manufacturer')
        for index, manufacturer of manufacturers
          if manufacturer.id == manufacturerId
            $('#manufacturer select').append('<option value="' + manufacturer.id + '" selected>' + manufacturer.title + '</option>')
          else
            $('#manufacturer select').append('<option value="' + manufacturer.id + '">' + manufacturer.title + '</option>')
        if typeof manufacturerId == 'undefined'
          $('#manufacturer select').append('<option value=""></option>')
        $('#manufacturer select').append('<option value="other"">Other</option>')

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

  updateReplacedBy = -> view.updateSelectList 'replacedBy', '/elter/temporal-procedures'
  updateRoutedTo = -> view.updateSelectList 'routedTo', '/elter/inputs'
  updateUsedBy = -> view.updateSelectList 'usedBy', '/elter/inputs'
  updateControlsFrequencyOf = -> view.updateSelectList 'controlsFrequencyOf', '/elter/inputs'
  updateVisibleThrough = -> view.updateSelectList 'visibleThrough', '/elter/temporal-procedures'
  updateOriginalStream = -> view.updateSelectList 'originalStream', '/elter/observation-placeholders'
  updatePowers = -> view.updateSelectList 'powers', '/elter/single-system-deployments'
  updateDeploymentInstallation = -> view.updateSelectList 'deploymentInstallation', '/elter/deployment-related-process-durations'
  updateDeploymentRemoval = -> view.updateSelectList 'deploymentRemoval', '/elter/deployment-related-process-durations'
  updateDeploymentCleaning = -> view.updateSelectList 'deploymentCleaning', '/elter/deployment-related-process-durations'
  updateDeploymentMaintenance = -> view.updateSelectList 'deploymentMaintenance', '/elter/deployment-related-process-durations'
  updateDeploymentProgramUpdate = -> view.updateSelectList 'deploymentProgramUpdate', '/elter/deployment-related-process-durations'
  updateCarriedOutBy = -> view.updateSelectList 'carriedOutBy', '/elter/persons'

  ->
    do updateManufacturer
    do updateFoiType
    do updateReplacedBy
    do updateRoutedTo
    do updateUsedBy
    do updateControlsFrequencyOf
    do updateOriginalStream
    do updatePowers
    do updateDeploymentInstallation
    do updateDeploymentRemoval
    do updateDeploymentCleaning
    do updateDeploymentMaintenance
    do updateDeploymentProgramUpdate
    do updateCarriedOutBy
