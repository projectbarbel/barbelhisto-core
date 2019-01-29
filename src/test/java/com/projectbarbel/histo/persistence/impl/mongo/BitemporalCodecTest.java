package com.projectbarbel.histo.persistence.impl.mongo;

import static org.junit.Assert.assertEquals;
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

import com.projectbarbel.histo.BarbelTestHelper;
import com.projectbarbel.histo.model.BitemporalStamp;

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
        BitemporalStamp stamp = BarbelTestHelper.random(BitemporalStamp.class);
        doNothing().when(writer).writeString(anyString(), anyString());

        codec.encode(writer, stamp, EncoderContext.builder().build()); // <- the test call

        ArgumentCaptor<String> documentCapture = ArgumentCaptor.forClass(String.class);
        verify(writer, times(5)).writeString(documentCapture.capture(),documentCapture.capture());
        assertEquals(documentCapture.getAllValues().get(1), stamp.getDocumentId());
        assertEquals(documentCapture.getAllValues().get(3), stamp.getRecordTime().getCreatedBy());
        assertEquals(documentCapture.getAllValues().get(5), stamp.getRecordTime().getState().name());
        assertEquals(documentCapture.getAllValues().get(7), stamp.getRecordTime().getInactivatedBy());
        assertEquals(documentCapture.getAllValues().get(9), stamp.getActivity());
    }

    @Test
    public void testEncode_EncodeDecode_equals() throws IOException {
        BitemporalCodec codec = new BitemporalCodec();
        BitemporalStamp stamp = BarbelTestHelper.random(BitemporalStamp.class);
        codec.encode(writer, stamp, EncoderContext.builder().build());
        BsonInput bsonInput = createInputBuffer();
        BitemporalStamp decodedBitemporalStamp = codec.decode(new BsonBinaryReader(bsonInput),
                DecoderContext.builder().build());
        assertEquals(stamp, decodedBitemporalStamp);
    }

    @Test(expected=AssertionError.class)
    public void noNulls_encode() {
        BitemporalCodec codec = new BitemporalCodec();
        codec.encode(null, null, null);
    }
    
    @Test(expected=AssertionError.class)
    public void noNulls_decode() {
        BitemporalCodec codec = new BitemporalCodec();
        codec.decode(null, null);
    }
    
    private BsonInput createInputBuffer() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        buffer.pipe(baos);
        return new ByteBufferBsonInput(new ByteBufNIO(ByteBuffer.wrap(baos.toByteArray())));
    }

}
