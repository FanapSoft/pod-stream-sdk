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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.upstream.BaseDataSource;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DataSpec;
import com.google.android.exoplayer2.upstream.TransferListener;
import com.google.android.exoplayer2.util.Assertions;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;

import ir.fanap.podstream.Util.Constants;
import ir.fanap.podstream.network.response.DashResponse;
import ir.fanap.podstream.network.response.TopicResponse;


/**
 * A {@link DataSource} for reading local files.
 */
//public final class MasoudDataSource extends BaseDataSource {


/**
 * A {@link DataSource} for reading local files.
 */
public final class ProgressiveDataSource extends BaseDataSource{

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
        KafkaDataProvider provider;

        public Factory(DashResponse response, KafkaDataProvider provider) {
            dashFile = response;
            this.provider = provider;
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
            dataSource = new ProgressiveDataSource(dashFile, provider);
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
    private long filmLength;
    private long readPosition;
    KafkaDataProvider provider;

    public ProgressiveDataSource(@NonNull DashResponse dashResponse, @NonNull KafkaDataProvider provider) {
        super(/* isNetwork= */ false);
        this.provider = provider;;
        this.filmLength = dashResponse.getSize();
        if (filmLength ==0)
            filmLength = 10000;
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

//            if (provider.isExistInStartBuffer(readPosition, readLength)) {
//                Log.e("buffering", "read: start");
//                System.arraycopy(provider.getStartBuffer(), (int) readPosition, buffer, offset, readLength);
//            } else {
                if (provider.shouldUpdateBuffer(readPosition, readLength)) {
                    long readLengthBuffer = Math.max(readLength, Constants.DefualtLengthValue);
                    provider.updateBuffer(readPosition, readLengthBuffer);
                }

                System.arraycopy(provider.getDataBuffer(), (int) (readPosition - provider.getOffsetMainBuffer()), buffer, offset, readLength);
//            }

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

}
