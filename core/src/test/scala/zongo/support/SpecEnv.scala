package zongo.support

import zio.*
import zongo.Mongo

type SpecEnv = SpecConfig & Mongo & ItemsRepo
