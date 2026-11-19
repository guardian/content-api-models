package com.gu.storypackage.model.v1

import java.io.ByteArrayInputStream

import com.twitter.scrooge.{ ThriftStructCodec, ThriftStruct }
import org.apache.thrift.protocol.TCompactProtocol
import org.apache.thrift.transport.{ TIOStreamTransport, TMemoryBuffer }

trait ThriftSerializer {

  def serializeToBytes(struct: ThriftStruct): Array[Byte] = {
    val buffer = new TMemoryBuffer(16384)
    val protocol = new TCompactProtocol(buffer)
    struct.write(protocol)
    buffer.getArray
  }

  def deserialize[T <: ThriftStruct](responseBody: Array[Byte], codec: ThriftStructCodec[T]): T = {
    val bbis = new ByteArrayInputStream(responseBody)
    val transport = new TIOStreamTransport(bbis)
    val protocol = new TCompactProtocol(transport)
    codec.decode(protocol)
  }
}