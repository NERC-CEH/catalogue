import _ from 'underscore'

export default _.template(`
<select data-name="value" class="editor-input">
    <option value="" selected >- Add physical state -</option>
    <option value="Air dried">Air dried</option>
    <option value="Chemical extract">Chemical extract</option>
    <option value="Chilled (refrigerated)">Chilled (refrigerated)</option>
    <option value="Fixed in formalin">Fixed in formalin</option>
    <option value="Formalin-Fixed Paraffin-Embedded (FFPE) tissue">Formalin-Fixed Paraffin-Embedded (FFPE) tissue</option>
    <option value="Freeze dried">Freeze dried</option>
    <option value="Fresh">Fresh</option>
    <option value="Frozen (-198 degrees C)">Frozen (-198 degrees C)</option>
    <option value="Frozen (-20 degrees C)">Frozen (-20 degrees C)</option>
    <option value="Frozen (-80 degrees C)">Frozen (-80 degrees C)</option>
    <option value="Natural state">Natural state</option>
    <option value="Oven dry">Oven dry</option>
    <option value="Preserved">Preserved</option>
    <option value="Preserved in alcohol">Preserved in alcohol</option>
    <option value="Slide">Slide</option>
    <option value="Taxidermy">Taxidermy</option>
    <option value="Under liquid nitrogen">Under liquid nitrogen</option>
</select>
`)
