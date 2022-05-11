/**
 * Runs when a sprint is resized on the calendar
 * @param info
 */
function eventResize (info) {
  // Data to send in post request to server

  // Add a day to returned start date due to how full calendar defines start date
  let startDate = new Date (info.event.start.toISOString().split("T")[0])
  startDate.setDate(startDate.getDate() + 1);

  let dataToSend= {
    "sprintId" : info.event.id,
    "sprintName" : info.event.title,
    "sprintStartDate" : startDate.toISOString().split("T")[0],
    "sprintEndDate" : info.event.end.toISOString().split("T")[0],
    "sprintDescription" : info.event.extendedProps.description,
    "sprintColour" : info.event.extendedProps.defaultColor
  }
  console.log(info.event.id)

  // Update sprint to have new start and end dates
  $.ajax({
    url: "sprintSubmit",
    type: "post",
    data: dataToSend,
    success: function(){
      $(".errorMessageParent").slideUp()
      $(".successMessage").text("Sprint dates updated successfully")
      $(".successMessageParent").slideUp()
      $(".successMessageParent").slideDown()
    },
    error: function(error){
      console.log(error.responseText)
      $(".errorMessage").text(error.responseText)
      $(".errorMessageParent").slideUp()
      $(".errorMessageParent").slideDown()
      info.revert()
    }
  })

}

/**
 * Function to handle event selection when clicked. Called by Full Calendar eventClick property.
 * @param info object supplied by FullCalendar contains various relevant properties
 */
function eventClick (info) {

  let canEdit = $("#canEdit").val() === "true";

  if (!canEdit) {
    return;
  }

  let events = info.view.calendar.getEvents();

  if (!info.event.extendedProps.selected) {
    // Deselects all events
    for (calEvent of events) {
      if (calEvent.extendedProps.isSprint) {
        calEvent.setExtendedProp("selected", false);
        calEvent.setProp("durationEditable", false);
        calEvent.setProp("backgroundColor", calEvent.extendedProps.defaultColor);
        calEvent.setProp("borderColor", '#13CEE2');
      }

    }

    // Selects this event
    info.event.setExtendedProp("selected", true);
    info.event.setProp("durationEditable", true);
    info.event.setProp("backgroundColor", '#aaa');
    info.event.setProp("borderColor", '#c2080b');

  } else {
    // Deselects this event
    info.event.setExtendedProp("selected", false);
    info.event.setProp("durationEditable", false);
    info.event.setProp("backgroundColor", info.event.extendedProps.defaultColor)
    info.event.setProp("borderColor", '#13CEE2');
  }

}

/**
 * $(document).ready fires off a function when the document has finished loading.
 * https://learn.jquery.com/using-jquery-core/document-ready/
 */
$(document).ready(function() {
  let projectId = $("#projectId").html();
  let calendarEl = document.getElementById('calendar');


  /**
   * Calendar functionality
   * https://fullcalendar.io/docs
   */
  let calendar = new FullCalendar.Calendar(calendarEl, {
    initialView: 'dayGridMonth',
    eventDurationEditable: false,
    eventResizableFromStart: true,
    eventResize: function( info ) {
      eventResize( info )
    },
    eventClick: function( info ) {
      eventClick( info )
    },
    themeSystem: 'bootstrap5',
    eventSources: [{ //The sources to grab the events from.
      url: 'getProjectSprintsWithDatesAsFeed', //Project sprints
      method: "get",
      extraParams: {
                projectId: projectId.toString()
              },

      failure: function(err){
        console.log(err.responseText)
      }
    },
      {
        url: 'getProjectAsFeed', // Project itself
        method: "get",
        display: "inverse-background",
        extraParams: {
          projectId: projectId.toString()
        },
        failure: function (err) {
          console.log(err.responseText)
        }
      },
      {
        url: 'getEventsAsFeed', // Get all milestones
        method: "get",
        extraParams: {
          projectId: projectId.toString()
        },
        failure: function (err) {
          console.log(err.responseText)
        }
      },
      {
        url: 'getMilestonesAsFeed', // Get all milestones
        method: "get",
        extraParams: {
          projectId: projectId.toString()
        },
        failure: function (err) {
          console.log(err.responseText)
        },
      }
    ],
    eventDidMount : function(info) {
      if(info.event.classNames.toString() === "milestoneCalendar") {
        info.el.innerHTML = `<col><svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" className="bi bi-trophy-fill" viewBox="0 0 16 16">
                                    <path d="M2.5.5A.5.5 0 0 1 3 0h10a.5.5 0 0 1 .5.5c0 .538-.012 1.05-.034 1.536a3 3 0 1 1-1.133 5.89c-.79 1.865-1.878 2.777-2.833 3.011v2.173l1.425.356c.194.048.377.135.537.255L13.3 15.1a.5.5 0 0 1-.3.9H3a.5.5 0 0 1-.3-.9l1.838-1.379c.16-.12.343-.207.537-.255L6.5 13.11v-2.173c-.955-.234-2.043-1.146-2.833-3.012a3 3 0 1 1-1.132-5.89A33.076 33.076 0 0 1 2.5.5zm.099 2.54a2 2 0 0 0 .72 3.935c-.333-1.05-.588-2.346-.72-3.935zm10.083 3.935a2 2 0 0 0 .72-3.935c-.133 1.59-.388 2.885-.72 3.935z"/> 
                                    </svg></col> <col> ${info.event.title} </col>`
      }
      else if(info.event.classNames.toString() === "eventCalendar") {
        info.el.innerHTML = `<col><svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" class="bi bi-calendar3-event-fill" viewBox="0 0 16 16">
                                    <path fill-rule="evenodd" d="M2 0a2 2 0 0 0-2 2h16a2 2 0 0 0-2-2H2zM0 14V3h16v11a2 2 0 0 1-2 2H2a2 2 0 0 1-2-2zm12-8a1 1 0 1 0 2 0 1 1 0 0 0-2 0z"/>
                                    </svg></col> <col> ${info.event.title} </col>`
      }
     },
  });


  calendar.render();


// -------------------------------------- Notification Source and listeners --------------------------------------------


  /** The source of notifications used to provide updates to the user such as events being edited */
  let eventSource = new EventSource("notifications");


  /**
   * This event listener listens for a notification that an element should be reloaded.
   * This happens if another user has changed an element.
   * It removes the class that shows the border and then calls ReloadEvent()
   */
  eventSource.addEventListener("reloadElement", function (event) {
    calendar.refetchEvents();
  })


  /**
   * Listens for a notification to remove an element (happens if another client deletes an element)
   */
  eventSource.addEventListener("notifyRemoveEvent", function (event) {
    calendar.refetchEvents();
  })


  /**
   * Listens for a notification to add a new element that another client has created
   */
  eventSource.addEventListener("notifyNewElement", function (event) {
    calendar.refetchEvents();
  })
})

