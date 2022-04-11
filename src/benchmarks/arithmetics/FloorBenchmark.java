package benchmarks.arithmetics;

import org.openjdk.jmh.annotations.*;
import lib.posit.Posit;

import java.util.concurrent.TimeUnit;

/**
 * Класс для проведения бенчмарков операции округления вниз.
 */
@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
public class FloorBenchmark {
    /**
     * Параметр для инициализации Posit.
     */
    @Param({"0f", "1f", "-123155.12344f", "-0.000094f", "356.5652f", "0.00033f"})
    private float value;

    /**
     * Инициализируемый Posit.
     */
    private Posit posit;

    /**
     * Прединициализация posit.
     */
    @Setup
    public void setupPosit(){
        posit = new Posit(value);
    }

    /**
     * Бенчмарк-метод округления вниз Posit.
     */
    @Benchmark
    @Fork(value = 1, warmups = 0)
    @Warmup(iterations = 2, time = 1)
    @Measurement(time = 1000, iterations = 5, timeUnit = TimeUnit.MILLISECONDS)
    public Posit floorPosit(){
        return posit.floor();
    }

    /**
     * Бенчмарк-метод округления вниз double.
     */
    @Benchmark
    @Fork(value = 1, warmups = 0)
    @Warmup(iterations = 2, time = 1)
    @Measurement(time = 1000, iterations = 5, timeUnit = TimeUnit.MILLISECONDS)
    public double floorFloat(){
        return Math.floor(value);
    }
}
