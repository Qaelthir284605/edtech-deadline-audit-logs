package edu;

import java.time.LocalDate;

public final class DeliveryDeadlineServiceTest {
    public static void main(String[] args) {
        DeliveryDeadlineService service = new DeliveryDeadlineService();
        String result = service.status("learner-17", LocalDate.of(2026, 10, 20), LocalDate.of(2026, 10, 23));
        if (!"learner-17:OVERDUE".equals(result)) {
            throw new AssertionError("deadline policy changed: " + result);
        }
        System.out.println("deadline policy: overdue learner is reported as OVERDUE");
    }
}
