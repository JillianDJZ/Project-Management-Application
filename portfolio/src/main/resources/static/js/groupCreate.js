$(document).ready(() => {
    //Jquery selectors to remove duplicity
    let shortName = $("#shortName")
    let longName = $("#longName")
    let formControl = $(".form-control");
    formControl.each(countCharacters)
    formControl.keyup(countCharacters)

    // On create group form submit
    $("#createGroupForm").submit(function (event) {
        event.preventDefault(); // Prevents submit

        let groupData = {
            "shortName": shortName.val(),
            "longName": longName.val(),
        }

        $.ajax({
            url: "groups/edit",
            type: "post",
            data: groupData,
            success: function () {
                location.href = "groups" // On success reloads page
            },
            error: function (error) {//Displays error in box on failure
                createAlert(error.responseText, true)
            }
        })
    })

    /**
     * Function that gets the maxlength of an input and lets the user know how many characters they have left.
     */
    function countCharacters() {
        let maxlength = $(this).attr("maxLength")
        let lengthOfCurrentInput = $(this).val().length;
        let counter = maxlength - lengthOfCurrentInput;
        let helper = $(this).next(".form-text"); //Gets the next div with a class that is form-text

        //If one character remains, changes from "characters remaining" to "character remaining"
        if (counter !== 1) {
            helper.text(counter + " characters remaining")
        } else {
            helper.text(counter + " character remaining")
        }
    }
})