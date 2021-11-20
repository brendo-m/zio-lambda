package zio.lambda

import zio._
import zio.blocking.Blocking
import zio.console._

object ZRuntimeApp extends App {

  override def run(args: List[String]): URIO[ZEnv, ExitCode] = {
    val lambdaLoaderLayer = (
      LambdaEnvironment.live ++
        Blocking.live ++
        Console.live
    ) >>> LambdaLoader.layer

    val runtimeApiLayer = (
      LambdaEnvironment.live ++
        Blocking.live ++
        SttpClient.layer
    ) >>> RuntimeApi.layer

    val zRuntimeLayer = runtimeApiLayer >>> ZRuntime.layer

    val appLayer = runtimeApiLayer ++ zRuntimeLayer ++ lambdaLoaderLayer

    LambdaLoader
      .loadLambda()
      .flatMap(zLambda => ZRuntime.processInvocation(zLambda.runZLambda))
      .catchAll(throwable => RuntimeApi.initializationError(InvocationError.fromThrowable(throwable)))
      .provideCustomLayer(appLayer)
      .exitCode
  }

}
