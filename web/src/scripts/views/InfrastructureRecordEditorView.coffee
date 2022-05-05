define [
	'cs!views/EditorView'
	'cs!views/editor/InputView'
	'cs!views/editor/TextareaView'
	'cs!views/editor/SingleObjectView'
	'cs!views/editor/SingleView'
	'cs!views/editor/SelectView'
	'cs!views/editor/ReadOnlyView'
	'cs!views/editor/TextOnlyView'
	'cs!views/editor/ParentView'
	'cs!views/editor/ParentLargeView'
	'cs!views/editor/ParentStringView'
	'cs!views/editor/ParentStringTextboxView'
	'cs!views/editor/PredefinedParentView'
	'cs!views/editor/PredefinedParentLargeView'
	'cs!views/editor/KeywordView'
	'cs!models/editor/Contact'
	'cs!views/editor/ContactView'
	'cs!models/editor/BoundingBox'
	'cs!views/editor/BoundingBoxView'
	'cs!views/editor/RelatedRecordView'
	'cs!views/editor/InfrastructureOnlineLinkView'
	'cs!models/editor/InfrastructureChallenge'
	'cs!views/editor/InfrastructureChallengeView'
	'cs!models/editor/InfrastructureCategory'
	'cs!views/editor/InfrastructureCategoryView'

], (EditorView, InputView, TextareaView, SingleObjectView, SingleView, SelectView, ReadOnlyView, TextOnlyView, ParentView, ParentLargeView, ParentStringView, ParentStringTextboxView, PredefinedParentView, PredefinedParentLargeView, KeywordView, Contact, ContactView, BoundingBox, BoundingBoxView, RelatedRecordView, InfrastructureOnlineLinkView, InfrastructureChallenge, InfrastructureChallengeView, InfrastructureCategory, InfrastructureCategoryView) -> EditorView.extend

	initialize: ->
		@model.set('type', 'infrastructureRecord') unless @model.has('type')

		@sections = [
			label: 'General'
			title: 'General'
			views: [

				new InputView
					model: @model
					modelAttribute: 'title'
					label: 'Name'
					helpText: """
                    	<p>Should reflect purpose (succinctly)</p><p>Should be consistent (within and across assets)</p>
                    """

				new TextOnlyView
					model: @model
					label: 'Purpose'
					text: """<p>Explain strategic relevance: What does it do? Why? Who cares?<br>Write in plain English and avoid (or define) acronyms.<br>Explain relevance to government policy agenda</p>
							"""

				new TextareaView
					model: @model
					modelAttribute: 'description'
					rows: 6

				new PredefinedParentView
					model: @model
					ModelType: Contact
					modelAttribute: 'owners'
					label: 'Owner'
					ObjectInputView: ContactView
					multiline: true
					predefined:
						'UKCEH Bangor':
							organisationName: 'UK Centre for Ecology & Hydrology'
							role: 'owner'
							email: 'enquiries@ceh.ac.uk'
							organisationIdentifier: 'https://ror.org/00pggkr55'
							address:
                				city: 'Bangor'
						'UKCEH Edinburgh':
							organisationName: 'UK Centre for Ecology & Hydrology'
							role: 'owner'
							email: 'enquiries@ceh.ac.uk'
							organisationIdentifier: 'https://ror.org/00pggkr55'
							address:
                				city: 'Edinburgh'
						'UKCEH Lancaster':
							organisationName: 'UK Centre for Ecology & Hydrology'
							role: 'owner'
							email: 'enquiries@ceh.ac.uk'
							organisationIdentifier: 'https://ror.org/00pggkr55'
							address:
                				city: 'Lancaster'
						'UKCEH Wallingford':
							organisationName: 'UK Centre for Ecology & Hydrology'
							role: 'owner'
							email: 'enquiries@ceh.ac.uk'
							organisationIdentifier: 'https://ror.org/00pggkr55'
							address:
                				city: 'Wallingford'

			]
		,  
			label: 'Categorisation'
			title: 'Categorisation'
			views: [
				new SingleObjectView
					model: @model
					modelAttribute: 'infrastructureCategory'
					ModelType: InfrastructureCategory
					label: 'Infrastructure category'
					ObjectInputView: InfrastructureCategoryView

				new ParentView
					model: @model
					ModelType: InfrastructureChallenge
					modelAttribute: 'infrastructureChallenge'
					label: 'Challenge/goal'
					ObjectInputView: InfrastructureChallengeView

				new SelectView
					model: @model
					modelAttribute: 'scienceArea'
					label: 'Science area'
					options: [
						{value: 'Atmospheric Chemistry and Effects', label: 'Atmospheric Chemistry and Effects'},
						{value: 'Biodiversity', label: 'Biodiversity'},
						{value: 'Hydro-climate Risks', label: 'Hydro-climate Risks'},
						{value: 'Pollution', label: 'Pollution'},
						{value: 'Soils and Land Use', label: 'Soils and Land Use'},
						{value: 'Water Resources', label: 'Water Resources'}
					]

				new ParentView
					model: @model
					modelAttribute: 'keywords'
					label: 'Other keywords'
					ObjectInputView: KeywordView


			]
		,  
			label: 'Scale'
			title: 'Scale'
			views: [

				new TextareaView
					model: @model
					modelAttribute: 'locationText'
					label: 'Location'
				
				new SelectView
					model: @model
					modelAttribute: 'infrastructureScale'
					label: 'Scale'
					options: [
						{value: 'UK', label: 'UK-wide'},
						{value: 'Landscape or catchment', label: 'Landscape or catchment'},
						{value: 'Area, city, farm, habitat', label: 'Area, city, farm, habitat'}
					]

				new PredefinedParentView
					model: @model
					modelAttribute: 'boundingBoxes'
					ModelType: BoundingBox
					label: 'Spatial extent'
					ObjectInputView: BoundingBoxView
					multiline: true
					predefined:
						'Great Britain':
							northBoundLatitude: 60.861
							eastBoundLongitude: 1.768
							southBoundLatitude: 49.864
							westBoundLongitude: -8.648

			]
		,  
			label: 'Capability'
			title: 'Capability'
			views: [

				new TextareaView
					model: @model
					modelAttribute: 'capabilities'
					label: 'Capabilities'
					rows: 6
					helpText: """
                    	<p>Describe the facility, experimental design.  What is it equipped to do or measure?</p><p>Be informative for external partners and users</p>
                    """

				new TextareaView
					model: @model
					modelAttribute: 'lifecycle'
					label: 'Lifecycle'				
					rows: 6

				new TextareaView
					model: @model
					modelAttribute: 'uniqueness'
					label: 'Uniqueness'				
					rows: 6

			]
		,  
			label: 'Use'
			title: 'Use'
			views: [

				new TextareaView
					model: @model
					modelAttribute: 'partners'
					label: 'Partners'
					rows: 6			
									
				new ParentStringView
					model: @model
					modelAttribute: 'users'
					label: 'Users'

				new TextareaView
					model: @model
					modelAttribute: 'access'
					label: 'Access'
					rows: 6

				new TextareaView
					model: @model
					modelAttribute: 'userCosts'
					label: 'User costs'
					rows: 6

				new TextareaView
					model: @model
					modelAttribute: 'fundingSources'
					label: 'Funding sources'
					rows: 6
					helpText: """
                    	<p>Include all funding sources</p><p>Be specific about NC awards/programmes</p>
                    """
			]
		,  
			label: 'Other'
			title: 'Other'
			views: [

				new ParentView
					model: @model
					modelAttribute: 'onlineResources'
					label: 'Online resources'
					ObjectInputView: InfrastructureOnlineLinkView
					multiline: true

				new ParentView
					model: @model
					modelAttribute: 'relatedRecords'
					label: 'Related records'
					ObjectInputView: RelatedRecordView
					multiline: true
				]
		]

		EditorView.prototype.initialize.apply @