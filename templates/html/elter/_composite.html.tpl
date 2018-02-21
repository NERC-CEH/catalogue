<#import "../new-form.html.tpl" as form>

<@form.value label="Observation Placeholders" class="composite">
    <ul class="list-unstyled">
        <li>
            <@form.delete name="observationPlacholder"></@form.delete>
            <a class="static-value" href="/documents/9699d939-a9e0-4ca5-816a-c2581b1c0298">
                <span class="fa fa-external-link-alt"></span><span>Go</span>
            </a>
            <@form.select name="foiType" class="observation-placeholder">
                <option value="id2"><a href="#">Second</a></option>
            </@form.select>
        </li>
        <li>
            <@form.delete name="observationPlacholder"></@form.delete>
            <@form.link name="linky" label="9699d939-a9e0-4ca5-816a-c2581b1c0298" href="http://google.com"></@form.link>
            <@form.select name="foiType" class="observation-placeholder">
                <option value="id1">First</option>
            </@form.select>
        </li>
        <li>
            <@form.delete name="observationPlacholder"></@form.delete>
            <@form.link name="linky" label="" href="#"></@form.link>
            <@form.select name="foiType" class="observation-placeholder">
                <option value="idx"></option>
                <option value="id1">First</option>
                <option value="id2">Second</option>
                <option value="other-observation-placeholder">Other</option>
            </@form.select>
        </li>
    </ul>
</@form.value>