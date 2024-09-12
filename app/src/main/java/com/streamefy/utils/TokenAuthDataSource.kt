import android.net.Uri
import android.util.Log
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DataSourceException
import com.google.android.exoplayer2.upstream.DataSpec
import com.google.android.exoplayer2.upstream.HttpDataSource
import com.google.android.exoplayer2.upstream.TransferListener

class TokenAuthDataSource(private val token: String,private var upstreamDataSource: DataSource) : DataSource {

    override fun open(dataSpec: DataSpec): Long {
//        val newSpec = dataSpec.buildUpon()
//            .setHttpRequestHeaders (mapOf("AccessKey" to "$token"))
//            .build()

        val newSpec = dataSpec.buildUpon()
            .setHttpRequestHeaders(mapOf("Authorization" to "Bearer $token"))
            .build()
//        return upstreamDataSource.open(newSpec)

        try {
            return upstreamDataSource.open(newSpec)
        } catch (e: HttpDataSource.InvalidResponseCodeException) {
            Log.e("TokenAuthDataSource", "Error opening data source: ${e.message}")
            throw e
        }

    }

    override fun read(data: ByteArray, offset: Int, length: Int): Int {
        return upstreamDataSource.read(data, offset, length)
    }

    override fun close() {
        upstreamDataSource.close()
    }

    override fun getUri(): Uri {
        return upstreamDataSource.uri!!
    }

    override fun addTransferListener(transferListener: TransferListener) {
        upstreamDataSource.addTransferListener(transferListener)
    }
}