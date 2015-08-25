<%@ include file="/include.jsp" %>
<%@ include file="/include-internal.jsp" %>

<jsp:useBean id="propertiesBean" type="jetbrains.buildServer.controllers.BasePropertiesBean" scope="request"/>
<jsp:useBean id="pullKeys" class="com.arcbees.pullrequest.Constants"/>

<script>
    BS.EditTriggersDialog.serializeParameters = function () {
        var e = BS.Util.serializeForm(this.formElement());
        var f = Form.getInputs(this.formElement(), "password");
        if (!f) {
            return e;
        }
        for (var c = 0; c < f.length; c++) {
            var b = f[c].name;
            if (b.indexOf("prop:") != 0) {
                continue;
            }
            var a = "prop:encrypted:" + f[c].id;
            e += "&" + a + "=";
            var d = $(a).value;
            if (d == "" || f[c].value != f[c].defaultValue) {
                d = BS.Encrypt.encryptData(f[c].value, this.formElement().publicKey.value);
            }
            e += d;
        }
        return e;
    }
</script>

<%@ include file="vcsSettings.jsp" %>
<tr>
    <th>Base branch:<l:star/></th>
    <td>
        <props:textProperty name="${keys.baseBranchKey}" className="longField"/>
        <span class="error" id="error_${keys.baseBranchKey}"></span>
        <span class="smallNote">Base VCS branch</span>
    </td>
</tr>
<l:settingsGroup title="Behaviour">
    <tr>
        <th>Approve on success:</th>
        <td>
            <props:checkboxProperty name="${pullKeys.approveOnSuccessKey}"/>
            <span class="error" id="error_${pullKeys.approveOnSuccessKey}"></span>
            <span class="smallNote">User should approve request on build success</span>
        </td>
    </tr>
</l:settingsGroup>
