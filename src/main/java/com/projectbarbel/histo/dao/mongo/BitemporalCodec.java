package com.projectbarbel.histo.dao.mongo;

import java.time.Instant;

import org.bson.BsonReader;
import org.bson.BsonWriter;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;

import com.projectbarbel.histo.model.BitemporalStamp;
import com.projectbarbel.histo.model.BitemporalStamp.Builder;
import com.projectbarbel.histo.model.ObjectState;

public class BitemporalCodec implements Codec<BitemporalStamp> {

    @Override
    public void encode(BsonWriter writer, BitemporalStamp value, EncoderContext encoderContext) {
        writer.writeStartDocument();
        writer.writeString("documentId", value.getDocumentId());
        writer.writeStartDocument("effectiveFrom");
        writer.writeInt64("seconds", value.getEffectiveFrom().getEpochSecond());
        writer.writeInt32("nanos", value.getEffectiveFrom().getNano());
        writer.writeEndDocument();
        writer.writeStartDocument("effectiveUntil");
        writer.writeInt64("seconds", value.getEffectiveUntil().getEpochSecond());
        writer.writeInt32("nanos", value.getEffectiveUntil().getNano());
        writer.writeEndDocument();
        writer.writeStartDocument("createdAt");
        writer.writeInt64("seconds", value.getCreatedAt().getEpochSecond());
        writer.writeInt32("nanos", value.getCreatedAt().getNano());
        writer.writeEndDocument();
        writer.writeString("createdBy", value.getCreatedBy());
        writer.writeStartDocument("inactivatedAt");
        writer.writeInt64("seconds", value.getInactivatedAt().getEpochSecond());
        writer.writeInt32("nanos", value.getInactivatedAt().getNano());
        writer.writeEndDocument();
        writer.writeString("status", value.getStatus().name());
        writer.writeString("inactivatedBy", value.getInactivatedBy());
        writer.writeString("activity", value.getActivity());
        writer.writeEndDocument();
    }

    @Override
    public Class<BitemporalStamp> getEncoderClass() {
        return BitemporalStamp.class;
    }

    @Override
    public BitemporalStamp decode(BsonReader reader, DecoderContext decoderContext) {
        Builder builder = BitemporalStamp.builder();
        reader.readStartDocument();
        builder.withDocumentId(reader.readString("documentId"));
        builder.withEffectiveFrom(readInstant(reader, builder, "effectiveFrom"));
        builder.withEffectiveUntil(readInstant(reader, builder, "effectiveUntil"));
        builder.withCreatedAt(readInstant(reader, builder, "createdAt"));
        builder.withCreatedBy(reader.readString("createdBy"));
        builder.withInactivatedAt(readInstant(reader, builder, "inactivatedAt"));
        builder.withStatus(ObjectState.valueOf(reader.readString("status")));
        builder.withInactivatedBy(reader.readString("inactivatedBy"));
        builder.withActivity(reader.readString("activity"));
        reader.readEndDocument();
        return builder.build();
    }

    private Instant readInstant(BsonReader reader, final Builder builder, String name) {
        reader.readName(name);
        reader.readStartDocument();
        long seconds = reader.readInt64("seconds");
        int nano = reader.readInt32("nanos");
        reader.readEndDocument();
        return Instant.ofEpochSecond(seconds, nano);
    }

}
