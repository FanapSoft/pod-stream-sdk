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
package ir.fanap.podstream.datasources;

import static java.lang.Math.min;

import android.net.Uri;
import android.text.TextUtils;

import androidx.annotation.Nullable;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.upstream.BaseDataSource;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DataSpec;
import com.google.android.exoplayer2.upstream.TransferListener;
import com.google.android.exoplayer2.util.Assertions;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;

import ir.fanap.podstream.kafka.KafkaClientManager;

/**
 * A {@link DataSource} for reading local files.
 */
public final class FileDataSource extends BaseDataSource {


    public static class FileDataSourceException extends IOException {

        public FileDataSourceException(IOException cause) {
            super(cause);
        }

        public FileDataSourceException(String message, IOException cause) {
            super(message, cause);
        }
    }


    public static final class Factory implements DataSource.Factory {
        @Nullable
        private TransferListener listener;
        KafkaClientManager kafkaManager;
        long fileSize;

        public Factory setListener(@Nullable TransferListener listener) {
            this.listener = listener;
            return this;
        }


        public Factory(long fileSize) {
            this.fileSize = fileSize;
            this.kafkaManager = KafkaClientManager.getInstance(null);
        }

        @Override
        public FileDataSource createDataSource() {
            final File root = android.os.Environment.getExternalStorageDirectory();

            byte[] videoBuffer = new byte[0];
            File file = new File(root.getAbsolutePath() + "/test2flv.mp4");
            byte[] temp = new byte[(int) file.length()];
            FileInputStream fileInputStream = null;
            try {
                fileInputStream = new FileInputStream(file);
                fileInputStream.read(temp);
                videoBuffer = ByteBuffer.allocate(videoBuffer.length + temp.length)
                        .put(videoBuffer)
                        .put(temp)
                        .array();
                fileInputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

            DataProvider provider = new DataProvider(fileSize);
            FileDataSource dataSource = new FileDataSource(provider, videoBuffer);
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
    private byte[] data;
    DataProvider provider;
    private long readPosition;

    public FileDataSource(DataProvider provider, byte[] data) {
        super(/* isNetwork= */ false);
        Assertions.checkNotNull(data);
        Assertions.checkArgument(data.length > 0);
        this.provider = provider;
        this.data = data;
    }


    @Override
    public long open(DataSpec dataSpec) throws IOException {
        uri = dataSpec.uri;
        readPosition = ((int) dataSpec.position);
        bytesRemaining = (int) (data.length - (dataSpec.position));

//        if (bytesRemaining <= 0 || readPosition + bytesRemaining > data.length) {
//            throw new IOException("Unsatisfiable range: [" + readPosition + ", " + dataSpec.length
//                    + "], length: " + data.length);
//        }

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

            System.arraycopy(provider.read(readPosition, readLength), (int) readPosition, buffer, offset, readLength);
        } catch (Exception e) {
            //   System.arraycopy(data, readPosition, buffer, offset, readLength);
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
    public void close() throws FileDataSourceException {
        uri = null;
        try {
            if (file != null) {
                file.close();
            }
        } catch (IOException e) {
            throw new FileDataSourceException(e);
        } finally {
            file = null;
            if (opened) {
                opened = false;
                transferEnded();
            }
        }
    }

    private static RandomAccessFile openLocalFile(Uri uri) throws FileDataSourceException {
        try {
            return new RandomAccessFile(Assertions.checkNotNull(uri.getPath()), "r");
        } catch (FileNotFoundException e) {
            if (!TextUtils.isEmpty(uri.getQuery()) || !TextUtils.isEmpty(uri.getFragment())) {
                throw new FileDataSourceException(
                        String.format(
                                "uri has query and/or fragment, which are not supported. Did you call Uri.parse()"
                                        + " on a string containing '?' or '#'? Use Uri.fromFile(new File(path)) to"
                                        + " avoid this. path=%s,query=%s,fragment=%s",
                                uri.getPath(), uri.getQuery(), uri.getFragment()),
                        e);
            }
            throw new FileDataSourceException(e);
        }
    }

    public void insertnewData(byte[] newData) {
        data = ByteBuffer.allocate(data.length + newData.length)
                .put(data)
                .put(newData)
                .array();
    }

    public Thread consuming = new Thread(() -> {
        final File root = android.os.Environment.getExternalStorageDirectory();
        for (int i = 2; i <= 30; i++) {

            File file1 = new File(root.getAbsolutePath() + "/s_" + i + ".m4s");
            byte[] bFile1 = new byte[(int) file1.length()];

            //convert file into array of bytes
            FileInputStream fileInputStream1 = null;
            try {
                fileInputStream1 = new FileInputStream(file1);
                fileInputStream1.read(bFile1);
                fileInputStream1.close();

                this.insertnewData(bFile1);

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        int a = 10;
    });
}
