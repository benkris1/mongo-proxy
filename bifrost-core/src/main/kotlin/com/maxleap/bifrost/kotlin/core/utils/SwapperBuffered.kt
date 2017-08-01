package `com`.maxleap.bifrost.kotlin.core.utils
import io.vertx.core.buffer.Buffer

interface SwapperBuffered {
  fun swapperBuffer(): Buffer
}