import _ from 'underscore'

export default _.template(`
<select data-name="value" class="editor-input purposeOfCollection">
  <option value="contractual">Contractual obligation</option>
  <option value="dataCollection">Data collection</option>
  <option value="dataSeries">Data series</option>
  <option value="cooperation">Duty of cooperation</option>
  <option value="emergencyResponse">Emergency response</option>
  <option value="innovation">Innovation/advancing science</option>
  <option value="international">International collaboration</option>
  <option value="legislative">Legislative</option>
  <option value="competence">Maintaining competence</option>
  <option value="ministerialCommitment">Ministerial commitment</option>
  <option value="modelling">Modelling</option>
  <option value="moral">Moral obligation</option>
  <option value="policy">Policy</option>
  <option value="statuoryAdvice">Statuory advice</option>
  <option value="strategic">Strategic goals</option>
</select>
`)
