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
 * @returns {boolean} returns true if userRole is above student.
 */
function checkPrivilege() {
    return userRoles.includes('COURSE_ADMINISTRATOR') || userRoles.includes('TEACHER');
}


/**
 * Returns true if the user has the Admin role
 */
function isAdmin() {
    return userRoles.includes('COURSE_ADMINISTRATOR')
}


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
 * @param alertMessage
 * @param isRed
 */
function createAlert(alertMessage, isRed) {


    let alertDiv = `<div id="alertPopUp" style="display: none">
                     <p id="alertPopUpMessage">${alertMessage}</p>
                     <button id="alertPopUpCloseButton" onclick="removeAlert()" class="noStyleButton"><svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor"  class="bi bi-x-circle" viewBox="0 0 16 16">
                         <path d="M8 15A7 7 0 1 1 8 1a7 7 0 0 1 0 14zm0 1A8 8 0 1 0 8 0a8 8 0 0 0 0 16z"/>
                         <path d="M4.646 4.646a.5.5 0 0 1 .708 0L8 7.293l2.646-2.647a.5.5 0 0 1 .708.708L8.707 8l2.647 2.646a.5.5 0 0 1-.708.708L8 8.707l-2.646 2.647a.5.5 0 0 1-.708-.708L7.293 8 4.646 5.354a.5.5 0 0 1 0-.708z"/>
                     </svg></button>
                     
                 </div>`

    $("body").append(alertDiv)
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

