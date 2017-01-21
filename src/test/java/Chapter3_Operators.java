import org.junit.Assert;
import org.junit.Test;
import rx.Observable;
import rx.schedulers.Schedulers;

import java.math.BigInteger;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;

public class Chapter3_Operators extends TestsBase {

    @org.junit.Test
    public void helloWorld() {
        System.out.println("Hello");
    }

    @org.junit.Test
    public void justTest() {
        Observable.just(1, 2, 3)
                .subscribe(i -> System.out.println(">"+i));
    }

    @org.junit.Test
    public void timerValue() {
        Observable
                .timer(1, SECONDS)
                .subscribeOn(Schedulers.immediate())
                .observeOn(Schedulers.immediate())
                .subscribe(
                        i -> Assert.assertEquals(i, Long.valueOf(0)));
        sleep(1100);
        System.out.println("done..");
    }

    @org.junit.Test
    public void timerTest() {
        Observable
                .timer(1, SECONDS)
                .subscribeOn(Schedulers.immediate())
                .observeOn(Schedulers.immediate())
                .doOnNext(i -> System.out.println("doOnNext" + i))
                .flatMap(i -> Observable.just(1, 2, 3))
                .subscribe(
                        i -> System.out.println(">" + i));
        sleep(1100);
        System.out.println("done..");
    }

    @org.junit.Test
    public void delayTest() {
        Observable.just(1, 2, 3).delay(1, SECONDS)
                .subscribe(i -> System.out.println(">"+i));
        sleep(1100);
    }

    @org.junit.Test
    public void delayItems() {
        Observable.just(1, 2, 3)
                .flatMap(i -> Observable.just(i).delay(i, SECONDS))
                .subscribe(i -> System.out.println(">" + i));
        sleep(3100);
    }

    @org.junit.Test
    public void varTimeDelay() {
        Observable
                .just("very long", "short", "a")
                .delay(word -> Observable.timer(word.length(), SECONDS))
                .subscribe(System.out::println);
        sleep(11000);
    }


    @org.junit.Test
    public void varTimeTimer() {
        Observable
                .just("very long", "short", "a")
                .flatMap(
                        word -> Observable.timer(word.length(), SECONDS).map(x -> word)
                )
                .subscribe(System.out::println);
        sleep(11000);
    }

    // p.107
    @Test
    public void cartesianProduct() {
        Observable<Integer> oneToEight = Observable.range(1, 8);
        Observable<String> ranks = oneToEight
                .map(Object::toString);

        Observable<String> files = oneToEight
                .map(x -> 'a' + x - 1)
                .map(ascii -> (char)ascii.intValue())
                .map(ch -> Character.toString(ch));

        Observable<String> squares = files
                .flatMap(file -> ranks.map(rank -> file + rank));

        squares.subscribe(
                s -> System.out.println(s));
    }

    //p.109
    @Test
    public void timestamped() {
        Observable<Long> red = Observable.interval(10, MILLISECONDS);
        Observable<Long> green = Observable.interval(10, MILLISECONDS);
        Observable.zip(
                red.timestamp(),
                green.timestamp(),
                (r, g) -> r.getTimestampMillis() - g.getTimestampMillis()
        ).forEach(System.out::println);
        sleep(1000);
    }

    @Test
    public void scanTest() {
        Observable<BigInteger> factorials = Observable
                .range(2, 100)
                .scan(BigInteger.ONE, (big, cur) ->
                        big.multiply(BigInteger.valueOf(cur)));
        factorials.takeLast(1).subscribe(bi -> System.out.println(bi));
    }

    // p.117
    @Test
    public void reduceTest() {
        Observable<BigInteger> factorials = Observable
                .range(2, 100)
                .reduce(BigInteger.ONE,
                        (big, cur) -> big.multiply(BigInteger.valueOf(cur)));
        factorials.subscribe(bi -> System.out.println(bi));
    }

    // p123
    @Test
    public void concatTest() {
        Observable<Integer> veryLong = Observable.range(1, 100).share();
        final Observable<Integer> ends = Observable.concat(
                veryLong.take(2),
                veryLong.takeLast(2)
        );
        ends.subscribe(i -> System.out.println(i));
    }

    // p.117?
    @Test
    public void collectTest() {
        // TODO: how to get rid of the trailing comma?
        Observable.range(1, 10)
                .collect(
                        StringBuilder::new,
                        (sb, x) -> sb.append(x).append(", "))
                .map(StringBuilder::toString)
                .subscribe(s -> System.out.println(s));
    }

}
