package com.github.evgeniy.publisher

import java.net.URI

import derevo.derive
import tofu.config.Configurable

@derive(Configurable)
case class AppConfig(
  httpPort: Int,
  queueUri: URI
)
