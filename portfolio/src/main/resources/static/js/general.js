$(document).ready(function () {
    // Checks to see if there is an error message to be displayed
    if (!$(".errorMessage").is(':empty')) {
        $(".errorMessageParent").show();
    }
    if (!$(".infoMessage").is(':empty')) {
        $(".infoMessageParent").show();
    }

    if (!$(".successMessage").is(':empty')) {
        $(".successMessageParent").show();
    }

    let tooltipTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="tooltip"]'))
    let tooltipList = tooltipTriggerList.map(function (tooltipTriggerEl) {
        return new bootstrap.Tooltip(tooltipTriggerEl)
    })

    removeElementIfNotAuthorized()
});


/**
 * Checks if a user has a role above student.
 *
 * @returns {boolean} returns true if userRole is above student.
 */
function checkPrivilege() {
    return userRoles.includes('COURSE_ADMINISTRATOR') || userRoles.includes('TEACHER');
}


/**
 * Returns true if the user has the Admin role.
 */
function isAdmin() {
    return userRoles.includes('COURSE_ADMINISTRATOR')
}


/**
 * Removes an element requiring teacher/admin permissions if the user does not have teacher/admin privileges.
 */
function removeElementIfNotAuthorized() {
    if (!checkPrivilege()) {
        $(".hasTeacherOrAbove").remove()
    }
}


/**
 * Helper function to remove alert element.
 */
function removeAlert() {
    let alert = $("#alertPopUp")
    alert.hide("slide", 100, function() {
        alert.remove();
    })
}


/**
 * Displays a dismissible alert down the bottom right of the screen.
 * If isRed is true, the background colour will be red, otherwise green.
 *
 * @param alertMessage
 * @param isRed
 * @param window - the location to show the error
 */
function createAlert(alertMessage, isRed, window = "body") {
    let CheckAlert = $("#alertPopUp")
    if (CheckAlert.is(":visible")) {
        CheckAlert.hide("slide", 100, function() {
            CheckAlert.remove();
        }).promise().done(function() { // If the alert is already displayed it removes it and then once that is done, runs the alert function
            alert(alertMessage, isRed, window)
        })
    } else {
        alert(alertMessage, isRed, window)
    }
}


function alert(alertMessage, isRed, window = "body") {
    let alertDiv = `<div id="alertPopUp" style="display: none">
                     <p id="alertPopUpMessage">${alertMessage}</p>
                     <button id="alertPopUpCloseButton" onclick="removeAlert()" class="noStyleButton"><svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor"  class="bi bi-x-circle" viewBox="0 0 16 16">
                         <path d="M8 15A7 7 0 1 1 8 1a7 7 0 0 1 0 14zm0 1A8 8 0 1 0 8 0a8 8 0 0 0 0 16z"/>
                         <path d="M4.646 4.646a.5.5 0 0 1 .708 0L8 7.293l2.646-2.647a.5.5 0 0 1 .708.708L8.707 8l2.647 2.646a.5.5 0 0 1-.708.708L8 8.707l-2.646 2.647a.5.5 0 0 1-.708-.708L7.293 8 4.646 5.354a.5.5 0 0 1 0-.708z"/>
                     </svg></button>
                     
                 </div>`

    $(window).append(alertDiv)
    let alert = $("#alertPopUp")

    if(isRed) {
        alert.removeClass("backgroundGreen")
        alert.addClass("backgroundRed")
    } else {
        alert.addClass("backgroundGreen")
        alert.removeClass("backgroundRed")
    }
    alert.show("slide", 100)
}


/**
 * Removes HTML tags from a string
 * See https://stackoverflow.com/questions/2794137/sanitizing-user-input-before-adding-it-to-the-dom-in-javascript
 *
 * @param string The string to sanitise
 * @returns A sanitised string
 */
