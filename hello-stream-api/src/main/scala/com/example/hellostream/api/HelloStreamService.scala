package com.example.hellostream.api

import akka.NotUsed
import akka.stream.scaladsl.Source
import com.lightbend.lagom.scaladsl.api.{Descriptor, Service, ServiceCall}

/**
  * The hello stream interface.
  *
  * This describes everything that Lagom needs to know about how to serve and
  * consume the HelloStream service.
  */
trait HelloStreamService extends Service {

  def index: ServiceCall[NotUsed, String]

  def stream: ServiceCall[Source[String, NotUsed], Source[String, NotUsed]]

  override final def descriptor: Descriptor = {
    import Service._

    named("hello-stream")
      .withCalls(
        namedCall("stream", stream),
        pathCall("/", index)
      ).withAutoAcl(true)
  }
}

