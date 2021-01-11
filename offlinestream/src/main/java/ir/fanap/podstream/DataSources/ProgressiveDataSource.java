package ir.fanap.podstream.DataSources;

/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.Nullable;

import com.example.kafkassl.kafkaclient.ConsumerClient;
import com.example.kafkassl.kafkaclient.ProducerClient;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.upstream.BaseDataSource;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DataSpec;
import com.google.android.exoplayer2.upstream.TransferListener;
import com.google.android.exoplayer2.util.Assertions;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.util.Properties;

import ir.fanap.podstream.network.response.DashResponse;

import static ir.fanap.podstream.offlineStream.PodStream.TAG;


/**
 * A {@link DataSource} for reading local files.
 */
//public final class MasoudDataSource extends BaseDataSource {


/**
 * A {@link DataSource} for reading local files.
 */
public final class ProgressiveDataSource extends BaseDataSource {

    /**
     * Creates base data source.
     *
     * @param isNetwork Whether the data source loads data through a network.
     */
    protected ProgressiveDataSource(boolean isNetwork) {
        super(isNetwork);
    }


    /**
     * {@link DataSource.Factory} for {@link com.google.android.exoplayer2.upstream.FileDataSource} instances.
     */
    public static final class Factory implements DataSource.Factory {

        @Nullable
        private TransferListener listener;
        DashResponse dashFile;
        private ProgressiveDataSource dataSource;

        public Factory(DashResponse response) {
            dashFile = response;
        }

        public ProgressiveDataSource getDataSource() {
            return dataSource;
        }

        /**
         * Sets a {@link TransferListener} for {@link com.google.android.exoplayer2.upstream.FileDataSource} instances created by this factory.
         * <p>
         * //         * @param listener The {@link TransferListener}.
         *
         * @return This factory.
         */


        @Override
        public ProgressiveDataSource createDataSource() {

            byte[] videoBuffer = new byte[0];//=new byte[50000];
//            MasoudDataSource dataSource = new MasoudDataSource(6000196, "192.168.112.32:9092", "CONTROL5313E3G5GK3HCP1I3481691607763212908", "STREAM5313E3G5GK3HCP1I3481691607763212908");
            dataSource = new ProgressiveDataSource(dashFile.getSize(), dashFile.getBrokerAddress(), dashFile.getProduceTopic(), dashFile.getConsumTopic());
            if (listener != null) {
                dataSource.addTransferListener(listener);
            }
            return dataSource;
        }
    }

    @Nullable
    private RandomAccessFile file;
    @Nullable
    private Uri uri;
    private long bytesRemaining;
    private boolean opened;
    private byte[] dataBuffer;
    private long startBuffer;
    private long endBuffer;
    private long filmLength;
    ConsumerClient consumerClient;
    ProducerClient producerClient;
    String consumTopic;
    String produceTopic;
    private long readPosition;

    public ProgressiveDataSource(long filmLength, String brokerAddres, String produceTopic, String consumTopic) {
        super(/* isNetwork= */ false);
        this.consumTopic = consumTopic;
        this.produceTopic = produceTopic;
        final Properties propertiesProducer = new Properties();
        propertiesProducer.setProperty("bootstrap.servers", brokerAddres);
        producerClient = new ProducerClient(propertiesProducer);
        producerClient.connect();
        propertiesProducer.setProperty("group.id", "777");
        propertiesProducer.setProperty("auto.offset.reset", "beginning");
        consumerClient = new ConsumerClient(propertiesProducer, consumTopic);
        consumerClient.connect();
        consumerClient.consumingTopic(5);
        this.filmLength = filmLength;

        updateBuffer(0, DefualtLengthValue);


    }

    @Override
    public long open(DataSpec dataSpec) throws IOException {

        uri = dataSpec.uri;
        readPosition = ((int) dataSpec.position);
        bytesRemaining = (int) (filmLength - (dataSpec.position));//2781222l
        if (bytesRemaining <= 0 || readPosition + bytesRemaining > filmLength) {
            throw new IOException("Unsatisfiable range: [" + readPosition + ", " + dataSpec.length
                    + "], length: " + filmLength);
        }
        return bytesRemaining;

    }

