import _ from 'underscore'

export default _.template(`
<select data-name="value" class="editor-input">
    <option value="" selected >- Add tissue -</option>
    <option value="Bone">Bone</option>
    <option value="Brain">Brain</option>
    <option value="Egg">Egg</option>
    <option value="Fat">Fat</option>
    <option value="Feather">Feather</option>
    <option value="Fur">Fur</option>
    <option value="Gut contents">Gut contents</option>
    <option value="Heart">Heart</option>
    <option value="Homogenised whole sample">Homogenised whole sample</option>
    <option value="Kidney">Kidney</option>
    <option value="Liver">Liver</option>
    <option value="Lung">Lung</option>
    <option value="Lymph node">Lymph node</option>
    <option value="Muscle">Muscle</option>
    <option value="Nerve/spinal cord">Nerve/spinal cord</option>
    <option value="Plasma">Plasma</option>
    <option value="Serum">Serum</option>
    <option value="Skin ">Skin </option>
    <option value="Spleen">Spleen</option>
    <option value="Trachea">Trachea</option>
    <option value="Whole blood">Whole blood</option>
    <option value="Whole body">Whole body</option>
</select>
`)
