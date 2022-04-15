package benchmarks.storage;

import org.openjdk.jmh.annotations.*;
import lib.posit.Posit;

import java.util.concurrent.TimeUnit;

/**
 * Класс для проведения бенчмарков для операции декодирования числа [Posit] в различные числовые типы.
 */
@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
public class DecodeBenchmark {

    /**
     * Параметр для инициализации Posit.
     */
    @Param(value = {"0f", "1f", "113f", "1923f", "0.003456f", "12312.9899f", "-1f", "-1321.11f"})
    private float value;

    /**
     * Инициализируемый Posit.
     */
    private Posit posit;

    /**
     * Прединициализация posit.
     */
    @Setup
    public void setupPosit() {
        posit = new Posit(value);
    }

    /**
     * Бенчмарк-метод для декодирования posit в double.
     */
    @Benchmark
    @Fork(value = 1, warmups = 0)
    @Warmup(iterations = 2, time = 1)
    @Measurement(time = 1000, iterations = 5, timeUnit = TimeUnit.MILLISECONDS)
    public double decodeToDouble() {
        return posit.toDouble();
    }


    /**
     * Бенчмарк-метод для декодирования posit в float.
     */
    @Benchmark
    @Fork(value = 1, warmups = 0)
    @Warmup(iterations = 2, time = 1)
    @Measurement(time = 1000, iterations = 5, timeUnit = TimeUnit.MILLISECONDS)
    public float decodeToFloat() {
        return posit.toFloat();
    }

    /**
     * Бенчмарк-метод для декодирования posit в int.
     */
    @Benchmark
    @Fork(value = 1, warmups = 0)
    @Warmup(iterations = 2, time = 1)
    @Measurement(time = 1000, iterations = 5, timeUnit = TimeUnit.MILLISECONDS)
    public int decodeToInt() {
        return posit.toInt();
    }

    /**
     * Бенчмарк-метод для декодирования posit в long.
     */
    @Benchmark
    @Fork(value = 1, warmups = 0)
    @Warmup(iterations = 2, time = 1)
    @Measurement(time = 1000, iterations = 5, timeUnit = TimeUnit.MILLISECONDS)
    public long decodeToLong() {
        return posit.toLong();
    }
}
