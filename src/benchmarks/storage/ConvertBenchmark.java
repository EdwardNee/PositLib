package benchmarks.storage;

import org.openjdk.jmh.annotations.*;
import lib.posit.Posit;

import java.util.concurrent.TimeUnit;

/**
 * Класс для проведения бенчмарков для операции инициализации числа [Posit].
 */
@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
public class ConvertBenchmark {

    /**
     * Параметр для инициализации Posit.
     */
    @Param(value = {"0f", "1f", "113f", "1923f", "0.003456f", "12312.9899f", "-1f", "-1321.11f"})
    private float value;

    /**
     * Бенчмарк-метод для запуска инициализации Posit.
     * @return Инициализированный тип [Posit].
     */
    @Benchmark
    @Fork(value = 1, warmups = 0)
    @Warmup(iterations = 2, time = 1)
    @Measurement(time = 1000, iterations = 5, timeUnit = TimeUnit.MILLISECONDS)
    public Posit positInit(){
        return new Posit(value);
    }

    /**
     * Бенчмарк-метод для запуска инициализации float.
     * @return Инициализированный тип float.
     */
    @Benchmark
    @Fork(value = 1, warmups = 0)
    @Warmup(iterations = 2, time = 1)
    @Measurement(time = 1000, iterations = 5, timeUnit = TimeUnit.MILLISECONDS)
    public float floatComputerInit(){
        return value;
    }
}
