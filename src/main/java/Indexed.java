import com.sun.istack.internal.NotNull;
import rx.Observable;

import java.util.Objects;

public class Indexed<T> {
    long index;
    T value;

    public Indexed(long index, @NotNull T value) {
        this.index = index;
        this.value = value;
    }

    public long getIndex() {
        return index;
    }

    public T getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Indexed<?> indexed = (Indexed<?>) o;
        return Objects.equals(index, indexed.index)
                && Objects.equals(value, indexed.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(index, value);
    }

    @Override
    public String toString() {
        return "Indexed{" +
                "index=" + index +
                ", value=" + value +
                '}';
    }

    private static class IndexedTransformer<T> implements Observable.Transformer<T, Indexed<T>> {

        private Long startValue;

        public IndexedTransformer(Long startValue) {
            this.startValue = startValue;
        }

        @Override
        public Observable<Indexed<T>> call(Observable<T> tObservable) {
            // TODO: this is an alternative approach with an Integer range
            // Observable<Integer> indexObservable = Observable.range(0, Integer.MAX_VALUE);
            Observable<Long> indexObservable = Observable.just(1).repeat().scan(startValue, (acc, val) -> acc + val);
            return Observable.zip(indexObservable, tObservable, Indexed::new);
        }
    }

    public static <T> IndexedTransformer<T> transformer() {
        return transformer(0L);
    }
    public static <T> IndexedTransformer<T> transformer(Long startValue) {
        return new IndexedTransformer<>(startValue);
    }
}