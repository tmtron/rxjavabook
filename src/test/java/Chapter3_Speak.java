import org.junit.Test;
import rx.Observable;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

public class Chapter3_Speak extends TestsBase {

    // p124
    public static class Pair<L,R> {

        private final L left;
        private final R right;

        public Pair(L left, R right) {
            this.left = left;
            this.right = right;
            System.err.println("new pair");
        }

        public L getLeft() { return left; }
        public R getRight() { return right; }

        @Override
        public int hashCode() { return left.hashCode() ^ right.hashCode(); }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof Pair)) return false;
            Pair pairo = (Pair) o;
            return this.left.equals(pairo.getLeft()) &&
                    this.right.equals(pairo.getRight());
        }
        public static <L, R> Pair<L, R> of(L left, R right) {
            return new Pair(left, right);
        }

    }
    Observable<String> speak(String quote, long millisPerChar) {
        String[] tokens = quote.replaceAll("[:,]", "").split(" ");
        Observable<String> words = Observable.from(tokens);
        Observable<Long> absoluteDelay = words
                .map(String::length)
                .map(len -> len * millisPerChar)
                .scan((total, current) -> total + current);
        return words
                .zipWith(absoluteDelay.startWith(0L), Pair::of)
                .flatMap(pair -> Observable.just(pair.getLeft())
                        .delay(pair.getRight(), MILLISECONDS));
    }
    @Test
    public void speakTest() {
        speak("This is some sentence.", 2)
                .subscribe(s -> System.out.print(s));
        sleep(1000);
        System.out.print("<");
    }
    @Test
    public void speakTestCollect() {
        speak("This is some sentence.", 2)
                .collect(
                        StringBuilder::new,
                        (sb, x) -> sb.append(x).append(" "))
                .map(StringBuilder::toString)
                .subscribe(System.out::print);
        sleep(1000);
        System.out.print("<");
    }
    @Test
    public void speakTest1() {
        Observable<String> speakObservable = speak("This is some sentence.", 2).share();
        // TODO: this creates 8 Pair classes
        System.out.print(">");
        Observable.concat(
                speakObservable.first(),
                speakObservable.skip(1).map(x -> " "+x)
        )
                .subscribe(s -> System.out.print(s));
        sleep(1000);
        System.out.print("<");
    }

    @Test
    public void speakTest2() {
        Observable<String> speakObservable = speak("This is some sentence.", 2);
        System.out.print(">");
        // TODO: this creates 8 Pair classes
        Observable<String> spaces = speakObservable.map(x -> " ").skipLast(1).concatWith(Observable.just(""));
        Observable.zip(speakObservable, spaces, (w, s) -> w+s)
                .subscribe(s -> System.out.print(s));
        sleep(1000);
        System.out.print("<");
    }

    @Test
    public void speakTest3() {
        Observable<String> speakObservable = speak("This is some sentence.", 2);
        System.out.print(">");
        //Observable<String> spaces = Observable.just(" ").repeat().startWith("");
        Observable<String> spaces = Observable.just("").concatWith(Observable.just(" ").repeat());
        Observable.zip(speakObservable, spaces, (word, s) -> s+word)
                .subscribe(s -> System.out.print(s));
        sleep(1000);
        System.out.print("<");
    }

    @Test
    public void speakTest_Indexed() {
        Observable<String> speakObservable = speak("This is some sentence.", 2);
        // make an observable with all indices: 0, 1, 2, ..
        Observable<Long> indexObservable = Observable.just(1).repeat().scan(0L, (acc, val) -> acc + val);
        System.out.print(">");
        Observable.zip(indexObservable, speakObservable, (i, word) -> new Indexed<>(i, word))
                .map(indexed -> (indexed.getIndex() == 0) ? indexed.getValue() : " " + indexed.getValue())
                .subscribe(System.out::print);
        sleep(1000);
        System.out.print("<");
    }

    @Test
    public void speakTest_IndexedCompose() {
        Observable<String> speakObservable = speak("This is some sentence.", 2);
        System.out.print(">");
        speakObservable.compose(Indexed.transformer())
                .map(indexed -> (indexed.getIndex() == 0) ? indexed.getValue() : " " + indexed.getValue())
                .toBlocking()
                .subscribe(System.out::print);
        //sleep(1000);
        System.out.print("<");
    }

    @Test
    public void speakTest_IndexedDirect() {
        Observable<String> speakObservable = speak("This is some sentence.", 2);
        Observable<Long> indexObservable = Observable.just(1).repeat().scan(0L, (acc, val) -> acc + val);
        System.out.print(">");
        Observable.zip(speakObservable, indexObservable, (word, i) -> {
            if (i > 0) {
                return " "+word;
            } else {
                return word;
            }
        }).subscribe(System.out::print);
        sleep(1000);
        System.out.print("<");
    }

}
