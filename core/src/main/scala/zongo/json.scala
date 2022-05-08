package zongo

import com.mongodb.MongoClientException
import mongo4cats.bson.*
import mongo4cats.codecs.MongoCodecProvider
import org.bson.codecs.{
  Codec,
  DecoderContext,
  DocumentCodec,
  EncoderContext,
  StringCodec
}
import org.bson.codecs.configuration.{CodecProvider, CodecRegistry}
import org.bson.{BsonReader, BsonType, BsonWriter, Document}
import scala.reflect.ClassTag
import zio.json.*

/** Provides some Encoders/Decoders for zio-json */
object json extends JsonCodecs {

  final case class MongoJsonParsingException(jsonString: String, message: String)
      extends MongoClientException(message)

  implicit def jsonCodecProvider[T: JsonEncoder: JsonDecoder: ClassTag]
      : MongoCodecProvider[T] =
    new MongoCodecProvider[T] {
      implicit val classT: Class[T]   =
        implicitly[ClassTag[T]].runtimeClass.asInstanceOf[Class[T]]
      override def get: CodecProvider = zioJsonBasedCodecProvider[T]
    }

  private def zioJsonBasedCodecProvider[T](implicit
      enc: JsonEncoder[T],
      dec: JsonDecoder[T],
      classT: Class[T]
  ): CodecProvider =
    new CodecProvider {
      override def get[Y](classY: Class[Y], registry: CodecRegistry): Codec[Y] =
        if (classY == classT || classT.isAssignableFrom(classY)) {
          new Codec[Y] {
            private val documentCodec: Codec[Document] =
              new DocumentCodec(registry).asInstanceOf[Codec[Document]]
            private val stringCodec: Codec[String]     = new StringCodec()

            override def encode(
                writer: BsonWriter,
                t: Y,
                encoderContext: EncoderContext
            ): Unit = {
              enc.toJsonAST(t.asInstanceOf[T]) match {
                case Right(json)      =>
                  val document = Document.parse(json.toString())
                  documentCodec.encode(writer, document, encoderContext)
                case Left(jsonString) =>
                  stringCodec.encode(
                    writer,
                    jsonString.replaceAll("\"", ""),
                    encoderContext
                  )
              }
            }

            override def getEncoderClass: Class[Y] = classY

            override def decode(
                reader: BsonReader,
                decoderContext: DecoderContext
            ): Y =
              reader.getCurrentBsonType match {
                case BsonType.DOCUMENT =>
                  val json = documentCodec.decode(reader, decoderContext).toJson()
                  json
                    .fromJson(dec)
                    .fold(
                      e => throw MongoJsonParsingException(json, e),
                      _.asInstanceOf[Y]
                    )
                case _                 =>
                  val string = stringCodec.decode(reader, decoderContext)
                  string
                    .fromJson(dec)
                    .fold(
                      e => throw MongoJsonParsingException(string, e),
                      _.asInstanceOf[Y]
                    )
              }

          }
        } else {
          null // scalastyle:ignore
        }
    }
}
