define [
  'cs!views/editor/ObjectInputView',
  'tpl!templates/editor/BoundingBox.tpl'
], (ObjectInputView, template) -> ObjectInputView.extend

  template: template

  boundingBoxes:
    eng:
      northBoundLatitude: 55.812
      eastBoundLongitude: 1.7675
      southBoundLatitude: 49.864
      westBoundLongitude: -6.4526
    gb:
      northBoundLatitude: 60.861
      eastBoundLongitude: 1.768
      southBoundLatitude: 49.864
      westBoundLongitude: -8.648
    ni:
      northBoundLatitude: 55.313
      eastBoundLongitude: -5.432
      southBoundLatitude: 54.022
      westBoundLongitude: -8.178
    sco:
      northBoundLatitude: 60.861
      eastBoundLongitude: -0.728
      southBoundLatitude: 54.634
      westBoundLongitude: -8.648
    uk:
      northBoundLatitude: 60.86099
      eastBoundLongitude: 1.767549
      southBoundLatitude: 49.86382
      westBoundLongitude: -8.648393
    wal:
      northBoundLatitude: 53.434
      eastBoundLongitude: -2.654
      southBoundLatitude: 51.375
      westBoundLongitude: -5.473
    wor:
      northBoundLatitude: 90.00
      eastBoundLongitude: 180.00
      southBoundLatitude: -90.00
      westBoundLongitude: -180.00


  events:
    'change select': 'predefined'

  predefined: (event) ->
    value = $(event.target).val()
    boundingBox = @boundingBoxes[value]
    @model.set boundingBox

