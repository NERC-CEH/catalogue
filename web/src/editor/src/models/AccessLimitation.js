import Backbone from 'backbone'

export default Backbone.Model.extend({

  defaults: {
    value: 'unknown',
    code: 'Unknown'
  },

  uris: {
    noLimitations: 'http://vocab.nerc.ac.uk/collection/N07/current/UNRS/',
    registrationRequired: 'http://vocab.nerc.ac.uk/collection/N07/current/RACC/',
    controlled: 'http://vocab.nerc.ac.uk/collection/N07/current/RAUT/',
    embargoed: 'http://vocab.nerc.ac.uk/collection/N07/current/EMBG/',
    superseded: 'http://vocab.nerc.ac.uk/collection/N07/current/RAUT/',
    withdrawn: 'http://vocab.nerc.ac.uk/collection/N07/current/RAUT/',
    deleted: 'http://vocab.nerc.ac.uk/collection/N07/current/PUNA/',
    'To access this data, a licence needs to be negotiated with the provider and there may be a cost': 'http://vocab.nerc.ac.uk/collection/N07/current/RAUT/',
    'public access limited according to Article 13(1)(a) of the INSPIRE Directive': 'http://inspire.ec.europa.eu/metadata-codelist/LimitationsOnPublicAccess/INSPIRE_Directive_Article13_1a',
    'public access limited according to Article 13(1)(b) of the INSPIRE Directive': 'http://inspire.ec.europa.eu/metadata-codelist/LimitationsOnPublicAccess/INSPIRE_Directive_Article13_1b',
    'public access limited according to Article 13(1)(c) of the INSPIRE Directive': 'http://inspire.ec.europa.eu/metadata-codelist/LimitationsOnPublicAccess/INSPIRE_Directive_Article13_1c',
    'public access limited according to Article 13(1)(d) of the INSPIRE Directive': 'http://inspire.ec.europa.eu/metadata-codelist/LimitationsOnPublicAccess/INSPIRE_Directive_Article13_1d',
    'public access limited according to Article 13(1)(e) of the INSPIRE Directive': 'http://inspire.ec.europa.eu/metadata-codelist/LimitationsOnPublicAccess/INSPIRE_Directive_Article13_1e',
    'public access limited according to Article 13(1)(f) of the INSPIRE Directive': 'http://inspire.ec.europa.eu/metadata-codelist/LimitationsOnPublicAccess/INSPIRE_Directive_Article13_1f',
    'public access limited according to Article 13(1)(g) of the INSPIRE Directive': 'http://inspire.ec.europa.eu/metadata-codelist/LimitationsOnPublicAccess/INSPIRE_Directive_Article13_1g',
    'public access limited according to Article 13(1)(h) of the INSPIRE Directive': 'http://inspire.ec.europa.eu/metadata-codelist/LimitationsOnPublicAccess/INSPIRE_Directive_Article13_1h'
  },

  availability: {
    noLimitations: 'Available',
    registrationRequired: 'Available',
    controlled: 'Controlled',
    embargoed: 'Embargoed',
    superseded: 'Superseded',
    unknown: 'Unknown',
    withdrawn: 'Withdrawn',
    deleted: 'Deleted',
    'To access this data, a licence needs to be negotiated with the provider and there may be a cost': 'Controlled',
    'public access limited according to Article 13(1)(a) of the INSPIRE Directive': 'Restricted',
    'public access limited according to Article 13(1)(b) of the INSPIRE Directive': 'Restricted',
    'public access limited according to Article 13(1)(c) of the INSPIRE Directive': 'Restricted',
    'public access limited according to Article 13(1)(d) of the INSPIRE Directive': 'Restricted',
    'public access limited according to Article 13(1)(e) of the INSPIRE Directive': 'Restricted',
    'public access limited according to Article 13(1)(f) of the INSPIRE Directive': 'Restricted',
    'public access limited according to Article 13(1)(g) of the INSPIRE Directive': 'Restricted',
    'public access limited according to Article 13(1)(h) of the INSPIRE Directive': 'Restricted'
  },

  descriptions: {
    noLimitations: 'Access to the resource is not subject to any access limitations and is available to all users without restriction.',
    registrationRequired: 'Access to the resource is limited to users who have created a user account or registered with the service providing access to the resource.',
    controlled: 'Access to the resource is limited to users who have been authorised by the data custodian or data owner in accordance with applicable policies, agreements, or licensing conditions',
    embargoed: 'Access to the resource is restricted until a specified date or the occurrence of a defined condition, after which access may be granted in accordance with applicable policies or licensing conditions',
    deleted: 'The resource has been permanently removed and is no longer available. The metadata record is retained as a tombstone or historical record.',
    superseded: 'Access to the resource is limited to users who have been authorised by the data custodian or data owner in accordance with applicable policies, agreements, or licensing conditions',
    withdrawn: 'Access to the resource is limited to users who have been authorised by the data custodian or data owner in accordance with applicable policies, agreements, or licensing conditions',
    'To access this data, a licence needs to be negotiated with the provider and there may be a cost': 'Access to the resource is limited to users who have been authorised by the data custodian or data owner in accordance with applicable policies, agreements, or licensing conditions',
    'public access limited according to Article 13(1)(a) of the INSPIRE Directive': 'Access would adversely affect the confidentiality of the proceedings of public authorities, where such confidentiality is provided for by law.',
    'public access limited according to Article 13(1)(b) of the INSPIRE Directive': 'Access would adversely affect international relations, public security or national defence.',
    'public access limited according to Article 13(1)(c) of the INSPIRE Directive': 'Access would adversely affect the course of justice, the ability of any person to receive a fair trial or the ability of a public authority to conduct an enquiry of a criminal or disciplinary nature.',
    'public access limited according to Article 13(1)(d) of the INSPIRE Directive': 'Access would adversely affect the confidentiality of commercial or industrial information, where such confidentiality is provided for by national or Community law to protect a legitimate economic interest, including the public interest in maintaining statistical confidentiality and tax secrecy.',
    'public access limited according to Article 13(1)(e) of the INSPIRE Directive': 'Access would adversely affect intellectual property rights.',
    'public access limited according to Article 13(1)(f) of the INSPIRE Directive': 'Access would adversely affect the confidentiality of personal data and/or files relating to a natural person where that person has not consented to the disclosure of the information to the public, where such confidentiality is provided for by national or Community law.',
    'public access limited according to Article 13(1)(g) of the INSPIRE Directive': 'Accesss would adversely affect the interests or protection of any person who supplied the information requested on a voluntary basis without being under, or capable of being put under, a legal obligation to do so, unless that person has consented to the release of the information concerned.',
    'public access limited according to Article 13(1)(h) of the INSPIRE Directive': 'Access would adversely affect the protection of the environment to which such information relates.'
  },

  initialize () {
    this.on('change:value', this.updateLimitation)
  },

  updateLimitation (model, value) {
    this.set('uri', this.uris[value] ? this.uris[value] : null)
    this.set('availability', this.availability[value] ? this.availability[value] : null)
    this.set('description', this.descriptions[value] ? this.descriptions[value] : null)
  }
})
