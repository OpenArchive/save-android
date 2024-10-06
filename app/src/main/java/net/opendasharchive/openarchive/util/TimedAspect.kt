package net.opendasharchive.openarchive.util

import kotlin.reflect.KClass
import kotlin.system.measureNanoTime

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class Timed(val logger: KClass<out Logger> = DefaultLogger::class)

interface Logger {
    fun log(message: String)
}

object DefaultLogger : Logger {
    override fun log(message: String) {
        println(message)
    }
}

class TimingAspect {
    fun <T> executeTimed(function: () -> T): T {
        val stackTrace = Thread.currentThread().stackTrace
        val callerMethodName = stackTrace[2].methodName

        val callerClass = Class.forName(stackTrace[2].className)
        val method = callerClass.declaredMethods.find { it.name == callerMethodName }

        val timedAnnotation = method?.getAnnotation(Timed::class.java) ?: return function()

        val logger = timedAnnotation.logger.objectInstance as? Logger ?: DefaultLogger

        var result: T
        val executionTimeNanos = measureNanoTime {
            result = function()
        }

        logger.log("Method $callerMethodName executed in ${executionTimeNanos / 1_000_000.0} ms")

        return result
    }
}