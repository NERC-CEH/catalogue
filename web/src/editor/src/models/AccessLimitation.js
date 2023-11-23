import Backbone from 'backbone'

export default Backbone.Model.extend({

  defaults: {
    value: 'unknown',
    code: 'Unknown'
  },

  uris: {
    'Registration is required to access this data': 'https://www.eidc.ac.uk/help/faq/registration',
    'no limitations to public access': 'http://inspire.ec.europa.eu/metadata-codelist/LimitationsOnPublicAccess/noLimitations',
    embargoed: 'https://www.eidc.ac.uk/help/faq/embargos',
    'public access limited according to Article 13(1)(a) of the INSPIRE Directive': 'http://inspire.ec.europa.eu/metadata-codelist/LimitationsOnPublicAccess/INSPIRE_Directive_Article13_1a',
    'public access limited according to Article 13(1)(b) of the INSPIRE Directive': 'http://inspire.ec.europa.eu/metadata-codelist/LimitationsOnPublicAccess/INSPIRE_Directive_Article13_1b',
    'public access limited according to Article 13(1)(c) of the INSPIRE Directive': 'http://inspire.ec.europa.eu/metadata-codelist/LimitationsOnPublicAccess/INSPIRE_Directive_Article13_1c',
    'public access limited according to Article 13(1)(d) of the INSPIRE Directive': 'http://inspire.ec.europa.eu/metadata-codelist/LimitationsOnPublicAccess/INSPIRE_Directive_Article13_1d',
    'public access limited according to Article 13(1)(e) of the INSPIRE Directive': 'http://inspire.ec.europa.eu/metadata-codelist/LimitationsOnPublicAccess/INSPIRE_Directive_Article13_1e',
    'public access limited according to Article 13(1)(f) of the INSPIRE Directive': 'http://inspire.ec.europa.eu/metadata-codelist/LimitationsOnPublicAccess/INSPIRE_Directive_Article13_1f',
    'public access limited according to Article 13(1)(g) of the INSPIRE Directive': 'http://inspire.ec.europa.eu/metadata-codelist/LimitationsOnPublicAccess/INSPIRE_Directive_Article13_1g',
    'public access limited according to Article 13(1)(h) of the INSPIRE Directive': 'http://inspire.ec.europa.eu/metadata-codelist/LimitationsOnPublicAccess/INSPIRE_Directive_Article13_1h'
  },

  codes: {
    'Registration is required to access this data': 'Available',
    'no limitations to public access': 'Available',
    embargoed: 'Embargoed',
    'in-progress': 'In-progress',
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
    this.set('code', this.codes[value] ? this.codes[value] : null)
    this.set('description', this.descriptions[value] ? this.descriptions[value] : null)
  }
})
