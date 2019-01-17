package com.projectbarbel.histo.dao.mongo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import org.bson.BsonBinaryReader;
import org.bson.BsonBinaryWriter;
import org.bson.BsonWriter;
import org.bson.ByteBufNIO;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;
import org.bson.io.BasicOutputBuffer;
import org.bson.io.BsonInput;
import org.bson.io.ByteBufferBsonInput;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import com.projectbarbel.histo.model.BitemporalStamp;

import io.github.benas.randombeans.api.EnhancedRandom;

public class BitemporalCodecTest {

    private BasicOutputBuffer buffer;
    private BsonBinaryWriter writer;

    @Before
    public void setUp() throws Exception {
        buffer = new BasicOutputBuffer();
        writer = new BsonBinaryWriter(buffer);
    }

    @After
    public void tearDown() {
        writer.close();
    }

    @Test
    public void testEncode_StringsWritten() {
        BsonWriter writer = mock(BsonWriter.class);
        BitemporalCodec codec = new BitemporalCodec();
        BitemporalStamp stamp = EnhancedRandom.random(BitemporalStamp.class);
        doNothing().when(writer).writeString(anyString(), anyString());

        codec.encode(writer, stamp, EncoderContext.builder().build()); // <- the test call

        ArgumentCaptor<String> documentCapture = ArgumentCaptor.forClass(String.class);
        verify(writer, times(5)).writeString(documentCapture.capture(),documentCapture.capture());
        assertEquals(documentCapture.getAllValues().get(1), stamp.getDocumentId());
        assertEquals(documentCapture.getAllValues().get(3), stamp.getCreatedBy());
        assertEquals(documentCapture.getAllValues().get(5), stamp.getStatus().name());
        assertEquals(documentCapture.getAllValues().get(7), stamp.getInactivatedBy());
        assertEquals(documentCapture.getAllValues().get(9), stamp.getActivity());
    }

    @Test
    public void testEncode_EncodeDecode_equals() throws IOException {
        BitemporalCodec codec = new BitemporalCodec();
        BitemporalStamp stamp = EnhancedRandom.random(BitemporalStamp.class);
        codec.encode(writer, stamp, EncoderContext.builder().build());
        BsonInput bsonInput = createInputBuffer();
        BitemporalStamp decodedBitemporalStamp = codec.decode(new BsonBinaryReader(bsonInput),
                DecoderContext.builder().build());
        assertEquals(stamp, decodedBitemporalStamp);
    }

    private BsonInput createInputBuffer() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        buffer.pipe(baos);
        return new ByteBufferBsonInput(new ByteBufNIO(ByteBuffer.wrap(baos.toByteArray())));
    }

}
