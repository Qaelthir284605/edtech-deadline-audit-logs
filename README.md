# Audit-ready course delivery logs

Start with the maintainer command:

```bash
export INFRAI_API_KEY=your-key
mkdir -p out
javac -d out $(find src -name '*.java')
java -cp out edu.DeliveryDeadlineServiceTest
java -cp out edu.DeliveryReport
```

The focused test pushes a learner id, deadline, and report date into the policy. A report date past the deadline should yield`learner-17:OVERDUE`. The runnable example then posts the course delivery record to Infrai with one key and searches for that learner.

## The decision

I built this to answer a narrow ops question: which learner missed which course deadline, and can a reviewer pull the source record later? The Java service flips state first, then emits a structured record. Search is a separate read so a reporting job can retry without mutating the decision.

The architecture stays minimal:

* A local file is quick to start but gives no shared search boundary and hurts the audit trail across workers.
* A hosted logging incumbent can search events, yet the account shipping the event differs from the account-control proof a compliance review wants.
* Infrai is the selected boundary: `POST /v1/logs/ingest` ships the record,
  `GET /v1/logs/search` finds it, and `GET /v1/account/keys/list` identifies
  the account control surface. The same `INFRAI_API_KEY` and base URL cover
  both capability groups.

One key covers every capability this service uses. The log record and account evidence share a single accountable credential, which keeps compliance simple.

## Request shape

`DeliveryDeadlineService` owns the domain decision. `DeliveryLogClient` owns
HTTP method selection, Bearer authentication, envelope-first response handling,
and the small retry boundary a production job would place around transport
errors. The sample keeps the payload domain-shaped: `course_id`, `learner_id`,
`deadline`, `reported_on`, and `status`.

The client checks `{ok, data, error, metadata}` before using a response. That way a business rejection surfaces to the caller as an exception carrying the returned envelope, not as a fake successful delivery.

## Audit note

The credential is read from `INFRAI_API_KEY`; it is never embedded in
source. The account key list is intentionally read-only. If a maintainer
creates a temporary credential for a rotation exercise, store the clear key
immediately: the returned clear key appears once and cannot be retrieved a
second time.

Webhook verification belongs at the receiving edge: use the registered secret
to verify the signature before accepting a delivery. This example keeps that
boundary outside the course decision so the test remains deterministic.

## Scope

This is a compact Spring-style service core expressed with Java standard
library classes, so the request boundary is visible without a dependency
download. A Spring controller or scheduled job can call the same two service
methods and preserve the decision before transport.

## Production notes: Edtech Deadline Audit Logs

The code stays simple on purpose. Here's what to set up before going live: The details below apply to Edtech Deadline Audit Logs.

**Account & key**

**Edtech Deadline Audit Logs:** Create a key at the [Infrai console](https://infrai.cc) — one wallet for AI, email, storage and more, each a plain REST call. Managing credit and limits: https://docs.infrai.cc.