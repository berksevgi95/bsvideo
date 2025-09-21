package com.bs.bsvideo.utils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

public class CallbackList<T> extends ArrayList<T> {

    public interface Callback<T> {

        public void onAdd(T t);
        public void onRemove(T t);

    }

    List<Callback<T>> callbackList = new ArrayList<>();

    public void addCallback(Callback<T> callback) {
        callbackList.add(callback);
    }

    public void removeCallback(Callback<T> callback) {
        callbackList.remove(callback);
    }

    @Override
    public boolean add(T t) {
        boolean result = super.add(t);
        if (result) {
            doAddCallback(t);
        }
        return result;
    }

    @Override
    public void add(int index, T element) {
        super.add(index, element);
        doAddCallback(element);
    }

    @Override
    public boolean addAll(@NonNull Collection<? extends T> c) {
        boolean result = super.addAll(c);
        c.forEach(this::doAddCallback);
        return result;
    }

    @Override
    public boolean addAll(int index, @NonNull Collection<? extends T> c) {
        boolean result = super.addAll(index, c);
        c.forEach(this::doAddCallback);
        return result;
    }

    @Override
    protected void removeRange(int fromIndex, int toIndex) {
        for (int i = fromIndex; i <= toIndex; i++) {
            doRemoveCallback(get(i));
        }
        super.removeRange(fromIndex, toIndex);
    }

    @Override
    public boolean remove(@Nullable Object o) {
        boolean result = super.remove(o);
        if (result) {
            doRemoveCallback((T) o);
        }
        return result;
    }

    @Override
    public boolean removeAll(@NonNull Collection<?> c) {
        boolean result = super.removeAll(c);
        c.forEach((element) -> {
            doRemoveCallback((T) element);
        });
        return result;
    }

    @Override
    public T remove(int index) {
        T t = super.remove(index);
        doRemoveCallback(t);
        return t;
    }

    @Override
    public boolean removeIf(@NonNull Predicate<? super T> filter) {
        return super.removeIf(filter);
    }

    @Override
    public T removeFirst() {
        T t = super.removeFirst();
        doRemoveCallback(t);
        return t;
    }

    @Override
    public T removeLast() {
        T t = super.removeLast();
        doRemoveCallback(t);
        return t;
    }

    private void doAddCallback(T t) {
        callbackList.forEach(callback -> {
            callback.onAdd(t);
        });
    }

    private void doRemoveCallback(T t) {
        callbackList.forEach(callback -> {
            callback.onRemove(t);
        });
    }
}
