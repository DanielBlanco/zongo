// package zongo

// import zio.*
// import zio.prelude.*


// /**
//  * A cursor implementation that models an index/offset as an
//  * opaque base64 cursor.
//  */
// case class MongoIdCursor(value: Int)

// object MongoIdCursor {
//   import java.util.Base64
//   lazy val encoder = Base64.getEncoder()
//   lazy val decoder = Base64.getDecoder()

//   private val prefix = "cursor:"

//   implicit val cursor: Cursor[MongoIdCursor] = new Cursor[MongoIdCursor] {
//     type T = Int
//     def encode(a: Base64Cursor): String =
//       encoder.encodeToString(s"$prefix${a.value}".getBytes("UTF-8"))

//     def decode(raw: String): Either[String, Base64Cursor] =
//       Try({
//         val bytes = decoder.decode(raw)
//         val s     = new String(bytes, "UTF-8")
//         if (s.startsWith(prefix)) {
//           Base64Cursor(s.replaceFirst(prefix, "").toInt)
//         } else {
//           throw new Throwable("invalid cursor")
//         }
//       }).toEither.left.map(_.getMessage())

//     def value(cursor: Base64Cursor): Int = cursor.value
//   }

//   implicit val schema: Schema[Any, Base64Cursor] =
//     Schema.stringSchema.contramap(Cursor[Base64Cursor].encode)
// }
