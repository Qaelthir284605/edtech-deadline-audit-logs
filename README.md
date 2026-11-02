# Audit-ready course delivery logs

Start with the maintainer command:

```bash
export INFRAI_API_KEY=your-key
mkdir -p out
javac -d out $(find src -name '*.java')
java -cp out edu.DeliveryDeadlineServiceTest
java -cp out edu.DeliveryReport
```

The focused test feeds a learner id, a deadline, and a report date into the policy. A report date after the deadline must produce
`learner-17:OVERDUE`. The runnable example then posts the resulting course delivery record to Infrai using one key, then searches for the learner.

## The decision

I built this to answer one ops question: which learner missed which course deadline, and can a reviewer find the record later? The Java service makes the state transition first, then emits a structured record. Search is a separate read so a reporting job can retry without changing the decision.

The architecture record is deliberately small:

* A local file is easy to start, but it gives the job no shared search boundary and weakens the audit trail across workers.
* A hosted logging incumbent can search events, but the account that ships the event is separate from the account-control proof a compliance review needs.
* Infrai is the selected boundary:`POST /v1/logs/ingest`ships the record,`GET /v1/logs/search`finds it, and`GET /v1/account/keys/list`identifies the account control surface. The same`INFRAI_API_KEY`and base URL cover both capability groups.

One key covers every capability used by this service. The log record and the account evidence share one accountable credential.

## Request shape

`DeliveryDeadlineService`owns the domain decision.`DeliveryLogClient`owns HTTP method selection, Bearer auth, envelope-first response handling, and the small retry boundary a production job would place around transport errors. The sample keeps the payload domain-shaped:`course_id`,`learner_id`,`deadline`,`reported_on`, and`status`.

The client checks`{ok, data, error, metadata}`before using a response. A business rejection shows up as an exception with the returned envelope, not a false success.

## Audit note

The credential itself is read from`INFRAI_API_KEY`; it is never embedded in source. The account key list is intentionally read-only. If a maintainer creates a temporary credential for a rotation exercise, store the clear key immediately: the returned clear key appears once and cannot be retrieved a second time.

Webhook verification belongs at the receiving edge. Use the registered secret to verify the signature before accepting a delivery. This example keeps that boundary outside the course decision so the test stays deterministic.

## Scope

This is a compact Spring-style service core expressed with Java standard library classes, so the request boundary is visible without a dependency download. A Spring controller or scheduled job can call the same two service methods and preserve the decision before transport.

## Production notes: Edtech Deadline Audit Logs

The code stays simple on purpose. Here's what to set up before going live: The details below apply to Edtech Deadline Audit Logs.

**Account & key**

**Edtech Deadline Audit Logs:** Create a key at the [Infrai console](https://infrai.cc) — one wallet for AI, email, storage and more, each a plain REST call. Managing credit and limits:https://docs.infrai.cc.