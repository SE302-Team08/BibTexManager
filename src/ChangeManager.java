import java.util.Objects;
import java.util.Optional;

public abstract class ChangeManager<T> {
    protected final T oldValue;
    protected final T newValue;

    protected ChangeManager(T oldValue, T newValue) {
        this.oldValue = oldValue;
        this.newValue = newValue;
    }

    abstract void redo();
    abstract ChangeManager<T> invert();

    Optional<ChangeManager<?>> mergeWith(ChangeManager<?> other) {
        // don't merge changes by default
        return Optional.empty();
    }

    @Override
    public int hashCode() {
        return Objects.hash(oldValue, newValue);
    }
}
