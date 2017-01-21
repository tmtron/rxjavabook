import org.junit.Test;
import rx.Observable;

import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static rx.Observable.just;

public class Chapter5 extends TestsBase {

    @Test
    public void p238a() {
        long startTime = System.currentTimeMillis();
        Observable
                .interval(7, MILLISECONDS)
                .timestamp()
                .sample(1, SECONDS)
                .map(ts -> ts.getTimestampMillis() - startTime + "ms: " + ts.getValue())
                .take(5)
                .toBlocking()
                .subscribe(System.out::println);
    }

    @Test
    public void p238b() {
        Observable<String> names =
                just("Mary", "Patricia", "Linda", "Barbara",
                        "Elizabeth", "Jennifer", "Maria", "Susan",
                        "Margaret", "Dorothy");
        Observable<Long> absoluteDelayMillis =
                just(0.1, 0.6, 0.9, 1.1,
                        3.3, 3.4, 3.5, 3.6,
                        4.4, 4.8)
                        .map(d -> (long) (d * 1_000));
        Observable<String> delayedNames = names
                .zipWith(absoluteDelayMillis,
                        (n, d) ->
                                just(n)
                                        .delay(d, MILLISECONDS))
                .flatMap(o -> o);
        delayedNames
                .sample(1, SECONDS)
                .subscribe(System.out::println,
                        System.err::println,
                        () -> System.out.println("completed")
                );
        sleep(5000);
    }

    @Test
    public void p238bTest() {
        long startTime = System.currentTimeMillis();
        Observable<String> names =
                just("Mary", "Patricia", "Linda", "Barbara",
                        "Elizabeth", "Jennifer", "Maria", "Susan",
                        "Margaret", "Dorothy");
        Observable<Long> absoluteDelayMillis =
                just(0.1, 0.6, 0.9, 1.1,
                        3.3, 3.4, 3.5, 3.6,
                        4.4, 4.8)
                        .map(d -> (long) (d * 1_000));
        names
                .zipWith(absoluteDelayMillis,
                        (n, d) ->
                                just(n)
                                        .delay(d, MILLISECONDS))
                .flatMap(o -> o.timestamp())
                .map(ts -> ts.getTimestampMillis() - startTime + "ms: " + ts.getValue())
                //.sample(1, SECONDS)
                .subscribe(System.out::println,
                        System.err::println,
                        () -> System.out.println(System.currentTimeMillis() - startTime + "ms: Completed"));
        sleep(5000);
    }

    @Test
    public void p238bEx() {
        long startTime = System.currentTimeMillis();
        Observable<String> names =
                just("Mary", "Patricia", "Linda", "Barbara",
                        "Elizabeth", "Jennifer", "Maria", "Susan",
                        "Margaret", "Dorothy");
        Observable<Long> absoluteDelayMillis =
                just(0.1, 0.6, 0.9, 1.1,
                        3.3, 3.4, 3.5, 3.6,
                        4.4, 4.8)
                        .map(d -> (long) (d * 1_000));
        Observable<String> delayedNames = names
                .zipWith(absoluteDelayMillis,
                        (n, d) ->
                                just(n)
                                        .delay(d, MILLISECONDS))
                .flatMap(o -> o.timestamp())
                .map(ts -> ts.getTimestampMillis() - startTime + "ms: " + ts.getValue());
        delayedNames
                .sample(1, SECONDS)
                .compose(Indexed.transformer(1L))
                .toBlocking()
                .subscribe(System.out::println);
    }

    @Test
    public void p241_buffer() {
        Observable
                .range(1, 7) //1, 2, 3, ... 7
                .buffer(3)
                .toBlocking()
                .subscribe((List<Integer> list) -> {
                            //if (list.size() == 3)
                            System.out.println(list);
                        }
                );
    }

    @Test
    public void p242_buffer_movingWindow() {
        Observable
                .range(1, 7)
                .buffer(3, 1)
                .toBlocking()
                .subscribe(System.out::println);
    }

    private double averageOfList(List<Double> list) {
        return list
                .stream()
                .collect(Collectors.averagingDouble(x -> x));
    }

    @Test
    public void p242_averageOfList() {
        Random random = new Random();
        Observable
                .defer(() -> just(random.nextGaussian()))
                .repeat(1000)
                .buffer(100, 1)
                .map(this::averageOfList)
                .toBlocking()
                .subscribe(System.out::println);
    }

    @Test
    public void p243_buffer() {
        Observable<List<Integer>> odd = Observable
                .range(1, 7)
                .buffer(1, 2);
        odd
                .toBlocking()
                .subscribe(System.out::println);
    }

    @Test
    public void p243_batchByTime() {
        long startTime = System.currentTimeMillis();
        Observable<String> names = just(
                "Mary", "Patricia", "Linda", "Barbara", "Elizabeth",
                "Jennifer", "Maria", "Susan", "Margaret", "Dorothy");
        Observable<Long> absoluteDelays = just(
                0.1, 0.6, 0.9, 1.1, 3.3,
                3.4, 3.5, 3.6, 4.4, 4.8
        ).map(d -> (long) (d * 1_000));
        Observable<String> delayedNames = Observable.zip(names,
                absoluteDelays,
                (n, d) -> just(n).delay(d, MILLISECONDS)
        ).flatMap(o -> o);
        delayedNames
                .buffer(1, SECONDS)
                .timestamp()
                .map(ts -> ts.getTimestampMillis() - startTime + "ms: " + ts.getValue())
                .retry()
                .toBlocking()
                .subscribe(System.out::println);
    }


}
