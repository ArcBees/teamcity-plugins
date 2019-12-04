<%@ include file="/include.jsp" %>
<%@ include file="/include-internal.jsp" %>

<jsp:useBean id="propertiesBean" type="jetbrains.buildServer.controllers.BasePropertiesBean" scope="request"/>

<script>
    BS.EditTriggersDialog.serializeParameters = function () {
        var e = BS.Util.serializeForm(this.formElement());
        var f = Form.getInputs(this.formElement())
            .filter(function(input) {
                return input.type === 'password' || input.hasAttribute('data-imitate-password');
            });
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
