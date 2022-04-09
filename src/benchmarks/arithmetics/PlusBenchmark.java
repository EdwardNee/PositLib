package benchmarks.arithmetics;

import org.openjdk.jmh.annotations.*;
import posit.Posit;

import java.util.concurrent.TimeUnit;

@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
public class PlusBenchmark {
    @Param({"0f", "1f", "-123155.12344f", "0.000094f", "978565f", "-9.094947017729282e-13f"})
    private float value1;
    @Param({"0f", "-1f", "356.5652f", "0.000312f", "0.00033f", "0.000003814697265625f"})
    private float value2;

    private Posit posit1;
    private Posit posit2;

    @Setup
    public void setupPosits(){
        posit1 = new Posit(value1);
        posit2 = new Posit(value2);
    }

    @Benchmark
    @Fork(value = 1, warmups = 0)
    @Warmup(iterations = 2, time = 1)
    @Measurement(time = 1000, iterations = 5, timeUnit = TimeUnit.MILLISECONDS)
    public Posit plusPosit(){
        return posit1.plus(posit2);
    }

    @Benchmark
    @Fork(value = 1, warmups = 0)
    @Warmup(iterations = 2, time = 1)
    @Measurement(time = 1000, iterations = 5, timeUnit = TimeUnit.MILLISECONDS)
    public float plusFloat(){
        return value1 + value2;
    }
}
