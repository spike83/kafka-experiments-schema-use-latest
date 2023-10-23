Reproducer for schema compatibility problem when using generated java classes and `use.latest.version=true`.

The topic will have records of 2 schemas which are fully compatible but the consumer using specific record and the latest version will fail.

There is four consumers looking at the same topic `offer`:

- `consumer-not-using-latest-generic-record`
- `consumer-using-latest-generic-record`
- `consumer-not-using-latest`
- `consumer-using-latest` <== the one we are interested in


In order to reproduce the problem start the `producer-1st-version` which produces records like this:

{"id":"...", "content":"..."}

then start all the consumers. All should be fine by then.

Now you start the `producer-2nd-version-full-compatible` which produces records like this:

{"id":"...", "content":"...", "forceUpdate":"..."}


You will see:

- `consumer-not-using-latest-generic-record` will still consume the records now with the new field when set
- `consumer-using-latest-generic-record` will still consume the records now with the new field and default when not set
- `consumer-not-using-latest` will still consume the records without the new field
- `consumer-using-latest` will fail with the following error:

```
DEBUG [main] i.c.k.s.c.r.RestService - Sending GET with input null to http://localhost:8081/schemas/ids/3?fetchMaxId=false&subject=offer-value
DEBUG [main] i.c.k.s.c.r.RestService - Sending GET with input null to http://localhost:8081/subjects/offer-value/versions/latest
DEBUG [main] i.c.k.s.c.r.RestService - Sending POST with input {"schema":"{\"type\":\"record\",\"name\":\"Offer\",\"namespace\":\"io.spoud.example\",\"fields\":[{\"name\":\"id\",\"type\":{\"type\":\"string\",\"avro.java.string\":\"String\"}},{\"name\":\"content\",\"type\":{\"type\":\"string\",\"avro.java.string\":\"String\"}},{\"name\":\"forceUpdate\",\"type\":[\"null\",\"boolean\"],\"default\":null}]}"} to http://localhost:8081/subjects/offer-value?normalize=false&deleted=true
Exception in thread "main" org.apache.kafka.common.errors.RecordDeserializationException: Error deserializing key/value for partition offer-0 at offset 7936. If needed, please seek past the record to continue consumption.
	at org.apache.kafka.clients.consumer.internals.CompletedFetch.parseRecord(CompletedFetch.java:309)
	at org.apache.kafka.clients.consumer.internals.CompletedFetch.fetchRecords(CompletedFetch.java:263)
	at org.apache.kafka.clients.consumer.internals.AbstractFetch.fetchRecords(AbstractFetch.java:340)
	at org.apache.kafka.clients.consumer.internals.AbstractFetch.collectFetch(AbstractFetch.java:306)
	at org.apache.kafka.clients.consumer.KafkaConsumer.pollForFetches(KafkaConsumer.java:1262)
	at org.apache.kafka.clients.consumer.KafkaConsumer.poll(KafkaConsumer.java:1186)
	at org.apache.kafka.clients.consumer.KafkaConsumer.poll(KafkaConsumer.java:1159)
	at io.spoud.example.AppUsingLatestVersion.main(AppUsingLatestVersion.java:37)
Caused by: org.apache.kafka.common.errors.SerializationException: Error deserializing Avro message for id 3
	at io.confluent.kafka.serializers.AbstractKafkaAvroDeserializer$DeserializationContext.read(AbstractKafkaAvroDeserializer.java:537)
	at io.confluent.kafka.serializers.AbstractKafkaAvroDeserializer.deserialize(AbstractKafkaAvroDeserializer.java:185)
	at io.confluent.kafka.serializers.KafkaAvroDeserializer.deserialize(KafkaAvroDeserializer.java:108)
	at org.apache.kafka.common.serialization.Deserializer.deserialize(Deserializer.java:73)
	at org.apache.kafka.clients.consumer.internals.CompletedFetch.parseRecord(CompletedFetch.java:300)
	... 7 more
Caused by: java.lang.IndexOutOfBoundsException: Invalid index: 2
	at io.spoud.example.Offer.put(Offer.java:119)
	at org.apache.avro.generic.GenericData.setField(GenericData.java:837)
	at org.apache.avro.specific.SpecificDatumReader.readField(SpecificDatumReader.java:139)
	at org.apache.avro.generic.GenericDatumReader.readRecord(GenericDatumReader.java:248)
	at org.apache.avro.specific.SpecificDatumReader.readRecord(SpecificDatumReader.java:123)
	at org.apache.avro.generic.GenericDatumReader.readWithoutConversion(GenericDatumReader.java:180)
	at org.apache.avro.generic.GenericDatumReader.read(GenericDatumReader.java:161)
	at org.apache.avro.generic.GenericDatumReader.read(GenericDatumReader.java:154)
	at io.confluent.kafka.serializers.AbstractKafkaAvroDeserializer$DeserializationContext.read(AbstractKafkaAvroDeserializer.java:505)
	... 11 more

```