    @Override
    public int read(byte[] buffer, int offset, int readLength) throws IOException {

        if (readLength == 0) {
            return 0;
        } else if (bytesRemaining == 0) {
            return C.RESULT_END_OF_INPUT;
        }

        readLength = (int) Math.min(readLength, bytesRemaining);

        try {
            if (shouldUpdateBuffer(readPosition, readLength)) {
                long readLengthBuffer = Math.max(readLength, DefualtLengthValue);

                updateBuffer(readPosition, readLengthBuffer);
            }
            System.arraycopy(dataBuffer, (int) (readPosition - startBuffer), buffer, offset, readLength);

        } catch (Exception e) {
            int a = 10;
        }

        readPosition += readLength;
        bytesRemaining -= readLength;
        return readLength;
    }

    @Override
    @Nullable
    public Uri getUri() {
        return uri;
    }

    @Override
    public void close() throws com.google.android.exoplayer2.upstream.FileDataSource.FileDataSourceException {
        uri = null;
        ByteBuffer buffers = ByteBuffer.allocate(Long.BYTES);
        buffers.putLong(-2);
        try {

            if (file != null) {
                file.close();
            }
        } catch (IOException e) {
            throw new com.google.android.exoplayer2.upstream.FileDataSource.FileDataSourceException(e);
        } finally {

            file = null;
            if (opened) {
                opened = false;
                transferEnded();
            }
        }
    }

    private static RandomAccessFile openLocalFile(Uri uri) throws com.google.android.exoplayer2.upstream.FileDataSource.FileDataSourceException {
        try {
            return new RandomAccessFile(Assertions.checkNotNull(uri.getPath()), "r");
        } catch (FileNotFoundException e) {
            if (!TextUtils.isEmpty(uri.getQuery()) || !TextUtils.isEmpty(uri.getFragment())) {
                throw new com.google.android.exoplayer2.upstream.FileDataSource.FileDataSourceException(
                        String.format(
                                "uri has query and/or fragment, which are not supported. Did you call Uri.parse()"
                                        + " on a string containing '?' or '#'? Use Uri.fromFile(new File(path)) to"
                                        + " avoid this. path=%s,query=%s,fragment=%s",
                                uri.getPath(), uri.getQuery(), uri.getFragment()),
                        e);
            }
            throw new com.google.android.exoplayer2.upstream.FileDataSource.FileDataSourceException(e);
        }
    }


    public static int DefualtLengthValue = 250000;

    public void updateBuffer(long offset, long length) {
        if (length > DefualtLengthValue) {
            getData(offset, length);
        } else {
            if ((offset + length) > filmLength)
                length = filmLength - offset;
            startBuffer = offset;
            endBuffer = offset + (length - 1);
            ByteBuffer buffers = ByteBuffer.allocate(Long.BYTES);
            buffers.putLong(-3);
            producerClient.produceMessege(buffers.array(), offset + "," + length, produceTopic);
            dataBuffer = consumerClient.consumingTopic(5);
            while (dataBuffer == null || dataBuffer.length < length) {
                dataBuffer = consumerClient.consumingTopic(5);

            }
        }
    }

    public void getData(long offset, long length) {

        dataBuffer = new byte[(int) length];
        startBuffer = offset;
        endBuffer = offset + (length - 1);
        boolean exit = false;
        for (int i = 0; i < length; i += DefualtLengthValue) {
            int newlength = DefualtLengthValue;
            if (i + newlength > length) {
                newlength = (int) length - i;
                exit = true;
            }

            byte[] newData;
            ByteBuffer buffers = ByteBuffer.allocate(Long.BYTES);
            buffers.putLong(-3);

            producerClient.produceMessege(buffers.array(), (i + offset) + "," + newlength, produceTopic);
            newData = consumerClient.consumingTopic(5);

            while (newData == null || newData.length < newlength) {
                newData = consumerClient.consumingTopic(5);

            }
            System.arraycopy(newData, 0, dataBuffer, i, newlength);
            System.out.println("offset :" + i + " length : " + newlength);
            if (exit)
                break;

        }

    }

    public void release() {
        ByteBuffer buffers = ByteBuffer.allocate(Long.BYTES);
        buffers.putLong(-2);
        producerClient.produceMessege(buffers.array(), ",", produceTopic);

    }

    public boolean shouldUpdateBuffer(long offset, long length) {
        if (offset < endBuffer && offset >= startBuffer && (offset + length) <= endBuffer)
            return false;
        return true;
    }

}
