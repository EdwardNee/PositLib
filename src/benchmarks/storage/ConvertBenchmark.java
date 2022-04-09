package benchmarks.storage;

import org.openjdk.jmh.annotations.*;
import posit.Posit;

import java.util.concurrent.TimeUnit;

@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
public class ConvertBenchmark {

    @Param(value = {"0f", "1f", "113f", "1923f", "0.003456f", "12312.9899f", "-1f", "-1321.11f"})
    private float value;

    @Benchmark
    @Fork(value = 1, warmups = 0)
    @Warmup(iterations = 2, time = 1)
    @Measurement(time = 1000, iterations = 5, timeUnit = TimeUnit.MILLISECONDS)
    public Posit positInit(){
        return new Posit(value);
    }

    @Benchmark
    @Fork(value = 1, warmups = 0)
    @Warmup(iterations = 2, time = 1)
    @Measurement(time = 1000, iterations = 5, timeUnit = TimeUnit.MILLISECONDS)
    public float floatComputerInit(){
        return value;
    }
}
