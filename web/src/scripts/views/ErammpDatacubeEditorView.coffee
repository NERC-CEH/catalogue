define [
	'cs!views/EditorView'
	'cs!views/editor/InputView'
	'cs!views/editor/TextareaView'
	'cs!views/editor/KeywordView'
	'cs!views/editor/CheckboxView'
	'cs!views/editor/ParentView'
	'cs!views/editor/ParentLargeView'
	'cs!views/editor/ParentStringView'
	'cs!views/editor/ParentStringTextboxView'
	'cs!views/editor/PredefinedParentView'
	'cs!views/editor/PredefinedParentLargeView'
	'cs!models/editor/BoundingBox'
	'cs!models/editor/PointOfContact'
	'cs!models/editor/MultipleDate'
	'cs!views/editor/BoundingBoxView'
	'cs!views/editor/SingleObjectView'
	'cs!views/editor/SingleView'
	'cs!views/editor/SelectView'
	'cs!views/editor/PointOfContactView'
	'cs!views/editor/LinkView'
	'cs!views/editor/DataTypeSchemaView'
	'cs!models/editor/DataTypeSchema'
	'cs!views/editor/ReadOnlyView'

], (EditorView, InputView, TextareaView, KeywordView, CheckboxView, ParentView, ParentLargeView, ParentStringView, ParentStringTextboxView, PredefinedParentView, PredefinedParentLargeView, BoundingBox, PointOfContact, MultipleDate, BoundingBoxView, SingleObjectView, SingleView, SelectView, PointOfContactView, LinkView, DataTypeSchemaView, DataTypeSchema, ReadOnlyView) -> EditorView.extend

	initialize: ->
		@model.set('type', 'erammpDatacube') unless @model.has('type')

		@sections = [
			label: 'General'
			title: 'General'
			views: [
				new InputView
					model: @model
					modelAttribute: 'title'
					label: 'Data cube name'

				new TextareaView
					model: @model
					modelAttribute: 'description'
					label: 'Description'
					rows: 3

				new InputView
					model: @model
					modelAttribute: 'version'
					label: 'Version'

				new InputView
					model: @model
					modelAttribute: 'dataFormat'
					label: 'Data format'
					placeholderAttribute: 'e.g. NetCDF, dbf, csv, shp'

				new PredefinedParentView
					model: @model
					ModelType: PointOfContact
					modelAttribute: 'provider'
					label: 'Data provider'
					ObjectInputView: PointOfContactView
					helpText: """
					          <p>The contact(s) responsible for this model and who can be contacted if there are questions about it.  A <b>named</b> person is recommended</p>
					          """
					predefined:
						'ADAS':
							organisationName: 'ADAS'
						'CEH':
							organisationName: 'Centre for Ecology & Hydrology'
						'Cranfield':
							organisationName: 'Cranfield'
						'Forest Research':
							organisationName: 'Forest Research'
			]
		,	
			label: 'Distribution'
			title: 'Distribution'
			views: [						
				new SelectView
							model: @model
							modelAttribute: 'constraints'
							label: 'Access/distribution constraints'
							options: [
								{value: 'unclassified', label: 'Unclassified (available for general disclosure)'},
								{value: 'restricted', label: 'Restricted (not for general disclosure)'},
								{value: 'confidential', label: 'Confidential (available for someone who can be entrusted with information)'},
								{value: 'secret', label: 'Secret (kept or meant to be kept private, unknown, or hidden from all but a select group of people)'},
								{value: 'SBU', label: 'Sensitive but unclassified (although unclassified, requires strict controls over its distribution)'},
								{value: 'forOfficialUseOnly', label: 'For official use only (unclassified information that is to be used only for official purposes determined by the designating body)'},
								{value: 'protected', label: 'Protected (compromise of the information could cause damage)'},
								{value: 'limitedDistribution', label: 'Limited distribution (desimination limited by designating body)'}
							]
							helpText: """
												<p>...</p>
												"""

				new ParentStringView
					model: @model
					modelAttribute: 'locations'
					label: 'Location'
					helpText: """
					          <p>Where is the data stored (eg SharePoint, SAN).  A direct path to the datacube is preferred.</p>
					          """

				new TextareaView
					model: @model
					modelAttribute: 'ipr'
					label: 'IPR'
					rows: 5
					helpText: """
					          <p>Are there any IPR issues to consider when using the data?</p>
					          """
			]
		,
			label: 'Schema'
			title: 'Schema'
			views: [
				
        new PredefinedParentLargeView
          model: @model
          ModelType: DataTypeSchema
          modelAttribute: 'schema'
          multiline: true
          label: 'Schema'
          ObjectInputView: DataTypeSchemaView
          predefined:
            'Boolean (true/false)':
              type: 'boolean'
            'Date':
              type: 'date'
              format: 'YYYY-MM-DD'
            'Date & time':
              type: 'datetime'
              format: 'YYYY-MM-DDThh:mm:ss'
            'Decimal number':
              type: 'number'
            'Email':
              type: 'string'
              format: 'email'
            'Geographic point':
              type: 'geopoint'
              format: 'lon, lat'
            'Integer':
              type: 'integer'
            'Text':
              type: 'string'
            'Time':
              type: 'time'
              format: 'hh:mm:ss'
            'URI':
              type: 'string'
              format: 'uri'
            'UUID':
              type: 'string'
              format: 'uuid'
            'Year':
              type: 'year'
              format: 'YYYY'
            'Year & month':
              type: 'yearmonth'
              format: 'YYYY-MM'
			]
		,
			label: 'Another section'
			title: 'Section 3'
			views: [
				new ParentView
					model: @model
					modelAttribute: 'keywords'
					label: 'Keywords'
					ObjectInputView: KeywordView
					helpText: """
					          <p>A list of keywords that help to identify and describe the model - used to improve search results and filtering. A keyword may be an entry from a vocabulary (with a uri) or just plain text.</p>
					          """
				
				new PredefinedParentView
          model: @model
          modelAttribute: 'boundingBoxes'
          ModelType: BoundingBox
          label: 'Spatial coverage'
          ObjectInputView: BoundingBoxView
          multiline: true
          predefined:
            Wales:
              northBoundLatitude: 53.434
              eastBoundLongitude: -2.654
              southBoundLatitude: 51.375
              westBoundLongitude: -5.473
            'England & Wales':
              northBoundLatitude: 55.812
              eastBoundLongitude: 1.768
              southBoundLatitude: 49.864
              westBoundLongitude: -6.452
            'UK (or Great Britain)':
              northBoundLatitude: 60.861
              eastBoundLongitude: 1.768
              southBoundLatitude: 49.864
              westBoundLongitude: -8.648
            World:
              northBoundLatitude: 90.00
              eastBoundLongitude: 180.00
              southBoundLatitude: -90.00
              westBoundLongitude: -180.00
          helpText: """
                    <p>A bounding box showing the area that the archive covers. It will encompass the remit of the archive, which may be larger than that represented by the samples actually in the archive. It is represented by north, south, east and west in decimal degrees (WGS84).</p>
                    <p>Enter the values, or click on the map to draw a  rectangle at the approximate location.</p>
                    """
        
				new SelectView
							model: @model
							modelAttribute: 'spatialRepresentationType'
							label: 'Spatial type'
							options: [
								{value: 'grid', label: 'Raster (grid)'},
								{value: 'vector', label: 'Vector'},
								{value: 'textTable', label: 'Tabular (e.g. spreadsheet, database table)'}
							]
							helpText: """
												<p>...</p>
												"""

				new ParentView
          model: @model
          modelAttribute: 'resourceLocators'
          label: 'Additional links'
          ObjectInputView: LinkView
          helpText: """
                    <p>A list of links to additional resources that may be of use to the user.</p>
                    """
				
				new InputView
					model: @model
					modelAttribute: 'dataSource'
					label: 'Data source'
					placeholderText: "this is a placeholder - it will be where you link to the source model's metadata "

				new InputView
					model: @model
					modelAttribute: 'spatialResolution'
					label: 'Spatial resolution'
				]
		]

		EditorView.prototype.initialize.apply @
