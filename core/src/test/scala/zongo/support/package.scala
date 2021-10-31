package zongo

import zio.Has
import zio.json._

package object support {
  type ItemsRepo = Has[ItemsRepo.Service]
}
