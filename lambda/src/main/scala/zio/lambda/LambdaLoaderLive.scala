package zio.lambda

import zio._
import zio.blocking._
import zio.console._

import java.net.URLClassLoader
import java.nio.file.Files
import java.nio.file.Paths
import scala.jdk.CollectionConverters._

final case class LambdaLoaderLive(environment: LambdaEnvironment, blocking: Blocking.Service, console: Console.Service)
    extends LambdaLoader {

  override def loadLambda(): Task[ZLambda[_, _]] =
    for {
      taskRoot <- ZIO.require(new Throwable("Task Root not defined"))(ZIO.succeed(environment.taskRoot))
      handler  <- ZIO.require(new Throwable("Function Handler not defined"))(ZIO.succeed(environment.handler))
      zLambda <-
        ZManaged
          .make(blocking.effectBlocking(Files.list(Paths.get(taskRoot))))(stream => ZIO.succeed(stream.close()))
          .use[Any, Throwable, ZLambda[_, _]] { stream =>
            val classLoader = new URLClassLoader(
              stream
                .iterator()
                .asScala
                .map(_.toUri().toURL())
                .toArray,
              ClassLoader.getSystemClassLoader()
            )

            blocking
              .effectBlocking(
                Class
                  .forName(
                    handler + "$",
                    true,
                    classLoader
                  )
                  .getDeclaredField("MODULE$")
                  .get(null)
                  .asInstanceOf[ZLambda[_, _]]
              )
          }
    } yield zLambda

}
