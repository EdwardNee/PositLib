package benchmarks.comparison;

import org.openjdk.jmh.annotations.*;
import lib.posit.Posit;

import java.util.concurrent.TimeUnit;

@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
public class CompareBenchmark {
    @Param({"0f", "1f", "-123155.12344f", "-0.000094f", "978565f", "0.0000012f"})
    private float value1;
    @Param({"0f", "-1f", "-123155.123444f", "0.000094f", "0.00033f", "0.0000012f"})
    private Float value2;

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
    public int comparePosit(){
        return posit1.compareTo(posit2);
    }

    @Benchmark
    @Fork(value = 1, warmups = 0)
    @Warmup(iterations = 2, time = 1)
    @Measurement(time = 1000, iterations = 5, timeUnit = TimeUnit.MILLISECONDS)
    public int compareFloat(){
        return Float.compare(value1, value2);
    }
}
