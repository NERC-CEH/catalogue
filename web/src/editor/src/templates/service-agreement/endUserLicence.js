import _ from 'underscore'

export default _.template(`
<div>
    <label class="radio-inline">
        <input class="ogl" type="radio" name="choice">
        Open Government Licence
    </label>
    <label class="radio-inline">
        <input class="other" type="radio" name="choice">
        Other
    </label>
</div>
<div class="resourceConstraint d-none">
    <textarea data-name="value" class="value editor-textarea" rows="2" placeholder="Please specify the licence"></textarea>
</div>
`)
