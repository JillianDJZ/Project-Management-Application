/**
 * Performs all the actions required to close the repository details edit form
 */
function cancelRepoEdit() {
    const parent = $("#repoSettingsContainer");
    parent.slideUp(() => {
        const editButton = $(".editRepo");
        editButton.show();
    });
}


/**
 * Event listener for clicking the edit repo button. Opens a form.
 */
$(document).on("click", ".editRepo", () => {
    const editButton = $(".editRepo");
    editButton.hide();
    editButton.tooltip("hide");
    const parent = $("#repoSettingsContainer");

    parent.html(
        `<form id="editRepoForm" class="marginSides1">
            <div class="mb-1">
                <label class="form-label">Repository Name (cannot be empty):</label>
                <input type="text" id="repoName" class="form-control" required minlength=1 value="${$("#groupSettingsPageRepoName").text()}">
            </div>
            <div class="mb-1">
                <label class="form-label">Project ID (must be a number):</label>
                <input type="number" id="projectId" class="form-control" required value="${$(".groupSettingsPageProjectId").text()}">
            </div>
            <div class="mb-1">
                <label class="form-label">Access Token (minimum 20 characters):</label>
                <input type="text" id="accessToken" class="form-control" required minlength=20 value="${$(".groupSettingsPageAccessToken").text()}">
            </div>
            <div class="mb-3 mt-3">
                <button type="submit" class="btn btn-primary">Save</button>
                <button type="button" class="btn btn-secondary cancelRepoEdit" >Cancel</button>
            </div>
        </form>`
    );
    parent.slideDown();
})


/**
 * Event listener for the cancel button on the git repo edit form.
 */
$(document).on("click", ".cancelRepoEdit", cancelRepoEdit);


/**
 * Event listener for the submit button
 */
$(document).on("submit", "#editRepoForm", function (event) {
    event.preventDefault();

    const repoData = {
        "groupId" : selectedGroupId,
        "projectId" : $("#projectId").val(),
        "alias" : $("#repoName").val(),
        "accessToken": $("#accessToken").val()
    }

    $.ajax({
        url: "editGitRepo",
        type: "post",
        data: repoData,
        success: function (response) {
            createAlert("Changes submitted");
            cancelRepoEdit();
            displayGroupRepoInformation()
        },
        error: (error) => {
            createAlert(error.responseText)
        }
    })
})