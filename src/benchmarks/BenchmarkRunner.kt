/**
 * @author <a href="mailto:eni@edu.hse.ru"> Eduard Ni</a>
 */
package benchmarks

/**
 * Класс для запуска бенчмарков.
 */
class BenchmarkRunner {
    companion object {
        /**
         * Функция запуска бенчмарков.
         */
        @JvmStatic
        fun main(args: Array<String>) {
            org.openjdk.jmh.Main.main(args)
        }
    }
}