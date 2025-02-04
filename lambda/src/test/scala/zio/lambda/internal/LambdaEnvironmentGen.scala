package zio.lambda.internal

import zio.random.Random
import zio.test._

object LambdaEnvironmentGen {

  val gen: Gen[Random with Sized, LambdaEnvironment] =
    for {
      runtimeApi      <- Gen.string(Gen.anyChar)
      handler         <- Gen.option(Gen.anyString)
      taskRoot        <- Gen.option(Gen.anyString)
      memoryLimitInMB <- Gen.anyInt
      logGroupName    <- Gen.option(Gen.anyString)
      logStreamName   <- Gen.option(Gen.anyString)
      functionName    <- Gen.option(Gen.anyString)
      functionVersion <- Gen.option(Gen.anyString)
    } yield LambdaEnvironment(
      runtimeApi,
      handler,
      taskRoot,
      memoryLimitInMB,
      logGroupName,
      logStreamName,
      functionName,
      functionVersion
    )
}
