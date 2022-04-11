package benchmarks.arithmetics;

import org.openjdk.jmh.annotations.*;
import lib.posit.Posit;

import java.util.concurrent.TimeUnit;

/**
 * Класс для проведения бенчмарков операции деления типа [Posit].
 */
@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
public class DivBenchmark {
    /**
     * Параметр для инициализации Posit.
     */
    @Param({"0f", "1f", "-123155.12344f", "-0.000094f", "978565f"})
    private float value1;
    /**
     * Параметр для инициализации Posit.
     */
    @Param({"0f", "-1f", "356.5652f", "0.000312f", "0.00033f"})
    private float value2;

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
     * Бенчмарк-метод операции деления Posit.
     */
    @Benchmark
    @Fork(value = 1, warmups = 0)
    @Warmup(iterations = 2, time = 1)
    @Measurement(time = 1000, iterations = 5, timeUnit = TimeUnit.MILLISECONDS)
    public Posit divPosit(){
        return posit1.div(posit2);
    }

    /**
     * Бенчмарк-метод операции деления float.
     */
    @Benchmark
    @Fork(value = 1, warmups = 0)
    @Warmup(iterations = 2, time = 1)
    @Measurement(time = 1000, iterations = 5, timeUnit = TimeUnit.MILLISECONDS)
    public float divFloat(){
        return value1 / value2;
    }
}
