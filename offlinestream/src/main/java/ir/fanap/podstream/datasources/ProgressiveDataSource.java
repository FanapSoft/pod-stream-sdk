package ir.fanap.podstream.datasources;


import android.net.Uri;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.upstream.BaseDataSource;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DataSpec;
import com.google.android.exoplayer2.upstream.TransferListener;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;

import ir.fanap.podstream.util.Constants;
import ir.fanap.podstream.network.response.DashResponse;


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


    private RandomAccessFile file;

    private Uri uri;
    private long bytesRemaining;
    private boolean opened;
    private long filmLength;
    private long readPosition;
    KafkaDataProvider provider;

    public ProgressiveDataSource(DashResponse mpegDashResponse, KafkaDataProvider provider) {
        super(/* isNetwork= */ false);
        this.provider = provider;
        this.filmLength = mpegDashResponse.getSize();
        if (filmLength == 0)
            filmLength = 10000;
    }

    @Override
    public long open(DataSpec dataSpec) throws IOException {
        uri = dataSpec.uri;
        readPosition = ((int) dataSpec.position);
        bytesRemaining = (int) (filmLength - (dataSpec.position));//2781222l
        if (bytesRemaining <= 0 || readPosition + bytesRemaining > filmLength) {
            if (provider.getListener() != null)
                provider.getListener().onError("Unsatisfiable range: [" + readPosition + ", " + dataSpec.length
                        + "], length: " + filmLength);
            throw new IOException("Unsatisfiable range: [" + readPosition + ", " + dataSpec.length
                    + "], length: " + filmLength);
        }

        return bytesRemaining;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public int read(@NonNull byte[] buffer, int offset, int readLength) throws IOException {
        if (readLength == 0) {
            return 0;
        } else if (bytesRemaining == 0) {
            return C.RESULT_END_OF_INPUT;
        }
        readLength = (int) Math.min(readLength, bytesRemaining);
        try {
            if (provider.shouldUpdateBuffer(readPosition, readLength)) {
                long readLengthBuffer = Math.max(readLength, Constants.DefaultLengthValue);
                provider.updateBuffer(readPosition, readLengthBuffer);
            }
            System.arraycopy(provider.getDataBuffer(), (int) (readPosition - provider.getOffsetMainBuffer()), buffer, offset, readLength);
        } catch (Exception ignored) {

        }
        readPosition += readLength;
        bytesRemaining -= readLength;
        return readLength;
    }


    @NonNull
    @Override
    public Uri getUri() {
        return uri;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
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
