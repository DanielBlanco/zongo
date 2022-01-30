package zongo

import zio.Has
import zio.json.*

package object support {
  type ItemsRepo = Has[ItemsRepo.Service]
}
