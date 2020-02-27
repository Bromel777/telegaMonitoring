package org.encryfoundation.tg.pipelines

import org.encryfoundation.tg.pipelines.chat.{InvokePipe, PrintPipe, ReadPipe}
import org.encryfoundation.tg.pipelines.json.HttpApiJsonParsePipe

object Pipes {

  val acceptedPipes = List(
    InvokePipe, PrintPipe, ReadPipe, HttpApiJsonParsePipe
  )
}
