package edu;

import java.time.LocalDate;

/** Runnable example for an educator reporting a learner deadline. */
public final class DeliveryReport {
    public static void main(String[] args) throws Exception {
        String key = System.getenv("INFRAI_API_KEY");
        if (key == null || key.isBlank()) {
            throw new IllegalStateException("Set INFRAI_API_KEY before running the example");
        }
        DeliveryDeadlineService decision = new DeliveryDeadlineService();
        String payload = decision.logPayload("java-101", "learner-17",
                LocalDate.of(2026, 10, 30), LocalDate.of(2026, 10, 23));
        DeliveryLogClient client = new DeliveryLogClient(key);
        System.out.println(client.ingest(payload));
        System.out.println(client.search("learner-17"));
        System.out.println(client.listKeys());
        System.out.println("The account key list is the audit anchor for this report.");
    }
}
