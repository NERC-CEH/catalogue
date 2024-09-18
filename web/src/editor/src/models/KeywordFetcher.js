import $ from 'jquery'
import { KeywordModel } from '../models'

export default {
  fetchKeywordsFromLegilo (model) {
    const datasetId = model.get('id')

    const apiUrl = `/documents/${datasetId}/suggestKeywords`

    return $.getJSON(apiUrl)
      .then(data => {
        return data.map(keywordData => new KeywordModel({
          name: keywordData.name,
          confidence: keywordData.confidence,
          uri: keywordData.url || ''
        }))
      })
      .catch(error => {
        console.error('Error fetching keywords:', error)
        throw error
      })
  }
}