function sanitise(string) {
    const map = {
        '&': '&amp;',
        '<': '&lt;',
        '>': '&gt;',
        '"': '&quot;',
        "'": '&#x27;',
        "/": '&#x2F;',
    };
    const reg = /[&<>"'/]/ig;
    return string.replace(reg, (match)=>(map[match]));
}


/**
 * Function that gets the maxlength of an input field and lets the user know how many characters they have left.
 */
function countCharacters() {
    let maxlength = $(this).attr("maxLength")
    let lengthOfCurrentInput = $(this).val().length;
    let counter = maxlength - lengthOfCurrentInput;
    let helper = $(this).next(".form-text-counted"); //Gets the next div with a class that is form-text

    //If one character remains, changes from "characters remaining" to "character remaining"
    if (counter !== 1) {
        helper.text(counter + " characters remaining")
    } else {
        helper.text(counter + " character remaining")
    }
}

/**
 * Regex that is all unicode letters, numbers and punctuation
 * This is equivalent to \p{L} \p{Nd}\ p{P}
 */
let regex = new RegExp("[!-#%-*,-;?-\\]_a-\\{\\}\u00A1\u00A7\u00AA\u00AB\u00B5-\u00B7\u00BA\u00BB\u00BF-\u00D6\u00D8-\u00F6\u00F8-\u02C1\u02C6-\u02D1\u02E0-\u02E4\u02EC\u02EE\u0370-\u0374\u0376\u0377\u037A-\u037F\u0386-\u038A\u038C\u038E-\u03A1\u03A3-\u03F5\u03F7-\u0481\u048A-\u052F\u0531-\u0556\u0559-\u058A\u05BE\u05C0\u05C3\u05C6\u05D0-\u05EA\u05EF-\u05F4\u0609\u060A\u060C\u060D\u061B\u061D-\u064A\u0660-\u066F\u0671-\u06D5\u06E5\u06E6\u06EE-\u06FC\u06FF-\u070D\u0710\u0712-\u072F\u074D-\u07A5\u07B1\u07C0-\u07EA\u07F4\u07F5\u07F7-\u07FA\u0800-\u0815\u081A\u0824\u0828\u0830-\u083E\u0840-\u0858\u085E\u0860-\u086A\u0870-\u0887\u0889-\u088E\u08A0-\u08C9\u0904-\u0939\u093D\u0950\u0958-\u0961\u0964-\u0980\u0985-\u098C\u098F\u0990\u0993-\u09A8\u09AA-\u09B0\u09B2\u09B6-\u09B9\u09BD\u09CE\u09DC\u09DD\u09DF-\u09E1\u09E6-\u09F1\u09FC\u09FD\u0A05-\u0A0A\u0A0F\u0A10\u0A13-\u0A28\u0A2A-\u0A30\u0A32\u0A33\u0A35\u0A36\u0A38\u0A39\u0A59-\u0A5C\u0A5E\u0A66-\u0A6F\u0A72-\u0A74\u0A76\u0A85-\u0A8D\u0A8F-\u0A91\u0A93-\u0AA8\u0AAA-\u0AB0\u0AB2\u0AB3\u0AB5-\u0AB9\u0ABD\u0AD0\u0AE0\u0AE1\u0AE6-\u0AF0\u0AF9\u0B05-\u0B0C\u0B0F\u0B10\u0B13-\u0B28\u0B2A-\u0B30\u0B32\u0B33\u0B35-\u0B39\u0B3D\u0B5C\u0B5D\u0B5F-\u0B61\u0B66-\u0B6F\u0B71\u0B83\u0B85-\u0B8A\u0B8E-\u0B90\u0B92-\u0B95\u0B99\u0B9A\u0B9C\u0B9E\u0B9F\u0BA3\u0BA4\u0BA8-\u0BAA\u0BAE-\u0BB9\u0BD0\u0BE6-\u0BEF\u0C05-\u0C0C\u0C0E-\u0C10\u0C12-\u0C28\u0C2A-\u0C39\u0C3D\u0C58-\u0C5A\u0C5D\u0C60\u0C61\u0C66-\u0C6F\u0C77\u0C80\u0C84-\u0C8C\u0C8E-\u0C90\u0C92-\u0CA8\u0CAA-\u0CB3\u0CB5-\u0CB9\u0CBD\u0CDD\u0CDE\u0CE0\u0CE1\u0CE6-\u0CEF\u0CF1\u0CF2\u0D04-\u0D0C\u0D0E-\u0D10\u0D12-\u0D3A\u0D3D\u0D4E\u0D54-\u0D56\u0D5F-\u0D61\u0D66-\u0D6F\u0D7A-\u0D7F\u0D85-\u0D96\u0D9A-\u0DB1\u0DB3-\u0DBB\u0DBD\u0DC0-\u0DC6\u0DE6-\u0DEF\u0DF4\u0E01-\u0E30\u0E32\u0E33\u0E40-\u0E46\u0E4F-\u0E5B\u0E81\u0E82\u0E84\u0E86-\u0E8A\u0E8C-\u0EA3\u0EA5\u0EA7-\u0EB0\u0EB2\u0EB3\u0EBD\u0EC0-\u0EC4\u0EC6\u0ED0-\u0ED9\u0EDC-\u0EDF\u0F00\u0F04-\u0F12\u0F14\u0F20-\u0F29\u0F3A-\u0F3D\u0F40-\u0F47\u0F49-\u0F6C\u0F85\u0F88-\u0F8C\u0FD0-\u0FD4\u0FD9\u0FDA\u1000-\u102A\u103F-\u1055\u105A-\u105D\u1061\u1065\u1066\u106E-\u1070\u1075-\u1081\u108E\u1090-\u1099\u10A0-\u10C5\u10C7\u10CD\u10D0-\u1248\u124A-\u124D\u1250-\u1256\u1258\u125A-\u125D\u1260-\u1288\u128A-\u128D\u1290-\u12B0\u12B2-\u12B5\u12B8-\u12BE\u12C0\u12C2-\u12C5\u12C8-\u12D6\u12D8-\u1310\u1312-\u1315\u1318-\u135A\u1360-\u1368\u1380-\u138F\u13A0-\u13F5\u13F8-\u13FD\u1400-\u166C\u166E-\u167F\u1681-\u169C\u16A0-\u16ED\u16F1-\u16F8\u1700-\u1711\u171F-\u1731\u1735\u1736\u1740-\u1751\u1760-\u176C\u176E-\u1770\u1780-\u17B3\u17D4-\u17DA\u17DC\u17E0-\u17E9\u1800-\u180A\u1810-\u1819\u1820-\u1878\u1880-\u1884\u1887-\u18A8\u18AA\u18B0-\u18F5\u1900-\u191E\u1944-\u196D\u1970-\u1974\u1980-\u19AB\u19B0-\u19C9\u19D0-\u19D9\u1A00-\u1A16\u1A1E-\u1A54\u1A80-\u1A89\u1A90-\u1A99\u1AA0-\u1AAD\u1B05-\u1B33\u1B45-\u1B4C\u1B50-\u1B60\u1B7D\u1B7E\u1B83-\u1BA0\u1BAE-\u1BE5\u1BFC-\u1C23\u1C3B-\u1C49\u1C4D-\u1C88\u1C90-\u1CBA\u1CBD-\u1CC7\u1CD3\u1CE9-\u1CEC\u1CEE-\u1CF3\u1CF5\u1CF6\u1CFA\u1D00-\u1DBF\u1E00-\u1F15\u1F18-\u1F1D\u1F20-\u1F45\u1F48-\u1F4D\u1F50-\u1F57\u1F59\u1F5B\u1F5D\u1F5F-\u1F7D\u1F80-\u1FB4\u1FB6-\u1FBC\u1FBE\u1FC2-\u1FC4\u1FC6-\u1FCC\u1FD0-\u1FD3\u1FD6-\u1FDB\u1FE0-\u1FEC\u1FF2-\u1FF4\u1FF6-\u1FFC\u2010-\u2027\u2030-\u2043\u2045-\u2051\u2053-\u205E\u2071\u207D-\u207F\u208D\u208E\u2090-\u209C\u2102\u2107\u210A-\u2113\u2115\u2119-\u211D\u2124\u2126\u2128\u212A-\u212D\u212F-\u2139\u213C-\u213F\u2145-\u2149\u214E\u2183\u2184\u2308-\u230B\u2329\u232A\u2768-\u2775\u27C5\u27C6\u27E6-\u27EF\u2983-\u2998\u29D8-\u29DB\u29FC\u29FD\u2C00-\u2CE4\u2CEB-\u2CEE\u2CF2\u2CF3\u2CF9-\u2CFC\u2CFE-\u2D25\u2D27\u2D2D\u2D30-\u2D67\u2D6F\u2D70\u2D80-\u2D96\u2DA0-\u2DA6\u2DA8-\u2DAE\u2DB0-\u2DB6\u2DB8-\u2DBE\u2DC0-\u2DC6\u2DC8-\u2DCE\u2DD0-\u2DD6\u2DD8-\u2DDE\u2E00-\u2E4F\u2E52-\u2E5D\u3001-\u3003\u3005\u3006\u3008-\u3011\u3014-\u301F\u3030-\u3035\u303B-\u303D\u3041-\u3096\u309D-\u30FF\u3105-\u312F\u3131-\u318E\u31A0-\u31BF\u31F0-\u31FF\u3400-\u4DBF\u4E00-\uA48C\uA4D0-\uA62B\uA640-\uA66E\uA673\uA67E-\uA69D\uA6A0-\uA6E5\uA6F2-\uA6F7\uA717-\uA71F\uA722-\uA788\uA78B-\uA7CA\uA7D0\uA7D1\uA7D3\uA7D5-\uA7D9\uA7F2-\uA801\uA803-\uA805\uA807-\uA80A\uA80C-\uA822\uA840-\uA877\uA882-\uA8B3\uA8CE-\uA8D9\uA8F2-\uA8FE\uA900-\uA925\uA92E-\uA946\uA95F-\uA97C\uA984-\uA9B2\uA9C1-\uA9CD\uA9CF-\uA9D9\uA9DE-\uA9E4\uA9E6-\uA9FE\uAA00-\uAA28\uAA40-\uAA42\uAA44-\uAA4B\uAA50-\uAA59\uAA5C-\uAA76\uAA7A\uAA7E-\uAAAF\uAAB1\uAAB5\uAAB6\uAAB9-\uAABD\uAAC0\uAAC2\uAADB-\uAAEA\uAAF0-\uAAF4\uAB01-\uAB06\uAB09-\uAB0E\uAB11-\uAB16\uAB20-\uAB26\uAB28-\uAB2E\uAB30-\uAB5A\uAB5C-\uAB69\uAB70-\uABE2\uABEB\uABF0-\uABF9\uAC00-\uD7A3\uD7B0-\uD7C6\uD7CB-\uD7FB\uF900-\uFA6D\uFA70-\uFAD9\uFB00-\uFB06\uFB13-\uFB17\uFB1D\uFB1F-\uFB28\uFB2A-\uFB36\uFB38-\uFB3C\uFB3E\uFB40\uFB41\uFB43\uFB44\uFB46-\uFBB1\uFBD3-\uFD3F\uFD50-\uFD8F\uFD92-\uFDC7\uFDF0-\uFDFB\uFE10-\uFE19\uFE30-\uFE52\uFE54-\uFE61\uFE63\uFE68\uFE6A\uFE6B\uFE70-\uFE74\uFE76-\uFEFC\uFF01-\uFF03\uFF05-\uFF0A\uFF0C-\uFF1B\uFF1F-\uFF3D\uFF3F\uFF41-\uFF5B\uFF5D\uFF5F-\uFFBE\uFFC2-\uFFC7\uFFCA-\uFFCF\uFFD2-\uFFD7\uFFDA-\uFFDC\\U00010000-\\U0001000B\\U0001000D-\\U00010026\\U00010028-\\U0001003A\\U0001003C\\U0001003D\\U0001003F-\\U0001004D\\U00010050-\\U0001005D\\U00010080-\\U000100FA\\U00010100-\\U00010102\\U00010280-\\U0001029C\\U000102A0-\\U000102D0\\U00010300-\\U0001031F\\U0001032D-\\U00010340\\U00010342-\\U00010349\\U00010350-\\U00010375\\U00010380-\\U0001039D\\U0001039F-\\U000103C3\\U000103C8-\\U000103D0\\U00010400-\\U0001049D\\U000104A0-\\U000104A9\\U000104B0-\\U000104D3\\U000104D8-\\U000104FB\\U00010500-\\U00010527\\U00010530-\\U00010563\\U0001056F-\\U0001057A\\U0001057C-\\U0001058A\\U0001058C-\\U00010592\\U00010594\\U00010595\\U00010597-\\U000105A1\\U000105A3-\\U000105B1\\U000105B3-\\U000105B9\\U000105BB\\U000105BC\\U00010600-\\U00010736\\U00010740-\\U00010755\\U00010760-\\U00010767\\U00010780-\\U00010785\\U00010787-\\U000107B0\\U000107B2-\\U000107BA\\U00010800-\\U00010805\\U00010808\\U0001080A-\\U00010835\\U00010837\\U00010838\\U0001083C\\U0001083F-\\U00010855\\U00010857\\U00010860-\\U00010876\\U00010880-\\U0001089E\\U000108E0-\\U000108F2\\U000108F4\\U000108F5\\U00010900-\\U00010915\\U0001091F-\\U00010939\\U0001093F\\U00010980-\\U000109B7\\U000109BE\\U000109BF\\U00010A00\\U00010A10-\\U00010A13\\U00010A15-\\U00010A17\\U00010A19-\\U00010A35\\U00010A50-\\U00010A58\\U00010A60-\\U00010A7C\\U00010A7F-\\U00010A9C\\U00010AC0-\\U00010AC7\\U00010AC9-\\U00010AE4\\U00010AF0-\\U00010AF6\\U00010B00-\\U00010B35\\U00010B39-\\U00010B55\\U00010B60-\\U00010B72\\U00010B80-\\U00010B91\\U00010B99-\\U00010B9C\\U00010C00-\\U00010C48\\U00010C80-\\U00010CB2\\U00010CC0-\\U00010CF2\\U00010D00-\\U00010D23\\U00010D30-\\U00010D39\\U00010E80-\\U00010EA9\\U00010EAD\\U00010EB0\\U00010EB1\\U00010F00-\\U00010F1C\\U00010F27\\U00010F30-\\U00010F45\\U00010F55-\\U00010F59\\U00010F70-\\U00010F81\\U00010F86-\\U00010F89\\U00010FB0-\\U00010FC4\\U00010FE0-\\U00010FF6\\U00011003-\\U00011037\\U00011047-\\U0001104D\\U00011066-\\U0001106F\\U00011071\\U00011072\\U00011075\\U00011083-\\U000110AF\\U000110BB\\U000110BC\\U000110BE-\\U000110C1\\U000110D0-\\U000110E8\\U000110F0-\\U000110F9\\U00011103-\\U00011126\\U00011136-\\U00011144\\U00011147\\U00011150-\\U00011172\\U00011174-\\U00011176\\U00011183-\\U000111B2\\U000111C1-\\U000111C8\\U000111CD\\U000111D0-\\U000111DF\\U00011200-\\U00011211\\U00011213-\\U0001122B\\U00011238-\\U0001123D\\U00011280-\\U00011286\\U00011288\\U0001128A-\\U0001128D\\U0001128F-\\U0001129D\\U0001129F-\\U000112A9\\U000112B0-\\U000112DE\\U000112F0-\\U000112F9\\U00011305-\\U0001130C\\U0001130F\\U00011310\\U00011313-\\U00011328\\U0001132A-\\U00011330\\U00011332\\U00011333\\U00011335-\\U00011339\\U0001133D\\U00011350\\U0001135D-\\U00011361\\U00011400-\\U00011434\\U00011447-\\U0001145B\\U0001145D\\U0001145F-\\U00011461\\U00011480-\\U000114AF\\U000114C4-\\U000114C7\\U000114D0-\\U000114D9\\U00011580-\\U000115AE\\U000115C1-\\U000115DB\\U00011600-\\U0001162F\\U00011641-\\U00011644\\U00011650-\\U00011659\\U00011660-\\U0001166C\\U00011680-\\U000116AA\\U000116B8\\U000116B9\\U000116C0-\\U000116C9\\U00011700-\\U0001171A\\U00011730-\\U00011739\\U0001173C-\\U0001173E\\U00011740-\\U00011746\\U00011800-\\U0001182B\\U0001183B\\U000118A0-\\U000118E9\\U000118FF-\\U00011906\\U00011909\\U0001190C-\\U00011913\\U00011915\\U00011916\\U00011918-\\U0001192F\\U0001193F\\U00011941\\U00011944-\\U00011946\\U00011950-\\U00011959\\U000119A0-\\U000119A7\\U000119AA-\\U000119D0\\U000119E1-\\U000119E3\\U00011A00\\U00011A0B-\\U00011A32\\U00011A3A\\U00011A3F-\\U00011A46\\U00011A50\\U00011A5C-\\U00011A89\\U00011A9A-\\U00011AA2\\U00011AB0-\\U00011AF8\\U00011C00-\\U00011C08\\U00011C0A-\\U00011C2E\\U00011C40-\\U00011C45\\U00011C50-\\U00011C59\\U00011C70-\\U00011C8F\\U00011D00-\\U00011D06\\U00011D08\\U00011D09\\U00011D0B-\\U00011D30\\U00011D46\\U00011D50-\\U00011D59\\U00011D60-\\U00011D65\\U00011D67\\U00011D68\\U00011D6A-\\U00011D89\\U00011D98\\U00011DA0-\\U00011DA9\\U00011EE0-\\U00011EF2\\U00011EF7\\U00011EF8\\U00011FB0\\U00011FFF-\\U00012399\\U00012470-\\U00012474\\U00012480-\\U00012543\\U00012F90-\\U00012FF2\\U00013000-\\U0001342E\\U00014400-\\U00014646\\U00016800-\\U00016A38\\U00016A40-\\U00016A5E\\U00016A60-\\U00016A69\\U00016A6E-\\U00016ABE\\U00016AC0-\\U00016AC9\\U00016AD0-\\U00016AED\\U00016AF5\\U00016B00-\\U00016B2F\\U00016B37-\\U00016B3B\\U00016B40-\\U00016B44\\U00016B50-\\U00016B59\\U00016B63-\\U00016B77\\U00016B7D-\\U00016B8F\\U00016E40-\\U00016E7F\\U00016E97-\\U00016E9A\\U00016F00-\\U00016F4A\\U00016F50\\U00016F93-\\U00016F9F\\U00016FE0-\\U00016FE3\\U00017000-\\U000187F7\\U00018800-\\U00018CD5\\U00018D00-\\U00018D08\\U0001AFF0-\\U0001AFF3\\U0001AFF5-\\U0001AFFB\\U0001AFFD\\U0001AFFE\\U0001B000-\\U0001B122\\U0001B150-\\U0001B152\\U0001B164-\\U0001B167\\U0001B170-\\U0001B2FB\\U0001BC00-\\U0001BC6A\\U0001BC70-\\U0001BC7C\\U0001BC80-\\U0001BC88\\U0001BC90-\\U0001BC99\\U0001BC9F\\U0001D400-\\U0001D454\\U0001D456-\\U0001D49C\\U0001D49E\\U0001D49F\\U0001D4A2\\U0001D4A5\\U0001D4A6\\U0001D4A9-\\U0001D4AC\\U0001D4AE-\\U0001D4B9\\U0001D4BB\\U0001D4BD-\\U0001D4C3\\U0001D4C5-\\U0001D505\\U0001D507-\\U0001D50A\\U0001D50D-\\U0001D514\\U0001D516-\\U0001D51C\\U0001D51E-\\U0001D539\\U0001D53B-\\U0001D53E\\U0001D540-\\U0001D544\\U0001D546\\U0001D54A-\\U0001D550\\U0001D552-\\U0001D6A5\\U0001D6A8-\\U0001D6C0\\U0001D6C2-\\U0001D6DA\\U0001D6DC-\\U0001D6FA\\U0001D6FC-\\U0001D714\\U0001D716-\\U0001D734\\U0001D736-\\U0001D74E\\U0001D750-\\U0001D76E\\U0001D770-\\U0001D788\\U0001D78A-\\U0001D7A8\\U0001D7AA-\\U0001D7C2\\U0001D7C4-\\U0001D7CB\\U0001D7CE-\\U0001D7FF\\U0001DA87-\\U0001DA8B\\U0001DF00-\\U0001DF1E\\U0001E100-\\U0001E12C\\U0001E137-\\U0001E13D\\U0001E140-\\U0001E149\\U0001E14E\\U0001E290-\\U0001E2AD\\U0001E2C0-\\U0001E2EB\\U0001E2F0-\\U0001E2F9\\U0001E7E0-\\U0001E7E6\\U0001E7E8-\\U0001E7EB\\U0001E7ED\\U0001E7EE\\U0001E7F0-\\U0001E7FE\\U0001E800-\\U0001E8C4\\U0001E900-\\U0001E943\\U0001E94B\\U0001E950-\\U0001E959\\U0001E95E\\U0001E95F\\U0001EE00-\\U0001EE03\\U0001EE05-\\U0001EE1F\\U0001EE21\\U0001EE22\\U0001EE24\\U0001EE27\\U0001EE29-\\U0001EE32\\U0001EE34-\\U0001EE37\\U0001EE39\\U0001EE3B\\U0001EE42\\U0001EE47\\U0001EE49\\U0001EE4B\\U0001EE4D-\\U0001EE4F\\U0001EE51\\U0001EE52\\U0001EE54\\U0001EE57\\U0001EE59\\U0001EE5B\\U0001EE5D\\U0001EE5F\\U0001EE61\\U0001EE62\\U0001EE64\\U0001EE67-\\U0001EE6A\\U0001EE6C-\\U0001EE72\\U0001EE74-\\U0001EE77\\U0001EE79-\\U0001EE7C\\U0001EE7E\\U0001EE80-\\U0001EE89\\U0001EE8B-\\U0001EE9B\\U0001EEA1-\\U0001EEA3\\U0001EEA5-\\U0001EEA9\\U0001EEAB-\\U0001EEBB\\U0001FBF0-\\U0001FBF9\\U00020000-\\U0002A6DF\\U0002A700-\\U0002B738\\U0002B740-\\U0002B81D\\U0002B820-\\U0002CEA1\\U0002CEB0-\\U0002EBE0\\U0002F800-\\U0002FA1D\\U00030000-\\U0003134A]+")