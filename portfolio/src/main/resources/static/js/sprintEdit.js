$(() => {

    let sprintId = $("#sprintId")
    let sprintName = $("#sprintName")
    let sprintStartDate = $("#sprintStartDate")
    let sprintEndDate = $("#sprintEndDate")
    let sprintDescription = $("#sprintDescription")
    let sprintColour = $("#sprintColour")
    let sprintForm = $("#sprintEditForm")

    // Checks when the start date changes that its not past the end date.
    sprintStartDate.on("change", () => {
        let errorDiv = $(".dateFeedback")
        if (checkDateOrder(sprintStartDate.val(), sprintEndDate.val())) {
            errorDiv.text("Start date must be before end date.")
        } else {
            errorDiv.text(`Please select a date no sooner than ${minDate} and no later than ${maxDate}`)
        }
    })


    sprintEndDate.on("change", () => {
        let errorDiv = $(".dateFeedback")
        if (checkDateOrder(sprintStartDate.val(), sprintEndDate.val())) {
            errorDiv.text("Start date must be before end date.")
        } else {
            errorDiv.text(`Please select a date no sooner than ${minDate} and no later than ${maxDate}`)
        }
    })

    sprintDescription.on("input", () => {
        let descText = sprintDescription.val().toString()
        if (GENERAL_UNICODE_REGEX.test(descText)) {
            sprintDescription[0].setCustomValidity("");
        } else {
            sprintDescription[0].setCustomValidity("Sprint description can only contain unicode letters, numbers, punctuation, symbols (but not emojis) and whitespace");

        }
    })


    // When submit button is clicked on sprint edit form
    sprintForm.on("submit", function (event) {
        event.preventDefault()

        if (sprintForm[0].checkValidity()) {
            let dataToSend = {
                "sprintId": sprintId.val(),
                "sprintName": sprintName.val(),
                "sprintStartDate": sprintStartDate.val(),
                "sprintEndDate": sprintEndDate.val(),
                "sprintDescription": sprintDescription.val(),
                "sprintColour": sprintColour.val()
            }

            $.ajax({
                url: "sprintSubmit",
                type: "post",
                data: dataToSend,
                success: function () {
                    sendNotification("sprint", sprintId.val(), "update")
                    window.history.back();
                },
                error: function (error) {
                    createAlert(error.responseText, "failure")
                }
            })
        } else {
            event.stopPropagation();
            const errorElements = sprintForm.find(".form-control:invalid")
            $('html, body').animate({
                scrollTop: $(errorElements[0]).offset().top - 100
            }, 50); //Scrolls to the first invalid field of the form
        }


    })
})


/**
 * Creates a message telling to user to refresh the page to see changes made by another user, if the sprint viewed is
 * the same object that was edited.
 */
function handleUpdateEvent(message) {
    const editorName = message.editorName;
    const editorId = message.editorId;

    if (parseInt(editorId) !== parseInt(userIdent)) {
        if (message.occasionId === $(document).find("#sprintId").val())  {
            createAlert(editorName + " updated this sprint. \nPlease refresh the page to view their changes. \n" +
                "Note this will undo your changes","info")
        } else if (message.occasionType === "sprint") {
            createAlert(editorName + " updated another sprint. \nPlease refresh the page to update available dates for this sprint. \n" +
                "Note this will undo your changes","info")
        }
    }
}


/**
 * Creates a message telling to user that the sprint they're editing was deleted.
 */
function handleDeleteEvent(message) {
    const editorName = message.editorName;
    const editorId = message.editorId;

    if (parseInt(editorId) !== parseInt(userIdent)) {
        if (message.occasionId === $(document).find("#sprintId").val()) {
            createAlert(editorName + " deleted this sprint. \nThis sprint can no longer be edited.\n" +
                "Please cancel.", "info")
        }
    }
}