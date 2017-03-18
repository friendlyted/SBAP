package ru.sbsoft.sbap.platform.dflt.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

/**
 *
 * @author Fedor Resnyanskiy, SBSOFT
 */
public class ListeningList<T> implements List<T> {

    private List<Consumer<T>> addListeners;
    private final Object addListenersLock = new Object();

    private List<Consumer<T>> removeListeners;
    private final Object removeListenersLock = new Object();

    private final List<T> list = new ArrayList<>();

    public List<Consumer<T>> getAddListeners() {
        synchronized (addListenersLock) {
            if (addListeners == null) {
                addListeners = new ArrayList<>();
            }
        }
        return addListeners;
    }

    public List<Consumer<T>> getRemoveListeners() {
        synchronized (removeListenersLock) {
            if (removeListeners == null) {
                removeListeners = new ArrayList<>();
            }
        }
        return removeListeners;
    }

    /**
     * Добавляет слушателя на добавление записей в список.
     *
     * @param listener
     * @return ссылку на удаление слушателя.
     */
    public Runnable addAddListener(Consumer<T> listener) {
        getAddListeners().add(listener);
        return () -> getAddListeners().remove(listener);
    }

    /**
     * Добавляет слушателя на удаление записей из списка.
     *
     * @param listener
     * @return ссылку на удаление слушателя.
     */
    public Runnable addRemoveListener(Consumer<T> listener) {
        getRemoveListeners().add(listener);
        return () -> getRemoveListeners().remove(listener);
    }

    private void onAdd(T t) {
        getAddListeners().forEach(l -> l.accept(t));
    }

    private void onRemove(Object t) {
        getRemoveListeners().forEach(l -> l.accept((T) t));
    }

    @Override
    public int size() {
        return list.size();
    }

    @Override
    public boolean isEmpty() {
        return list.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return list.contains(o);
    }

    @Override
    public Iterator<T> iterator() {
        return list.iterator();
    }

    @Override
    public Object[] toArray() {
        return list.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return list.toArray(a);
    }

    @Override
    public boolean add(T e) {
        onAdd(e);
        return list.add(e);
    }

    @Override
    public boolean remove(Object o) {
        onRemove((T) o);
        return list.remove(o);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return list.containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        c.forEach(t -> onAdd(t));
        return list.addAll(c);
    }

    @Override
    public boolean addAll(int index, Collection<? extends T> c) {
        c.forEach(t -> onAdd(t));
        return list.addAll(index, c);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        c.forEach(t -> onRemove(t));
        return list.removeAll(c);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException("too difficult");
        //return list.retainAll(c);
    }

    @Override
    public void replaceAll(UnaryOperator<T> operator) {
        throw new UnsupportedOperationException("too difficult");
        //list.replaceAll(operator);
    }

    @Override
    public void sort(Comparator<? super T> c) {
        list.sort(c);
    }

    @Override
    public void clear() {
        list.forEach(t -> onRemove(t));
        list.clear();
    }

    @Override
    public boolean equals(Object o) {
        return list.equals(o);
    }

    @Override
    public int hashCode() {
        return list.hashCode();
    }

    @Override
    public T get(int index) {
        return list.get(index);
    }

    @Override
    public T set(int index, T element) {
        return list.set(index, element);
    }

    @Override
    public void add(int index, T element) {
        onAdd(element);
        list.add(index, element);
    }

    @Override
    public T remove(int index) {
        final T result = list.remove(index);
        onRemove(result);
        return result;
    }

    @Override
    public int indexOf(Object o) {
        return list.indexOf(o);
    }

    @Override
    public int lastIndexOf(Object o) {
        return list.lastIndexOf(o);
    }

    @Override
    public ListIterator<T> listIterator() {
        return list.listIterator();
    }

    @Override
    public ListIterator<T> listIterator(int index) {
        return list.listIterator(index);
    }

    @Override
    public List<T> subList(int fromIndex, int toIndex) {
        return list.subList(fromIndex, toIndex);
    }

    @Override
    public Spliterator<T> spliterator() {
        return list.spliterator();
    }

    @Override
    public boolean removeIf(Predicate<? super T> filter) {
        throw new UnsupportedOperationException("too difficult");
        //return list.removeIf(filter);
    }

    @Override
    public Stream<T> stream() {
        return list.stream();
    }

    @Override
    public Stream<T> parallelStream() {
        return list.parallelStream();
    }

    @Override
    public void forEach(Consumer<? super T> action) {
        list.forEach(action);
    }

}
