package com.apollographql.apollo3.api.internal

import com.apollographql.apollo3.api.Operation
import com.apollographql.apollo3.api.ResponseAdapterCache
import com.apollographql.apollo3.api.internal.json.JsonWriter
import com.apollographql.apollo3.api.internal.json.use
import okio.Buffer
import okio.ByteString
import kotlin.jvm.JvmStatic

object OperationRequestBodyComposer {

  @JvmStatic
  fun compose(
      operation: Operation<*>,
      autoPersistQueries: Boolean,
      withQueryDocument: Boolean,
      responseAdapterCache: ResponseAdapterCache
  ): ByteString {
    val buffer = Buffer()
    JsonWriter.of(buffer).use { writer ->
      with(writer) {
        serializeNulls = true
        beginObject()
        name("operationName").value(operation.name())
        name("variables")
        operation.serializeVariables(this, responseAdapterCache)
        if (autoPersistQueries) {
          name("extensions")
          beginObject()
          name("persistedQuery")
          beginObject()
          name("version").value(1)
          name("sha256Hash").value(operation.operationId())
          endObject()
          endObject()
        }
        if (!autoPersistQueries || withQueryDocument) {
          name("query").value(operation.queryDocument())
        }
        endObject()
      }
    }
    return buffer.readByteString()
  }
}