package edu;

import java.time.LocalDate;

/** Makes the reporting decision before a delivery log is shipped. */
public final class DeliveryDeadlineService {
    public String status(String learnerId, LocalDate deadline, LocalDate reportDate) {
        if (reportDate.isAfter(deadline)) {
            return learnerId + ":OVERDUE";
        }
        return learnerId + ":ON_TIME";
    }

    public String logPayload(String courseId, String learnerId, LocalDate deadline, LocalDate reportDate) {
        String state = status(learnerId, deadline, reportDate);
        return "{\"message\":\"Course delivery status for " + learnerId + "\",\"level\":\"info\",\"course_id\":\"" + courseId + "\",\"learner_id\":\"" + learnerId
                + "\",\"deadline\":\"" + deadline + "\",\"reported_on\":\"" + reportDate
                + "\",\"status\":\"" + state.substring(state.indexOf(':') + 1) + "\"}";
    }
}
