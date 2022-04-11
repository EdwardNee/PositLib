package benchmarks.comparison;

import org.openjdk.jmh.annotations.*;
import lib.posit.Posit;

import java.util.concurrent.TimeUnit;

/**
 * Класс для проведения бенчмарков на сравнение чисел [Posit].
 */
@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
public class CompareBenchmark {
    /**
     * Параметр для инициализации Posit.
     */
    @Param({"0f", "1f", "-123155.12344f", "-0.000094f", "978565f", "0.0000012f"})
    private float value1;
    /**
     * Параметр для инициализации Posit.
     */
    @Param({"0f", "-1f", "-123155.123444f", "0.000094f", "0.00033f", "0.0000012f"})
    private Float value2;

    /**
     * Инициализируемый Posit.
     */
    private Posit posit1;
    /**
     * Инициализируемый Posit.
     */
    private Posit posit2;

    /**
     * Прединициализация posit.
     */
    @Setup
    public void setupPosits(){
        posit1 = new Posit(value1);
        posit2 = new Posit(value2);
    }

    /**
     * Бенчмарк-метод операции сравнения Posit.
     */
    @Benchmark
    @Fork(value = 1, warmups = 0)
    @Warmup(iterations = 2, time = 1)
    @Measurement(time = 1000, iterations = 5, timeUnit = TimeUnit.MILLISECONDS)
    public int comparePosit(){
        return posit1.compareTo(posit2);
    }

    /**
     * Бенчмарк-метод операции сравнения float.
     */
    @Benchmark
    @Fork(value = 1, warmups = 0)
    @Warmup(iterations = 2, time = 1)
    @Measurement(time = 1000, iterations = 5, timeUnit = TimeUnit.MILLISECONDS)
    public int compareFloat(){
        return Float.compare(value1, value2);
    }
}
